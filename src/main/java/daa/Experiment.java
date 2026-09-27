package daa;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public final class Experiment {
    private static final int[] INPUT_SIZES = {128, 512, 2_048, 8_192, 32_768};
    private static final int TRIALS = 3;
    private static final List<String> INPUT_TYPES =
            List.of("random", "sorted", "reverse-sorted", "duplicate-heavy");
    private static final List<String> ALGORITHMS =
            List.of("MergeSort", "RandomizedQuickSort", "DeterministicSelect", "ClosestPair");

    private Experiment() {
    }

    public static Summary run(Path projectRoot) throws IOException {
        Path resultsFile = projectRoot.resolve("results").resolve("results.csv");
        Files.createDirectories(resultsFile.getParent());

        int rowCount = 0;
        try (BufferedWriter writer = Files.newBufferedWriter(resultsFile, StandardCharsets.UTF_8)) {
            writer.write("algorithm,input_type,n,trial,execution_time_ns,max_recursion_depth,"
                    + "comparisons,swaps,recursive_calls");
            writer.newLine();
            for (int size : INPUT_SIZES) {
                for (int inputTypeIndex = 0; inputTypeIndex < INPUT_TYPES.size(); inputTypeIndex++) {
                    String inputType = INPUT_TYPES.get(inputTypeIndex);
                    for (int trial = 1; trial <= TRIALS; trial++) {
                        long seed = 0x5EEDL + (long) size * 31 + inputTypeIndex * 997L + trial;
                        for (String algorithm : ALGORITHMS) {
                            Measurement measurement = measure(algorithm, inputType, size, trial, seed);
                            writer.write(measurement.toCsv());
                            writer.newLine();
                            rowCount++;
                        }
                    }
                }
            }
        }

        Path plotsDirectory = projectRoot.resolve("docs").resolve("plots");
        ResultPlotter.generate(resultsFile, plotsDirectory);
        return new Summary(rowCount, INPUT_SIZES.length, INPUT_TYPES.size(), ALGORITHMS.size(), TRIALS);
    }

    private static Measurement measure(String algorithm, String inputType, int size, int trial, long seed) {
        long started;
        long elapsed;
        AlgorithmMetrics metrics;

        switch (algorithm) {
            case "MergeSort" -> {
                int[] values = createArray(inputType, size, seed);
                MergeSorter sorter = new MergeSorter();
                started = System.nanoTime();
                sorter.sort(values);
                elapsed = System.nanoTime() - started;
                metrics = sorter.metrics();
            }
            case "RandomizedQuickSort" -> {
                int[] values = createArray(inputType, size, seed);
                QuickSorter sorter = new QuickSorter(seed);
                started = System.nanoTime();
                sorter.sort(values);
                elapsed = System.nanoTime() - started;
                metrics = sorter.metrics();
            }
            case "DeterministicSelect" -> {
                int[] values = createArray(inputType, size, seed);
                DeterministicSelector selector = new DeterministicSelector();
                started = System.nanoTime();
                selector.select(values, size / 2);
                elapsed = System.nanoTime() - started;
                metrics = selector.metrics();
            }
            case "ClosestPair" -> {
                List<Point> points = createPoints(inputType, size, seed);
                ClosestPairSolver solver = new ClosestPairSolver();
                started = System.nanoTime();
                solver.solve(points);
                elapsed = System.nanoTime() - started;
                metrics = solver.metrics();
            }
            default -> throw new IllegalArgumentException("Unknown algorithm: " + algorithm);
        }

        return new Measurement(algorithm, inputType, size, trial, elapsed,
                metrics.maxRecursionDepth(), metrics.comparisons(), metrics.swaps(), metrics.recursiveCalls());
    }

    private static int[] createArray(String inputType, int size, long seed) {
        int[] values = new int[size];
        Random random = new Random(seed);
        switch (inputType) {
            case "random" -> {
                for (int i = 0; i < size; i++) {
                    values[i] = random.nextInt(size * 2 + 1) - size;
                }
            }
            case "sorted" -> {
                for (int i = 0; i < size; i++) {
                    values[i] = i;
                }
            }
            case "reverse-sorted" -> {
                for (int i = 0; i < size; i++) {
                    values[i] = size - i;
                }
            }
            case "duplicate-heavy" -> {
                for (int i = 0; i < size; i++) {
                    values[i] = random.nextInt(8);
                }
            }
            default -> throw new IllegalArgumentException("Unknown input type: " + inputType);
        }
        return values;
    }

    private static List<Point> createPoints(String inputType, int size, long seed) {
        Random random = new Random(seed);
        List<Point> points = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            double x;
            double y;
            switch (inputType) {
                case "random" -> {
                    x = random.nextInt(size * 4 + 1);
                    y = random.nextInt(size * 4 + 1);
                }
                case "sorted" -> {
                    x = i;
                    y = random.nextInt(size * 4 + 1);
                }
                case "reverse-sorted" -> {
                    x = size - i;
                    y = random.nextInt(size * 4 + 1);
                }
                case "duplicate-heavy" -> {
                    x = random.nextInt(8);
                    y = random.nextInt(8);
                }
                default -> throw new IllegalArgumentException("Unknown input type: " + inputType);
            }
            points.add(new Point(x, y));
        }
        return points;
    }

    public record Summary(int rows, int inputSizes, int inputTypes, int algorithms, int trials) {
    }

    private record Measurement(String algorithm, String inputType, int size, int trial, long elapsedNanos,
                              int recursionDepth, long comparisons, long swaps, long recursiveCalls) {
        private String toCsv() {
            return algorithm + "," + inputType + "," + size + "," + trial + "," + elapsedNanos + ","
                    + recursionDepth + "," + comparisons + "," + swaps + "," + recursiveCalls;
        }
    }
}
