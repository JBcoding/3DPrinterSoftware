import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.util.Arrays;
import java.util.stream.Collectors;

public class Function {
    public enum Operator {
        ADD("+"), SUBTRACT("-"), MULTIPLY("*"), SIN("s"), COS("c"), CONSTANT("C");

        String symbol;
        Operator(String s) {
            this.symbol = s;
        }
    }

    protected Operator op;
    Function[] functions;
    double value; // used if op is a constant

    public Function(Operator op, Object... functions) {
        this.op = op;
        this.functions = new Function[functions.length];
        for (int i = 0; i < functions.length; i++) {
            if (functions[i] instanceof Function) {
                this.functions[i] = (Function) functions[i];
            } else if (functions[i] instanceof Number) {
                this.functions[i] = new Function(Operator.CONSTANT, ((Number) functions[i]).doubleValue());
            } else {
                throw new IllegalStateException("Not a number or function argument given");
            }
        }
        this.value = 0;
    }

    public Function(Operator op, double value) {
        this.op = op;
        this.value = value;
    }

    @Override
    public String toString() {
        switch (op) {
            case CONSTANT:
                return String.valueOf(value);
            case SIN:
                return "sin(t)";
            case COS:
                return "cos(t)";
            case ADD:
            case MULTIPLY:
            case SUBTRACT:
                return "(" + Arrays.stream(functions).map(Function::toString).collect(Collectors.joining(String.format(" %s ", op.symbol))) + ")";
            default:
                throw new IllegalStateException("The operator " + op.toString() + " is not implemented yet");
        }
    }
}
