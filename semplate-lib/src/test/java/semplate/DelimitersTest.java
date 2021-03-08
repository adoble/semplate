package semplate;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

class DelimitersTest {

	@Test
	void testCommentDelimiters() {
		Delimiters delimiters = new Delimiters();
		
		delimiters.commentStartDelimiter("[");
		assertEquals("[", delimiters.commentStartDelimiter().orElse(""));
		
		delimiters.commentEndDelimiter(")");
		assertEquals(")", delimiters.commentEndDelimiter().orElse(""));
		
		delimiters.commentStartDelimiter("<!--");
		assertEquals("<!--", delimiters.commentStartDelimiter().orElse(""));
		
		delimiters.commentEndDelimiter("-->");
		assertEquals("-->", delimiters.commentEndDelimiter().orElse(""));
		
		delimiters.commentStartDelimiter("");
		assertTrue(delimiters.commentStartDelimiter().isEmpty());
		
		delimiters.commentEndDelimiter("");
		assertTrue(delimiters.commentEndDelimiter().isEmpty());
		
		
	}
	
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
	void testIterateDelimiters() {
		String[] testStarts = {"(", "[", "<", "<span>"};
		String[] testEnds = {")", "]", ">", "</span>"};
		
		Delimiters delimiters = new Delimiters();
        		
		delimiters.add(testStarts[0], testEnds[0]);
		delimiters.add(testStarts[1], testEnds[1]);
		delimiters.add(testStarts[2], testEnds[2]);
		delimiters.add(testStarts[3], testEnds[3]);
        
		int i = 0;
        for (Delimiters.Delimiter delim: delimiters) {
        	assertEquals(testStarts[i], delim.start().orElse(""));
        	i++;
        }
        
	}

	

}
