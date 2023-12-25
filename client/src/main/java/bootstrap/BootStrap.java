package bootstrap;

import config.Config;
import controller.Client;
import controller.Killer;
import controller.MainClient;
import domain.Query;
import domain.Response;
import protocol.Request;

import java.util.List;

import static config.Config.DEFAULT_PATHS;

public class BootStrap {
    public static void main(String[] args) {
        System.out.println("Java version: " + System.getProperty("java.version"));
        int threadNumber = 8;
        int variant = 1;
        var folders = Config.DEFAULT_PATHS.subList(0, 1);
        int code = (new MainClient()).buildIndex(threadNumber, variant, folders);
        if (code != Request.CREATED) {
            return;
        }
        var client = new Client();
        List<Response> expected = List.of(new Response(DEFAULT_PATHS.getFirst(), 6));
        client.doQueries(List.of(new Query("British playwright Ronald Harwood adapts", expected)));
        var killer = new Killer();
        killer.askServerToFinish();
    }
}
