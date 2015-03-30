package clojure.lang;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;

public class CountsTest  {

    @Test
    public void test() {
        final Counts counts = new Counts();

        assertEquals(counts, counts);
        assertEquals(counts, new Counts());
        assertEquals(counts, new Counts(Counts.leftResolver, 0, 0));
        assertNotEquals(counts, new Counts(Counts.leftResolver, 1, 0));
        assertNotEquals(counts, new Counts(Counts.leftResolver, 1, 1));
        assertNotEquals(counts, new Counts(Counts.leftResolver, 0, 1));
        assertNotEquals(counts, new Counts(Counts.rightResolver, 0, 0));
        assertNotEquals(counts, new Counts(Counts.rightResolver, 1, 0));
        assertNotEquals(counts, new Counts(Counts.rightResolver, 1, 1));
        assertNotEquals(counts, new Counts(Counts.rightResolver, 0, 1));
        assertNotEquals(counts, "not a Counts instance");
        
        assertNotNull(counts.toString());
    }
}
