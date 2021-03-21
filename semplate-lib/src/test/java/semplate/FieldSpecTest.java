package semplate;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class FieldTest {
	
	String[] testStrings = {
							"{{source:pattern=\"[%s]\"}}",
							"{{sourceLink:pattern=\"(%s)\"}}",
							"{{text:pattern=\"Preamble %s postamble\"}}"	
						   };

	@Test
	void test() {

		Field field = Field.of(testStrings[0]);
		assertEquals("source", field.fieldName());
		assertEquals("[", field.delimiter().start().orElse(""));
		assertEquals("]", field.delimiter().end().orElse(""));

		field = Field.of(testStrings[1]);
		assertEquals("sourceLink", field.fieldName());
		assertEquals("(", field.delimiter().start().orElse(""));
		assertEquals(")", field.delimiter().end().orElse(""));

		field = Field.of(testStrings[2]);
		assertEquals("text", field.fieldName());
		assertEquals("Preamble ", field.delimiter().start().orElse(""));
		assertEquals(" postamble", field.delimiter().end().orElse(""));

	}
	
	@Test
	void testFieldWithoutPattern() {
		assertThrows(IllegalArgumentException.class, () -> Field.of("{{fieldname}}"));
	}
	
	@Test
	void testMalformedField( ) {
		assertThrows(IllegalArgumentException.class, () -> Field.of("{{kj koj dvd"));
		
	}

}
