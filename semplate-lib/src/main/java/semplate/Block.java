package semplate;

import static com.google.common.base.Preconditions.*;

import java.util.ArrayList;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import semplate.valuemap.*;

class Block {    StringBuffer text = new StringBuffer();
	
	ArrayList<FieldSpec> fieldSpecs = new ArrayList<>();
	
	boolean terminated = true;
	
	Block() { 
		super(); 
	}
		
	/** 
	 * Initialise the block using the semantic block line in the markdown 
	 * 
	 * Preconditions: 
	 * - Parameter <code<semanticBlockLine</code> cannot be null
	 * - The block must be in a terminated state
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

	/**
	 * Create an empty (no text and semantic information block) 
	 * 
	 * @return An empty block. 
	 */
    static Block empty() { return new Block();}
	
	/**
	 * True if the block is empty, i.e. contains no semantic information and text 
	 *
	 * @return True if empty
	 */
    boolean isEmpty() {  
		return fieldSpecs.isEmpty() && (text.length() == 0);
	}
	
    /**
     * Appends markdown text to this block.
     * 
     * Precondition:
     * - The block is not terminated.
     * 
     * @param textValueLine The markdown line to be appended
     * @return This block
     */
	Block appendText(CharSequence textValueLine) {
		checkState(!terminated);
		
		text.append(textValueLine); 
		
		return this;
	}
	
	/**
	 * A function used in reading the markdown to kept track of the block state. 
	 * 
	 * @return A terminated block or an empty block. 
	 */
	static Function <String, Block> block() {
		Block block = new Block(); 
		return line -> { 
			             if (line.isBlank()) { block.terminate(); return block;}  
		                 else if (line.contains("{{") && line.contains("}}")) {block.initialise(line); return block;}
                         else if (!block.isTerminated()) {block.appendText(line); return Block.empty();}
                         else {return Block.empty();}  // Line has text that does not have an associated semantic block
		               };
	}
	
	
	/**
	 * Converts the information in this block to a value map
	 * 
	 * @return A value map representing the semantic information in this block.
	 */
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
	
	/**
	 * Returns an array of delimiter objects representing the delimiters specified in this block. 
	 * 
	 * @return Array of delimiters
	 */
	Delimiter[] delimiters() {
		Delimiter[] delimiters = new Delimiter[fieldSpecs.size()];
		
		for (int i = 0; i < fieldSpecs.size(); i++) {
			delimiters[i] = fieldSpecs.get(i).delimiter();
		}
		return delimiters;
	}
	
	/**
	 * Signals that the block is terminated, i.e. the semantic information and text for this 
	 * block has been fully entered.
	 * @return This block
	 */
	Block terminate() {
		terminated = true;

		return this;
	}
	
	/**
	 * Has the block been terminated, i.e. has the semantic information and text for this 
	 * block has been fully entered.
	 * 
	 * @return True if terminated 
	 */
	boolean isTerminated() {
		return terminated;
	}
		 
	
	@Override
	public String toString() {
		return "Block [fieldSpecs=" + fieldSpecs + ", text=" + text + "]";
	}


	
	
}



