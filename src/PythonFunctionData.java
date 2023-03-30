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

    public PythonFunctionData(String xt, String yt, String variableName, double lowerBound, double upperBound) {
        this.xt = xt;
        this.yt = yt;
        this.variableName = variableName;
        this.lowerBound = min(lowerBound, upperBound);
        this.upperBound = max(lowerBound, upperBound);
    }

    public static String getPythonScript(PythonFunctionData f1, PythonFunctionData f2) {
        return String.format("from scipy.optimize import least_squares\n" +
                        "import math\n" +
                        "\n" +
                        "def equations(p):\n" +
                        "    %s, %s = p\n" +
                        "    return (%s - %s, %s - %s)\n" +
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
                        "    if res.cost < 0.1:\n" +
                        "        if res.x[0] < smallestValidX:\n" +
                        "            solutionWithSmallest = res.x\n" +
                        "            smallestValidX = res.x[0]\n" +
                        "        if res.x[0] > largestValidX:\n" +
                        "            solutionWithLargest = res.x\n" +
                        "            largestValidX = res.x[0]\n" +
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
            data[0] = (data[0] - solveFor.lowerBound) / (solveFor.upperBound - solveFor.lowerBound);
            data[1] = (data[1] - against.lowerBound) / (against.upperBound - against.lowerBound);
            data[2] = (data[2] - solveFor.lowerBound) / (solveFor.upperBound - solveFor.lowerBound);
            data[3] = (data[3] - against.lowerBound) / (against.upperBound - against.lowerBound);
            return data;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
