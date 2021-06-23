package semplate;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import semplate.valuemap.ValueMap;

class BlockTest {

	@BeforeEach
	void setUp() throws Exception {
	}

	@AfterEach
	void tearDown() throws Exception {
	}

	@Test
	void testInitialise() {
		String semanticBlockLine = "<!--{{source:pattern=\"[%s]\"}}{{sourceLink:pattern=\"(%s)\"}}-->";
		Delimiter[] expected = {
				                   new Delimiter().start("[").end("]"),
                 				   new Delimiter().start("(").end(")"),
		                       };

		Block block = new Block().initialise(semanticBlockLine);

		Delimiter[] results = block.delimiters();
		
		assertArrayEquals(expected, results);

	}
	
	@Test
	void testTerminate() {
		Block block = new Block(); 
		
		assertTrue(block.isTerminated());
		
		block.initialise("<!--{{source:pattern=\"[%s]\"}}");
		assertFalse(block.isTerminated());
		
		block.appendText("The [first] line.");
		block.appendText("A second line.");
		
		assertFalse(block.isTerminated());
		block.terminate();
		
		assertTrue(block.isTerminated());
		
		assertThrows(IllegalStateException.class, () -> block.appendText("A line after termination"));
		
	}

	@Test
	void testEmpty() {
		Block block = Block.empty();
		
		assertNotNull(block);
		assertTrue(block.isEmpty());
	}

	
	@Test
	void testToValueMap() {
		String semanticBlockLine = "<!--{{field1:pattern=\"[%s]\"}}{{field2:pattern=\"(%s)\"}}-->";
		Block block = new Block().initialise(semanticBlockLine);
		block.appendText("The [first] line.");
		block.appendText("A second line.");
		block.appendText("This will be the (third line).");
		
		ValueMap valueMap = block.toValueMap();
		
		assertEquals("first", valueMap.getValue("field1").orElse(""));
		assertEquals("third line", valueMap.getValue("field2").orElse(""));
		
	}

	
}
