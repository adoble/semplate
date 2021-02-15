/**
 * TODO
 */
package semplate;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.*;

import semplate.annotations.Templatable;
import semplate.annotations.TemplateField;
import semplate.valuemap.ValueMap;

import java.lang.reflect.*;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * TODO
 * @author Andrew
 *
 */
public class Template  {
	// Special fields are preceded with template
	final private String templateCommentField = "{{template.comment}}"; //TODO make static
	
	final private static Pattern fieldPattern = Pattern.compile("\\{{2}[^\\}]*\\}{2}"); 
	final private static Pattern listPattern = Pattern.compile("\\{{3}[^\\}]*\\}{3}"); 
	
	private static enum ParseTokens {fieldname, subFieldname, index, value};
	
	Map<ParseTokens, Optional<CharSequence>> collectionMap = new HashMap<>();
	
	private Path templatePath;

	private String string;
	Optional<String> commentStartDelimiter;
	Optional<String> commentEndDelimiter;
	
	
	
	
	/**
	 * TODO
	 * @param templatePath
	 */
	public Template() {
		super();  //TODO required?

		collectionMap.put(ParseTokens.fieldname, Optional.empty());
		collectionMap.put(ParseTokens.subFieldname, Optional.empty());
		collectionMap.put(ParseTokens.index, Optional.empty());
		collectionMap.put(ParseTokens.value, Optional.empty());
	} 

	
	/**
	 * TODO
	 * @param templatePath
	 */
	public void config(Path templatePath) throws IOException {
		this.templatePath = templatePath;

		try (Stream<String> stream = Files.lines(templatePath, Charset.defaultCharset())) {
			parseTemplateStream(stream);
		}
		

		
	}
	
	
	/**
	 * TODO
	 * @param templateStream
	 */
	public void config(InputStream templateStream) throws IOException { 
		//TODO
        

		BufferedReader reader = new BufferedReader(new InputStreamReader(templateStream));
		try (Stream<String> stream = reader.lines()) {
			parseTemplateStream(stream);
		}

	}
	
	/**
	 * Generates a markdown file as specified by the template file using the information
	 * in the node object
	 * @param dataObject An object annotated with template field information
	 * @param outputFilePath
	 */
	public void generate(Object dataObject, Path outputFilePath) throws IOException {
		
		ValueMap fieldValueMap = buildFieldValueMap(dataObject);
			    
		try (Stream<String> stream= Files.lines(templatePath, Charset.defaultCharset())) {
            List<String> replacements = stream
            		.flatMap(line -> templateExpand(line, fieldValueMap))  // Expand any lists
            		.map(line -> templateReplace(line, fieldValueMap))     // Replace the fields and add meta data
            		.collect(Collectors.toList());
            
            replacements.forEach(s -> System.out.println("--->" + s));
            
            //System.out.println(String.join("\n", replacements));
            Files.write(outputFilePath, replacements);
	    }
	    
	    

	}
	
	/**
	 * Updates a markdown files using the annotated fields in the object .
	 * 
	 * @param object
	 * @param outputFilePath
	 */
	public void update(Object object, Path outputFilePath) {
		//TODO 
	}


	/**
	 * Reads the specified markup file and creates a new  object of class nodeClass that contains the
	 * data semantically represented in the file.
	 * 
	 * @param objectClass
	 * @param markupFilePath
	 * @return
	 */
	public Object read(Class<?> objectClass, Path markupFilePath) throws ReadException {
		Object dataObject;
		
		ValueMap valueMap = readValueMap(markupFilePath); 
		
		System.out.println("ValueMap:\n" + valueMap);
		
		dataObject = constructDataObject(objectClass, valueMap);
		
			
		System.out.println(valueMap);
		
		
		return dataObject;
	}


