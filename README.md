# Divide-and-Conquer Algorithms

## Project Overview

This project implements and analyzes four divide-and-conquer algorithms for Assignment 1: MergeSort, randomized QuickSort, deterministic selection using Median-of-Medians, and the divide-and-conquer closest pair of points.

The purpose of the assignment is to implement classic divide-and-conquer algorithms, analyze their theoretical complexity, measure their practical performance, and compare experimental results with theoretical expectations.

Implementations are located in `src/main/java/daa`, while correctness tests are located in the top-level `tests/` directory.

The application writes trial-level measurements to `results/results.csv`, generates PNG plots in `docs/plots/`, and saves readable program output and test-result screenshots in `docs/screenshots/`. The project uses Java 17 and has no runtime dependencies.

## Algorithm Analysis

### MergeSort

MergeSort recursively splits the input until a range contains at most 16 values, insertion-sorts that small range, and merges sorted halves using one reusable auxiliary array allocated for the whole sort. The merge operation is linear in the combined range size.

- **Time:** Θ(n log n) in the best, average, and worst cases.
- **Space:** Θ(n) auxiliary storage and Θ(log n) recursion stack.
- **Recurrence:** `T(n) = 2T(n/2) + Θ(n)` above the fixed cutoff. By the Master Theorem, `T(n) = Θ(n log n)`.

### Randomized QuickSort

QuickSort chooses a pivot uniformly at random from the active range and uses an in-place three-way partition for values less than, equal to, and greater than the pivot. It recursively sorts the smaller nontrivial partition and iterates over the larger one, limiting stack growth.

- **Time:** Expected Θ(n log n); worst-case Θ(n²).
- **Space:** O(log n) stack frames due to smaller-side recursion; partitioning itself uses O(1) extra space.
- **Recurrence:** For a balanced split, `T(n) = 2T(n/2) + Θ(n) = Θ(n log n)`. In the worst case, `T(n) = T(n - 1) + Θ(n) = Θ(n²)`. Random pivots provide balanced behavior in expectation, while smaller-first recursion controls stack depth but does not remove the quadratic time worst case.

### Deterministic Select (Median-of-Medians)

Deterministic Select processes groups of at most five elements, finds their medians in place, moves the medians into a prefix, recursively selects the median of those medians, and partitions the active range around that pivot. It then continues only in the partition containing the requested zero-based rank.

- **Time:** Θ(n) worst case.
- **Space:** O(log n) recursion stack; partitioning and group processing use O(1) additional storage.
- **Recurrence:** Groups of five provide a pivot that discards a constant fraction of the elements, up to constant-size rounding. Thus `T(n) ≤ T(ceil(n/5)) + T(7n/10 + O(1)) + Θ(n)`. The recursive fractions sum to less than one, giving Θ(n) worst-case complexity.

### Closest Pair of Points

The solver orders points by x-coordinate using a bottom-up merge sort, creates a y-ordered copy, and recursively solves the left and right halves. At each split, it partitions the y-ordered points, merges the halves back in y-order, and checks the narrow vertical strip in y-order. The strip check stops when the y-distance cannot improve the current best pair. Ranges of at most three points are checked directly. Fewer than two input points produce no pair and an infinite distance.

- **Time:** Θ(n log n), including the initial ordering.
- **Space:** Θ(n) auxiliary storage and O(log n) recursion stack.
- **Recurrence:** `T(n) = 2T(n/2) + Θ(n)` for the recursive work and strip processing. By the Master Theorem, `T(n) = Θ(n log n)`.

## Experimental Results

Run `daa.Main` from the repository root to regenerate the CSV, plots, and program-output screenshots.

Each algorithm/input-type/size combination has three trials. The tested input sizes are 128, 512, 2,048, 8,192, and 32,768.

Integer-array workloads are:

- Random
- Sorted
- Reverse-sorted
- Duplicate-heavy

The point workloads use corresponding x-orderings or a small duplicate-heavy coordinate grid.

Input generation is performed outside the timed interval. Algorithm execution is measured using `System.nanoTime()`.

The CSV records:

- Execution time
- Maximum recursion depth
- Comparisons
- Swaps
- Recursive calls

### Execution Time and Recursion Depth

The following values are medians of three trials on the recorded Java 17 run using random input. Times are in milliseconds and depths represent maximum active recursion depth.

