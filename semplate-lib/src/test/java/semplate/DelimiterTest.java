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
		String text = "This contains (some text) in delimiters";
		
		delimiter.pair("()");
		Pattern pattern = delimiter.pattern();
		
		Matcher matcher = pattern.matcher(text);
		System.out.println(matcher);
		// \([^\)]*\)
		
		assertTrue(matcher.find(0));
		String result = matcher.group();
		assertEquals("(some text)", result);
		
	}
	
	@Test
	void testPatternWithMutiCharacterDelimiters() {
		String text = "The word population in <span>{{year}}</span> is <span>{{population}}</span> according to the World Bank.";
		
		delimiter.start("<span>").end("</span>");
		
        Pattern pattern = delimiter.pattern();
		
		Matcher matcher = pattern.matcher(text);
		System.out.println(matcher);
		
		assertTrue(matcher.find(0));
		String result = matcher.group();
		assertEquals("<span>{{year}}</span>", result);
		
		assertTrue(matcher.find());
		result = matcher.group();
		assertEquals("<span>{{population}}</span>", result);
		
	}
	
	@Test
	void testPatternWithNoEndDelimiter() {
		String text = "This contains (some text) in delimiters";

		delimiter.start("(").end("");
		Pattern pattern = delimiter.pattern();
		Matcher matcher = pattern.matcher(text);
		assertTrue(matcher.find(0));
		String result = matcher.group();
		assertEquals("(some text) in delimiters", result);

	}
	
	@Test
	void testPatternWithNoStartDelimiter() {
		String text = "This contains (some text) in delimiters";

		delimiter.start("").end(")");
		Pattern pattern = delimiter.pattern();
		Matcher matcher = pattern.matcher(text);
		assertTrue(matcher.find(0));
		String result = matcher.group();
		assertEquals("This contains (some text)", result);

	}
	
	@Test
	void testPatternWithNoDelimiters() {
		String text = "This contains (some text) in delimiters";

		delimiter.start("").end("");
		Pattern pattern = delimiter.pattern();
		Matcher matcher = pattern.matcher(text);
		assertTrue(matcher.find(0));
		String result = matcher.group();
		assertEquals("This contains (some text) in delimiters", result);

	}
	
    @Test
	void testInsert() {
    	delimiter.start("(").end(")");
    	
    	Delimiter insert = new Delimiter().start("{{").end("}}");
    	delimiter.insert(insert);
    	
    	assertEquals("({{", delimiter.start().orElse(""));
    	assertEquals("}})", delimiter.end().orElse(""));
    	
       	delimiter.start("(").end("");
    	delimiter.insert(insert);
    	assertEquals("({{", delimiter.start().orElse(""));
    	assertEquals("}}", delimiter.end().orElse(""));
    	
    	delimiter.start("").end(")");
    	delimiter.insert(insert);
    	assertEquals("{{", delimiter.start().orElse(""));
    	assertEquals("}})", delimiter.end().orElse(""));
    	
    	delimiter.start("(").end(")");
    	insert.start("").end("");
    	assertEquals("(", delimiter.start().orElse(""));
    	assertEquals(")", delimiter.end().orElse(""));
		
	}
    
    @Test
    void testEquals() {
    	Delimiter testDelimiter = new Delimiter().start("(").end(")");
    	delimiter.start("(").end(")");
    	assertTrue(delimiter.equals(testDelimiter));
    	
    	testDelimiter.start("<span>").end("</span>");
    	delimiter.start("<span>").end("</span>");
    	assertTrue(delimiter.equals(testDelimiter));
    	    	
    	testDelimiter = new Delimiter().start("[").end("]");
    	delimiter.start("(").end(")");
    	assertFalse(delimiter.equals(testDelimiter));
    	
    	delimiter = new Delimiter();
    	testDelimiter = new Delimiter().start("//");
    	delimiter.start("//");
    	assertTrue(delimiter.equals(testDelimiter));
    	
    	testDelimiter = new Delimiter().start("//").end("");
    	delimiter.start("//");
    	assertTrue(delimiter.equals(testDelimiter));
    	
    	testDelimiter.start("<span>").end("</span>");
    	delimiter.start("span").end("</span>");
    	assertFalse(delimiter.equals(testDelimiter));
    	
    	
    	
    	
    }
	
	
	
}
