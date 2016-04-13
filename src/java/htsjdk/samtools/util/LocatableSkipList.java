package htsjdk.samtools.util;

import java.io.Serializable;
import java.util.*;

/**
 * Holds many intervals in memory, with an efficient operation to get
 * intervals that overlap a given query interval.
 *
 * This class allows intervals to lie on different contigs.
 */
public final class LocatableSkipList<T extends Locatable> implements Serializable {
    private static final long serialVersionUID = 1L;

    private final Map<String, LocatableSkipListOneContig<T>> intervals;

    /**
     * Creates an LocatableSkipList that holds a copy of the given intervals, sorted
     * and indexed.
     *
     * @param loc Locatables, not necessarily sorted. Will be iterated over exactly once.
     */
    public LocatableSkipList(final Iterable<T> loc) {
        if (loc == null) {
            throw new IllegalArgumentException("null loc");
        }
        final Map<String, List<T>> variantsPerContig = new HashMap<>();
        for (final T v : loc) {
            final String k = v.getContig();
            variantsPerContig.putIfAbsent(k, new ArrayList<>());
            variantsPerContig.get(k).add(v);
        }
        intervals = new HashMap<>();
        for (final String k : variantsPerContig.keySet()) {
            intervals.put(k, new LocatableSkipListOneContig<>(variantsPerContig.get(k)));
        }
    }

    /**
     * Returns all the intervals that overlap with the query.
     * The query doesn't *have* to be in the same contig as any interval we
     * hold, but of course if it isn't you'll get an empty result.
     * The returned list may not be modifiable - client code should not
     * depend on being able to modify the result.
     */
    public List<T> getOverlapping(final Locatable query) {
        if (query == null) {
            throw new IllegalArgumentException("null query");
        }
        final String k = query.getContig();
        final LocatableSkipListOneContig<T> result = intervals.get(k);
        if (result == null){
            return Collections.emptyList();
        }
        return result.getOverlapping(query);
    }

}
