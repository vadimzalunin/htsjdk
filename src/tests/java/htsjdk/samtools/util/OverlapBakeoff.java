package htsjdk.samtools.util;

import htsjdk.samtools.*;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.List;

public class OverlapBakeoff {

    @Test
    public void testOverlapDetector() throws IOException {
        final String intervals = "/Users/akiezun/IdeaProjects/gatk/Broad.human.exome.b37.interval_list";
        final List<Interval> uniqueTargets = IntervalList.fromFile(new File(intervals)).uniqued().getIntervals();
        StopWatch setupStopWatch= new StopWatch();
        setupStopWatch.start();
        final OverlapDetector<Interval> targetDetector = new OverlapDetector<>(0, 0);
        targetDetector.addAll(uniqueTargets, uniqueTargets);
        setupStopWatch.stop();

        long readCount = 0L;
        Histogram<Integer> counts = new Histogram<>();
        final File bamFile = new File("/Users/akiezun/bin/data/CEUTrio.HiSeq.WEx.b37.NA12892.chr10.bam");
        final SamReaderFactory readerFactory = SamReaderFactory.makeDefault().validationStringency(ValidationStringency.SILENT);
        StopWatch queryStopWatch= new StopWatch();
        try (final SamReader reader = readerFactory.open(bamFile)) {
            for (final SAMRecord read : reader) {
                readCount++;

                final Interval interval = new Interval(read.getContig(), read.getStart(), read.getEnd());
                queryStopWatch.start();
                final Collection<Interval> targets = targetDetector.getOverlaps(interval);
                queryStopWatch.stop();
                counts.increment(targets.size());
            }
        }
        System.out.println("OverlapDetector");
        System.out.println("setup time:" + setupStopWatch.getElapsedTime());
        System.out.println("query time:" + queryStopWatch.getElapsedTime());
        System.out.println("readCount:" + readCount + " counts:" + counts);
    }

    @Test
    public void testSkipList() throws IOException {
        // Setup the overlap detector
        final String intervals = "/Users/akiezun/IdeaProjects/gatk/Broad.human.exome.b37.interval_list";
        final List<Interval> uniqueTargets = IntervalList.fromFile(new File(intervals)).uniqued().getIntervals();
        StopWatch setupStopWatch= new StopWatch();
        setupStopWatch.start();
        final LocatableSkipList<Interval> targetDetector = new LocatableSkipList<>(uniqueTargets);
        setupStopWatch.stop();

        long readCount = 0L;
        Histogram<Integer> counts = new Histogram<>();
        final File bamFile = new File("/Users/akiezun/bin/data/CEUTrio.HiSeq.WEx.b37.NA12892.chr10.bam");
        final SamReaderFactory readerFactory = SamReaderFactory.makeDefault().validationStringency(ValidationStringency.SILENT);
        StopWatch queryStopWatch= new StopWatch();
        try (final SamReader reader = readerFactory.open(bamFile)) {
            for (final SAMRecord read : reader) {
                readCount++;

                final Interval interval = new Interval(read.getContig(), read.getStart(), read.getEnd());
                queryStopWatch.start();
                final Collection<Interval> targets = targetDetector.getOverlapping(interval);
                queryStopWatch.stop();
                counts.increment(targets.size());
            }
        }
        System.out.println("LocatableSkipList");
        System.out.println("setup time:" + setupStopWatch.getElapsedTime());
        System.out.println("query time:" + queryStopWatch.getElapsedTime());
        System.out.println("readCount:" + readCount + " counts:" + counts);
    }
}
