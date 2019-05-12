package numbers;

import java.util.HashMap;
import java.util.List;

class Translator {

    static final HashMap<String, String> englishNumbers = new HashMap<>() {
        {
            put("zero", "0");
            put("one", "1");
            put("two", "2");
            put("three", "3");
            put("four", "4");
            put("five", "5");
            put("six", "6");
            put("seven", "7");
            put("eight", "8");
            put("nine", "9");
        }
    };

    static final HashMap<String, String> germanNumbers = new HashMap<>() {
        {
            put("null", "0");
            put("eins", "1");
            put("zwei", "2");
            put("drei", "3");
            put("vier", "4");
            put("fünf", "5");
            put("sechs", "6");
            put("sieben", "7");
            put("acht", "8");
            put("neun", "9");
        }
    };

    static final HashMap<String, String> morseNumbers = new HashMap<>() {
        {
            put("-----", "0");
            put(".----", "1");
            put("..---", "2");
            put("...--", "3");
            put("....-", "4");
            put(".....", "5");
            put("-....", "6");
            put("--...", "7");
            put("---..", "8");
            put("----.", "9");
        }
    };

    static final HashMap<String, HashMap<String, String>> numberIndex = new HashMap<>() {
        {
            put("ENG", englishNumbers);
            put("GER", germanNumbers);
            put("MOR", morseNumbers);
        }
    };

    static String translate(String language, List<String> content) {
        StringBuilder output = new StringBuilder();

        for (String text : content) {
            output.append(numberIndex.get(language).get(text));
        }

        return output.toString();
    }

    static boolean knows(Message message) {
        return numberIndex.containsKey(message.getType());
    }

}
