package daa;

import java.util.List;
import java.util.Objects;

public final class ClosestPairSolver {
    private static final Result NO_PAIR = new Result(null, null, Double.POSITIVE_INFINITY);

    private final AlgorithmMetrics metrics = new AlgorithmMetrics();
    private Entry[] auxiliary;

    public Result solve(List<Point> points) {
        Objects.requireNonNull(points, "points");
        metrics.reset();
        for (Point point : points) {
            Objects.requireNonNull(point, "points contains null");
        }
        if (points.size() < 2) {
            return NO_PAIR;
        }

        Entry[] byX = new Entry[points.size()];
        for (int i = 0; i < points.size(); i++) {
            byX[i] = new Entry(points.get(i), i, 0);
        }
        auxiliary = new Entry[byX.length];
        sort(byX, ComparatorOrder.X);

        Entry[] byY = byX.clone();
        sort(byY, ComparatorOrder.Y);
        Closest closest = findClosest(byX, byY, 0, byX.length, 1);
        return new Result(closest.first.point, closest.second.point, closest.distance);
    }

    public AlgorithmMetrics metrics() {
        return metrics;
    }

    private Closest findClosest(Entry[] byX, Entry[] byY, int low, int high, int depth) {
        metrics.recordRecursiveCall(depth);
        int length = high - low;
        if (length <= 3) {
            Closest best = new Closest(null, null, Double.POSITIVE_INFINITY);
            for (int i = low; i < high; i++) {
                for (int j = i + 1; j < high; j++) {
                    best = closer(best, byX[i], byX[j]);
                }
            }
            return best;
        }

        int middle = low + length / 2;
        int leftOutput = low;
        int rightOutput = middle;
        for (int i = low; i < high; i++) {
            Entry entry = byY[i];
            if (entry.rank < middle) {
                auxiliary[leftOutput++] = entry;
            } else {
                auxiliary[rightOutput++] = entry;
            }
        }
        System.arraycopy(auxiliary, low, byY, low, high - low);

        Closest left = findClosest(byX, byY, low, middle, depth + 1);
        Closest right = findClosest(byX, byY, middle, high, depth + 1);
        Closest best = left.distance <= right.distance ? left : right;
        double splitX = byX[middle].point.x();

        int leftIndex = low;
        int rightIndex = middle;
        int output = low;
        while (leftIndex < middle && rightIndex < high) {
            metrics.recordComparison();
            if (compareByY(byY[leftIndex], byY[rightIndex]) <= 0) {
                auxiliary[output++] = byY[leftIndex++];
            } else {
                auxiliary[output++] = byY[rightIndex++];
            }
        }
        while (leftIndex < middle) {
            auxiliary[output++] = byY[leftIndex++];
        }
        while (rightIndex < high) {
            auxiliary[output++] = byY[rightIndex++];
        }
        System.arraycopy(auxiliary, low, byY, low, high - low);

        int stripSize = 0;
        for (int i = low; i < high; i++) {
            double dx = byY[i].point.x() - splitX;
            metrics.recordComparison();
            if (Math.abs(dx) < best.distance) {
                auxiliary[stripSize++] = byY[i];
            }
        }
        for (int i = 0; i < stripSize; i++) {
            for (int j = i + 1; j < stripSize && j <= i + 7; j++) {
                double dy = auxiliary[j].point.y() - auxiliary[i].point.y();
                metrics.recordComparison();
                if (Math.abs(dy) >= best.distance) {
                    break;
                }
                best = closer(best, auxiliary[i], auxiliary[j]);
            }
        }
        return best;
    }

    private Closest closer(Closest current, Entry first, Entry second) {
        metrics.recordComparison();
        double distance = distance(first.point, second.point);
        return current.first == null || distance < current.distance
                ? new Closest(first, second, distance)
                : current;
    }

    private double distance(Point first, Point second) {
        double dx = first.x() - second.x();
        double dy = first.y() - second.y();
        return Math.hypot(dx, dy);
    }

    private void sort(Entry[] entries, ComparatorOrder order) {
        int length = entries.length;
        for (int width = 1; width < length; width *= 2) {
            for (int low = 0; low < length; low += width * 2) {
                int middle = Math.min(low + width, length);
                int high = Math.min(low + width * 2, length);
                int left = low;
                int right = middle;
                int output = low;
                while (left < middle && right < high) {
                    metrics.recordComparison();
                    if (compare(entries[left], entries[right], order) <= 0) {
                        auxiliary[output++] = entries[left++];
                    } else {
                        auxiliary[output++] = entries[right++];
                    }
                }
                while (left < middle) {
                    auxiliary[output++] = entries[left++];
                }
                while (right < high) {
                    auxiliary[output++] = entries[right++];
                }
                System.arraycopy(auxiliary, low, entries, low, high - low);
            }
        }
        if (order == ComparatorOrder.X) {
            for (int i = 0; i < length; i++) {
                entries[i].rank = i;
            }
        }
    }

    private int compare(Entry first, Entry second, ComparatorOrder order) {
        return order == ComparatorOrder.X ? compareByX(first, second) : compareByY(first, second);
    }

    private int compareByX(Entry first, Entry second) {
        int comparison = Double.compare(first.point.x(), second.point.x());
        if (comparison == 0) {
            comparison = Double.compare(first.point.y(), second.point.y());
        }
        return comparison != 0 ? comparison : Integer.compare(first.index, second.index);
    }

    private int compareByY(Entry first, Entry second) {
        int comparison = Double.compare(first.point.y(), second.point.y());
        if (comparison == 0) {
            comparison = Double.compare(first.point.x(), second.point.x());
        }
        return comparison != 0 ? comparison : Integer.compare(first.index, second.index);
    }

    private enum ComparatorOrder {
        X,
        Y
    }

    private static final class Entry {
        private final Point point;
        private final int index;
        private int rank;

        private Entry(Point point, int index, int rank) {
            this.point = point;
            this.index = index;
            this.rank = rank;
        }
    }

    private record Closest(Entry first, Entry second, double distance) {
    }

    public record Result(Point first, Point second, double distance) {
        public boolean hasPair() {
            return first != null;
        }
    }
}
