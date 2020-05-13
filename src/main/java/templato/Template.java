/**
 * TODO
 */
package templato;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.*;
import java.util.*;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.*;
import java.lang.annotation.Annotation;
import java.lang.reflect.*;

import templato.annotations.Templatable;
import templato.annotations.TemplateField;
import templato.annotations.TemplateList;

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
	
	private Path templatePath;
	Optional<String> commentStartDelimiter;
	Optional<String> commentEndDelimiter;
	
	
	
	
	/**
	 * TODO
	 * @param templatePath
	 */
	public Template() {
		super();
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
		Object object;
		
		//TODO what happens if the specified objectClass does not have a constructor with no parameters?
		
		try {
			object = objectClass.getDeclaredConstructor().newInstance();
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
				| NoSuchMethodException | SecurityException e) {
			// TODO Auto-generated catch block
			throw new ReadException();
		}
		
		// Build up a value map from the file
		//TODO
		
		
		try (Stream<String> stream= Files.lines(markupFilePath, Charset.defaultCharset())) {
			//valueMap = stream.
			stream
				.filter(fieldPattern.asPredicate())  // Only process lines that contain a field
				//.flatMap(line -> Stream.of(line.split("\\{{2}|\\}{2}")))   // Now extract the fields as a stream [^\\{]\\{{2}|[^\\\\}]\\\\}{2}\"
				//.flatMap(line -> pattern.splitAsStream(line))   // Now extract the fields as a stream [^\\{]\\{{2}|[^\\\\}]\\\\}{2}\"
				.flatMap(line -> extractKeyValuePair(line))   // Now extract the key value pairs as Strings
				//.map(keyValuePairStr -> buildFieldValueMap(keyValuePairStr, valueMap))
	
				// .collect(ValueMap::new, ValueMap::put, ValueMap::putAll);
				.forEach(System.out::println);
	
				//            		.flatMap(line -> templateExpand(line, fieldValueMap))  // Expand any lists
				//            		.map(line -> templateReplace(line, fieldValueMap))     // Replace the fields and add meta data
				//            		.collect(Collectors.toMap());
		} catch (IOException e) {
			throw new ReadException();
		}
		
		// From the value map set up the returned object
		//TODO
		
		
		return object;
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
			if (fieldNameParts.length > 1 && valueMap.isList(fieldNameParts[0])) { // The field name has the form name.name and the value is a list
				entries = valueMap.getDataList(fieldNameParts[0]); // Looking at a list so find out how many entries
				// Now replace  each list field in the line with an indicator showing that this is a list. 
				// For example:
				//    {{references.id}} 
				// is replaced with:
				//   {{references.id[]}} 
				// This makes it easier to substitute the index value later when this line is expanded. 
				substitutedLine = substitutedLine.replace("{{" + fieldNameParts[0] +  "." + fieldNameParts[1] + "}}", 
						"{{" +  fieldNameParts[0] + "." + fieldNameParts[1] + "[]}}");
			} 
		}
		
		if (entries != null) {
			// Now expand the substitutedLine. For instance, if the Iterable field references has three entries then:
			//   * {{references.id[]}} -> {{references.name[]}}
			// is transformed to:
			//   * {{references.id[0]}} -> {{references.name[0]}}
			//   * {{references.id[1]}} -> {{references.name[1]}}
			//   * {{references.id[2]}} -> {{references.name[2]}}
			List<String> expandedLines = new ArrayList<String>();
			String expandedLine;
			for (int i = 0; i < entries.size(); i++) {
				expandedLine = substitutedLine.replace("[]}}", "[" + i + "]}}");
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
		Object valueObject; 
		String listFieldName ;
		String subFieldName;
		int index;

		if (!fieldName.contains(".")) {
			if (fieldValueMap.containsField(fieldName)) {
				valueObject =  fieldValueMap.getDataObject(fieldName);
				if (valueObject != null) valueString = valueObject.toString();
				else valueString = "ERROR";
			} else {
				valueString ="UNKNOWN";
			} 

		} else {
			// Field name is a compound (i.e. with a dot separator) and, by this time, should have been appended 
			// with the list notation [].

			Scanner scanner = new Scanner(fieldName);
			scanner.useDelimiter("\\.|\\[|\\]");
			listFieldName = scanner.next();
			subFieldName = scanner.next();
			index = Integer.parseInt(scanner.next());
			scanner.close();

						
			// Get the List
			List<ValueMap> list = fieldValueMap.getDataList(listFieldName);
			//Now get the value map for the list entry 
			ValueMap listEntryMap = list.get(index);
			
			// Now get the list entry attribute 
			valueObject = listEntryMap.getDataObject(subFieldName);
			if (valueObject != null) {
				valueString = valueObject.toString();
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
							fieldValueMap.putObject(field.getName(), fieldValue);
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
							List<ValueMap> listValues = new ArrayList<ValueMap>();

							ValueMap fieldIterationMap;


							for (Object listEntry: fieldIterable) {
								fieldIterationMap = buildFieldValueMap(listEntry);
								listValues.add(fieldIterationMap);
							}
							fieldValueMap.putList(field.getName(), listValues);
							

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
