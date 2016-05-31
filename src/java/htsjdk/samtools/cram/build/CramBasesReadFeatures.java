package htsjdk.samtools.cram.build;

import htsjdk.samtools.SAMRecord;
import htsjdk.samtools.cram.encoding.readfeatures.Deletion;
import htsjdk.samtools.cram.encoding.readfeatures.InsertBase;
import htsjdk.samtools.cram.encoding.readfeatures.Insertion;
import htsjdk.samtools.cram.encoding.readfeatures.ReadBase;
import htsjdk.samtools.cram.encoding.readfeatures.ReadFeature;
import htsjdk.samtools.cram.encoding.readfeatures.RefSkip;
import htsjdk.samtools.cram.encoding.readfeatures.SoftClip;
import htsjdk.samtools.cram.encoding.readfeatures.Substitution;
import htsjdk.samtools.cram.structure.CramCompressionRecord;
import htsjdk.samtools.cram.structure.SubstitutionMatrix;

import java.util.Arrays;
import java.util.Collection;

/**
 * Created by vadim on 27/04/2016.
 */
final class CramBasesReadFeatures {
    final private SubstitutionMatrix substitutionMatrix;
    final private byte[] ref;
    /**
     * A zero-based offset of the reference array: alignment start of the first base in the reference array would be
     * refOffsetZeroBased+1
     * alignment start X translates into ref array coordinates like this: X-refOffsetZeroBased-1
     */
    final private int refOffsetZeroBased;

    CramBasesReadFeatures(SubstitutionMatrix substitutionMatrix, byte[] ref, int refOffsetZeroBased) {
        this.substitutionMatrix = substitutionMatrix;
        this.ref = ref;
        this.refOffsetZeroBased = refOffsetZeroBased;
    }

    private final int refArrayIndex(int alignmentStart_1based) {
        return alignmentStart_1based - refOffsetZeroBased - 1;
    }

    final boolean isAlignmentPositionInsideOfReferenceArray(int pos) {
        final int refArrayIndex_Right = refArrayIndex(pos);
        return refArrayIndex_Right >= 0 && refArrayIndex_Right < ref.length;
    }

    final boolean isCompletelyInsideOfReferenceArray(int start, int len) {
        return isAlignmentPositionInsideOfReferenceArray(start) && isAlignmentPositionInsideOfReferenceArray(start + len - 1);
    }

    final void copyRefBases(byte[] bases, int readAlignmentStart, int readLength) {
        if (isCompletelyInsideOfReferenceArray(readAlignmentStart, readLength)) {
            System.arraycopy(ref, refArrayIndex(readAlignmentStart), bases, 0, readLength);
        } else {
            Arrays.fill(bases, (byte) 'N');
            if (isAlignmentPositionInsideOfReferenceArray(readAlignmentStart)) {
                final int indexOfStartInRefArray = refArrayIndex(readAlignmentStart);
                System.arraycopy(ref, indexOfStartInRefArray, bases, 0, Math.min(readLength, ref.length - indexOfStartInRefArray));
            }
        }
    }

    final void restoreReadBases(byte[] bases, final Collection<ReadFeature> readFeatures, int readAlignmentStart) {
        restoreReadBases(bases, readAlignmentStart, bases.length, readFeatures);
    }

    final void restoreReadBases(byte[] bases, int readAlignmentStart, int readLength, final Collection<ReadFeature> readFeatures) {
        AlignmentPosition alignmentPosition = new AlignmentPosition(0, readAlignmentStart - 1 - refOffsetZeroBased);

        if (readFeatures == null || readFeatures.isEmpty()) {
            copyRefBases(bases, readAlignmentStart, readLength);
            return;
        }

        for (final ReadFeature variation : readFeatures) {
            copyRefBasesBetweenFeatures(bases, alignmentPosition, variation);
            applyReadFeatureAndIncrementPosition(bases, alignmentPosition, variation);
        }

        copyTailingBases(bases, readLength, alignmentPosition);
        applyReadBaseFeatures(bases, readFeatures);
        normalizeBases(bases, readLength);
    }

    private void applyReadFeatureAndIncrementPosition(byte[] bases, AlignmentPosition pos, ReadFeature variation) {
        switch (variation.getOperator()) {
            case Substitution.operator:
                applySubstitutionAndIncrementPosition(bases, pos, (Substitution) variation);
                break;
            case Insertion.operator:
                applyInsertionAndIncrementPosition(bases, pos, (Insertion) variation);
                break;
            case SoftClip.operator:
                applySoftClipAndIncrementPosition(bases, pos, (SoftClip) variation);
                break;
            case Deletion.operator:
                pos.referenceArrayIndex += ((Deletion) variation).getLength();
                break;
            case InsertBase.operator:
                applyInsertBaseAndIncrementPosition(bases, pos, (InsertBase) variation);
                break;
            case RefSkip.operator:
                pos.referenceArrayIndex += ((RefSkip) variation).getLength();
                break;
        }
    }

