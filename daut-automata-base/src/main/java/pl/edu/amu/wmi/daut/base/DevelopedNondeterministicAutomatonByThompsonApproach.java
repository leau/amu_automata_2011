package pl.edu.amu.wmi.daut.base;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

public class DevelopedNondeterministicAutomatonByThompsonApproach implements Acceptor {

    private final AutomatonSpecification automaton;
    private HashMap<List<State>, HashMap<String, List<State>>> map =
            new HashMap<List<State>, HashMap<String, List<State>>>();

    DevelopedNondeterministicAutomatonByThompsonApproach(final AutomatonSpecification specification)
    {
        automaton = specification;
    }

    @Override
    public boolean accepts(String text) {
        boolean accept = false;
        boolean added;
        boolean skip;

        int i = 0;
        int limit = text.length();

        List<State> currentStates = new LinkedList<State>();
        List<State> temporaryStates = new LinkedList<State>();
        List<State> pStates = new LinkedList<State>();
        List<State> startMapStates = new LinkedList<State>();

        currentStates.add(automaton.getInitialState());

        do {
            skip = false;

            if (map.containsKey(currentStates)) {
                HashMap<String, List<State>> temporaryMap =
                        new HashMap<String, List<State>>();
                temporaryMap.putAll(map.get(currentStates));

                if(i == limit || limit == 0) {
                    if (temporaryMap.containsKey("")) {
                        currentStates.clear();
                        currentStates.addAll(temporaryMap.get(""));
                        skip = true;
                    } else {
                        startMapStates.addAll(currentStates);
                    }
                } else {
                    char[] ch = new char[1];
                    ch[0] = text.charAt(i);
                    String str = new String(ch);

                    if (temporaryMap.containsKey(str)) {
                        currentStates.clear();
                        currentStates.addAll(temporaryMap.get(str));
                        skip = true;
                    } else {
                        startMapStates.addAll(currentStates);
                    }
                }
            } else {
                startMapStates.addAll(currentStates);
            }


            if(!skip) {
                do {
                    added = false;

                    for (State someState : currentStates) {
                        List<State> epsilonStates =
                                new LinkedList<State>(epsilonClosure(someState));

                        for (State eState : epsilonStates) {
                            if (!currentStates.contains(eState)
                                    && !pStates.contains(eState)) {
                                pStates.add(eState);
                                added = true;
                            }
                        }
                    }
                    currentStates.addAll(pStates);
                    pStates.clear();
                } while (added);
            }


            if (limit != 0 && i != limit && !skip) {
                for (State someState : currentStates) {
                    List<OutgoingTransition> someStateTransitions =
                            new LinkedList<OutgoingTransition>(
                            automaton.allOutgoingTransitions(someState));

                    for (OutgoingTransition transition : someStateTransitions) {
                        if (transition.getTransitionLabel().canAcceptCharacter(text.charAt(i))
                                && !temporaryStates.contains(transition.getTargetState())) {
                            temporaryStates.add(transition.getTargetState());
                        }
                    }
                }

                currentStates.clear();
                currentStates.addAll(temporaryStates);
                temporaryStates.clear();
            }

            if (!skip) {
                HashMap<String, List<State>> temporaryMap =
                        new HashMap<String, List<State>>();

                if(limit == 0 || i == limit) {
                    temporaryMap.put("", currentStates);
                    map.put(startMapStates, temporaryMap);
                    startMapStates.clear();
                } else {
                    char[] ch = new char[1];
                    ch[0] = text.charAt(i);
                    String str = new String(ch);

                    temporaryMap.put(str, currentStates);
                    map.put(startMapStates, temporaryMap);
                    startMapStates.clear();
                }
            }

            i++;

        } while (i <= limit);

        for (State someState : currentStates) {
            if (automaton.isFinal(someState)) {
                accept = true;
            }
        }

        return accept;
    }

    private List<State> epsilonClosure(State state) {
        List<State> epsilonStates = new LinkedList<State>();
        List<State> temporaryStates = new LinkedList<State>();
        List<State> pStates = new LinkedList<State>();
        boolean added;

        epsilonStates.add(state);

        do {
            added = false;

            for (State someState : epsilonStates) {
                List<OutgoingTransition> someStateTransitions = new LinkedList<OutgoingTransition>(
                        automaton.allOutgoingTransitions(someState));

                for (OutgoingTransition transition : someStateTransitions) {
                    if (transition.getTransitionLabel().canBeEpsilon()
                            && !temporaryStates.contains(transition.getTargetState())) {
                        temporaryStates.add(transition.getTargetState());
                    }
                }

                for (State tState : temporaryStates) {
                    if (!epsilonStates.contains(tState)
                            && !pStates.contains(tState)) {
                        pStates.add(tState);
                        added = true;
                    }
                }
                temporaryStates.clear();
            }

            epsilonStates.addAll(pStates);
            pStates.clear();

        } while (added);

        return epsilonStates;
    }
}
