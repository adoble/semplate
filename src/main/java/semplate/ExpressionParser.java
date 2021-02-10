package semplate;

import java.util.*;

public enum ExpressionParser {
	
	FieldName {
		 @Override 
		 public ExpressionParser process(char c) {
			 if ( c != '.' && c != '=' && c != '[')  {
	         	collector.append(c);
	         	return FieldName;
	         } else {
	         	collectionMap.put("fieldName", collector.toString());
	         	collector.setLength(0);
	         	if (c == '.') return SubFieldname;
	         	else if (c == '[') return Index;
	         	else return ValueStart;  // c == '='
	         } 
		 }
	}, 
	SubFieldname {
		 @Override 
		 public ExpressionParser process(char c) {
			 if (c != '=' && c != '[') {
				 collector.append(c);
				 return SubFieldname;
			 }
             else {
             	collectionMap.put("subFieldName", collector.toString());
	            collector.setLength(0);
             	if ( c == '[') return Index;
             	else return ValueStart;
             }
			 
		 }
	}, 
	Index{
		 @Override 
		 public ExpressionParser process(char c) {
			 if (c != ']') {
				 collector.append(c);
				 return Index;
			 }
             else {
            	 collectionMap.put("index", collector.toString());
            	 collector.setLength(0);
            	 return ValueStart;
             }
		 }
	}, 
	ValueStart {
		@Override 
		 public ExpressionParser process(char c) {
			if (c == '"') return Value;
			else return ValueStart;
		}
	},
	Value {
		 @Override 
		 public ExpressionParser process(char c) {
			 if (c != '"') {
				 collector.append(c);  //Remove quotes
				 return Value;
			 } else {
				 collectionMap.put("value", collectionMap.getOrDefault("value", "") + collector.toString());
				 collector.setLength(0);
				 return Terminator;
			 }
		 }
	},
	Terminator {
		@Override 
		public ExpressionParser process(char c) {
			if (c != '"') {
				// The previous quote symbol was part of the value so add that and the new char
				//collector.append('"').append(c); 
				collectionMap.put("value", collectionMap.getOrDefault("value", "") + '"' + c);
				return Value;
			} else {
				collectionMap.put("value", collectionMap.getOrDefault("value", "")  + c);
				return Terminator; 
			}
		}

	};
	
//	private static Map<String, String> collectionMap = new HashMap<>();
//	private static  StringBuffer collector = new StringBuffer();
	private static Map<String, String> collectionMap;
	private static  StringBuffer collector;
	
	public abstract ExpressionParser process(char c);
	
	
	public static Map<String, String> parse(String expression) {
		ExpressionParser parser = ExpressionParser.FieldName;
		collectionMap = new HashMap<>(); 
		collector = new StringBuffer();
		
		for(int i = 0; i < expression.length(); i++) {
			parser = parser.process(expression.charAt(i));
		}
		return collectionMap;
		
	}

}
