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

                switch (result.status()) {
                    case CORRECT -> {
                        System.out.println("Korrekt!");
                        correctCount++;
                    }
                    case ALMOST -> System.out.println("Nästan korrekt.. (" + result.details() + ")");
                    case WRONG -> System.out.println("Fel. Rätt svar: " + item.primaryEnglish());
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

interface VocabularyProvider {
    List<VocabItem> getVocabulary();
}

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

record VocabItem(String swedish, List<String> acceptedEnglish) {
    public String primaryEnglish() {
        return acceptedEnglish.get(0);
    }
}

class AnswerEvaluator {
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
            boolean almost = matches > targetLength / 2;

            if (almost) {
                String details = matches + "/" + targetLength + " bokstäver på rätt plats jämfört med \"" + accepted + "\"";
                return new EvaluationResult(EvaluationStatus.ALMOST, details);
            }
        }

        return new EvaluationResult(EvaluationStatus.WRONG, "Ingen majoritet korrekt");
    }

    private String normalize(String s) {
        return s.trim().toLowerCase();
    }
}

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

enum EvaluationStatus {CORRECT, ALMOST, WRONG}

record EvaluationResult(EvaluationStatus status, String details) {}
