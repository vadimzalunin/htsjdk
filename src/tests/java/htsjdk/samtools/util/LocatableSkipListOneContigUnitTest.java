package htsjdk.samtools.util;

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class LocatableSkipListOneContigUnitTest {


    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testNullCtorArg() throws Exception {
        new LocatableSkipListOneContig(null);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testNullArg() throws Exception {
        List<Locatable> input = Arrays.asList(
                new Interval("1",10,100)
        );
        final LocatableSkipListOneContig<Locatable> l = new LocatableSkipListOneContig<>(input);
        l.getOverlapping(null);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testNotSameContig() throws Exception {
        List<Locatable> input = Arrays.asList(
                new Interval("1",10,100),
                new Interval("2",10,100)
        );
        final LocatableSkipListOneContig<Locatable> l = new LocatableSkipListOneContig<>(input);
    }

    @Test
    public void testEmptyInput() throws Exception {
        List<Locatable> empty = new ArrayList<>();
        final LocatableSkipListOneContig<Locatable> l = new LocatableSkipListOneContig<>(empty);
        Assert.assertTrue(l.getOverlapping(new Interval("", 10, 100)).isEmpty()); //try to fool it by using empty contig
        Assert.assertTrue(l.getOverlapping(new Interval("1", 10, 100)).isEmpty());
    }

    @DataProvider(name="intervals")
    public Object[][] intervals(){
        List<Locatable> input = Arrays.asList(
                new Interval("1",10,100)
        );
        List<Locatable> empty = new ArrayList<>();
        List<Locatable> manyOverlapping = Arrays.asList(
                new Interval("1",10,100),
                // special case: multiple intervals starting at the same place
                new Interval("1",20,50),
                new Interval("1",20,50),
                new Interval("1",20,50)
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

    @Test
    public void testLotsOfTinyIntervals() throws Exception {
        List<Locatable> input = new ArrayList<>();
        int n = 1000000;
        for (int i = 0; i < n; i++) {
            input.add(new Interval("1", 3*i+1, 3*i+2)); //1:1-2, 1:4-5, 1:7-8
        }
        final LocatableSkipListOneContig<Locatable> skipList = new LocatableSkipListOneContig<>(input);
        final List<Locatable> overlapping = skipList.getOverlapping(new Interval("1", 1, 3 * n + 2));
        Assert.assertEquals(input, overlapping);
    }

    @Test(dataProvider = "intervals")
    public void testOverlap(List<Locatable> input, Interval query, List<Locatable> expected) throws Exception {
        LocatableSkipListOneContig<Locatable> ints = new LocatableSkipListOneContig<>(input);
        List<Locatable> actual = ints.getOverlapping(query);
        Assert.assertEquals(
                actual,
                expected
        );
    }

    @DataProvider(name = "IntervalOverlapData")
    public static Object[][] getIntervalOverlapData() {
        final Locatable standardInterval = new Interval("1", 10, 20);
        final Locatable oneBaseInterval = new Interval("1", 10, 10);

        return new Object[][] {
                { standardInterval, new Interval("2", 10, 20), false },
                { standardInterval, new Interval("1", 1, 5), false },
                { standardInterval, new Interval("1", 1, 9), false },
                { standardInterval, new Interval("1", 1, 10), true },
                { standardInterval, new Interval("1", 1, 15), true },
                { standardInterval, new Interval("1", 10, 10), true },
                { standardInterval, new Interval("1", 10, 15), true },
                { standardInterval, new Interval("1", 10, 20), true },
                { standardInterval, new Interval("1", 15, 20), true },
                { standardInterval, new Interval("1", 15, 25), true },
                { standardInterval, new Interval("1", 20, 20), true },
                { standardInterval, new Interval("1", 20, 25), true },
                { standardInterval, new Interval("1", 21, 25), false },
                { standardInterval, new Interval("1", 25, 30), false },
                { oneBaseInterval, new Interval("2", 10, 10), false },
                { oneBaseInterval, new Interval("1", 1, 5), false },
                { oneBaseInterval, new Interval("1", 1, 9), false },
                { oneBaseInterval, new Interval("1", 1, 10), true },
                { oneBaseInterval, new Interval("1", 10, 10), true },
                { oneBaseInterval, new Interval("1", 10, 15), true },
                { oneBaseInterval, new Interval("1", 11, 15), false },
                { oneBaseInterval, new Interval("1", 15, 20), false },
                { standardInterval, null, false },
                { standardInterval, standardInterval, true }
        };
    }

    @Test(dataProvider = "IntervalOverlapData")
    public void testOverlap( final Locatable firstInterval, final Locatable secondInterval, final boolean expectedOverlapResult ) {
        Assert.assertEquals(LocatableSkipListOneContig.overlaps(firstInterval, secondInterval), expectedOverlapResult,
                "overlap() returned incorrect result for intervals " + firstInterval + " and " + secondInterval);
    }

}
