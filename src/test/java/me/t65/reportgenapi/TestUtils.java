package me.t65.reportgenapi;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Collection;
import java.util.List;

public class TestUtils {

    public static <T> void assertListContains(Collection<T> expectedItems, List<T> actualList) {
        for (T expected : expectedItems) {
            assertTrue(
                    actualList.contains(expected),
                    "List expected to contain " + expected + " but contains " + actualList);
        }
    }
}
