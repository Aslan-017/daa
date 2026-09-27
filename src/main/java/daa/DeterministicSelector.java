package daa;

import java.util.Objects;

public final class DeterministicSelector {
    private static final int GROUP_SIZE = 5;

    private final AlgorithmMetrics metrics = new AlgorithmMetrics();

    public int select(int[] values, int k) {
        Objects.requireNonNull(values, "values");
        if (k < 0 || k >= values.length) {
            throw new IndexOutOfBoundsException("k must be in [0, " + values.length + ")");
        }
        metrics.reset();
        return selectRange(values, 0, values.length - 1, k, 1);
    }

    public AlgorithmMetrics metrics() {
        return metrics;
    }

    private int selectRange(int[] values, int low, int high, int target, int depth) {
        metrics.recordRecursiveCall(depth);
        if (high - low + 1 <= GROUP_SIZE) {
            insertionSort(values, low, high + 1);
            return values[target];
        }

        int pivot = medianOfMedians(values, low, high, depth);
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

        if (target < less) {
            return selectRange(values, low, less - 1, target, depth + 1);
        }
        if (target > greater) {
            return selectRange(values, greater + 1, high, target, depth + 1);
        }
        return values[target];
    }

    private int medianOfMedians(int[] values, int low, int high, int depth) {
        int groupCount = (high - low + GROUP_SIZE) / GROUP_SIZE;
        for (int group = 0; group < groupCount; group++) {
            int groupLow = low + group * GROUP_SIZE;
            int groupHigh = Math.min(groupLow + GROUP_SIZE, high + 1);
            insertionSort(values, groupLow, groupHigh);
            int median = groupLow + (groupHigh - groupLow) / 2;
            swap(values, low + group, median);
        }

        int mediansHigh = low + groupCount - 1;
        int targetMedian = low + groupCount / 2;
        return selectRange(values, low, mediansHigh, targetMedian, depth + 1);
    }

    private void insertionSort(int[] values, int low, int high) {
        for (int i = low + 1; i < high; i++) {
            int value = values[i];
            int position = i;
            while (position > low) {
                metrics.recordComparison();
                if (values[position - 1] <= value) {
                    break;
                }
                values[position] = values[position - 1];
                position--;
            }
            values[position] = value;
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
