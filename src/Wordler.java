import java.util.*;

/**
 * Entry point for the Wordler application.
 */
public class Wordler {
    public static void main(String[] arg) {
        VocabularyProvider provider = new InMemoryVocabulary();

        WordGame game = new WordGame(provider);
        game.run();
    }
}

/**
 * Controls the game flow (initial version).
 */
class WordGame {

    private final VocabularyProvider provider;

    public WordGame(VocabularyProvider provider) {
        this.provider = provider;
    }

    public void run() {
        System.out.println("Spelet körs...");
        System.out.println("Antal ord i listan: " + provider.getVocabulary().size());
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
 * Vocabulary provider that keeps words in memory (hardcoded list).
 */
class InMemoryVocabulary implements VocabularyProvider {

    /**
     * @return predefined vocabulary list
     */
    @Override
    public List<VocabItem> getVocabulary() {
        List<VocabItem> list = new ArrayList<>();
        list.add(new VocabItem("försöka", List.of("attempt")));
        list.add(new VocabItem("förklara", List.of("explain")));
        list.add(new VocabItem("bestämma", List.of("decide")));
        return list;
    }
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
