package semplate;

import java.util.regex.Pattern;

final class Patterns {
	//TODO remove this and use the pattern instead
	final static String TEMPLATE_COMMENT_FIELD = "{@template.comment}}"; 
	
	final static Pattern COMMENT_DIRECTIVE_PATTERN = Pattern.compile("\\{@template.comment\\}\\}");

	final static Pattern DELIMITER_DIRECTIVE_PATTERN = Pattern.compile("\\{@template.delimiter.(?<type>.*?):(?<delim>.*?)\\}\\}");

	final static Pattern FIELD_PATTERN = Pattern.compile("\\{{2}(?<fieldname>[^\\}]*)\\}{2}");  

}
