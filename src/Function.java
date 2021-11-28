import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.util.Arrays;
import java.util.stream.Collectors;

public class Function {
    public enum Operator {
        ADD("+"), MULTIPLY("*"), SIN("s"), COS("c"), CONSTANT("C"); // for subtract just add negative numbers

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

    public double calculateValue(double t) {
        switch (op) {
            case CONSTANT:
                return value;
            case SIN:
                return Math.sin(t);
            case COS:
                return Math.cos(t);
            case ADD:
                return Arrays.stream(functions).mapToDouble(f -> f.calculateValue(t)).reduce(0, Double::sum);
            case MULTIPLY:
                return Arrays.stream(functions).mapToDouble(f -> f.calculateValue(t)).reduce(1, (a, b) -> a * b);
            default:
                throw new IllegalStateException("The operator " + op.toString() + " is not implemented yet");
        }
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
                return "(" + Arrays.stream(functions).map(Function::toString).collect(Collectors.joining(String.format(" %s ", op.symbol))) + ")";
            default:
                throw new IllegalStateException("The operator " + op.toString() + " is not implemented yet");
        }
    }
}
