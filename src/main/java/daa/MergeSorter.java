package daa;

import java.util.Objects;

public final class MergeSorter {
    private static final int INSERTION_SORT_CUTOFF = 16;

    private final AlgorithmMetrics metrics = new AlgorithmMetrics();
    private int[] auxiliary;

    public void sort(int[] values) {
        Objects.requireNonNull(values, "values");
        metrics.reset();
        auxiliary = new int[values.length];
        sortRange(values, 0, values.length, 1);
    }

    public AlgorithmMetrics metrics() {
        return metrics;
    }

    private void sortRange(int[] values, int low, int high, int depth) {
        metrics.recordRecursiveCall(depth);
        if (high - low <= INSERTION_SORT_CUTOFF) {
            insertionSort(values, low, high);
            return;
        }

        int middle = low + (high - low) / 2;
        sortRange(values, low, middle, depth + 1);
        sortRange(values, middle, high, depth + 1);
        merge(values, low, middle, high);
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

    private void merge(int[] values, int low, int middle, int high) {
        int left = low;
        int right = middle;
        int output = low;

        while (left < middle && right < high) {
            metrics.recordComparison();
            if (values[left] <= values[right]) {
                auxiliary[output++] = values[left++];
            } else {
                auxiliary[output++] = values[right++];
            }
        }
        while (left < middle) {
            auxiliary[output++] = values[left++];
        }
        while (right < high) {
            auxiliary[output++] = values[right++];
        }
        System.arraycopy(auxiliary, low, values, low, high - low);
    }
}
