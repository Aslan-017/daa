# Divide-and-Conquer Algorithms

## Project Overview

This project implements and measures four algorithms for Assignment 1: MergeSort, randomized QuickSort, deterministic selection using Median-of-Medians, and the divide-and-conquer closest pair of points. Implementations are in `src/main/java/daa`; correctness tests are in the assignment's top-level `tests/` directory.

The application writes trial-level measurements to `results/results.csv`, generates PNG plots in `docs/plots/`, and saves readable output and test-result images in `docs/screenshots/`. It uses Java 17 and has no runtime dependencies.

## Algorithm Analysis

### MergeSort

MergeSort recursively splits the input until a range contains at most 16 values, insertion-sorts that small range, and merges sorted halves with one reusable auxiliary array allocated for the whole sort. The merge is linear in the combined range size.

- **Time:** Θ(n log n) in the best, average, and worst cases.
- **Space:** Θ(n) auxiliary storage and Θ(log n) recursion stack.
- **Recurrence:** `T(n) = 2T(n/2) + Θ(n)` above the fixed cutoff. By the Master Theorem, `T(n) = Θ(n log n)`.

### Randomized QuickSort

QuickSort chooses a pivot uniformly at random from the active range and uses an in-place three-way partition for values less than, equal to, and greater than the pivot. It recursively sorts the smaller nontrivial partition and iterates over the larger one, limiting stack growth.

- **Time:** Expected Θ(n log n); worst-case Θ(n²).
- **Space:** O(log n) stack frames due to smaller-side recursion; partitioning itself uses O(1) extra space.
- **Recurrence:** For a balanced split, `T(n) = 2T(n/2) + Θ(n) = Θ(n log n)`. In the worst case, `T(n) = T(n - 1) + Θ(n) = Θ(n²)`. Random pivots give the balanced behavior in expectation; smaller-first recursion controls stack depth but does not remove the quadratic time worst case.

### Deterministic Select (Median-of-Medians)

Selection sorts groups of at most five in place, moves their medians into a prefix, recursively selects the median of those medians, and partitions the active range in place around that pivot. It continues only in the partition containing the requested zero-based rank.

- **Time:** Θ(n) worst case.
- **Space:** O(log n) recursion stack; partitioning and group processing use O(1) additional storage.
- **Recurrence:** Groups of five ensure the pivot discards at least about three tenths of the elements, up to constant-size rounding. Thus `T(n) ≤ T(ceil(n/5)) + T(7n/10 + O(1)) + Θ(n)`. The recursive fractions sum to less than one, so the recurrence is Θ(n), consistent with Akra–Bazzi intuition.

### Closest Pair of Points

The solver orders points by x-coordinate with a bottom-up merge sort, makes a y-ordered copy, and recursively solves the left and right halves. At each split it partitions the y-ordered points, merges the halves back in y-order, then checks the narrow vertical strip in y-order, stopping comparisons once the y-distance cannot improve the best pair. Ranges of at most three points are checked directly. Fewer than two input points produce no pair and an infinite distance.

- **Time:** Θ(n log n), including the initial x/y ordering.
- **Space:** Θ(n) auxiliary arrays and O(log n) recursion stack.
- **Recurrence:** `T(n) = 2T(n/2) + Θ(n)` for the recursive work and strip scan. The Master Theorem gives Θ(n log n); the initial comparison sorts have the same asymptotic bound.

## Experimental Results

Run `daa.Main` from the repository root to regenerate the CSV, plots, and program-output screenshots. Each algorithm/input-type/size combination has three trials. Sizes are 128, 512, 2,048, 8,192, and 32,768. Integer-array workloads are random, sorted, reverse-sorted, and duplicate-heavy; the point workloads use corresponding x-orderings or a small duplicate-heavy coordinate grid. Array/point generation occurs outside the timed interval; algorithm work is timed with `System.nanoTime()`. The CSV records execution time, maximum recursive depth, comparisons, swaps, and recursive calls.

### Tables

The following random-input values are medians of three trials on the recorded Java 17 run. Times are milliseconds; depths are maximum active recursion depth.

