package semplate;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;

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
	void testSemantics() {
		String semanticBlockLine = "<!--{{source:pattern=\"[%s]\"}}{{sourceLink:pattern=\"(%s)\"}}-->";
		Delimiter[] expected = {
				                   new Delimiter().start("[").end("]"),
                 				   new Delimiter().start("(").end(")"),
		                       };

		Block block = Block.semantics(semanticBlockLine);

		Delimiter[] results = block.delimiters();
		
		assertArrayEquals(expected, results);

	}

	@Test
	void testEmpty() {
		Block block = Block.empty();
		
		assertNotNull(block);
		assertTrue(block.isEmpty());
	}

	
	@Test
	void testInit() {
		String semanticBlockLine = "<!--{{source:pattern=\"[%s]\"}}{{sourceLink:pattern=\"(%s)\"}}-->";
		Block block = Block.semantics(semanticBlockLine);
		block.appendText("The first line.");
		block.appendText("A second line.");
		block.appendText("This will be the third line.");
		
		assertFalse(block.isEmpty());
		
		block.init();
		
		assertTrue(block.isEmpty());
	}

	@Test
	void testToValueMap() {
		String semanticBlockLine = "<!--{{field1:pattern=\"[%s]\"}}{{field2:pattern=\"(%s)\"}}-->";
		Block block = Block.semantics(semanticBlockLine);
		block.appendText("The [first] line.");
		block.appendText("A second line.");
		block.appendText("This will be the (third line).");
		
		ValueMap valueMap = block.toValueMap();
		
		System.out.println(valueMap);
		
		assertEquals("first", valueMap.getValue("field1").orElse(""));
		assertEquals("third line", valueMap.getValue("field2").orElse(""));
		
	}

	
}
