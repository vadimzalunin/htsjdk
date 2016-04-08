package htsjdk.samtools.fastq;

import org.testng.Assert;
import org.testng.annotations.Test;

public class FastqRecordTest {

    @Test
    public void testBasic() {
        final String seqHeaderPrefix = "FAKE0003 Original version has Solexa scores from 62 to -5 inclusive (in that order)";
        final String seqLine = "ACGTACGTACGTACGTACGTACGTACGTACGTACGTACGTACGTACGTACGTACGTACGTACGTACGT";
        final String qualHeaderPrefix = "";
        final String qualLine = ";<=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\]^_`abcdefghijklmnopqrstuvwxyz{|}~";
        final FastqRecord fastqRecord = new FastqRecord(seqHeaderPrefix, seqLine, qualHeaderPrefix, qualLine);

        Assert.assertNull(fastqRecord.getBaseQualityHeader());

        Assert.assertEquals(fastqRecord.getReadHeader(), seqHeaderPrefix);
        Assert.assertEquals(fastqRecord.getBaseQualityString(), qualLine);
        Assert.assertEquals(fastqRecord.getReadString(), seqLine);
        Assert.assertNotNull(fastqRecord.toString());//just check not nullness
        Assert.assertNotEquals(fastqRecord, null);
        Assert.assertNotEquals(null, fastqRecord);
        Assert.assertEquals(fastqRecord, fastqRecord);
        Assert.assertNotEquals(fastqRecord, "fred");
        Assert.assertNotEquals("fred", fastqRecord);
        Assert.assertEquals(fastqRecord.length(), seqLine.length());
        Assert.assertEquals(fastqRecord.getBaseQualityString().length(), fastqRecord.getReadString().length());
        Assert.assertEquals(fastqRecord.getReadString().length(), fastqRecord.length());
    }

    @Test
    public void testBasicEmptyHeaderPrefix() {
        final String seqHeaderPrefix = "";
        final String seqLine = "ACGTACGTACGTACGTACGTACGTACGTACGTACGTACGTACGTACGTACGTACGTACGTACGTACGT";
        final String qualHeaderPrefix = "";
        final String qualLine = ";<=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\]^_`abcdefghijklmnopqrstuvwxyz{|}~";
        final FastqRecord fastqRecord = new FastqRecord(seqHeaderPrefix, seqLine, qualHeaderPrefix, qualLine);
        Assert.assertNull(fastqRecord.getReadHeader());
        Assert.assertNull(fastqRecord.getBaseQualityHeader());
    }

