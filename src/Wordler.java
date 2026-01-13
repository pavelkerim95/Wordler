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
 * Controls the game flow: shows words and reads user input.
 */
class WordGame {

    private static final int Max_Questions = 10;

    private final VocabularyProvider provider;

    public WordGame(VocabularyProvider provider) {
        this.provider = provider;
    }

    public void run() {
        List<VocabItem> items = provider.getVocabulary();
        Collections.shuffle(items);

        try (Scanner scan = new Scanner(System.in)) {
            int asked = 0;

            for (VocabItem item : items) {
                if (asked >= Max_Questions) break;

                System.out.println("------------------------------------------");
                System.out.println("Svenska ordet: " + item.swedish());
                System.out.print("Översätt till engelska (Q för att avsluta): ");

                String input = scan.nextLine().trim();
                if (input.equalsIgnoreCase("Q")) break;

                asked++;
            }

            System.out.println("Antal frågor: " + asked);
        }
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
