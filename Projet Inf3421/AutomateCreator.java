import java.util.*;

public class AutomateCreator {
    
    private static class State {
        String name;
        boolean isInitial;
        boolean isFinal;
        List<Transition> transitions = new ArrayList<>();
        
        public State(String name, boolean isInitial, boolean isFinal) {
            this.name = name;
            this.isInitial = isInitial;
            this.isFinal = isFinal;
        }
    }

    private static class Transition {
        State from;
        State to;
        char input;
        
        public Transition(State from, State to, char input) {
            this.from = from;
            this.to = to;
            this.input = input;
        }
    }

    private static List<State> states = new ArrayList<>();
    private static List<Transition> transitions = new ArrayList<>();

    public static void main(String[] args) {
        createAutomate();
        
        String reason = isDeterministic();
        if (reason.isEmpty()) {
            System.out.println("L'automate est déterministe fini.");
            
            Scanner scanner = new Scanner(System.in);
            
            while (true) {
                System.out.print("Entrez une chaîne de caractères (ou tapez 'q' pour quitter) : ");
                String input = scanner.nextLine();
                
                if (input.equalsIgnoreCase("q")) {
                    break;
                }
                
                if (isAccepted(input)) {
                    System.out.println("La chaîne '" + input + "' est acceptée par l'automate.");
                } else {
                    System.out.println("La chaîne '" + input + "' n'est pas acceptée par l'automate.");
                    explainRejection(input);
                }
            }
        } else {
            System.out.println("L'automate n'est pas déterministe fini. Raison : " + reason);
        }
    }

    private static void createAutomate() {
        Scanner scanner = new Scanner(System.in);
        
        System.out.print("Nombre d'états : ");
        int nbStates = scanner.nextInt();
        scanner.nextLine();
        
        for (int i = 0; i < nbStates; i++) {
            System.out.print("Nom de l'état " + (i+1) + " : ");
            String stateName = scanner.nextLine();
            boolean isInitial = getValidInitialFlag(new Scanner(System.in));
            boolean isFinal = getValidFinalFlag(new Scanner(System.in));
            
            State state = new State(stateName, isInitial, isFinal);
            states.add(state);
        }
        
        System.out.print("Nombre de transitions : ");
        int nbTransitions = scanner.nextInt();
        scanner.nextLine();
        
        for (int i = 0; i < nbTransitions; i++) {
            System.out.print("État de départ : ");
            String fromStateName = scanner.nextLine();
            State toState = getValidToState(new Scanner(System.in));
            
            System.out.print("Caractère d'entrée : ");
            char input = scanner.nextLine().charAt(0);
            
            State fromState = getState(fromStateName);
            
            
            Transition transition = new Transition(fromState, toState, input);
            fromState.transitions.add(transition);
            transitions.add(transition);
        }
    }

    private static String isDeterministic() {
        StringBuilder reason = new StringBuilder();

        // Vérifier qu'il n'y a pas plus d'un état initial
        int nbInitialStates = 0;
        for (State state : states) {
            if (state.isInitial) {
                nbInitialStates++;
            }
        }
        if (nbInitialStates != 1) {
            reason.append("Il y a plus d'un état initial, ");
        }

        // Vérifier que chaque état a au plus deux transitions avec des entrées différentes
        for (State state : states) {
            Map<Character, List<State>> transitionsMap = new HashMap<>();
            
            for (Transition transition : state.transitions) {
                char input = transition.input;
                State toState = transition.to;
                
                if (!transitionsMap.containsKey(input)) {
                    transitionsMap.put(input, new ArrayList<>());
                }
                
                transitionsMap.get(input).add(toState);
                
                if (transitionsMap.get(input).size() > 1) {
                    reason.append("L'état ").append(state.name).append(" a plus de deux transitions, ");
                    break;
                }
            }
            
            if (transitionsMap.size() > 2) {
                reason.append("L'état ").append(state.name).append(" a des transitions avec les mêmes caractères d'entrée, ");
            }
        }
        
        return reason.toString().trim();
    }

