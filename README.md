## MercurySharedPreferences — In-Mem Caching Over SharedPreferences (WIP)

### Idea & Assumptions

MercurySharedPreferences was built with the vision of improving the performance of SharedPreferences by introducing a scoring-based in-mem cache. The scoring would happen whenever the key was accessed (put, get, update). Basically build a heatmap specific to that User and not generalise the behaviour.

The assumption was that tracking key access frequency and prefetching frequently accessed data would reduce read times and provide an optimized experience for applications that rely on frequent data access.

However, the challenge lies in the tradeoff: while the caching layer promises to be faster, even small modifications—like scoring—resulted in unexpected performance degradation. This is a work-in-progress (WIP) as the approach didn’t yield the desired performance improvements and needs further optimization.

### Benchmark Results

Refer to the [Benchmark.kt](https://github.com/achyuth13/mercury-cache/blob/main/mercury/src/main/java/com/caramel/mercury/Benchmark.kt) file for how it was benchmarked

#### Write Benchmark
- Compared SharedPreferences write operations against the custom cache.

Result: SharedPreferences outperformed the custom caching mechanism, especially when additional processing (e.g., scoring) was added.

```
SharedPreferences WRITE time: 354531 ns
Mercury SharedPreferences WRITE time: 8482604 ns
```
This was expected, given the scoring and then writing.

#### Read Benchmark
Compared the read time of SharedPreferences with MercurySharedPreferences.

Result: The custom cache, despite its advantages in theory, showed increased latency due to the added processing logic during key access.

```
SharedPreferences READ times per key (ns): {key1=[4739, 1563, 1615, 1302], key2=[1250, 1198, 1250, 1198], key3=[1250, 1563, 1458, 1406], key4=[1406, 1250, 1198, 1198]}

Mercury READ times per key (ns): {key1=[23365781, 574010, 386615, 797813], key2=[211406, 1027813, 208490, 197344], key3=[675677, 185521, 303333, 154063], key4=[135312, 135625, 125989, 309010]}

```

Ridiculously high!!!!


### What did I try to optimise?
- I tried doing the scoring in async - that proved to be costly because I had to intialise the scope.
- Tried using Flow, and channel, similar - had to initialise the channel, emit.
- Tried using a local hashmap and deferring scoring from an immediate to a periodic scoring - not as bad as the first two, but still exepensive.

### Status:
The project is still in its early stages. While the design is in place, the performance results were not as expected, and further optimization is required.


### Conclusion
When I first started this project, I was genuinely excited. I dived deep into design patterns and Kotlin, trying to understand the internals thoroughly. But, as it often goes, my assumptions were quickly challenged. The first time I ran the benchmark, I thought something was wrong (spoiler: it wasn’t!). It was a humbling experience, and it proved to me that even a single line of code can significantly impact performance. Initially, I considered abandoning the project, but I decided to reach out to the community instead. Maybe someone out there has found a better way to tackle these challenges.