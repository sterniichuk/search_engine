package bootstrap;

import service.ProcessFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

import static config.Config.*;

public class Bootstrap {

    private static final Map<String, String> arguments = new HashMap<>(Map.of(
            source, System.getProperty("user.dir"),
            variant, "24",
            clientNumber, "32"
    ));

    private static final int CPU_CORES = 8;
    private static final int CPU_LOGICAL_CORES = 16;
    private static final int MIN_THREADS = CPU_CORES / 2;

    private static final List<Integer> threadNumbers = List.of(
            1,
            MIN_THREADS,
            CPU_CORES,
            CPU_LOGICAL_CORES,
            CPU_LOGICAL_CORES * 2,
            CPU_LOGICAL_CORES * 4,
            CPU_LOGICAL_CORES * 8,
            CPU_LOGICAL_CORES * 16
    );


    public static void main(String[] args) {
        System.out.println("Java version: " + System.getProperty("java.version"));
        updateArguments(args, arguments);
        var builder = new ProcessFactory();
        threadNumbers.forEach(threadNumber -> applicationRuntimeLifeCycle(threadNumber, builder));
    }

    private static void applicationRuntimeLifeCycle(Integer threadNumber, ProcessFactory builder) {
        boolean serverStarted = false;
        try {
            System.out.println(STR. """
                    \n
                    Number of threads: \{ threadNumber }
                    Used arguments: \{ arguments.toString() }
                    """ );
            builder.exec(ServerRunner.class, List.of());//run server
            Thread.sleep(100);
            serverStarted = true;
            builder.exec(BuilderRunner.class, getBuilderArguments(threadNumber)).waitFor();//send request to build index
            int numberOfClients = getIntValue(clientNumber, arguments);
            System.out.println("Start clients");
            List<Process> list = IntStream.range(0, numberOfClients)
                    .mapToObj(i -> {
                        try {
                            return builder.exec(ClientRunner.class, getClientArguments());
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    })
                    .toList();//send search requests
            for (Process process : list) {
                process.waitFor();//wait for all processes
            }
            System.out.println("Finish clients");
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            stopServer(serverStarted, builder);
        }
    }

    private static List<String> getClientArguments() {
        String variantValue = validInt();
        String folder = arguments.get(source);
        return List.of(variant, variantValue, source, folder);
    }

    private static void stopServer(boolean serverStarted, ProcessFactory builder) {
        if (serverStarted) {
            try {
                var killer = builder.exec(KillerRunner.class, List.of());
                killer.waitFor();//kill server and wait
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        }
    }

    private static List<String> getBuilderArguments(Integer threadNumber) {
        String variantValue = validInt();
        return List.of(variant, variantValue, threads, threadNumber + "");
    }

    private static String validInt() {
        int number = getIntValue(config.Config.variant, arguments);
        return number + "";
    }
}
