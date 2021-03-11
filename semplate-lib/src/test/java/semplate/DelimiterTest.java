package semplate;

import static org.junit.jupiter.api.Assertions.*;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class DelimiterTest {

	
	Delimiter delimiter;
	
	@BeforeEach
	void setUp() throws Exception {
		delimiter = new Delimiter();
	}

	@AfterEach
	void tearDown() throws Exception {
	}


	@Test
	void testStart() {
		
		assertTrue(delimiter.start().isEmpty());
		
		delimiter.start("<");
		assertEquals("<", delimiter.start().orElse(""));
		
		delimiter.start("");
		assertTrue(delimiter.start().isEmpty());
		
		delimiter.start("<span>");
		assertEquals("<span>", delimiter.start().orElse(""));
		
	}

	@Test
	void testEnd() {
		assertTrue(delimiter.end().isEmpty());

		delimiter.end("<");
		assertEquals("<", delimiter.end().orElse(""));

		delimiter.end("");
		assertTrue(delimiter.end().isEmpty());
		
		delimiter.end("</span>");
		assertEquals("</span>", delimiter.end().orElse(""));
		
	}

	@Test
	void testPair() {

		delimiter.pair("[]");
		assertEquals("[", delimiter.start().orElse(""));
		assertEquals("]", delimiter.end().orElse(""));

		assertThrows(IllegalArgumentException.class, () -> delimiter.pair(""));
		assertThrows(IllegalArgumentException.class, () -> delimiter.pair("("));
		assertThrows(IllegalArgumentException.class, () -> delimiter.pair("())"));
	}

	@Test
	void testPattern() {
		String text = "This contains (some text) in delimiters\n";
		
		delimiter.pair("()");
		Pattern pattern = delimiter.pattern();
		
		Matcher matcher = pattern.matcher(text);
		System.out.println(matcher);
		// \([^\)]*\)
		
		assertTrue(matcher.find(0));
		String result = matcher.group();
		assertEquals("(some text)", result);
		
		delimiter.start("(").end("");
		assertThrows(IllegalArgumentException.class, () ->  delimiter.pattern());
		
		delimiter.start("").end(")");
		assertThrows(IllegalArgumentException.class, () ->  delimiter.pattern());
		
	}
	
	
}