    private static boolean isAccepted(String input) {
        if (isDeterministic().isEmpty()) {
            State currentState = null;
            
            // Trouver l'état initial
            for (State state : states) {
                if (state.isInitial) {
                    currentState = state;
                    break;
                }
            }
            
            for (char c : input.toCharArray()) {
                boolean transitionFound = false;
                
                // Chercher une transition depuis l'état courant avec le caractère d'entrée c
                for (Transition transition : currentState.transitions) {
                    if (transition.input == c) {
                        currentState = transition.to;
                        transitionFound = true;
                        break;
                    }
                }
                
                if (!transitionFound) {
                    return false;
                }
            }
            
            // Vérifier que l'état final est atteint
            return currentState.isFinal;
        } else {
            return false;
        }
    }

    private static void explainRejection(String input) {
        State currentState = null;
        
        // Trouver l'état initial
        for (State state : states) {
            if (state.isInitial) {
                currentState = state;
                break;
            }
        }
        
        int i = 0;
        for (char c : input.toCharArray()) {
            boolean transitionFound = false;
            
            // Chercher une transition depuis l'état courant avec le caractère d'entrée c
            for (Transition transition : currentState.transitions) {
                if (transition.input == c) {
                    currentState = transition.to;
                    transitionFound = true;
                    break;
                }
            }
            
            if (!transitionFound) {
                System.out.println("La chaîne n'est pas acceptée après le caractère '" + c + "' à l'index " + i + ".");
                return;
            }
            
            i++;
        }
        
        if (!currentState.isFinal) {
            System.out.println("La chaîne atteint l'état final mais n'est pas acceptée.");
        }
    }
    private static String getValidInput(Scanner scanner) {
        while (true) {
            System.out.print("Entrez une chaîne de caractères (ou tapez 'q' pour quitter) : ");
            String input = scanner.nextLine();
            
            if (input.equalsIgnoreCase("q")) {
                return "q";
            }
            
            if (!input.isEmpty()) {
                return input;
            }
            
            System.out.println("L'entrée ne peut pas être vide. Veuillez réessayer.");
        }
    }

    private static State getValidToState(Scanner scanner) {
        while (true) {
            System.out.print("Entrez le nom de l'état cible de la transition : ");
            String stateName = scanner.nextLine();
            
            State state = getState(stateName);
            if (state != null) {
                return state;
            }
            
            System.out.println("L'état '" + stateName + "' n'existe pas. Veuillez réessayer.");
        }
    }

    private static char getValidTransitionInput(Scanner scanner) {
        while (true) {
            System.out.print("Entrez le caractère d'entrée de la transition : ");
            String input = scanner.nextLine();
            
            if (input.length() == 1) {
                return input.charAt(0);
            }
            
            System.out.println("L'entrée doit être un seul caractère. Veuillez réessayer.");
        }
    }

    private static String getValidStateName(Scanner scanner) {
        while (true) {
            System.out.print("Entrez le nom de l'état : ");
            String stateName = scanner.nextLine();
            
            if (!stateName.isEmpty() && getState(stateName) == null) {
                return stateName;
            }
            
            System.out.println("Le nom d'état ne peut pas être vide et doit être unique. Veuillez réessayer.");
        }
    }

    private static boolean getValidInitialFlag(Scanner scanner) {
        while (true) {
            System.out.print("Cet état est-il initial ? (y/n) ");
            String response = scanner.nextLine();
            
            if (response.equalsIgnoreCase("y")) {
                return true;
            } else if (response.equalsIgnoreCase("n")) {
                return false;
            }
            
            System.out.println("Réponse invalide. Veuillez répondre 'y' ou 'n'.");
        }
    }

    private static boolean getValidFinalFlag(Scanner scanner) {
        while (true) {
            System.out.print("Cet état est-il final ? (y/n) ");
            String response = scanner.nextLine();
            
            if (response.equalsIgnoreCase("y")) {
                return true;
            } else if (response.equalsIgnoreCase("n")) {
                return false;
            }
            
            System.out.println("Réponse invalide. Veuillez répondre 'y' ou 'n'.");
        }
    }

    private static State getState(String name) {
        for (State state : states) {
            if (state.name.equals(name)) {
                return state;
            }
        }
        return null;
    }
}