    private void copyRefBasesBetweenFeatures(byte[] bases, AlignmentPosition pos, ReadFeature variation) {
        for (; pos.readBasesArrayIndex < variation.getPosition() - 1; pos.readBasesArrayIndex++) {
            bases[pos.readBasesArrayIndex] = getByteOrDefault(ref, pos.referenceArrayIndex++, (byte) 'N');
        }
    }

    private void copyTailingBases(byte[] bases, int readLength, AlignmentPosition pos) {
        for (; pos.readBasesArrayIndex <= readLength - 1
                && pos.referenceArrayIndex < ref.length; pos.readBasesArrayIndex++, pos.referenceArrayIndex++) {
            bases[pos.readBasesArrayIndex] = ref[pos.referenceArrayIndex];
        }
    }

    private void applyInsertBaseAndIncrementPosition(byte[] bases, AlignmentPosition pos, InsertBase insert) {
        bases[pos.readBasesArrayIndex++] = insert.getBase();
    }

    private void applySoftClipAndIncrementPosition(byte[] bases, AlignmentPosition pos, SoftClip softClip) {
        for (int i = 0; i < softClip.getSequence().length; i++) {
            bases[pos.readBasesArrayIndex++] = softClip.getSequence()[i];
        }
    }

    private void applyInsertionAndIncrementPosition(byte[] bases, AlignmentPosition pos, Insertion insertion) {
        for (int i = 0; i < insertion.getSequence().length; i++) {
            bases[pos.readBasesArrayIndex++] = insertion.getSequence()[i];
        }
    }

    private final void applySubstitutionAndIncrementPosition(byte[] bases, AlignmentPosition pos, Substitution variation) {
        final Substitution substitution = variation;
        byte refBase = getByteOrDefault(ref, pos.referenceArrayIndex, (byte) 'N');
        refBase = normalizeBase(refBase);
        final byte base = substitutionMatrix.base(refBase, substitution.getCode());
        substitution.setBase(base);
        substitution.setReferenceBase(refBase);
        bases[pos.readBasesArrayIndex] = base;

        pos.readBasesArrayIndex++;
        pos.referenceArrayIndex++;
    }

    static final void normalizeBases(byte[] bases, int len) {
        for (int i = 0; i < len; i++) {
            bases[i] = normalizeBase(bases[i]);
        }
    }

    static final void applyReadBaseFeatures(byte[] bases, Collection<ReadFeature> readFeatures) {
        // ReadBase overwrites bases:
        for (final ReadFeature variation : readFeatures) {
            switch (variation.getOperator()) {
                case ReadBase.operator:
                    final ReadBase readBase = (ReadBase) variation;
                    bases[variation.getPosition() - 1] = readBase.getBase();
                    break;
                default:
                    break;
            }
        }
    }

    /**
     * Restore read bases for a {@link CramCompressionRecord} using it's read features. Allocates a new byte array for
     * the record read bases is necessary.
     *
     * @param record restore read bases in this record
     */
    final void restoreReadBases(final CramCompressionRecord record) {
        if (record.isUnknownBases() || record.readLength == 0) {
            record.readBases = SAMRecord.NULL_SEQUENCE;
        }
        final int readLength = record.readLength;
        if (record.readBases == null) {
            record.readBases = new byte[readLength];
        }

        restoreReadBases(record.readBases, record.alignmentStart, record.readLength, record.readFeatures);
    }

    private static final byte normalizeBase(final byte base) {
        switch (base) {
            case 'a':
            case 'A':
                return 'A';

            case 'c':
            case 'C':
                return 'C';

            case 'g':
            case 'G':
                return 'G';

            case 't':
            case 'T':
                return 'T';

            default:
                return 'N';
        }
    }

    private static final byte getByteOrDefault(final byte[] array, final int pos,
                                               final byte outOfBoundsValue) {
        if (pos >= array.length) {
            return outOfBoundsValue;
        } else {
            return array[pos];
        }
    }

    /**
     * A zero-based index in the reference array and a corresponding zero-based index in the read bases array.
     */
    static class AlignmentPosition {
        int readBasesArrayIndex = 0;
        int referenceArrayIndex = 0;

        public AlignmentPosition(int readBasesArrayIndex, int referenceArrayIndex) {
            this.readBasesArrayIndex = readBasesArrayIndex;
            this.referenceArrayIndex = referenceArrayIndex;
        }
    }
}
