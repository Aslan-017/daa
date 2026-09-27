package daa;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class Main {
    private Main() {
    }

    public static void main(String[] args) throws IOException {
        Path projectRoot = Path.of(".");
        List<String> output = new ArrayList<>();
        int[] input = {9, 3, 7, 3, 1, 8, 2};

        int[] mergeInput = input.clone();
        MergeSorter mergeSorter = new MergeSorter();
        mergeSorter.sort(mergeInput);
        report(output, "MergeSort: " + Arrays.toString(mergeInput));

        int[] quickInput = input.clone();
        QuickSorter quickSorter = new QuickSorter(42L);
        quickSorter.sort(quickInput);
        report(output, "Randomized QuickSort: " + Arrays.toString(quickInput));

        int[] selectionInput = input.clone();
        int rank = selectionInput.length / 2;
        DeterministicSelector selector = new DeterministicSelector();
        int selected = selector.select(selectionInput, rank);
        report(output, "Deterministic Select (k=" + rank + "): " + selected);

        List<Point> points = List.of(new Point(0, 0), new Point(5, 4), new Point(2, 2), new Point(3, 3));
        ClosestPairSolver closestPairSolver = new ClosestPairSolver();
        ClosestPairSolver.Result closest = closestPairSolver.solve(points);
        report(output, "Closest pair distance: " + closest.distance());

        Experiment.Summary summary = Experiment.run(projectRoot);
        report(output, "Experiment rows: " + summary.rows() + " (" + summary.inputSizes() + " sizes, "
                + summary.inputTypes() + " input types, " + summary.algorithms() + " algorithms, "
                + summary.trials() + " trials)");
        report(output, "CSV: results/results.csv");
        report(output, "Plots: docs/plots/execution-time-vs-n.png, docs/plots/recursion-depth-vs-n.png");

        ScreenshotGenerator.writeTerminalScreenshot(
                projectRoot.resolve("docs").resolve("screenshots").resolve("program-output.png"),
                "Assignment 1 - Program output", output);
        ScreenshotGenerator.writePlotsScreenshot(
                projectRoot.resolve("docs").resolve("plots").resolve("execution-time-vs-n.png"),
                projectRoot.resolve("docs").resolve("plots").resolve("recursion-depth-vs-n.png"),
                projectRoot.resolve("docs").resolve("screenshots").resolve("plots-results.png"));
    }

    private static void report(List<String> output, String line) {
        System.out.println(line);
        output.add(line);
    }
}
