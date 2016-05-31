package htsjdk.samtools.cram.structure;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by vadim on 26/04/2016.
 */
public class AlignmentSpanMapBuilder  {
    private Map<Integer, AlignmentSpan> map = new HashMap<>();


    public void addSpan(final int seqId, final AlignmentSpan span) {
        if (map.containsKey(seqId)) {
            map.get(seqId).add(span.getStart(), span.getSpan(), span.getCount(), span.getBases());
        } else {
            map.put(seqId, span);
        }
    }

    public void addSpan(final int seqId, final int start, final int span, final int count, final long bases) {
        if (map.containsKey(seqId)) {
            map.get(seqId).add(start, span, count, bases);
        } else {
            map.put(seqId, new AlignmentSpan(start, span, count, bases));
        }
    }

    public void addAllSpans(final Map<Integer, AlignmentSpan> addition) {
        for (final Map.Entry<Integer, AlignmentSpan> entry : addition.entrySet()) {
            addSpan(entry.getKey(), entry.getValue().getStart(), entry.getValue().getCount(), entry.getValue().getSpan(), entry.getValue().getBases());
        }
    }

    public Map<Integer, AlignmentSpan> getMap() {
        return map;
    }
}