	private ValueMap readValueMap(Path markupFilePath) throws ReadException {
		ValueMap valueMap; 
		try (Stream<String> stream= Files.lines(markupFilePath, Charset.defaultCharset())) {
			 valueMap = stream.filter(fieldPattern.asPredicate())  // Only process lines that contain a field
			      .flatMap(line -> extractKeyValuePair(line))   // Now extract the key value pairs as Strings
			      .filter(s -> !s.contains("template.comment"))  // Filter out the template.comment directive TODO change
			      .map(nameValuePair -> constructValueMap(nameValuePair))
			      //.collect(ArrayList::new, (r,s) -> accumulateList(r,s), (r, s) -> combineList(r,s));
			      //.collect(ArrayList<String>::new, (r,s) -> accumulate(r,s), (r1, r2) -> combine(r1,r2));
			      .collect(ValueMap::new, ValueMap::merge, ValueMap::merge);
		      //.collect(objectClass.getDeclaredConstructor().newInstance(), 
		     	 //		  (object, s) -> objectBuilder(object, s), 
		     	 //		  (object, s) -> objectBuilder(object, object));
		
		} catch (IOException e) {
			throw new ReadException(e);
		}
		return valueMap;
	}
	
	private Object constructDataObject(Class<?> objectClass, ValueMap valueMap) throws ReadException {
		Object dataObject;
		
		//TODO what happens if the specified objectClass does not have a constructor with no parameters?
		try {
			dataObject = objectClass.getDeclaredConstructor().newInstance();
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
				| NoSuchMethodException | SecurityException e) {
			// TODO Auto-generated catch block
			throw new ReadException();
		}
		
		
		
		System.out.println("VALUEMAP:\n" + valueMap);
		
		// Using the entries in the value map find the corresponding fields and set them.
		// TODO simple case first
		Set<String> fieldNames = valueMap.fieldNames();
		
		for (String fieldName : fieldNames) {
			// Get the value
			if (!valueMap.isValueMap(fieldName)) {
				Optional<Object> fieldValue = valueMap.getValue(fieldName);
				System.out.println("   " + fieldName + ":==" + fieldValue.orElse("EMPTY"));
				
				Field field;
				try {
					field = dataObject.getClass().getDeclaredField(fieldName);
					if (field.getAnnotation(TemplateField.class) != null ) {
						setField(dataObject, field, fieldValue);
							
					} else {
						// If the field has not been annotated then silently ignore
					}
					
					//TODO check annotation
				} catch (NoSuchFieldException | SecurityException e) {
					// TODO Auto-generated catch block
					System.out.println("FIELD: fieldname " +  fieldName + " is unknown. " + e);
				
				}
				
			} else {
               //TODO --> value is a value map
			}
		}
		
	
		
		return dataObject;
	}


