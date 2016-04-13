package htsjdk.samtools.util;

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class LocatableSkipListUnitTest {

    @DataProvider(name="intervals")
    public Object[][] intervals(){
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

    @Test(dataProvider = "intervals")
    public void testOverlap(List<Locatable> input, Locatable query, List<Locatable> expected) throws Exception {
        LocatableSkipList<Locatable> ints = new LocatableSkipList<>(input);
        List<Locatable> actual = ints.getOverlapping(query);
        Assert.assertEquals(
                actual,
                expected
        );
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testNullCtorArg() throws Exception {
        new LocatableSkipList<>(null);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testNullQuery() throws Exception {
        List<Locatable> input = Arrays.asList(
                new Interval("1", 10, 100),
                new Interval("2", 200, 300)
        );
        final LocatableSkipList<Locatable> skipList = new LocatableSkipList<>(input);
        skipList.getOverlapping(null);
    }
}