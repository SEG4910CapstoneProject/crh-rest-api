package me.t65.reportgenapi.utils;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class JSoupHtmlRemoverTests {

    private JSoupHtmlRemover jSoupHtmlRemover;

    @BeforeEach
    public void beforeEach() {
        jSoupHtmlRemover = new JSoupHtmlRemover();
    }

    @Test
    public void testUnescapeAndRemoveHtml_success() {
        String includedString =
                "Text &lt;p&gt;this is &lt;b&gt;some&lt;/b&gt; text. 5 &lt; 9 and 8 &gt; 10 Do not"
                        + " forget about this/that&lt;/p&gt; more text &lt;script&gt;This text is"
                        + " ignored&lt;/script&gt;";
        String expectedString =
                "Text this is some text. 5 < 9 and 8 > 10 Do not forget about this/that more text";

        String actual = jSoupHtmlRemover.unescapeAndRemoveHtml(includedString);

        assertEquals(expectedString, actual);
    }
}
