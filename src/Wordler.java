import java.util.*;

/**
 * Entry point for the Wordler application.
 */
public class Wordler {
    public static void main(String[] arg) {
        VocabularyProvider provider = new InMemoryVocabulary();
        AnswerEvaluator evaluator = new AnswerEvaluator();

        WordGame game = new WordGame(provider, evaluator);
        game.run();
    }
}

/**
 * Controls the game flow: shows words and reads user input.
 */
class WordGame {

    private static final int Max_Questions = 10;

    private final VocabularyProvider provider;
    private final AnswerEvaluator evaluator;

    public WordGame(VocabularyProvider provider, AnswerEvaluator evaluator) {
        this.provider = provider;
        this.evaluator = evaluator;
    }

    public void run() {
        List<VocabItem> items = provider.getVocabulary();
        Collections.shuffle(items);

        try (Scanner scan = new Scanner(System.in)) {
            int correctCount = 0;
            int asked = 0;

            for (VocabItem item : items) {
                if (asked >= Max_Questions) break;

                System.out.println("------------------------------------------");
                System.out.println("Svenska ordet: " + item.swedish());
                System.out.print("Översätt till engelska (Q för att avsluta): ");

                String input = scan.nextLine().trim();
                if (input.equalsIgnoreCase("Q")) break;

                EvaluationResult result = evaluator.evaluate(input, item);
                if (result.status() == EvaluationStatus.CORRECT) {
                    correctCount++;
                }

                asked++;
            }

            System.out.println("========== RESULTAT ==========");
            System.out.println("Antal frågor: " + asked);
            System.out.println("Antal rätt: " + correctCount);
            System.out.println("==============================");
        }
    }
}

/**
 * Evaluates a user's answer: correct, almost correct, or wrong.
 */
class AnswerEvaluator {

    public EvaluationResult evaluate(String userInput, VocabItem item) {
        String guess = normalize(userInput);

        for (String accepted : item.acceptedEnglish()) {
            String target = normalize(accepted);
            if (guess.equals(target)) {
                return new EvaluationResult(EvaluationStatus.CORRECT, "Exakt match");
            }
        }

        return new EvaluationResult(EvaluationStatus.WRONG, "Inte korrekt");
    }

    private String normalize(String s) {
        return s.trim().toLowerCase();
    }
}

/**
 * Utility for comparing two strings by matching characters at the same positions.
 */
class Similarity {

    public static int positionMatches(String guess, String target) {
        int min = Math.min(guess.length(), target.length());
        int matches = 0;
        for (int i = 0; i < min; i++) {
            if(guess.charAt(i) == target.charAt(i)) matches++;
        }
        return matches;
    }
}

/**
 * Possible outcomes of evaluating an answer.
 */
enum EvaluationStatus {CORRECT, ALMOST, WRONG}

/**
 * Result from evaluating a user's answer.
 *
 * @param status evaluation status
 * @param details extra info to show to the user
 */
record EvaluationResult(EvaluationStatus status, String details) {}

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

    public String primaryEnglish() {
        return acceptedEnglish.get(0);
    }
}
