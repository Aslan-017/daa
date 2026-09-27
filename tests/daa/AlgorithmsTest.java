package daa;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(TestScreenshotExtension.class)
class AlgorithmsTest {
    @Test
    void bothSortersMatchArraysSortForRequiredInputShapes() {
        Random random = new Random(202603L);
        int[] randomValues = new int[4_096];
        int[] duplicateValues = new int[4_096];
        int[] sortedValues = new int[4_096];
        int[] reverseValues = new int[4_096];
        for (int i = 0; i < randomValues.length; i++) {
            randomValues[i] = random.nextInt();
            duplicateValues[i] = random.nextInt(9) - 4;
            sortedValues[i] = i;
            reverseValues[i] = reverseValues.length - i;
        }

        List<int[]> inputs = List.of(
                new int[0],
                new int[]{7},
                randomValues,
                sortedValues,
                reverseValues,
                duplicateValues
        );
        for (int[] input : inputs) {
            int[] expected = input.clone();
            Arrays.sort(expected);

            int[] mergeInput = input.clone();
            new MergeSorter().sort(mergeInput);
            assertArrayEquals(expected, mergeInput, "MergeSort input length " + input.length);

            int[] quickInput = input.clone();
            new QuickSorter(1234L + input.length).sort(quickInput);
            assertArrayEquals(expected, quickInput, "QuickSort input length " + input.length);
        }
    }

    @Test
    void deterministicSelectMatchesSortedReferenceForAtLeastOneHundredRandomCases() {
        Random random = new Random(4815162342L);
        for (int trial = 0; trial < 250; trial++) {
            int[] values = new int[1 + random.nextInt(250)];
            for (int i = 0; i < values.length; i++) {
                values[i] = random.nextInt(101) - 50;
            }
            int[] expectedOrder = values.clone();
            Arrays.sort(expectedOrder);
            int rank = random.nextInt(values.length);

            int selected = new DeterministicSelector().select(values, rank);

            assertEquals(expectedOrder[rank], selected, "trial " + trial + ", rank " + rank);
            Arrays.sort(values);
            assertArrayEquals(expectedOrder, values, "selection must preserve the input multiset");
        }
    }

    @Test
    void closestPairMatchesBruteForceForSmallInputsIncludingTwoThousandPoints() {
        ClosestPairSolver solver = new ClosestPairSolver();
        assertFalse(solver.solve(List.of()).hasPair());
        assertFalse(solver.solve(List.of(new Point(4, -2))).hasPair());

        Random random = new Random(8675309L);
        for (int trial = 0; trial < 80; trial++) {
            int size = 2 + random.nextInt(149);
            List<Point> points = randomPoints(random, size, trial % 5 == 0);
            assertClosestDistanceMatchesBruteForce(solver, points, trial);
        }

        List<Point> maximumTestSize = randomPoints(random, 2_000, false);
        assertClosestDistanceMatchesBruteForce(solver, maximumTestSize, "n=2000");
        List<Point> duplicates = List.of(
                new Point(1, 1), new Point(1, 1), new Point(2, 2), new Point(1, 1),
                new Point(-3, 4), new Point(-3, 5), new Point(100, -100)
        );
        assertClosestDistanceMatchesBruteForce(solver, duplicates, "duplicate points");
        assertClosestDistanceMatchesBruteForce(solver,
                List.of(new Point(-1e200, 0), new Point(1e200, 0)), "large finite coordinates");
    }

    private static List<Point> randomPoints(Random random, int size, boolean useSmallCoordinateRange) {
        List<Point> points = new ArrayList<>(size);
        int bound = useSmallCoordinateRange ? 8 : 20_001;
        for (int i = 0; i < size; i++) {
            points.add(new Point(random.nextInt(bound) - bound / 2.0,
                    random.nextInt(bound) - bound / 2.0));
        }
        return points;
    }

    private static void assertClosestDistanceMatchesBruteForce(
            ClosestPairSolver solver, List<Point> points, Object label) {
        ClosestPairSolver.Result result = solver.solve(points);
        double expected = bruteForceDistance(points);
        assertTrue(result.hasPair(), "pair should exist for " + label);
        assertEquals(expected, result.distance(), 1e-10, "closest distance for " + label);
    }

    private static double bruteForceDistance(List<Point> points) {
        double best = Double.POSITIVE_INFINITY;
        for (int i = 0; i < points.size(); i++) {
            for (int j = i + 1; j < points.size(); j++) {
                double dx = points.get(i).x() - points.get(j).x();
                double dy = points.get(i).y() - points.get(j).y();
                best = Math.min(best, Math.hypot(dx, dy));
            }
        }
        return best;
    }
}
