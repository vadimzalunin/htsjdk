package htsjdk.samtools.cram.build;

import htsjdk.samtools.SAMRecord;
import htsjdk.samtools.cram.encoding.readfeatures.BaseQualityScore;
import htsjdk.samtools.cram.encoding.readfeatures.ReadBase;
import htsjdk.samtools.cram.encoding.readfeatures.ReadFeature;
import htsjdk.samtools.cram.structure.CramCompressionRecord;

import java.util.Arrays;
import java.util.Collection;

/**
 * Created by vadim on 28/04/2016.
 */
class CramQualityScoreReadFeatures {
    final private static byte MISSING_SCORE = -1;
    final private static byte DEFAULT_QUALITY_SCORE = '?';
    final private byte defaultQualityScore;

    CramQualityScoreReadFeatures(byte defaultQualityScore) {
        this.defaultQualityScore = defaultQualityScore;
    }

    CramQualityScoreReadFeatures() {
        this(DEFAULT_QUALITY_SCORE);
    }

    void restoreQualityScores(final CramCompressionRecord record) {
        if (!record.isForcePreserveQualityScores() && isStar_ForUnforcedQualityScores(record.readFeatures)) {
            record.qualityScores = SAMRecord.NULL_QUALS;
            return;
        }

        if (record.isForcePreserveQualityScores()) {
            if (record.qualityScores == null || record.qualityScores.length < record.readLength) {
                throw new IllegalArgumentException("Expecting a non-null quality scores array of size " + record.readLength);
            }
            // check if the scores are empty:
            if (areAllQualityScoresMissing(record.qualityScores, record.readLength)) {
                record.qualityScores = SAMRecord.NULL_QUALS;
                return;
            }
            // there is nothing to be done: the scores are already as they should be
        } else {
            // allocate if needed:
            if (record.qualityScores == null) {
                record.qualityScores = new byte[record.readLength];
            }
            if (record.qualityScores.length < record.readLength) {
                throw new IllegalArgumentException("Expecting a non-null quality scores array of size " + record.readLength);
            }

            // the main trouble: apply read features onto the quality score array:
            record.qualityScores = restoreUnforcedQualityScores(record.qualityScores, record.readLength, record.readFeatures);
        }
    }

    boolean areAllQualityScoresMissing(byte[] scores, int readLength) {
        int missingScoresCounter = 0;
        for (int i = 0; i < readLength; i++) {
            if (scores[i] == MISSING_SCORE) {
                scores[i] = defaultQualityScore;
                missingScoresCounter++;
            }
        }

        return missingScoresCounter == scores.length;
    }

    boolean isStar_ForUnforcedQualityScores(Collection<ReadFeature> readFeatures) {
        if (readFeatures != null || readFeatures.isEmpty()) {
            return true;
        }

        for (final ReadFeature feature : readFeatures) {
            switch (feature.getOperator()) {
                case BaseQualityScore.operator:
                    return false;
                case ReadBase.operator:
                    return false;
                default:
                    break;
            }
        }

        return true;
    }

    byte[] restoreUnforcedQualityScores(byte[] scores, int len, Collection<ReadFeature> readFeatures) {
        Arrays.fill(scores, 0, len, defaultQualityScore);
        if (readFeatures != null) {
            for (final ReadFeature feature : readFeatures) {
                switch (feature.getOperator()) {
                    case BaseQualityScore.operator:
                        int pos = feature.getPosition();
                        scores[pos - 1] = ((BaseQualityScore) feature).getQualityScore();
                        break;
                    case ReadBase.operator:
                        pos = feature.getPosition();
                        scores[pos - 1] = ((ReadBase) feature).getQualityScore();
                        break;

                    default:
                        break;
                }
            }
        }
        return scores;
    }

}
