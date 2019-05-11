package numbers;

import java.util.HashMap;

class Translator {

    static final HashMap<String, Integer> englishNumbers = new HashMap<String, Integer>() {
        {
            put("zero", 0);
            put("one", 1);
            put("two", 2);
            put("three", 3);
            put("four", 4);
            put("five", 5);
            put("six", 6);
            put("seven", 7);
            put("eight", 8);
            put("nine", 9);
        }
    };

    static final HashMap<String, Integer> germanNumbers = new HashMap<String, Integer>() {
        {
            put("null", 0);
            put("eins", 1);
            put("zwei", 2);
            put("drei", 3);
            put("vier", 4);
            put("fünf", 5);
            put("sechs", 6);
            put("sieben", 7);
            put("acht", 8);
            put("neun", 9);
        }
    };

    static final HashMap<String, Integer> morseNumbers = new HashMap<String, Integer>() {
        {
            put("-----", 0);
            put(".----", 1);
            put("..---", 2);
            put("...--", 3);
            put("....-", 4);
            put(".....", 5);
            put("-....", 6);
            put("--...", 7);
            put("---..", 8);
            put("----.", 9);
        }
    };

    static final HashMap<String, HashMap<String, Integer>> numberIndex = new HashMap<String, HashMap<String, Integer>>() {
        {
            put("ENG", englishNumbers);
            put("GER", germanNumbers);
            put("MOR", morseNumbers);
        }
    };

    private static Integer translateNumber(String language, String number) {
        return numberIndex.get(language).get(number);
    }

    static int translateNumbers(String language, String[] values) {
        StringBuilder output = new StringBuilder();

        for (int i = 0; i < values.length; i++) {
            output.append(Translator.translateNumber(language, values[i]).toString());
        }

        return Integer.parseInt(output.toString());
    }

}
