package semplate;

import java.util.*;
import static com.google.common.base.Preconditions.*;


import semplate.valuemap.*;

public class Block {
	String semantics;
	StringBuffer text;
	
	public Block() {
		semantics = "";
		text = new StringBuffer();
		
	}
	
	private Block(String semanticBlockLine) {
		super();
		
		checkArgument(semanticBlockLine != null, "Cannot construct a Block with a null parameter");
		semantics = semanticBlockLine;
	}
	
	
	public  Block semantics(String sematicBlockLine) { return new Block(sematicBlockLine); }

	public static Block empty() { return new Block();}
	
	public boolean isEmpty() {  
		return semantics.isEmpty() && text.isEmpty();
	}
	
	public Block appendTextValue(CharSequence textValueLine) {
		//text.append(textValueLine).append("\n");
		//text = text.flatMap(s -> s.append(textValueLine).append("\n")); 
		text.append(textValueLine).append("\n"); 
		
		return this;
	}
	
	public Block init() {
		semantics = "";
		text = new StringBuffer();;
		return this;
	}
	
	public ValueMap toValueMap() {
		
		ValueMap valueMap = new ValueMap();
		valueMap.put("author", "Plato");
		valueMap.put("title", "Test Title");
		
		return valueMap;
		
	}
	
		 
	public static boolean isSemanticBlock(String line) { return line.contains("{@") && line.contains("}}"); }

	@Override
	public String toString() {
		return "Block [semantics=" + semantics + ", text=" + text + "]";
	} 
	
	
}



