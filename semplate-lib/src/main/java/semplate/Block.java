package semplate;

import static com.google.common.base.Preconditions.*;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import semplate.*;
import semplate.valuemap.*;

class Block {    String semanticBlockLine;
	StringBuffer text = new StringBuffer();
	
	ArrayList<FieldSpec> fieldSpecs = new ArrayList<>();
	
	Block() { super(); }
		
	/** A factory method to create a new block using the line containing the semantic block
	 * 
	 * @param semanticBlockLine  Contains the field specifications
	 * @return A block based on the field specifications in semanticBlockLine
	 */
    static Block semantics(String semanticBlockLine) { 
		Block block = new Block();
		checkArgument(semanticBlockLine != null, "Cannot construct a Block with a null parameter");
		//checkArgument(isSemanticBlock(semanticBlockLine), "Argument string \"%s\" does not form a semantic block.", semanticBlockLine); 

		Pattern fieldPattern = FieldSpec.pattern();
		Matcher fieldMatcher = fieldPattern.matcher(semanticBlockLine);  

		while (fieldMatcher.find()) {
			FieldSpec field = FieldSpec.of(fieldMatcher.group());
			block.fieldSpecs.add(field); 
		}

		return block; 
	}

	static Block empty() { return new Block();}
	
	boolean isEmpty() {  
		return fieldSpecs.isEmpty() && text.isEmpty();
	}
	
	Block appendText(CharSequence textValueLine) {
		text.append(textValueLine); 
		
		return this;
	}
	
	Block init() {
		text = new StringBuffer();;
		fieldSpecs.clear();
		
		return this;
	}
	
	ValueMap toValueMap() {

		
		ValueMap valueMap = new ValueMap();
		
		for (FieldSpec fieldSpec: fieldSpecs) {
			// Assemble a regex with matcher to find the first element specified by the field specification
			//String regex = ".*?";
			String regex = fieldSpec.delimiter().start().map(d -> Pattern.quote(d)).orElse("^");  // Quote the start delimiter 
			regex +=  "(?<value>.*?)";
            regex += fieldSpec.delimiter().end().map(d -> Pattern.quote(d)).orElse("$");  // Quote the start delimiter 
            
            Pattern pattern = Pattern.compile(regex);
            Matcher matcher = pattern.matcher(this.text.toString());
                       
            if (matcher.find()) {
			   String value = matcher.group("value");  // Just take the first one found
			   valueMap.put(fieldSpec.fieldName(), value);
            }
		}
		
		return valueMap;
		
	}
	
	Delimiter[] delimiters() {
		Delimiter[] delimiters = new Delimiter[fieldSpecs.size()];
		
		for (int i = 0; i < fieldSpecs.size(); i++) {
			delimiters[i] = fieldSpecs.get(i).delimiter();
		}
		return delimiters;
	}
	
		 
	@Override
	public String toString() {
		return "Block [semantics=" + semanticBlockLine + ", text=" + text + "]";
	} 
	
	
}