| n | MergeSort ms / depth | Randomized QuickSort ms / depth | Deterministic Select ms / depth | Closest Pair ms / depth |
|---:|---:|---:|---:|---:|
| 128 | 0.119 / 4 | 0.207 / 4 | 0.173 / 6 | 1.548 / 7 |
| 512 | 0.154 / 6 | 0.171 / 6 | 0.138 / 8 | 2.622 / 9 |
| 2,048 | 0.585 / 8 | 0.915 / 7 | 0.358 / 10 | 7.079 / 11 |
| 8,192 | 1.330 / 10 | 2.267 / 8 | 0.878 / 12 | 16.277 / 13 |
| 32,768 | 5.307 / 12 | 5.574 / 9 | 2.554 / 14 | 68.764 / 15 |

Median execution time in milliseconds at n = 32,768 across the four input types:

| Input type | MergeSort | Randomized QuickSort | Deterministic Select | Closest Pair |
|---|---:|---:|---:|---:|
| Random | 5.31 | 5.57 | 2.55 | 68.76 |
| Sorted | 1.58 | 3.37 | 0.96 | 50.70 |
| Reverse-sorted | 1.21 | 3.40 | 1.30 | 43.92 |
| Duplicate-heavy | 2.94 | 0.87 | 0.94 | 39.97 |

These measurements are machine- and JVM-dependent, and the short runs have no explicit warm-up phase; small differences should not be treated as stable rankings.

### Plot References

The plots show median results for random input:

- [Execution time vs. n](docs/plots/execution-time-vs-n.png)
- [Maximum recursion depth vs. n](docs/plots/recursion-depth-vs-n.png)

## Discussion

- **Do the results match theoretical complexity?** Broadly, yes: sorting and closest-pair times increase subquadratically across the measured sizes, and selection remains linear-work in theory. Timing noise, allocation, and the short trial duration mean the table is illustrative rather than a fitted complexity proof.
- **How does input structure affect performance?** MergeSort remains Θ(n log n), but the number of comparisons and insertion-sort work vary with order. Randomized QuickSort avoids a fixed input-dependent pivot and its three-way partition handles duplicate-heavy arrays efficiently. Selection's pivot guarantee applies regardless of input order. Closest Pair must sort points first, so already ordered x-coordinates can reduce presort work; coordinate distribution also affects strip candidates.
- **Why does smaller-first recursion help QuickSort?** At every partition, the recursively processed side has at most half the elements. Its stack therefore remains O(log n), even if repeatedly iterating over the other side takes quadratic time.
- **Why does Median-of-Medians guarantee O(n)?** A pivot chosen from medians of groups of five ensures a constant fraction of elements lies on each side (apart from rounding). The recurrence spends linear work per level while its two recursive subproblems together are strictly smaller than n by a constant fraction.
- **Why is divide-and-conquer Closest Pair faster than O(n²) for large inputs?** Sorting and splitting cost Θ(n log n). The geometric packing argument bounds the useful y-ordered strip comparisons per point by a constant, making each combine step linear rather than comparing every possible pair.
- **What practical factors affect performance?** JVM warm-up and JIT compilation, garbage collection, object allocation, cache locality, CPU scheduling, random seeds, and timer resolution all affect measured times. These results use three trials without a dedicated warm-up, so the CSV is useful for comparison and reproducibility, not as a benchmark of absolute machine performance.

## Testing

Run `mvn test`. The sorting tests compare both implementations with `Arrays.sort()` for random, sorted, reverse-sorted, duplicate-heavy, empty, and single-element inputs. Deterministic Select is checked on 250 randomized arrays/ranks against the sorted reference and for preservation of the input multiset. Closest Pair is checked against an O(n²) brute-force reference on randomized datasets, duplicate points, and a 2,000-point dataset.

## Reflection

Implementing all four algorithms made the distinction between theoretical guarantees and the details that preserve them especially clear. Reusing MergeSort's buffer avoids repeated allocations, smaller-first recursion bounds QuickSort's stack without changing its worst-case running time, and grouping by five is what gives deterministic selection its guaranteed linear bound.

The closest-pair implementation required the most careful coordination: points are initially sorted in both coordinate orders, the y-order must be partitioned with each recursive x-range, and the strip scan must retain that y-order to stay linear per level. Comparing it with brute force on small datasets and tracking comparisons alongside time helped validate both correctness and the intended divide-and-conquer structure.

## Screenshots

- Program output: [program-output.png](docs/screenshots/program-output.png)
- JUnit test results: [test-results.png](docs/screenshots/test-results.png)
- Generated plots/results: [plots-results.png](docs/screenshots/plots-results.png)
