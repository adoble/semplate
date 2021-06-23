package semplate;

import static org.junit.jupiter.api.Assertions.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.is;


import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.jupiter.api.Test;

class TestPatterns {

	
	@Test
	void testDelimiterDirectivePatternWhenCorrect() {
		Pattern pattern = Patterns.DELIMITER_DIRECTIVE_PATTERN;


		String testStrings[] = {
				"<--{@template.delimiter.start:\"xx\"}}-->",
				"<--{@template.delimiter.end:\"xx\"}}-->",
				"<--{@template.delimiter.pair:\"xx\"}}-->",
				"//{@template.delimiter.start:\"xx\"}}-->",
				"//{@template.delimiter.end:\"xx\"}}-->",
				"//{@template.delimiter.pair:\"xx\"}}-->",
		};

		for (String s: testStrings) {
			Matcher matcher = pattern.matcher(s);
			assertTrue(matcher.find());
			assertEquals("\"xx\"", matcher.group("delim"));
			assertThat(matcher.group("type"), anyOf(is("start"), is("end"), is("pair")));
		}

	}

	@Test
	void testDelimiterDirectivePatternWhenInorrect() {
		Pattern pattern = Patterns.DELIMITER_DIRECTIVE_PATTERN;


		String testStrings[] = {
				"<--{@-->",
				"<--{{fieldname}}-->",
				"<--{{template.delimiter.pair:\"xx\"}}-->",
		};

		for (String s: testStrings) {
			Matcher matcher = pattern.matcher(s);
			assertFalse(matcher.find());
		}

	}

	@Test 
	void testFieldPatternWhenCorrect() {
		Pattern pattern = Patterns.FIELD_PATTERN;

		String testStrings[] = {
				"<--{{fieldname0}}-->",
				"<--{{fieldname1}}-->",
				"//{{fieldname2}}"
		};

		for (int i = 0 ; i < testStrings.length; i++) {
			Matcher matcher = pattern.matcher(testStrings[i]);
			assertTrue(matcher.find());
			assertEquals("fieldname" + i , matcher.group("fieldname"));
		}

	}
	
	@Test 
	void testFieldPatternWhenIncorrect() {
		Pattern pattern = Patterns.FIELD_PATTERN;

		String testStrings[] = {
				"<--{{fieldname0}-->",
				"<--{@fieldname1}}-->",
				"//{fieldname2}"
		};

		for (String s : testStrings) {
			Matcher matcher = pattern.matcher(s);
			assertFalse(matcher.find());
		}

	}
	
	@Test
	void CommentPatternDirectiveWhenCorrect() {
		Pattern pattern = Patterns.COMMENT_DIRECTIVE_PATTERN;

		String testStrings[] = {
				"<--{@template.comment}}-->",
				"//{@template.comment}}-->",
		};
		
		for (String s : testStrings) {
			Matcher matcher = pattern.matcher(s);
			assertTrue(matcher.find());
		}

		
	}
	
	@Test
	void CommentPatternDirectiveWhenIncorrect() {
		Pattern pattern = Patterns.COMMENT_DIRECTIVE_PATTERN;

		String testStrings[] = {
				"<--{@template.comment}-->",
				"<--template.comment-->",
				"//{{template.comment}}-->",
				"//{template.comment}-->",
				"//{{{template.comment}}}-->",
				
		};
		
		for (String s : testStrings) {
			Matcher matcher = pattern.matcher(s);
			assertFalse(matcher.find(), "String \"" + s + "\" incorrectly matched!");
		}

		
	}
	

}
