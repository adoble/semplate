package semplate;

import static org.junit.jupiter.api.Assertions.*;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
		
		assertTrue(delimiters.isDelimited("(hello)"));
		assertTrue(delimiters.isDelimited("<Loret ipsum>"));
		assertTrue(delimiters.isDelimited("[value]"));
		assertTrue(delimiters.isDelimited("[]"));
		
		assertFalse(delimiters.isDelimited("hello"));
		assertFalse(delimiters.isDelimited("(hello"));
		assertFalse(delimiters.isDelimited("hello)"));
		assertFalse(delimiters.isDelimited("preamble(hello)"));
		assertFalse(delimiters.isDelimited("(hello)postamble"));
		assertFalse(delimiters.isDelimited("preamble(hello)postamble"));
		assertFalse(delimiters.isDelimited(""));

  }
	
	@Test
	void testPattern() {
		String text = "The word population in <span>{{year}}</span> is <span>{{population}}</span> according to [{{source}}]({{sourceLink}}).";

		Delimiters delimiters = new Delimiters();

		delimiters.add("<span>", "</span>");
		delimiters.addPair("()");
		delimiters.addPair("[]");

		Pattern pattern = delimiters.pattern();

		Matcher matcher = pattern.matcher(text);

		assertTrue(matcher.find(0));
		assertEquals("<span>{{year}}</span>", matcher.group());
		assertTrue(matcher.find());
		assertEquals("<span>{{population}}</span>", matcher.group());
		assertTrue(matcher.find());
		assertEquals("[{{source}}]", matcher.group());
		assertTrue(matcher.find());
		assertEquals("({{sourceLink}})", matcher.group());


	}	
	
	@Test
	void testPatternWithNoDelimiters() {
		String text = "The word population in <span>{{year}}</span> is <span>{{population}}</span> according to [{{source}}]({{sourceLink}}).";

		Delimiters delimiters = new Delimiters();  // No delimiters
		Pattern pattern = delimiters.pattern();
			
		Matcher matcher = pattern.matcher(text);
		assertTrue(matcher.find(0));
		assertEquals(text, matcher.group());
		
	}
	
	@Test
	void testInsertAll() {
		Delimiters delimiters = new Delimiters();
		
		delimiters.addPair("()");
		delimiters.addPair("[]");
		delimiters.addPair("<>");
		
		delimiters.insertAll("{{", "}}");
		
		for (Delimiter d: delimiters) {
			assertEquals(3, d.start().orElse("").length());
			assertEquals("{{", d.start().orElse("").substring(1, 3));
		
			assertEquals(3, d.end().orElse("").length());
			assertEquals("}}", d.end().orElse("").substring(0, 2));
		
		}
		
	}
	
	@Test
	void testClone( ) throws CloneNotSupportedException {
        Delimiters delimiters = new Delimiters();
		
        Delimiter[] testDelimiters = {  
        		new Delimiter().pair("()"),
        		new Delimiter().pair("[]"),
        		new Delimiter().pair("<>")
        };

      for (Delimiter d: testDelimiters) {
    	  delimiters.add(d);
      }
		
		Delimiters clone = delimiters.clone();
		
		  Delimiter[] results = new Delimiter[3];
		  
		  int i = 0;
		  for (Delimiter d: clone) {
	    	  results[i++] = d;
	      }
			  
		assertArrayEquals(testDelimiters, results);
		
	}
	
	

}
