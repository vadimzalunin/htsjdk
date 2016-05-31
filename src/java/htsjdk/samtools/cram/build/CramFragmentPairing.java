package htsjdk.samtools.cram.build;

import htsjdk.samtools.SAMRecord;
import htsjdk.samtools.cram.structure.CramCompressionRecord;

import java.util.List;

/**
 * Created by vadim on 28/04/2016.
 */
class CramFragmentPairing {
    private long globalReadCounter = 0;
    private String readNamePrefix = "";

    CramFragmentPairing(long globalReadCounter, String readNamePrefix) {
        this.globalReadCounter = globalReadCounter;
        this.readNamePrefix = readNamePrefix;
    }

    protected String buildReadName(final int index) {
        return readNamePrefix + String.valueOf(globalReadCounter + index);
    }

    final void restoreTemplates(List<CramCompressionRecord> records, final int globalReadCounter) {
        this.globalReadCounter = globalReadCounter;
        int recordIndexInContext = 0;
        for (final CramCompressionRecord record : records) {
            restoreTemplatePointers(record, recordIndexInContext, records);
            recordIndexInContext++;
        }

        recordIndexInContext = 0;
        for (final CramCompressionRecord record : records) {
            restoreMateInfoUsingPointers(record, recordIndexInContext);
            recordIndexInContext++;
        }

        this.globalReadCounter += records.size();
    }

    final void restoreTemplatePointers(CramCompressionRecord record, int recordIndexInContext, List<CramCompressionRecord> context) {
        if (!record.isMultiFragment() || record.isDetached()) {
            resetFragmentPointers(record);
        } else if (record.isHasMateDownStream()) {
            connectWithDownstreamFragment(record, recordIndexInContext, context);
        }
        // record not matching the above conditions has been already processed
    }

    final void connectWithDownstreamFragment(CramCompressionRecord record, int recordIndexInContext, List<CramCompressionRecord> context) {
        final int nextFragmentIndexInContext = recordIndexInContext + record.recordsToNextFragment;
        final CramCompressionRecord downMate = context.get(nextFragmentIndexInContext);

        record.next = downMate;
        downMate.previous = record;
    }

    final void resetFragmentPointers(CramCompressionRecord record) {
        record.recordsToNextFragment = -1;

        record.next = null;
        record.previous = null;
    }

    /**
     * Restore SAM-level mate information based on CRAM record next/previous pointers.
     * This method traverses next pointers and sets mate info and template size.
     * Note: since this is done in forward direction until the end of template (or detached record) there is no need
     * to process records with set previous pointer - they have already been processed.
     *
     * @param firstRecordInTemplate
     */
    final void restoreMateInfoUsingPointers(final CramCompressionRecord firstRecordInTemplate, final int readIndexInContext) {
        if (firstRecordInTemplate.next == null || firstRecordInTemplate.previous != null) {
            return;
        }

        if (firstRecordInTemplate.readName == null) {
            firstRecordInTemplate.readName = buildReadName(readIndexInContext);
        }
        CramCompressionRecord currentRecord = firstRecordInTemplate;
        while (currentRecord.next != null) {
            connectFragmentToNextFragmentInTemplate(currentRecord, currentRecord.next);
            currentRecord = currentRecord.next;
        }

        // cur points to the last segment now:
        final CramCompressionRecord lastRecordInTemplate = currentRecord;
        // connect the last to point to the first:
        connectFragmentToNextFragmentInTemplate(lastRecordInTemplate, firstRecordInTemplate);

        final int templateLength = computeInsertSize(firstRecordInTemplate, lastRecordInTemplate);
        firstRecordInTemplate.templateSize = templateLength;
        lastRecordInTemplate.templateSize = -templateLength;
    }

    /**
     * All "mate" fields on the fragment will reflect the state of the next fragment.
     * Read name propagates from the fragment onto the next fragment.
     *
     * @param fragment     the record where the "mate" fields will be updated
     * @param nextFragment the next record (mate) where the read name will be updated
     */
    final static void connectFragmentToNextFragmentInTemplate(final CramCompressionRecord fragment, final CramCompressionRecord nextFragment) {
        fragment.mateAlignmentStart = nextFragment.alignmentStart;
        fragment.setMateUnmapped(nextFragment.isSegmentUnmapped());
        fragment.setMateNegativeStrand(nextFragment.isNegativeStrand());
        fragment.mateSequenceID = nextFragment.sequenceId;
        if (fragment.mateSequenceID == SAMRecord.NO_ALIGNMENT_REFERENCE_INDEX) {
            fragment.mateAlignmentStart = SAMRecord.NO_ALIGNMENT_START;
        }

        nextFragment.readName = fragment.readName;
    }

    /**
     * The method is similar in semantics to
     * {@link htsjdk.samtools.SamPairUtil#computeInsertSize(SAMRecord, SAMRecord)
     * computeInsertSize} but operates on CRAM native records instead of
     * SAMRecord objects.
     *
     * @param firstEnd  first mate of the pair
     * @param secondEnd second mate of the pair
     * @return template length
     */
    final static int computeInsertSize(final CramCompressionRecord firstEnd,
                                       final CramCompressionRecord secondEnd) {
        if (firstEnd.isSegmentUnmapped() || secondEnd.isSegmentUnmapped()) {
            return 0;
        }
        if (firstEnd.sequenceId != secondEnd.sequenceId) {
            return 0;
        }

        final int firstEnd5PrimePosition = firstEnd.isNegativeStrand() ? firstEnd.getAlignmentEnd() : firstEnd.alignmentStart;
        final int secondEnd5PrimePosition = secondEnd.isNegativeStrand() ? secondEnd.getAlignmentEnd() : secondEnd.alignmentStart;

        final int adjustment = (secondEnd5PrimePosition >= firstEnd5PrimePosition) ? +1 : -1;
        return secondEnd5PrimePosition - firstEnd5PrimePosition + adjustment;
    }
}
