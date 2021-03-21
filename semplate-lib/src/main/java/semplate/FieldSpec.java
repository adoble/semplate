package semplate;


import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.google.common.base.Preconditions.*;

class FieldSpec {
	final private static Pattern fieldPattern = Pattern.compile("\\{\\{(?<field>.*?):pattern=\"(?<start>.*?)%s(?<end>.*?)\"\\}\\}");
	
	
	private String fieldName = ""; 	
    private Delimiter delimiter = new Delimiter();
    
    static FieldSpec of(String fieldString) {
    	Matcher matcher = fieldPattern.matcher(fieldString);
    	
    	checkArgument(matcher.matches(), "Argument \"%s\"  does not match a field specification", fieldString);

    	FieldSpec field = new FieldSpec();
    	// Parse the field string. These are of the form {{fieldname:pattern="pattern"}}
    	field.fieldName = matcher.group("field");
    	
    	field.delimiter.start(matcher.group("start")).end(matcher.group("end"));
    	
    	return field;
    	
    }
    
    String fieldName() {
    	return fieldName;
    }
    
    Delimiter delimiter() {
    	return delimiter;
    }
    
    static Pattern pattern() {
    	return fieldPattern;
    }

	@Override
	public String toString() {
		return "FieldSpec [fieldName=" + fieldName + ", delimiter=" + delimiter + "]";
	}
}