| n | MergeSort ms / depth | Randomized QuickSort ms / depth | Deterministic Select ms / depth | Closest Pair ms / depth |
|---:|---:|---:|---:|---:|
| 128 | 0.119 / 4 | 0.207 / 4 | 0.173 / 6 | 1.548 / 7 |
| 512 | 0.154 / 6 | 0.171 / 6 | 0.138 / 8 | 2.622 / 9 |
| 2,048 | 0.585 / 8 | 0.915 / 7 | 0.358 / 10 | 7.079 / 11 |
| 8,192 | 1.330 / 10 | 2.267 / 8 | 0.878 / 12 | 16.277 / 13 |
| 32,768 | 5.307 / 12 | 5.574 / 9 | 2.554 / 14 | 68.764 / 15 |

### Results for Different Input Types

Median execution time in milliseconds at n = 32,768 across the four input types:

| Input type | MergeSort | Randomized QuickSort | Deterministic Select | Closest Pair |
|---|---:|---:|---:|---:|
| Random | 5.31 | 5.57 | 2.55 | 68.76 |
| Sorted | 1.58 | 3.37 | 0.96 | 50.70 |
| Reverse-sorted | 1.21 | 3.40 | 1.30 | 43.92 |
| Duplicate-heavy | 2.94 | 0.87 | 0.94 | 39.97 |

These measurements are machine- and JVM-dependent. The runs are short and use three trials without an explicit warm-up phase, so small timing differences should not be treated as stable performance rankings.

### Plot References

The plots show median results for random input:

- [Execution time vs. n](docs/plots/execution-time-vs-n.png)
- [Maximum recursion depth vs. n](docs/plots/recursion-depth-vs-n.png)

## Discussion

- **Do the results match theoretical complexity?** Broadly, yes. Sorting and closest-pair execution times increase subquadratically across the measured sizes, while deterministic selection has linear worst-case work in theory. Timing noise, allocation, and the short trial duration mean that the measurements are illustrative rather than a formal complexity proof.

- **How does input structure affect performance?** MergeSort remains Θ(n log n), but the amount of comparison and insertion-sort work can vary with input order. Randomized QuickSort avoids a fixed input-dependent pivot, and its three-way partition handles duplicate-heavy arrays efficiently. Deterministic Select maintains its pivot guarantee regardless of input order. Closest Pair performs an initial ordering of points, so input ordering and coordinate distribution can affect practical execution time.

- **Why does smaller-first recursion help QuickSort?** After each partition, the recursively processed side contains at most half of the active elements. Therefore, the recursion stack remains O(log n), even when the other partition is processed iteratively and the total running time can still become quadratic in the worst case.

- **Why does Median-of-Medians guarantee O(n)?** Dividing the elements into groups of five and taking their medians provides a pivot that eliminates a constant fraction of the elements from further consideration. The recurrence performs linear work at each level while the recursive subproblems together form a sufficiently smaller fraction of the original problem, resulting in Θ(n) worst-case complexity.

- **Why is divide-and-conquer Closest Pair faster than O(n²) for large inputs?** The algorithm sorts and divides the points in Θ(n log n) time. During the combine step, the geometric packing argument limits the number of relevant y-ordered strip comparisons for each point to a constant number. Therefore, the combine step is linear instead of comparing every possible pair.

- **What practical factors affect performance?** JVM warm-up and JIT compilation, garbage collection, object allocation, cache locality, CPU scheduling, random seeds, and timer resolution can all affect measured execution times. The experiments use three trials without a dedicated warm-up, so the CSV results are intended for comparison and reproducibility rather than absolute machine benchmarking.

## Testing

The project includes JUnit tests that verify the correctness of all required algorithm categories.

### Sorting

MergeSort and randomized QuickSort are compared against `Arrays.sort()` using:

- Random arrays
- Sorted arrays
- Reverse-sorted arrays
- Duplicate-heavy arrays
- Empty arrays
- Single-element arrays

### Deterministic Select

Deterministic Select is tested on 250 random cases with different array sizes and randomly selected ranks. Each result is compared against the corresponding value from a sorted reference array using `Arrays.sort()`.

The tests also verify that the input array preserves the same multiset of values after selection.

### Closest Pair

Closest Pair is compared against an O(n²) brute-force implementation for multiple small random datasets, including cases with duplicate coordinates.

The test suite also includes an explicit dataset containing 2,000 points, duplicate points, and large finite coordinates.

### Test Execution

Run the complete test suite with:

```bash
mvn test