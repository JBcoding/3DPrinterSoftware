import java.util.Arrays;
import java.util.stream.Collectors;

public class Function {
    public enum Operator {
        ADD("+"), MULTIPLY("*"), SIN("s"), COS("c"), CONSTANT("C"), DIVIDE("/"), SQRT("SQRT"); // for subtract just add negative numbers

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
            case DIVIDE:
                return functions[0].calculateValue(t) / functions[1].calculateValue(t);
            case SQRT:
                return Math.sqrt(functions[0].calculateValue(t));
            default:
                throw new IllegalStateException("The operator " + op.toString() + " is not implemented yet");
        }
    }

    public Function differentiate() {
        // https://www.mathsisfun.com/calculus/derivatives-rules.html
        switch (op) {
            case CONSTANT:
                return new Function(Operator.CONSTANT, 0);
            case SIN:
                return new Function(Operator.COS, (Object[]) functions);
            case COS:
                return new Function(Operator.MULTIPLY, -1, new Function(Operator.SIN, (Object[]) functions));
            case ADD:
                return new Function(Operator.ADD, Arrays.stream(functions).map(Function::differentiate).toArray(Object[]::new));
            case MULTIPLY:
                Function[] differentiatedParts = new Function[functions.length];
                for (int i = 0; i < functions.length; i++) {
                    Function[] subDifferentiatedParts = new Function[functions.length];
                    for (int j = 0; j < functions.length; j++) {
                        if (i == j) {
                            subDifferentiatedParts[j] = functions[j].differentiate();
                        } else {
                            subDifferentiatedParts[j] = functions[j];
                        }
                    }
                    differentiatedParts[i] = new Function(Operator.MULTIPLY, (Object[]) subDifferentiatedParts);
                }
                return new Function(Operator.ADD, (Object[]) differentiatedParts);
            case DIVIDE:
                Function f0 = functions[0];
                Function f1 = functions[1];
                Function f0d = f0.differentiate();
                Function f1d = f1.differentiate();
                Function f0f1d = new Function(Operator.MULTIPLY, f0, f1d);
                Function f1f0d = new Function(Operator.MULTIPLY, f1, f0d);
                Function f1f1 = new Function(Operator.MULTIPLY, f1, f1);
                return new Function(Operator.DIVIDE, new Function(Operator.ADD, f1f0d, new Function(Operator.MULTIPLY, f0f1d, -1)), f1f1);
            case SQRT:
                // dt sqrt(f(t))
                // 1/2 * (f(t))^(-1/2) * f'(t)
                // 1/2 * 1/sqrt(f(t)) * f'(t)
                // 1/2 * 1/sqrt(f(t)) * f'(t)
                Function fd = functions[0].differentiate();
                return new Function(Operator.MULTIPLY, new Function(Operator.CONSTANT, 1/2.0), new Function(Operator.DIVIDE, 1, new Function(Operator.SQRT, functions[0])), fd);
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
            case DIVIDE:
                return "(" + Arrays.stream(functions).map(Function::toString).collect(Collectors.joining(String.format(" %s ", op.symbol))) + ")";
            case SQRT:
                return "sqrt(" + functions[0].toString() + ")";
            default:
                throw new IllegalStateException("The operator " + op.toString() + " is not implemented yet");
        }
    }

    public String toPython(String variableName) {
        switch (op) {
            case CONSTANT:
                return String.valueOf(value);
            case SIN:
                return String.format("math.sin(%s)", variableName);
            case COS:
                return String.format("math.cos(%s)", variableName);
            case ADD:
            case MULTIPLY:
            case DIVIDE:
                return "(" + Arrays.stream(functions).map(f -> f.toPython(variableName)).collect(Collectors.joining(String.format(" %s ", op.symbol))) + ")";
            case SQRT:
                return "math.sqrt(" + functions[0].toPython(variableName) + ")";
            default:
                throw new IllegalStateException("The operator " + op + " is not implemented yet");
        }
    }
}
