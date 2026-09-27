package daa;

public final class AlgorithmMetrics {
    private long comparisons;
    private long swaps;
    private long recursiveCalls;
    private int maxRecursionDepth;

    void reset() {
        comparisons = 0;
        swaps = 0;
        recursiveCalls = 0;
        maxRecursionDepth = 0;
    }

    void recordComparison() {
        comparisons++;
    }

    void recordSwap() {
        swaps++;
    }

    void recordRecursiveCall(int depth) {
        recursiveCalls++;
        maxRecursionDepth = Math.max(maxRecursionDepth, depth);
    }

    public long comparisons() {
        return comparisons;
    }

    public long swaps() {
        return swaps;
    }

    public long recursiveCalls() {
        return recursiveCalls;
    }

    public int maxRecursionDepth() {
        return maxRecursionDepth;
    }
}
