package htsjdk.samtools.cram.structure;

import htsjdk.samtools.SAMRecord;
import htsjdk.samtools.ValidationStringency;
import htsjdk.samtools.cram.encoding.reader.DataReaderFactory;
import htsjdk.samtools.cram.encoding.reader.RefSeqIdReader;
import htsjdk.samtools.cram.io.DefaultBitInputStream;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by vadim on 26/04/2016.
 */
public class AlignmentSpanUtils {


    public static Map<Integer, AlignmentSpan> getReferences(final Container container, final ValidationStringency validationStringency) throws IOException {
        AlignmentSpanMapBuilder mapBuilder = new AlignmentSpanMapBuilder();

        for (final Slice slice : container.slices) {
            mapBuilder.addAllSpans(getReferences(slice, container.header, validationStringency));
        }
        return mapBuilder.getMap();
    }

    public static Map<Integer, AlignmentSpan> getReferences(final Slice slice, final CompressionHeader header, final ValidationStringency validationStringency) throws IOException {
        AlignmentSpanMapBuilder mapBuilder = new AlignmentSpanMapBuilder();
        switch (slice.sequenceId) {
            case SAMRecord.NO_ALIGNMENT_REFERENCE_INDEX:
                mapBuilder.addSpan(SAMRecord.NO_ALIGNMENT_REFERENCE_INDEX, SAMRecord.NO_ALIGNMENT_START, 0, slice.nofRecords, slice.bases);
                break;
            case Slice.MULTI_REFERENCE:
                final DataReaderFactory dataReaderFactory = new DataReaderFactory();
                final Map<Integer, InputStream> inputMap = new HashMap<Integer, InputStream>();
                for (final Integer exId : slice.external.keySet()) {
                    inputMap.put(exId, new ByteArrayInputStream(slice.external.get(exId)
                            .getRawContent()));
                }

                final RefSeqIdReader reader = new RefSeqIdReader(Slice.MULTI_REFERENCE, slice.alignmentStart, validationStringency);
                dataReaderFactory.buildReader(reader, new DefaultBitInputStream(
                                new ByteArrayInputStream(slice.coreBlock.getRawContent())),
                        inputMap, header, slice.sequenceId);

                for (int i = 0; i < slice.nofRecords; i++) {
                    reader.read();
                }
                mapBuilder.addAllSpans(reader.getReferenceSpans());
                break;
            default:
                mapBuilder.addSpan(slice.sequenceId, slice.alignmentStart, slice.alignmentSpan, slice.nofRecords, slice.bases);
                break;
        }
        return mapBuilder.getMap();
    }

    public static Map<Integer, AlignmentSpan> getReferencesForCramRecords(Collection<CramCompressionRecord> records) {
        AlignmentSpanMapBuilder mapBuilder = new AlignmentSpanMapBuilder();
        for (final CramCompressionRecord record : records) {
            if (record.alignmentStart > SAMRecord.NO_ALIGNMENT_START) {
                mapBuilder.addSpan(record.sequenceId, record.alignmentStart, record.getAlignmentEnd() - record.alignmentStart, 1, record.readLength);
            }
        }

        return mapBuilder.getMap();
    }


    public static Map<Integer, AlignmentSpan> getReferencesForSAMRecords(Collection<SAMRecord> records) {
        AlignmentSpanMapBuilder mapBuilder = new AlignmentSpanMapBuilder();
        for (final SAMRecord record : records) {
            if (record.getAlignmentStart() > SAMRecord.NO_ALIGNMENT_START) {
                mapBuilder.addSpan(record.getReferenceIndex(), record.getAlignmentStart(), record.getAlignmentEnd() - record.getAlignmentStart(), 1, record.getReadLength());
            }
        }

        return mapBuilder.getMap();
    }
}
