import java.util.*;

/**
 * Wordler is a vocabulary game where the user is presented
 * with a Swedish word and must guess the correct English translation.
 * The program evaluates the answer as correct, almost correct, or wrong.
 * The application is designed to be easily extendable in the future,
 * for example with file-based vocabularies, synonyms, or support for
 * additional languages.
 */

public class Wordler {
    /**
     * Program entry point.
     * @param arg command-line arguments (not used)
     */
    public static void main(String[] arg) {
        VocabularyProvider provider = new InMemoryVocabulary();
        AnswerEvaluator evaluator = new AnswerEvaluator();

        WordGame game = new WordGame(provider, evaluator);
        game.run();
    }
}

/**
 * Controls the main game flow.
 * Responsible for presenting words to the user, reading input,
 * limiting the number of questions, evaluating answers, and
 * displaying statistics.
 */
class WordGame {

    /** Maximum number of questions per game session. */
    private static final int Max_Questions = 10;

    private final VocabularyProvider provider;
    private final AnswerEvaluator evaluator;

    /**
     * Creates a new WordGame instance.
     * @param provider  source of vocabulary items
     * @param evaluator evaluator used to judge user answers
     */
    public WordGame(VocabularyProvider provider, AnswerEvaluator evaluator) {
        this.provider = provider;
        this.evaluator = evaluator;
    }

    /**
     * Runs the main game loop.
     * The game ends when the user enters 'Q' or when the maximum
     * number of questions has been reached.
     */
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

                switch (result.status()) {
                    case CORRECT -> {
                        System.out.println("Korrekt!");
                        correctCount++;
                    }
                    case ALMOST ->
                            System.out.println("Nästan korrekt.. (" + result.details() + ")");
                    case WRONG ->
                            System.out.println("Fel. Rätt svar: " + item.primaryEnglish());
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
 * Interface for providing vocabulary data.
 * This abstraction makes it easy to change how vocabulary is loaded,
 * for example from a file or database.
 */
interface VocabularyProvider {

    /**
     * Returns all available vocabulary items.
     * @return list of vocabulary items
     */
    List<VocabItem> getVocabulary();
}

/**
 * In-memory implementation of {@link VocabularyProvider}.
 * Vocabulary is hardcoded directly in the program.
 */
class InMemoryVocabulary implements VocabularyProvider {

    /**
     * Returns a predefined list of Swedish-English vocabulary items.
     * @return list of vocabulary items
     */
    @Override
    public List<VocabItem> getVocabulary() {
        List<VocabItem> list = new ArrayList<>();
        list.add(new VocabItem("försöka", List.of("attempt")));
        list.add(new VocabItem("förklara", List.of("explain")));
        list.add(new VocabItem("bestämma", List.of("decide")));
        list.add(new VocabItem("förstå", List.of("understand")));
        list.add(new VocabItem("förändra", List.of("change")));
        list.add(new VocabItem("förbättra", List.of("improve")));
        list.add(new VocabItem("berätta", List.of("tell")));
        list.add(new VocabItem("beskriva", List.of("describe")));
        list.add(new VocabItem("använda", List.of("use")));
        list.add(new VocabItem("påverka", List.of("affect")));
        list.add(new VocabItem("tillvägagångssätt", List.of("procedure")));
        list.add(new VocabItem("överensstämmelse", List.of("consistency")));
        list.add(new VocabItem("ansvarsfördelning", List.of("allocation")));
        list.add(new VocabItem("förutsägbarhet", List.of("predictability")));
        list.add(new VocabItem("meningsskiljaktighet", List.of("disagreement")));
        list.add(new VocabItem("konsekvens", List.of("consequence")));
        return list;
    }
}

/**
 * Represents a vocabulary item consisting of a Swedish word
 * and one or more accepted English translations.
 * @param swedish the Swedish word
 * @param acceptedEnglish accepted English translations
 */
record VocabItem(String swedish, List<String> acceptedEnglish) {

    /**
     * Returns the primary English translation.
     * @return first accepted English translation
     */
    public String primaryEnglish() {
        return acceptedEnglish.get(0);
    }
}

/**
 * Evaluates a user's answer against a vocabulary item.
 */
class AnswerEvaluator {

    /**
     * Evaluates the user's input and determines the result.
     * @param userInput the user's guessed translation
     * @param item the vocabulary item being translated
     * @return evaluation result
     */
    public EvaluationResult evaluate(String userInput, VocabItem item) {
        String guess = normalize(userInput);

        for (String accepted : item.acceptedEnglish()) {
            String target = normalize(accepted);
            if (guess.equals(target)) {
                return new EvaluationResult(EvaluationStatus.CORRECT, "Exakt match");
            }
        }

        for (String accepted : item.acceptedEnglish()) {
            String target = normalize(accepted);
            int matches = Similarity.positionMatches(guess, target);
            int targetLength = target.length();

            if (matches > targetLength / 2) {
                String details = matches + "/" + targetLength
                        + " bokstäver på rätt plats jämfört med \"" + accepted + "\"";
                return new EvaluationResult(EvaluationStatus.ALMOST, details);
            }
        }

        return new EvaluationResult(EvaluationStatus.WRONG, "Ingen majoritet korrekt");
    }

    /**
     * Normalizes a string for comparison.
     * @param s input string
     * @return trimmed and lowercased string
     */
    private String normalize(String s) {
        return s.trim().toLowerCase();
    }
}

/**
 * Utility class for comparing strings based on character positions.
 */
class Similarity {

    /**
     * Counts how many characters match at the same position in two strings.
     * @param guess user's input
     * @param target correct answer
     * @return number of matching characters
     */
    public static int positionMatches(String guess, String target) {
        int min = Math.min(guess.length(), target.length());
        int matches = 0;

        for (int i = 0; i < min; i++) {
            if (guess.charAt(i) == target.charAt(i)) {
                matches++;
            }
        }
        return matches;
    }
}

/**
 * Possible evaluation outcomes.
 */
enum EvaluationStatus {CORRECT, ALMOST, WRONG}

/**
 * Result from evaluating a user's answer.
 * @param status evaluation status
 * @param details additional information
 */
record EvaluationResult(EvaluationStatus status, String details) {}
