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

    @Test
    public void testCopyWithNullSeq() {
        final String seqHeaderPrefix = "header";
        final String seqLine = null;
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

    @Test
    public void testEqualsWithNullSeq() {
        final String seqHeaderPrefix = "header";
        final String seqLine = "GATTACA";
        final String qualHeaderPrefix = "";
        final String qualLine = ";<=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\]^_`abcdefghijklmnopqrstuvwxyz{|}~";
        final FastqRecord fastqRecord1 = new FastqRecord(seqHeaderPrefix, null, qualHeaderPrefix, qualLine);
        final FastqRecord fastqRecord2 = new FastqRecord(seqHeaderPrefix, seqLine, qualHeaderPrefix, qualLine);
        Assert.assertNotEquals(fastqRecord1, fastqRecord2);
        Assert.assertNotEquals(fastqRecord2, fastqRecord1);

        Assert.assertNotEquals(fastqRecord1.hashCode(), fastqRecord2.hashCode());
        Assert.assertNotEquals(fastqRecord2.hashCode(), fastqRecord1.hashCode());
        Assert.assertEquals(fastqRecord1.hashCode(), fastqRecord1.hashCode());
        Assert.assertEquals(fastqRecord2.hashCode(), fastqRecord2.hashCode());
    }

    @Test
    public void testEqualsWithNullHeader() {
        final String seqLine = "GATTACA";
        final String qualHeaderPrefix = "";
        final String qualLine = ";<=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\]^_`abcdefghijklmnopqrstuvwxyz{|}~";
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
        final String qualLine = ";<=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\]^_`abcdefghijklmnopqrstuvwxyz{|}~";
        final FastqRecord fastqRecord1 = new FastqRecord(seqHeaderPrefix, seqLine, null, qualLine);
        final FastqRecord fastqRecord2 = new FastqRecord(seqHeaderPrefix, seqLine, "qualHeaderPrefix", qualLine);
        Assert.assertNotEquals(fastqRecord1, fastqRecord2);
        Assert.assertNotEquals(fastqRecord2, fastqRecord1);

        Assert.assertNotEquals(fastqRecord1.hashCode(), fastqRecord2.hashCode());
        Assert.assertNotEquals(fastqRecord2.hashCode(), fastqRecord1.hashCode());
        Assert.assertEquals(fastqRecord1.hashCode(), fastqRecord1.hashCode());
        Assert.assertEquals(fastqRecord2.hashCode(), fastqRecord2.hashCode());
    }

    @Test
    public void testEqualsWithNullQualLine() {
        final String seqHeaderPrefix = "header";
        final String seqLine = "GATTACA";
        final String qualLine = ";<=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\]^_`abcdefghijklmnopqrstuvwxyz{|}~";
        final FastqRecord fastqRecord1 = new FastqRecord(seqHeaderPrefix, seqLine, "qualHeaderPrefix", null);
        final FastqRecord fastqRecord2 = new FastqRecord(seqHeaderPrefix, seqLine, "qualHeaderPrefix", qualLine);
        Assert.assertNotEquals(fastqRecord1, fastqRecord2);
        Assert.assertNotEquals(fastqRecord2, fastqRecord1);
        Assert.assertNotEquals(fastqRecord1.hashCode(), fastqRecord2.hashCode());
        Assert.assertNotEquals(fastqRecord2.hashCode(), fastqRecord1.hashCode());
        Assert.assertEquals(fastqRecord1.hashCode(), fastqRecord1.hashCode());
        Assert.assertEquals(fastqRecord2.hashCode(), fastqRecord2.hashCode());
    }

}