package service;

import java.util.Arrays;

public class TextProcessor {
    /**
     * List of common suffixes
     */
    private static final String[] suffixes = {
            "s", "es", "ed", "ing", "ly", "er", "or", "ion", "able", "ible",
            "ment", "ness", "ful", "less", "al", "ive", "ize", "ify", "ise", "ist"
    };
    private static final int SMALL_WORD_LENGTH = 3;

    public String[] processText(String line) {
        var words = line.toLowerCase()
                .replaceAll("(?<=[a-z])\\.|'s", "")//Tokenization, Normalization
                .split("\\W+");
        return Arrays.stream(words).filter(s -> !s.isEmpty()).map(this::stemWord).toArray(String[]::new);//Stemming
    }


    private String stemWord(String word) {
        for (String suffix : suffixes) {
            if (word.length() > SMALL_WORD_LENGTH && word.length() > suffix.length() && word.endsWith(suffix)) {
                return word.substring(0, word.length() - suffix.length());
            }
        }
        return word;
    }
}