	private void setField(Object dataObject, Field field, Optional<Object> fieldValue) {

		if (fieldValue.isEmpty()) return;
		String valStr = fieldValue.orElseThrow().toString();

		System.out.println(field.getType());
		Class<?> fieldType = field.getType();

		field.setAccessible(true);

		try {
			
			if (fieldType.equals(String.class)) {
				field.set(dataObject, valStr); 
			} else if (fieldType.equals(Integer.TYPE)) {
				int val = Integer.parseInt(valStr);
				field.setInt(dataObject, val);
			} else if (fieldType.equals(Integer.class)) {
				Integer val = Integer.parseInt(valStr);
				field.set(dataObject, val);
			} else if (fieldType.equals(Short.TYPE)) {
				short val = Short.parseShort(valStr);
				field.setShort(dataObject, val);
			} else if (fieldType.equals(Short.class)) {
				Short val = Short.parseShort(valStr);
				field.set(dataObject, val);
			} else if (fieldType.equals(Byte.TYPE)) {
				byte val = Byte.parseByte(valStr);
				field.setByte(dataObject, val);
			} else if (fieldType.equals(Byte.class)) {
				Byte val = Byte.parseByte(valStr);
				field.set(dataObject, val);
			} else if (fieldType.equals(Long.TYPE)) { 
				long val = Long.parseLong(valStr);
				field.setLong(dataObject, val);
			} else if (fieldType.equals(Long.class)) { 
				Long val = Long.parseLong(valStr);
				field.set(dataObject, val);
			} else if (fieldType.equals(Double.TYPE)) { 
				double val = Double.parseDouble(valStr);
				field.setDouble(dataObject, val);
			} else if (fieldType.equals(Double.class)) { 
				Double val = Double.parseDouble(valStr);
				field.set(dataObject, val);
			} else if (fieldType.equals(Float.TYPE)) { 
				float val = Float.parseFloat(valStr);
				field.setFloat(dataObject, val);
			} else if (fieldType.equals(Float.class)) { 
				Float val = Float.parseFloat(valStr);
				field.set(dataObject, val);
			} else if (fieldType.equals(Boolean.TYPE)) { 
				boolean val = Boolean.parseBoolean(valStr);
				field.setBoolean(dataObject, val);
			} else if (fieldType.equals(Boolean.class)) { 
				Boolean val = Boolean.parseBoolean(valStr);
				field.set(dataObject, val);
			} else if (fieldType.equals(Character.TYPE) ) { 
				char val = valStr.charAt(0);
				field.setChar(dataObject, val);
			} else if (fieldType.equals(Character.class)) {
				Character val = Character.valueOf(valStr.charAt(0));
				field.set(dataObject, val);
			} else if (fieldType.equals(String.class)) {
				System.out.println(valStr);
				field.set(dataObject, valStr);
			} 
			// Dates are formatted according to ISO_LOCAL_DATE or ISO_LOCAL_DATE_TIME. 
			  else if (fieldType.equals(LocalDate.class)) {
				LocalDate val = LocalDate.parse(valStr);
				field.set(dataObject, val);
			} else if (fieldType.equals(LocalDateTime.class)) {
				LocalDateTime val = LocalDateTime.parse(valStr);
				field.set(dataObject, val);
			} else if (fieldType.equals(ZonedDateTime.class)) {
				ZonedDateTime val = ZonedDateTime.parse(valStr);
				field.set(dataObject, val);
			}
			// Use standard string representation of URLs
			else if (fieldType.equals(URL.class)) {
				URL val = new URL(valStr);
				field.set(dataObject, val);
			} else {
				System.err.println("Type of " + field.getName() + " is unknown");
			}
		} catch (IllegalArgumentException | IllegalAccessException | MalformedURLException e) {
			System.err.println("ERROR: Cannot set field " + field.getName());
			System.err.println(e.getMessage());
		}

	}


