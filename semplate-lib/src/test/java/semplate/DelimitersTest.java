package semplate;

import static org.junit.jupiter.api.Assertions.*;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

class DelimitersTest {

	
	@Test
	void testAddDelimiters() { 
        Delimiters delimiters = new Delimiters();
        
        delimiters.add("(", ")");
        delimiters.add("[", "]");
        delimiters.add("<", ">");
        delimiters.add("<span>", "</span>");
        delimiters.add("//", "");
        
        
        assertEquals(5,  delimiters.number());
        
		assertEquals("(", delimiters.startDelimiter(0).orElse(""));
		assertEquals(")", delimiters.endDelimiter(0).orElse(""));
		
		assertTrue(delimiters.endDelimiter(4).isEmpty());
		
		
	}
	
	@Test
	void testDelimiterPair() {
		
		Delimiters delimiters = new Delimiters();
		
		delimiters.addPair("()");
		delimiters.addPair("[]");
		delimiters.addPair("<>");

		
		assertEquals("(", delimiters.startDelimiter(0).orElse("")); 
		assertEquals(")", delimiters.endDelimiter(0).orElse("")); 
		assertEquals("[", delimiters.startDelimiter(1).orElse("")); 
		assertEquals("]", delimiters.endDelimiter(1).orElse("")); 
		assertEquals("<", delimiters.startDelimiter(2).orElse("")); 
		assertEquals(">", delimiters.endDelimiter(2).orElse("")); 
        
		assertThrows(IllegalArgumentException.class, () -> delimiters.addPair(""));
		assertThrows(IllegalArgumentException.class, () -> delimiters.addPair("["));
		assertThrows(IllegalArgumentException.class, () -> delimiters.addPair("<!>"));
	}
	
	@Test
	void testAddDelimiterObject() {
		Delimiters delimiters = new Delimiters();
		
		Delimiter d1 = new Delimiter().pair("()");
		Delimiter d2 = new Delimiter().start("//");
		
		delimiters.add(d1);
		delimiters.add(d2);
		
		assertEquals(2, delimiters.number());
		
		int i = 0;
		for (Delimiter d : delimiters) {
		  if (i == 0) {
			  assertEquals("(", d.start().orElse(""));
			  assertEquals(")", d.end().orElse(""));
		  }
		  if (i == 1) {
			  assertEquals("//", d.start().orElse(""));
			  assertTrue(d.end().isEmpty());
		  }
		  i++;
		}
		
		
	}
	
	
	@Test 
	void testIterateDelimiters() {
		String[] testStarts = {"(", "[", "<", "<span>"};
		String[] testEnds = {")", "]", ">", "</span>"};
		
		Delimiters delimiters = new Delimiters();
        		
		delimiters.add(testStarts[0], testEnds[0]);
		delimiters.add(testStarts[1], testEnds[1]);
		delimiters.add(testStarts[2], testEnds[2]);
		delimiters.add(testStarts[3], testEnds[3]);
        
		int i = 0;
        for (Delimiter delim: delimiters) {
        	assertEquals(testStarts[i], delim.start().orElse(""));
        	i++;
        }
        
	}

	@Test
	void testAddDelimitersObject() {
		Delimiter[] testData = {
				new Delimiter().start("<").end(">"), 
				new Delimiter().start("[").end("]"), 
				new Delimiter().start("<").end(">"), 
				new Delimiter().start("[").end("]")
         		};
		
		Delimiters delimiters = new Delimiters();
		
		delimiters.add(testData[0]);
		delimiters.add(testData[1]);
		Delimiters extraDelimiters = new Delimiters();
		
		extraDelimiters.add(testData[2]);
		extraDelimiters.add(testData[3]);
		
		delimiters.add(extraDelimiters);
		
		assertEquals(4, delimiters.number());

        int i = 0;
		for (Delimiter d: delimiters) {
			d.start().equals(testData[i].start());   			
			d.end().equals(testData[i].end());   	
			i++;
		}
		
	}
	
	@Test 
	void testSurround() {
		Delimiters delimiters = new Delimiters();

		delimiters.addPair("()");
		delimiters.addPair("[]");
		delimiters.addPair("<>");
		
		assertTrue(delimiters.suround("(hello)"));
		assertTrue(delimiters.suround("<Loret ipsum>"));
		assertTrue(delimiters.suround("[value]"));
		assertTrue(delimiters.suround("[]"));
		
		assertFalse(delimiters.suround("hello"));
		assertFalse(delimiters.suround("(hello"));
		assertFalse(delimiters.suround("hello)"));
		assertFalse(delimiters.suround("preamble(hello)"));
		assertFalse(delimiters.suround("(hello)postamble"));
		assertFalse(delimiters.suround("preamble(hello)postamble"));
		assertFalse(delimiters.suround(""));

  }
	
@Test
void testPattern() {
	String text = "The word population in <span>{{year}}</span> is <span>{{population}}</span> according to [{{source}}]({{sourceLink}}).";
	
	Delimiters delimiters = new Delimiters();
	
	delimiters.add("<span>", "</span>");
	delimiters.addPair("()");
	delimiters.addPair("[]");
	
	Pattern pattern = delimiters.pattern();
	
	System.out.println(pattern);
	
	Matcher matcher = pattern.matcher(text);
	
	while ( matcher.find()) {
		System.out.println(matcher.group());
	}
	
	assertTrue(matcher.find(0));
	assertEquals("<span>{{year}}</span>", matcher.group());
	assertTrue(matcher.find());
	assertEquals("<span>{{population}}</span>", matcher.group());
	assertTrue(matcher.find());
	assertEquals("[{{source}}]", matcher.group());
	assertTrue(matcher.find());
	assertEquals("({{sourceLink}})", matcher.group());
	
	
}	

}