    @Test
    public void testCopy() {
        final String seqHeaderPrefix = "FAKE0003 Original version has Solexa scores from 62 to -5 inclusive (in that order)";
        final String seqLine = "ACGTACGTACGTACGTACGTACGTACGTACGTACGTACGTACGTACGTACGTACGTACGTACGTACGT";
        final String qualHeaderPrefix = "";
        final String qualLine = ";<=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\]^_`abcdefghijklmnopqrstuvwxyz{|}~";
        final FastqRecord fastqRecord = new FastqRecord(seqHeaderPrefix, seqLine, qualHeaderPrefix, qualLine);
        final FastqRecord fastqRecordCopy = new FastqRecord(fastqRecord);

        Assert.assertEquals(fastqRecord, fastqRecordCopy);
        Assert.assertNotSame(fastqRecord, fastqRecordCopy);
        Assert.assertSame(fastqRecord.getReadString(), fastqRecordCopy.getReadString());
        Assert.assertSame(fastqRecord.getBaseQualityString(), fastqRecordCopy.getBaseQualityString());
        Assert.assertSame(fastqRecord.getBaseQualityHeader(), fastqRecordCopy.getBaseQualityHeader());
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testNullSeq() {
        final String seqHeaderPrefix = "header";
        final String seqLine = null;
        final String qualHeaderPrefix = "";
        final String qualLine = ";<=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\]^_`abcdefghijklmnopqrstuvwxyz{|}~";
        new FastqRecord(seqHeaderPrefix, seqLine, qualHeaderPrefix, qualLine);
    }

    @Test
    public void testEqualsWithNullHeader() {
        final String seqLine = "GATTACA";
        final String qualHeaderPrefix = "";
        final String qualLine = "ABCDEFG";
        final FastqRecord fastqRecord1 = new FastqRecord("", seqLine, qualHeaderPrefix, qualLine);
        final FastqRecord fastqRecord2 = new FastqRecord("header", seqLine, qualHeaderPrefix, qualLine);
        Assert.assertNotEquals(fastqRecord1, fastqRecord2);
        Assert.assertNotEquals(fastqRecord2, fastqRecord1);

        Assert.assertNotEquals(fastqRecord1.hashCode(), fastqRecord2.hashCode());
        Assert.assertNotEquals(fastqRecord2.hashCode(), fastqRecord1.hashCode());
        Assert.assertEquals(fastqRecord1.hashCode(), fastqRecord1.hashCode());
        Assert.assertEquals(fastqRecord2.hashCode(), fastqRecord2.hashCode());
    }

    @Test
    public void testEqualsWithNullBaseQualityHeader() {
        final String seqHeaderPrefix = "header";
        final String seqLine = "GATTACA";
        final String qualLine = "ABCDEFG";
        final FastqRecord fastqRecord1 = new FastqRecord(seqHeaderPrefix, seqLine, null, qualLine);
        final FastqRecord fastqRecord2 = new FastqRecord(seqHeaderPrefix, seqLine, "qualHeaderPrefix", qualLine);
        Assert.assertNotEquals(fastqRecord1, fastqRecord2);
        Assert.assertNotEquals(fastqRecord2, fastqRecord1);

        Assert.assertNotEquals(fastqRecord1.hashCode(), fastqRecord2.hashCode());
        Assert.assertNotEquals(fastqRecord2.hashCode(), fastqRecord1.hashCode());
        Assert.assertEquals(fastqRecord1.hashCode(), fastqRecord1.hashCode());
        Assert.assertEquals(fastqRecord2.hashCode(), fastqRecord2.hashCode());
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testNullQual() {
        final String seqHeaderPrefix = "header";
        final String seqLine = "GATTACA";
        new FastqRecord(seqHeaderPrefix, seqLine, "qualHeaderPrefix", null);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testNullString() {
        final String seqHeaderPrefix = "header";
        final String qualLine = "GATTACA";
        new FastqRecord(seqHeaderPrefix, null, "qualHeaderPrefix", qualLine);
    }


    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testEmptyQual() {
        final String seqHeaderPrefix = "header";
        final String seqLine = "GATTACA";
        new FastqRecord(seqHeaderPrefix, seqLine, "qualHeaderPrefix", "");
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testEmptyString() {
        final String seqHeaderPrefix = "header";
        final String qualLine = "GATTACA";
        new FastqRecord(seqHeaderPrefix, "", "qualHeaderPrefix", qualLine);
    }

    @Test
    public void testNotEqualQuals() {
        final String seqLine1 = "GATTACA";
        final String qualLine1 = "ABCDEFG";

        final String seqLine2 = seqLine1;
        final String qualLine2 = seqLine2.replace('A', 'X');

        final FastqRecord fastqRecord1 = new FastqRecord("header", seqLine1, "qualHeaderPrefix", qualLine1);
        final FastqRecord fastqRecord2 = new FastqRecord("header", seqLine2, "qualHeaderPrefix", qualLine2);
        Assert.assertNotEquals(fastqRecord1, fastqRecord2);
        Assert.assertNotEquals(fastqRecord2, fastqRecord1);

        Assert.assertEquals(fastqRecord1.getReadString(), fastqRecord2.getReadString());
        Assert.assertNotEquals(fastqRecord1.getBaseQualityString(), fastqRecord2.getBaseQualityString());

        Assert.assertNotEquals(fastqRecord1.hashCode(), fastqRecord2.hashCode());
        Assert.assertNotEquals(fastqRecord2.hashCode(), fastqRecord1.hashCode());
    }

    @Test
    public void testNotEqualStrings() {
        final String seqLine1 = "GATTACA";
        final String qualLine1 = "ABCDEFG";

        final String seqLine2 = seqLine1.replace('A', 'X');
        final String qualLine2 = qualLine1;

        final FastqRecord fastqRecord1 = new FastqRecord("header", seqLine1, "qualHeaderPrefix", qualLine1);
        final FastqRecord fastqRecord2 = new FastqRecord("header", seqLine2, "qualHeaderPrefix", qualLine2);
        Assert.assertNotEquals(fastqRecord1, fastqRecord2);
        Assert.assertNotEquals(fastqRecord2, fastqRecord1);

        Assert.assertNotEquals(fastqRecord1.getReadString(), fastqRecord2.getReadString());
        Assert.assertEquals(fastqRecord1.getBaseQualityString(), fastqRecord2.getBaseQualityString());

        Assert.assertNotEquals(fastqRecord1.hashCode(), fastqRecord2.hashCode());
        Assert.assertNotEquals(fastqRecord2.hashCode(), fastqRecord1.hashCode());
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testNotEqualLengths() {
        final String seqLine1 = "GATTACA";
        final String qualLine1 = seqLine1 + "X";

        new FastqRecord("header", seqLine1, "qualHeaderPrefix", qualLine1);
    }
}