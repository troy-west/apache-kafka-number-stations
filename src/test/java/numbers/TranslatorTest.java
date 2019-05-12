package numbers;

import junit.framework.TestCase;

import java.util.List;

public class TranslatorTest extends TestCase {

    public void testTranslateEnglish() {
        assertEquals("1", Translator.translate("ENG", List.of("one")));
    }

    public void testTranslateMoreEnglish() {
        assertEquals("12", Translator.translate("ENG", List.of("one", "two")));
    }

    public void testTranslateGerman() {
        assertEquals("4", Translator.translate("GER", List.of("vier")));
    }

    public void testTranslateMoreGerman() {
        assertEquals("40", Translator.translate("GER", List.of("vier", "null")));
    }

    public void testTranslateMorse() {
        assertEquals("3", Translator.translate("MOR", List.of("...--")));
    }

    public void testTranslateMoreMorse() {
        assertEquals("37", Translator.translate("MOR", List.of("...--", "--...")));
    }
}