	private ValueMap constructValueMap(String nameValuePair) {
		ValueMap valueMap = new ValueMap();
		
		Map<String, String> parsedNameValuePair = ExpressionParser.parse(nameValuePair);
	
		if (parsedNameValuePair.containsKey("subFieldName") && parsedNameValuePair.containsKey("index")) {
			// List reference pair of the form:
			//   fieldname.subFieldName[index]="value"
			if (parsedNameValuePair.containsKey("fieldName") && parsedNameValuePair.containsKey("value")) {
			  ValueMap subValueMap = new ValueMap();
              subValueMap.put(parsedNameValuePair.get("subFieldName"), parsedNameValuePair.get("value"));
              valueMap.add(parsedNameValuePair.get("fieldName"), subValueMap);
			} else {
				valueMap.put("ERROR:" , "Meta data incorrect:(" + nameValuePair + ")");
			}
		} else {
			//Simple value pair of the form:
			//   fieldName="value"
			if (parsedNameValuePair.containsKey("fieldName") && parsedNameValuePair.containsKey("value"))
			 valueMap.put(parsedNameValuePair.get("fieldName"), parsedNameValuePair.get("value"));
			else {
				valueMap.put("ERROR:" , "Meta data incorrect:(" + nameValuePair + ")");
			}
		}
		
		System.out.print("C::" + nameValuePair + "----->");
		System.out.println(valueMap);
		
		
		 
	    return valueMap;  
	
	}
	
	
	// TODO make this secure if the user has modified the file. 
	private ValueMap constructValueMapTODELETE(String nameValuePair)  {
		Optional<String> fieldName = Optional.empty();
		Optional<String> subFieldName = Optional.empty();
		Optional<String> valueString = Optional.empty();
		Optional<Integer> index = Optional.empty();

		//Parse the string
		String[] parts = nameValuePair.split("[.]|=|\\[|\\]");

//		System.out.print("parts:");
//		for (String part: parts) {
//			System.out.print(part);
//			System.out.print(", ");
//			}
//		System.out.println();
		
		System.out.println("Input -->" + nameValuePair);
		
		 

		
		if (parts.length == 2) {
			fieldName = Optional.of(parts[0]);
			valueString = Optional.of(parts[1].replace("\"",  ""));  // Removing quotes);
		}
		if (parts.length == 5) {
			fieldName = Optional.of(parts[0]);
			subFieldName = Optional.of(parts[1]);
			index = Optional.of(Integer.parseInt(parts[2]));
			valueString = Optional.of(parts[4].replace("\"",  ""));  // Removing quotes
		} 
		
	    StringBuffer sb = new StringBuffer();
	    fieldName.ifPresent(s -> sb.append(s));
	    subFieldName.ifPresent(s -> sb.append('.').append(s));
	    index.ifPresent(s -> sb.append('[').append(s).append(']'));
	    valueString.ifPresent(s -> sb.append('=').append(s));
	    
	    
	    
	    System.out.println(sb);
	    
		

		// Now split the fieldname if it has a list qualifier (e.g. [2])
//		parts = fieldName.split("\\[|\\]");
//		if (parts.length > 1) {
//			fieldName = parts[0];
//			index = Integer.parseInt(parts[1]);
//		}

		ValueMap vm = new ValueMap(); 
//		if (index == null) {
//			vm.put(fieldName, valueString);
//		} else {
//			ValueMap vmList = new ValueMap;
//			vmList.add
//			vm.add(fieldName, valueString)
//		}	


		return vm;


	}
	
	private void accumulateList(ArrayList<String> r, String s) {
		System.out.println("ACCUMULATE: " + s);
		r.add(s);
	
	}
	
	private void combineList(ArrayList<String> r1, ArrayList<String> r2) {
		System.out.println("COMBINE");
		r1.addAll(r2);
	}


	private Stream<String> extractKeyValuePair(String line) {
		List<String> keyValuePairs = new ArrayList<String>();
		String keyValuePair = "";
		
		Matcher matcher = fieldPattern.matcher(line);
		while(matcher.find()) {
			// Remove the delimiters
			keyValuePair = matcher.group().replaceAll("\\{|\\}", "");  // Removed the field delimiters
			keyValuePairs.add(keyValuePair);
		}
	
		return keyValuePairs.stream();
	
	}



	public Path getTemplatePath() {
		
		return templatePath;
	}


	public Optional<String> getCommentStartDelimiter() {
		return commentStartDelimiter;
	}


	public Optional<String> getCommentEndDelimiter() {
		return commentEndDelimiter;
	}

	/* Expands any line that contains field references to an Iterable to a stream of template lines, 
	 * each one of which represents one entry of the list.
	 */
	@SuppressWarnings("unchecked")
	private Stream<String> templateExpand(String line, ValueMap valueMap) {
		String substitutedLine = line; 
		List<ValueMap> entries = null;;
				
		Matcher fieldMatcher = fieldPattern.matcher(line);
		if (line.contains(templateCommentField)) {
			return Stream.of(line);
		}
		
		while (fieldMatcher.find()) {
			String fieldName = fieldMatcher.group().replaceAll("\\{|\\}", "");  // Removed the field delimiters
			String fieldNameParts[] = fieldName.split("[.]");  
			if (fieldNameParts.length > 1 && valueMap.isValueMap(fieldNameParts[0])) { // The field name has the form name.name and the value is a value map
				entries = valueMap.getValueMaps(fieldNameParts[0]); // Looking at a list so find out how many entries
				// Now replace  each list field in the line with an indicator showing that this is a list. 
				// For example:
				//    {{references.id}} 
				// is replaced with:
				//   {{references.[].id}} 
				// This makes it easier to substitute the index value later when this line is expanded. 
				substitutedLine = substitutedLine.replace("{{" + fieldNameParts[0] +  "." + fieldNameParts[1] + "}}", 
						"{{" +  fieldNameParts[0] + ".[]." + fieldNameParts[1] + "}}");
			} 
		}
		
		if (entries != null) {
			// Now expand the substitutedLine. For instance, if the Iterable field references has three entries then:
			//   * {{references.[].id}} -> {{references.[].name}}
			// is transformed to:
			//   * {{references.0.id}} -> {{references.0.name}}
			//   * {{references.1.id}} -> {{references.1.name}}
			//   * {{references.2.id}} -> {{references.2.name}}
			List<String> expandedLines = new ArrayList<String>();
			String expandedLine;
			for (int i = 0; i < entries.size(); i++) {
				expandedLine = substitutedLine.replace(".[].", "." + i + ".");
				expandedLines.add(expandedLine);
			}
			return expandedLines.stream();
		} else {
			return Stream.of(line);
		}

		
	}

