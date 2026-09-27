package daa;

import java.util.Objects;
import java.util.Random;

public final class QuickSorter {
    private final AlgorithmMetrics metrics = new AlgorithmMetrics();
    private final Random random;

    public QuickSorter() {
        this(new Random());
    }

    public QuickSorter(long seed) {
        this(new Random(seed));
    }

    private QuickSorter(Random random) {
        this.random = random;
    }

    public void sort(int[] values) {
        Objects.requireNonNull(values, "values");
        metrics.reset();
        sortRange(values, 0, values.length - 1, 1);
    }

    public AlgorithmMetrics metrics() {
        return metrics;
    }

    private void sortRange(int[] values, int low, int high, int depth) {
        metrics.recordRecursiveCall(depth);
        while (low < high) {
            int pivot = values[low + random.nextInt(high - low + 1)];
            int less = low;
            int current = low;
            int greater = high;

            while (current <= greater) {
                metrics.recordComparison();
                if (values[current] < pivot) {
                    swap(values, less++, current++);
                } else {
                    metrics.recordComparison();
                    if (values[current] > pivot) {
                        swap(values, current, greater--);
                    } else {
                        current++;
                    }
                }
            }

            int leftSize = less - low;
            int rightSize = high - greater;
            if (leftSize < rightSize) {
                if (leftSize > 1) {
                    sortRange(values, low, less - 1, depth + 1);
                }
                low = greater + 1;
            } else {
                if (rightSize > 1) {
                    sortRange(values, greater + 1, high, depth + 1);
                }
                high = less - 1;
            }
        }
    }

    private void swap(int[] values, int first, int second) {
        if (first != second) {
            int temporary = values[first];
            values[first] = values[second];
            values[second] = temporary;
            metrics.recordSwap();
        }
    }
}
