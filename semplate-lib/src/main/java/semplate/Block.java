package semplate;

import static com.google.common.base.Preconditions.*;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import semplate.*;
import semplate.valuemap.*;

class Block {    StringBuffer text = new StringBuffer();
	
	ArrayList<FieldSpec> fieldSpecs = new ArrayList<>();
	
	boolean terminated = true;
	
	Block() { 
		super(); 
	}
		
	/** 
	 * 
	 * @param semanticBlockLine  Contains the field specifications
	 * @return A block based on the field specifications in semanticBlockLine
	 */
    Block initialise(String semanticBlockLine) { 
		checkArgument(semanticBlockLine != null, "Cannot construct a Block with a null parameter");
		checkState(terminated);
		
		terminated = false;
		text = new StringBuffer();
		fieldSpecs.clear();
		
		Pattern fieldPattern = FieldSpec.pattern();
		Matcher fieldMatcher = fieldPattern.matcher(semanticBlockLine);  

		while (fieldMatcher.find()) {
			FieldSpec field = FieldSpec.of(fieldMatcher.group());
			this.fieldSpecs.add(field); 
		}

		return this; 
	}

	static Block empty() { return new Block();}
	
	boolean isEmpty() {  
		return fieldSpecs.isEmpty() && (text.length() == 0);
	}
	
	Block appendText(CharSequence textValueLine) {
		checkState(!terminated);
		
		text.append(textValueLine); 
		
		return this;
	}
	
	static Function <String, Block> block() {
		Block block = new Block(); 
		return line -> { 
			             if (line.isBlank()) { block.terminate(); return block;}  
		                 //else if (line.contains("{{") && line.contains("}}")) {block.semantics(line); return block;}
		                 else if (line.contains("{{") && line.contains("}}")) {block.initialise(line); return block;}
                         else if (!block.isTerminated()) {block.appendText(line); return Block.empty();}
                         else {return Block.empty();}  // Line has text that does not have an associated semantic block
		               };
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
	
	Block terminate() {
		terminated = true;

		return this;
	}
	
	boolean isTerminated() {
		return terminated;
	}
		 
	@Override
	public String toString() {
		return "Block [fieldSpecs=" + fieldSpecs + ", text=" + text + "]";
	}


	
	
}