	private String templateReplace(String line, ValueMap valueMap) {

		Matcher templateMatcher = fieldPattern.matcher(line);
		
		//TODO need to handle the case for {{template.comment}}.

		if (templateMatcher.find() &&  !line.contains(templateCommentField)) {
			// Line contains a field or a list 
			
			// First replace anything that is a valid field
			String replacedTemplateLine =  templateMatcher.replaceAll(mr -> fieldSubstitution(mr, valueMap)); 
		
			// Now build the meta data that is appended to the end of the line
			StringBuilder metaData = new StringBuilder();
			if (commentStartDelimiter.isPresent()) { 
				metaData.append(commentStartDelimiter.get()).append(line);
				if (commentEndDelimiter.isPresent()) {
					metaData.append(commentEndDelimiter.get());
				}  	
			}

			Matcher metaDataMatcher = fieldPattern.matcher(metaData);
			String metaDataLine = metaDataMatcher.replaceAll(mr -> metaDataSubstitution(mr, valueMap));

			return replacedTemplateLine + " " + metaDataLine;
		} else {
			return line;
		}


	}	
	
	private String fieldSubstitution(MatchResult mr, ValueMap valueMap) {
	
		String fieldName = mr.group().replaceAll("\\{|\\}", "");  // Removed the field delimiters
		
		// Field names starting with "template." are ignored. Note that these are always alone on a line.
	    if (fieldName.startsWith("template.")) {
	    	if (fieldName.substring("template.".length()).equals("comment")) {
	          return "{{template.comment}}";		
	    	}
	    }
	    
	    
	    String valueString = getFieldValueAsString(fieldName, valueMap);
	    
	    
	    
	    
		return valueString;
		
	}
	

	private String metaDataSubstitution(MatchResult mr, ValueMap valueMap) {
	     String fieldName = mr.group().replaceAll("\\{|\\}", "");  // Removed the field delimiters
		
		// Field names starting with "template." are ignored. Note that these are always alone on a line.
	    if (fieldName.startsWith("template.")) {
	    	if (fieldName.substring("template.".length()).equals("comment")) {
	          return "{{template.comment}}";		
	    	}
	    }
	    
	    //return "{{" + fieldName + "=\"" + valueMap.getOrDefault(fieldName, "UNKNOWN") + "\"}}";
	    String valueString = getFieldValueAsString(fieldName, valueMap);
	        
	    return "{{" + fieldName + "=\"" + valueString + "\"}}";
		
	}
			
