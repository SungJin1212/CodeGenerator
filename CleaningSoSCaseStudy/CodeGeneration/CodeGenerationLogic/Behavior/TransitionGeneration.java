package CleaningSoSCaseStudy.CodeGeneration.CodeGenerationLogic.Behavior;

import CodeGeneration.XMLParseDataType.Behavior.State;
import CodeGeneration.XMLParseDataType.Behavior.Synchronization;
import CodeGeneration.XMLParseDataType.Behavior.Transition;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeVariableName;

import javax.lang.model.element.Modifier;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class TransitionGeneration {

    public static ArrayList<MethodSpec> getTransition(ArrayList<Transition> transitions, Set<Synchronization> synchronizations, ArrayList<State> states) { //return transition code
        ArrayList<MethodSpec> results = new ArrayList<>();
        Set<String> dependentClass = new HashSet<>();
        Set<String> normalTriggerNames = new HashSet<>();
        Set<String> sendTriggerNames = new HashSet<>();
        Set<String> receiveTriggerNames = new HashSet<>();

        for(Transition t : transitions) {
            char last = getLastChar(t.getTrigger());
            if (last == '!') {
                sendTriggerNames.add(getMethodName(t.getTrigger()));
            }
            else if (last == '?') {
                receiveTriggerNames.add(getMethodName(t.getTrigger()));
            }
            else {
                normalTriggerNames.add(t.getTrigger());
            }
        }

        for(String eachTrigger : normalTriggerNames) {
            results.add(getTransitionCode(transitions,eachTrigger,states));
        }
        for(String eachTrigger : receiveTriggerNames) {
            results.add(getTransitionCode(transitions,eachTrigger,states));
        }
        for(String eachTrigger : sendTriggerNames) {
            for(Synchronization synchronization : synchronizations) {
                if(eachTrigger.equals(getMethodName(synchronization.getTrigger()))) {
                    dependentClass.add(synchronization.getName());
                }
            }
            results.add(getSendTransitionCode(transitions,eachTrigger,dependentClass,states));
            dependentClass.clear();
        }

        return results;
    }

    private static MethodSpec getSendTransitionCode(ArrayList<Transition> transitions, String curTriggerName, Set<String> dependentClass, ArrayList<State> states) {
        MethodSpec.Builder builder = MethodSpec.methodBuilder(curTriggerName).addModifiers(Modifier.PUBLIC).returns(boolean.class);


        for(String dependentClassName : dependentClass) {
            builder.addParameter(TypeVariableName.get(dependentClassName), getFirstChar(dependentClassName)).build();
        }

        CodeBlock transitionCode = getSendTransitionLogicCode(transitions,curTriggerName,dependentClass,states);

        builder.addCode(transitionCode);

        return builder.build();

    }

    private static CodeBlock getSendTransitionLogicCode(ArrayList<Transition> transitions, String curTriggerName, Set<String> dependentClass, ArrayList<State> states) {
        boolean isGuard;
        boolean isProbability;

        CodeBlock.Builder builder = CodeBlock.builder();

        builder.addStatement("boolean wasEventProcessed = false");
        builder.addStatement("Status aStatus = status");
        builder.beginControlFlow("switch(aStatus)");

        for(Transition t : transitions) {
            if (getMethodName(t.getTrigger()).equals(curTriggerName)) {

                builder.beginControlFlow("case " + t.getFrom() + ":");

                for (String dependentClassName : dependentClass) {
                    builder.addStatement(getFirstChar(dependentClassName) + "." + getMethodName(t.getTrigger()) + "()");
                } //call dependent classes.

                if (!t.getAction().equals("NoAction")) {
                    String[] actionList = t.getAction().split(",");
                    for (String s : actionList) {
                        builder.addStatement(s + "()");
                    }
                }

                builder.addStatement("setStatus(Status." + t.getTo() + ")");

                for(State s : states) {
                    if (s.getStateName().equals(t.getTo())) { // 도착하는 state를 찾아서
                        if(s.getAtomic().equals("1")) {
                            for(Transition tran : transitions) {
                                if(tran.getFrom().equals(s.getStateName())) {
                                    builder.addStatement("$N()", tran.getTrigger());
                                    break;
                                }
                            }
                        }
                        break;
                    }
                }
                builder.addStatement("wasEventProcessed = true");
                builder.addStatement("break");
                builder.endControlFlow();
            }
        }

        builder.endControlFlow();

        builder.addStatement("return wasEventProcessed");

        return builder.build();
    }

    private static MethodSpec getTransitionCode(ArrayList<Transition> transitions, String curTriggerName, ArrayList<State> states) {
        MethodSpec.Builder builder = MethodSpec.methodBuilder(curTriggerName).addModifiers(Modifier.PUBLIC).returns(boolean.class);

        CodeBlock transitionCode = getTransitionLogicCode(transitions,curTriggerName,states);

        builder.addCode(transitionCode);

        return builder.build();

    }

    private static CodeBlock getTransitionLogicCode(ArrayList<Transition> transitions, String curTriggerName, ArrayList<State> states) {
        boolean isGuard;
        boolean isProbability;

        CodeBlock.Builder builder = CodeBlock.builder();

        builder.addStatement("boolean wasEventProcessed = false");
        builder.addStatement("Status aStatus = status");
        builder.beginControlFlow("switch(aStatus)");

        for(Transition t : transitions) {
            if (getMethodName(t.getTrigger()).equals(curTriggerName)) {


                builder.beginControlFlow("case " + t.getFrom() + ":");


                if (!t.getAction().equals("NoAction")) { /*Action code generation*/
                    String[] actionList = t.getAction().split(",");
                    for (String s : actionList) {
                        builder.addStatement(s + "()");
                    }
                }

                builder.addStatement("setStatus(Status." + t.getTo() + ")");

                for(State s : states) {
                    if (s.getStateName().equals(t.getTo())) { // 도착하는 state를 찾아서
                        if(s.getAtomic().equals("1")) {
                            for(Transition tran : transitions) {
                                if(tran.getFrom().equals(s.getStateName())) {
                                    builder.addStatement("$N()", tran.getTrigger());
                                    break;
                                }
                            }
                        }
                        break;
                    }
                }

                builder.addStatement("wasEventProcessed = true");
                builder.addStatement("break");
//                if (isGuard) builder.endControlFlow();
                builder.endControlFlow();
            }
        }

        builder.endControlFlow();

        builder.addStatement("return wasEventProcessed");

        return builder.build();
    }

    private static String getMethodName(String channel) {
        StringBuilder methodName = new StringBuilder();

        for(int i=0; i<channel.length(); i++) {
            int idx = channel.charAt(i);

            if(idx >= 65 && idx <=122) {
                if (idx == 91) break;
                methodName.append(channel.charAt(i));
            }
        }
        return methodName.toString();

    }
    private static char getLastChar(String str) {
        return str.charAt(str.length()-1);
    }
    private static String getFirstChar(String str) {
        return String.valueOf(str.charAt(0));
    }
}
