import java.util.*;

/**
 * Entry point for the Wordler application.
 */
public class Wordler {
    public static void main(String[] arg) {
        System.out.println("Wordler startar...");
    }
}

/**
 * Abstraction for providing vocabulary items.
 * Makes it easy to replace in-memory words with file-based words later.
 */
interface VocabularyProvider {

    /**
     * @return a list of vocabulary items
     */
    List<VocabItem> getVocabulary();
}

/**
 * Represents one Swedish word and one or more accepted English translations (supports synonyms).
 *
 * @param swedish the Swedish word
 * @param acceptedEnglish accepted English translations
 */
record VocabItem(String swedish, List<String> acceptedEnglish) {

    /**
     * @return the primary (first) English translation
     */
    public String primaryEnglish() {
        return acceptedEnglish.get(0);
    }
}
