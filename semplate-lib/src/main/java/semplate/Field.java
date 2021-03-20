package semplate;

import java.util.*;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.base.*;
import static com.google.common.base.Preconditions.*;

class Field {
	//final private static String fieldPatternSpec = "\\{{2}[^\\}]*\\}{2}";
	
	final private static Pattern fieldPattern = Pattern.compile("\\{\\{(?<field>.*?):pattern=\"(?<start>.*?)%s(?<end>.*?)\"\\}\\}");
	
	
	private String fieldName; 	
    private Delimiter delimiter;
    
    static Field of(String fieldString) {
    	Matcher matcher = fieldPattern.matcher(fieldString);
    	
    	checkArgument(matcher.matches(), "Argument \"%s\"  does not match a field specification", fieldString);

    	Field field = new Field();
    	// Parse the field string. These are of the form {{fieldname:pattern="pattern"}}
    	field.fieldName = matcher.group("name");
    	
    	field.delimiter.start(matcher.group("start")).end(matcher.group("end"));
    	
    	return field;
    	
    }
    
    String fieldName() {
    	return fieldName;
    }
    
    Delimiter delimiter() {
    	return delimiter();
    }
}
