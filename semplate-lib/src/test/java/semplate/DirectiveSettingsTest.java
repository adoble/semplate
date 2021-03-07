package semplate;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class DirectiveSettingsTest {

	@Test
	void testCommentDelimiters() {
		DirectiveSettings settings = new DirectiveSettings();
		
		settings.commentStartDelimiter("[");
		assertEquals("[", settings.commentStartDelimiter().orElse(""));
		
		settings.commentEndDelimiter(")");
		assertEquals(")", settings.commentEndDelimiter().orElse(""));
		
		settings.commentStartDelimiter("<!--");
		assertEquals("<!--", settings.commentStartDelimiter().orElse(""));
		
		settings.commentEndDelimiter("-->");
		assertEquals("-->", settings.commentEndDelimiter().orElse(""));
		
		settings.commentStartDelimiter("");
		assertTrue(settings.commentStartDelimiter().isEmpty());
		
		settings.commentEndDelimiter("");
		assertTrue(settings.commentEndDelimiter().isEmpty());
		
		
	}
	
	@Test
	void testDelimiters() { 
        DirectiveSettings settings = new DirectiveSettings();
		
		settings.startDelimiter("[");
		assertEquals("[", settings.startDelimiter().orElse(""));
		
		settings.endDelimiter("]");
		assertEquals("]", settings.endDelimiter().orElse(""));
		
		settings.startDelimiter("");
		assertTrue(settings.startDelimiter().isEmpty());
		
		settings.endDelimiter("");
		assertTrue(settings.endDelimiter().isEmpty());
		
	}
	
	@Test
	void testDelimiterPair() {
		DirectiveSettings settings = new DirectiveSettings();

		settings.delimiterPair("()");
		assertEquals("(", settings.startDelimiter().orElse(""));
		assertEquals(")", settings.endDelimiter().orElse(""));

		assertThrows(IllegalArgumentException.class, () -> settings.delimiterPair(""));
		assertThrows(IllegalArgumentException.class, () -> settings.delimiterPair("["));
		assertThrows(IllegalArgumentException.class, () -> settings.delimiterPair("<!>"));
	}

	

}
