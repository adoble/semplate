package semplate;

import static org.junit.jupiter.api.Assertions.*;

import java.util.*;

import org.junit.jupiter.api.Test;

import semplate.ExpressionParser;

class ExpressionParserTest {

	@Test
	void testSimpleFieldValuePair() {
					
		Map<String, String> values = ExpressionParser.parse("author=\"Plato\"");
		
		assertTrue(values.containsKey("fieldName"));
		assertEquals("author", values.get("fieldName"));
		assertTrue(values.containsKey("value"));
		assertEquals("Plato", values.get("value"));
		
		assertFalse(values.containsKey("subFieldName"));
		assertFalse(values.containsKey("index"));
		
		
		
	
	}
	
	@Test 
	void testAmbiguousFieldValuePair() {
				
		String expression = "title=\"The value of being \"Ernest\"\"";
		
        Map<String, String> values = ExpressionParser.parse(expression);
		
		assertTrue(values.containsKey("fieldName"));
		assertEquals("title", values.get("fieldName"));
		assertTrue(values.containsKey("value"));
		assertEquals("The value of being \"Ernest\"", values.get("value"));
		
		assertFalse(values.containsKey("subFieldName"));
		assertFalse(values.containsKey("index"));
		
	}
	
	@Test 
	void testListValues() {
		String expression = "reference.title[1]=\"The Works of Plato\"";
		Map<String, String> values = ExpressionParser.parse(expression);

		assertTrue(values.containsKey("fieldName"));
		assertEquals("reference", values.get("fieldName"));
		
		assertTrue(values.containsKey("subFieldName"));
		assertEquals("title", values.get("subFieldName"));
		
		assertTrue(values.containsKey("index"));
		assertEquals("1", values.get("index"));
		
		assertTrue(values.containsKey("value"));
		assertEquals("The Works of Plato", values.get("value"));
		
	}
	
	@Test void testAmbiguousListValues() {
		
		String expression = "reference.title[42]=\"The Works of \"Anonymous\" - see Ref[34]\"";
		Map<String, String> values = ExpressionParser.parse(expression);

		assertTrue(values.containsKey("fieldName"));
		assertEquals("reference", values.get("fieldName"));
		
		assertTrue(values.containsKey("subFieldName"));
		assertEquals("title", values.get("subFieldName"));
		
		assertTrue(values.containsKey("index"));
		assertEquals("42", values.get("index"));
		
		assertTrue(values.containsKey("value"));
		assertEquals("The Works of \"Anonymous\" - see Ref[34]", values.get("value"));
	}



}