	private String getFieldValueAsString(String fieldName, ValueMap fieldValueMap) {
		String valueString;
		Optional<Object> valueObject; 
		String listFieldName ;
		String subFieldName;
		int index;

		if (!fieldName.contains(".")) {
			if (fieldValueMap.containsField(fieldName)) {
				valueObject =  fieldValueMap.getValue(fieldName);
				valueString = valueObject.orElse("ERROR").toString();
			} else {
				valueString ="UNKNOWN";
			} 

		} else {
			// Field name is a compound (i.e. with a dot separator) and, by this time, should have been appended 
			// with the list notation [].

			Scanner scanner = new Scanner(fieldName);
			scanner.useDelimiter("\\.|\\[|\\]");
			listFieldName = scanner.next();
			index = Integer.parseInt(scanner.next());
			subFieldName = scanner.next();
			scanner.close();

						
			// Get the List
			List<ValueMap> list = fieldValueMap.getValueMaps(listFieldName);
			//Now get the value map for the list entry 
			ValueMap listEntryMap = list.get(index);
			
			// Now get the list entry attribute 
			valueObject = listEntryMap.getValue(subFieldName);
			if (valueObject != null) {
				valueString = ((Optional<Object>)valueObject).get().toString();
			} else {
				valueString = "UNKNOWN";
			}
		}
		return valueString;
	}
	
	/**
     * Parser the stream of template lines and extracts the start end end delimiters of comments,
     * @param stream Stream of template lines.
     */
	private void parseTemplateStream(Stream<String> stream) {
		
    	String templateComment = stream.filter(line -> line.contains(templateCommentField)).findAny().orElse("");
		if (!templateComment.isEmpty()) {
			commentStartDelimiter = Optional.of(templateComment.substring(0,templateComment.indexOf(templateCommentField)));
			commentEndDelimiter = Optional.of(templateComment.substring(commentStartDelimiter.get().length() + templateCommentField.length(), templateComment.length()));

		} else {
			commentStartDelimiter = Optional.empty();
			commentStartDelimiter = Optional.empty();
		}
    }


	@SuppressWarnings("unchecked")  // Suppress warning when casting type Object to type Iterable. 
	                                // During runtime the code checks that is is a valid operation.
	private ValueMap buildFieldValueMap(Object dataObject) {
		Object fieldValue;
		Iterable<Object> fieldIterable;
		
		ValueMap fieldValueMap = new ValueMap();
		
		Class<?> c = dataObject.getClass();
	
		if (c.isAnnotationPresent(Templatable.class)) {

			for(Field field: c.getDeclaredFields()) {
				if (field.isAnnotationPresent(TemplateField.class)) {
					field.setAccessible(true);


					try {
						fieldValue = field.get(dataObject);
						
						System.out.println(fieldValue.getClass().getName());
						
						if (!(fieldValue instanceof Iterable) && !field.getType().isArray()) {
							// Scalar value
							fieldValueMap.put(field.getName(), fieldValue);
						} else {
							if (field.getType().isArray()) {
								
								// Unpack the array. Need to do this as:
								// a) the array needs to Iterable and for some reason they are not. 
								// b) Just using simple casting does not seem to work for arrays
								// See https://stackoverflow.com/questions/8095016/unpacking-an-array-using-reflection
								List<Object> l = new ArrayList<Object>(Array.getLength(fieldValue)); 
								for (int i= 0; i < Array.getLength(fieldValue); i++) {
									l.add(Array.get(fieldValue,i));
								}
								fieldIterable = (Iterable<Object>) l;
								
							} else {
								// The field value is of type Iterable
								fieldIterable = (Iterable<Object>) fieldValue;
							}
							// Vector/Iterable value
							//fieldIterable = (Iterable<Object>) fieldValue;

							// Create a list of maps with the values in them
							//List<ValueMap> listValues = new ArrayList<ValueMap>();

							ValueMap fieldIterationMap;
							for (Object listEntry: fieldIterable) {
								fieldIterationMap = buildFieldValueMap(listEntry);
								//listValues.add(fieldIterationMap);
								fieldValueMap.add(field.getName(), fieldIterationMap);
								
							}
							//fieldValueMap.putListValue(field.getName(), listValues);
							

						}


					} catch (IllegalArgumentException | IllegalAccessException e) {
						fieldValue = null; //"ERROR";  //TODO make sure that null is represented as a string "ERROR" in the calling functions
					} 
				}
			}
		}

		return fieldValueMap;
		
	
	}
	
	
	
}
