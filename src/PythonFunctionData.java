import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static java.lang.Math.max;
import static java.lang.Math.min;

public class PythonFunctionData {
    String xt;
    String yt;
    String variableName;
    double lowerBound;
    double upperBound;

    double originalLowerBound;
    double originalUpperBound;

    public PythonFunctionData(String xt, String yt, String variableName, double lowerBound, double upperBound) {
        this.xt = xt;
        this.yt = yt;
        this.variableName = variableName;
        this.lowerBound = min(lowerBound, upperBound);
        this.upperBound = max(lowerBound, upperBound);

        this.originalLowerBound = lowerBound;
        this.originalUpperBound = upperBound;
    }

    public double normalize(double value) {
        return (value - originalLowerBound) / (originalUpperBound - originalLowerBound);
    }

    // TODO(mbjorn): Name things in this class better.....
    public static String getPythonScript(PythonFunctionData f1, PythonFunctionData f2) {
        return String.format("from scipy.optimize import least_squares\n" +
                        "import math\n" +
                        "\n" +
                        "def equations(p):\n" +
                        "    %s, %s = p\n" +
                        "    return ((%s) - (%s), (%s) - (%s))\n" +
                        "\n" +
                        "bounds = ((%s, %s), (%s, %s))\n" +
                        "\n" +
                        "smallestValidX = 1000000\n" +
                        "largestValidX = -1000000\n" +
                        "\n" +
                        "solutionWithSmallest = None\n" +
                        "solutionWithLargest = None\n" +
                        "\n" +
                        "for p in range(1, 100):\n" +
                        "    x0 = bounds[0][0] + (bounds[1][0] - bounds[0][0]) * (p / 100.0)\n" +
                        "    y0 = bounds[0][1] + (bounds[1][1] - bounds[0][1]) * (p / 100.0)\n" +
                        "    res = least_squares(equations, (x0, y0), bounds = bounds)\n" +
                        "    if res.cost < 0.0001:\n" +
                        "        if res.x[0] < smallestValidX:\n" +
                        "            solutionWithSmallest = res.x\n" +
                        "            smallestValidX = res.x[0]\n" +
                        "        if res.x[0] > largestValidX:\n" +
                        "            solutionWithLargest = res.x\n" +
                        "            largestValidX = res.x[0]\n" +
                        "## MOB-EOF"
                , f1.variableName, f2.variableName, f1.xt, f2.xt, f1.yt, f2.yt, f1.lowerBound, f2.lowerBound, f1.upperBound, f2.upperBound);
    }

    public static String getPythonScriptForAllIntersections(PythonFunctionData f1, PythonFunctionData f2) {
        return String.format("from scipy.optimize import least_squares\n" +
                        "import math\n" +
                        "\n" +
                        "def equations(p):\n" +
                        "    %s, %s = p\n" +
                        "    return ((%s) - (%s), (%s) - (%s))\n" +
                        "\n" +
                        "bounds = ((%s, %s), (%s, %s))\n" +
                        "\n" +
                        "valid_solutions = []\n" +
                        "\n" +
                        "for p in range(1, 100):\n" +
                        "    x0 = bounds[0][0] + (bounds[1][0] - bounds[0][0]) * (p / 100.0)\n" +
                        "    y0 = bounds[0][1] + (bounds[1][1] - bounds[0][1]) * (p / 100.0)\n" +
                        "    res = least_squares(equations, (x0, y0), bounds = bounds)\n" +
                        "    if res.cost < 0.000001:\n" +
                        "        valid_solutions.append(res.x)\n" +
                        "    y0 = bounds[1][1] + (bounds[0][1] - bounds[1][1]) * (p / 100.0)\n" +
                        "    res = least_squares(equations, (x0, y0), bounds = bounds)\n" +
                        "    if res.cost < 0.000001:\n" +
                        "        valid_solutions.append(res.x)\n" +
                        "    t_b = [[bounds[0][0], bounds[0][1] + (bounds[1][1] - bounds[0][1]) * p/100.0], [(bounds[1][0] - bounds[0][0]) * p/100.0 + bounds[0][0], bounds[1][1]]]\n" +
                        "    x0 = (t_b[0][0] + t_b[1][0]) / 2\n" +
                        "    y0 = (t_b[0][1] + t_b[1][1]) / 2\n" +
                        "    res = least_squares(equations, (x0, y0), bounds = t_b)\n" +
                        "    if res.cost < 0.000001:\n" +
                        "        valid_solutions.append(res.x)\n" +
                        "## MOB-EOF"
                , f1.variableName, f2.variableName, f1.xt, f2.xt, f1.yt, f2.yt, f1.lowerBound, f2.lowerBound, f1.upperBound, f2.upperBound);
    }

    // TODO(mbjorn) Fix so we return proper formatted data in a subclass or something
    public static double[] solveFor(PythonFunctionData solveFor, PythonFunctionData against) {
        String script = getPythonScript(solveFor, against);
        try {
            Socket socket = new Socket("localhost", 54321);
            OutputStream output = socket.getOutputStream();
            output.write(script.getBytes(StandardCharsets.UTF_8));

            BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            String result = input.lines().collect(Collectors.joining("\n"));

            if (result.length() < 4) {
                return null;
            }

            double[] data = Arrays.stream(result.split("\n")).mapToDouble(Double::parseDouble).toArray();
            data[0] = solveFor.normalize(data[0]);
            data[1] = against.normalize(data[1]);
            data[2] = solveFor.normalize(data[2]);
            data[3] = against.normalize(data[3]);
            return data;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    // TODO(mbjorn) Fix so we return proper formatted data in a subclass or something
    public static double[] findAllIntersections(PythonFunctionData f1, PythonFunctionData f2) {
        String script = getPythonScriptForAllIntersections(f1, f2);
        try {
            Socket socket = new Socket("localhost", 54321);
            OutputStream output = socket.getOutputStream();
            output.write(script.getBytes(StandardCharsets.UTF_8));

            BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            String result = input.lines().collect(Collectors.joining("\n")).trim();

            if (result.length() == 0) {
                return new double[0];
            }

            double[] data = Arrays.stream(result.trim().split("\n")).mapToDouble(Double::parseDouble).toArray();
            for (int i = 0; i < data.length; i += 2) {
                data[i] = f1.normalize(data[i]);
                data[i + 1] = f2.normalize(data[i + 1]);
            }
            return data;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
