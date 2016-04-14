package htsjdk.samtools.util;

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.*;

public class OverlapDetectorTest {

    @DataProvider(name="intervalsMultipleContigs")
    public Object[][] intervalsMultipleContigs(){
        List<Locatable> input = Arrays.asList(
                new Interval("1", 10, 100),
                new Interval("2", 200, 300)
        );
        List<Locatable> empty = new ArrayList<>();
        List<Locatable> contig1 = Arrays.asList(
                new Interval("1",  10, 100)
        );
        List<Locatable> contig2 = Arrays.asList(
                new Interval("2", 200, 300)
        );

        // returns input, query range, expected SimpleIntervals
        return new Object[][]{
                // we already test elsewhere that it works within a contig, so here we just have to make sure that
                // it picks the correct contig and can deal with not-yet-mentioned contigs.
                new Object[]{input, new Interval("1", 100, 200), contig1},
                new Object[]{input, new Interval("1", 1, 5), empty},
                new Object[]{input, new Interval("2", 100, 200), contig2},
                new Object[]{input, new Interval("3", 100, 200), empty},
        };
    }

    @Test(dataProvider = "intervalsMultipleContigs")
    public void testOverlap(List<Locatable> input, Locatable query, Collection<Locatable> expected) throws Exception {
        final OverlapDetector<Locatable> targetDetector = new OverlapDetector<>(0, 0);
        targetDetector.addAll(input, input);

        Collection<Locatable> actual = targetDetector.getOverlaps(query);
        Assert.assertEquals(
                actual,
                expected
        );
    }

    @DataProvider(name="intervalsSameContig")
    public Object[][] intervalsSameContig(){
        List<Locatable> input = Arrays.asList(
                new Interval("1",10,100)
        );
        List<Locatable> empty = new ArrayList<>();
        List<Locatable> manyOverlapping = Arrays.asList(
                new Interval("1",10,100),
                // special case: multiple intervals starting at the same place
                new Interval("1",20,50),
                new Interval("1",20,51),
                new Interval("1",20,52)
        );
        List<Locatable> mixInput = Arrays.asList(
                // ends before query interval
                new Interval("1",10,20),
                // ends in query interval
                new Interval("1",10,60),
                // equal to query interval
                new Interval("1",30,50),
                // covered by query interval
                new Interval("1",40,42),
                // ends after query interval
                new Interval("1",45,60),
                // starts after query interval
                new Interval("1",60,100)
        );
        List<Locatable> mixExpected = Arrays.asList(
                // ends in query interval
                new Interval("1",10,60),
                // equal to query interval
                new Interval("1",30,50),
                // covered by query interval
                new Interval("1",40,42),
                // ends after query interval
                new Interval("1",45,60)
        );
        // returns input single SimpleInterval, query range, expected SimpleInterval
        return new Object[][]{
                // single-point boundary cases
                new Object[]{input, new Interval("1", 10, 10), input},
                new Object[]{input, new Interval("1", 100, 100), input},
                new Object[]{input, new Interval("1", 9, 9), empty},
                new Object[]{input, new Interval("1", 11, 11), input},
                new Object[]{input, new Interval("1", 99, 99), input},
                new Object[]{input, new Interval("1", 101, 101), empty},
                // different contig
                new Object[]{input, new Interval("2", 10, 100), empty},
                // empty list boundary case
                new Object[]{empty, new Interval("1", 101, 101), empty},
                // input exactly matches the query interval
                new Object[]{input, new Interval("1", 10, 100), input},
                // multiple intervals in the same place (potential edge case for indexing)
                new Object[]{manyOverlapping, new Interval("1", 20, 20), manyOverlapping},
                // input with multiple intervals
                new Object[]{mixInput, new Interval("1",30,50), mixExpected}
        };
    }


    @Test(dataProvider = "intervalsSameContig")
    public void testOverlap(List<Locatable> input, Interval query, List<Locatable> expected) throws Exception {
        final OverlapDetector<Locatable> targetDetector = OverlapDetector.create(input);

        Set<Locatable> actual = targetDetector.getOverlaps(query);
        Assert.assertEquals(actual, new HashSet<>(expected));

        Assert.assertEquals(targetDetector.overlapsAny(query), !expected.isEmpty());

        Assert.assertEquals(new HashSet<>(targetDetector.getAll()), new HashSet<>(input));
    }

    @Test
    public void testLotsOfTinyIntervals() throws Exception {
        List<Locatable> input = new ArrayList<>();
        int n = 1000000;
        for (int i = 0; i < n; i++) {
            input.add(new Interval("1", 3*i+1, 3*i+2)); //1:1-2, 1:4-5, 1:7-8
        }
        final OverlapDetector<Locatable> detector = OverlapDetector.create(input);
        final Set<Locatable> overlapping = detector.getOverlaps(new Interval("1", 1, 3 * n + 2));
        Assert.assertEquals(new HashSet<>(input), overlapping);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testAddAllDifferentSizes() throws Exception {
        List<Locatable> input = Arrays.asList(
                new Interval("1",10,100)
        );
        final OverlapDetector<Locatable> targetDetector = OverlapDetector.create(input);

        List<Locatable> input1Interval = Arrays.asList(
                new Interval("1",11,101)
        );

        List<Locatable> input2Intervals = Arrays.asList(
                new Interval("1",20,200),
                new Interval("1",20,200)
        );
        targetDetector.addAll(input1Interval, input2Intervals);

    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testNullObjectAddLHS() throws Exception {
        List<Locatable> input = Arrays.asList(
                new Interval("1",10,100)
        );
        final OverlapDetector<Locatable> targetDetector = OverlapDetector.create(input);
        targetDetector.addLhs(null, new Interval("2",10,100));
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testNullIntervalAddLHS() throws Exception {
        List<Locatable> input = Arrays.asList(
                new Interval("1",10,100)
        );
        final OverlapDetector<Locatable> targetDetector = OverlapDetector.create(input);
        targetDetector.addLhs(new Interval("2",10,100), null);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testNullObjectsAddAll() throws Exception {
        List<Locatable> input = Arrays.asList(
                new Interval("1",10,100)
        );
        final OverlapDetector<Locatable> targetDetector = OverlapDetector.create(input);
        targetDetector.addAll(null, Arrays.asList(new Interval("2",10,100)));
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testNullIntervalsAddAll() throws Exception {
        List<Locatable> input = Arrays.asList(
                new Interval("1",10,100)
        );
        final OverlapDetector<Locatable> targetDetector = OverlapDetector.create(input);
        targetDetector.addAll(Arrays.asList(new Interval("2",10,100)), null);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testDifferentSizesAddAll() throws Exception {
        List<Locatable> input = Arrays.asList(
                new Interval("1",10,100)
        );
        final OverlapDetector<Locatable> targetDetector = OverlapDetector.create(input);
        final List<Locatable> l1 = Arrays.asList(new Interval("2", 10, 100));
        final List<Locatable> l2 = Arrays.asList(new Interval("2", 10, 100), new Interval("3", 10, 100));
        targetDetector.addAll(l1, l2);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testNullArgGetOverlaps() throws Exception {
        List<Locatable> input = Arrays.asList(
                new Interval("1",10,100)
        );
        final OverlapDetector<Locatable> targetDetector = OverlapDetector.create(input);
        targetDetector.getOverlaps(null);
    }

    @Test
    public void testAddTwice() throws Exception {
        List<Locatable> input = Arrays.asList(
                new Interval("1",10,100),
                new Interval("1",10,100)
        );
        final OverlapDetector<Locatable> targetDetector = OverlapDetector.create(input);
    }
}
