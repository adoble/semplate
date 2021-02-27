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

import com.google.common.base.CharMatcher;
import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import static com.google.common.base.Preconditions.*;

import semplate.annotations.Templatable;
import semplate.annotations.TemplateField;
import semplate.valuemap.ValueMap;

import java.lang.reflect.*;
import java.net.MalformedURLException;
import java.net.URL;


/** Provides facilities to be able to generate, read and update markdown files using data in POJOs.
 * 
 *  
 * The main functions are:
 * 
 *<p> -  {@linkplain #generate(Object, Path) generate} - Generate a markdown file using a template and the data in a POJO. The markdown file contains semantic about the data used. 
 * 
 *<p> - {@linkplain #read(Class, Path) read} - Read a previously generated markdown file and, using the semantic information in it, reconstruct a POJO. 
 * 
 *<p> - {@linkplain #update(Object, Path) update} - Read a previously generated markdown file and update it using data in a POJO. 
 * 
 * @author Andrew Doble
 *
 */
/**
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
	 * Constructs and initialises a template object 
	 * 
	 */
	public Template() {
		super();  //TODO required?

		collectionMap.put(ParseTokens.fieldname, Optional.empty());
		collectionMap.put(ParseTokens.subFieldname, Optional.empty());
		collectionMap.put(ParseTokens.index, Optional.empty());
		collectionMap.put(ParseTokens.value, Optional.empty());
	}


	/**
	 * Specifies the template to be used in generating the markdown files.
	 *
	 * @param templatePath A path to the template file.
	 */
	public void config(Path templatePath) throws IOException {
		this.templatePath = templatePath;

		try (Stream<String> stream = Files.lines(templatePath, Charset.defaultCharset())) {
			parseTemplateStream(stream);
		}



	}


	/**
	 * Specifies an input stream of the template files used for generating the markdown files.

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
	 * in the data object.

	 * @param dataObject An object annotated with template field information
	 * @param outputFilePath Path specifiing the markdown file to be generated
	 */
	public void generate(Object dataObject, Path outputFilePath) throws IOException {

		ValueMap fieldValueMap = buildFieldValueMap(dataObject);

		try (Stream<String> stream= Files.lines(templatePath, Charset.defaultCharset())) {
            List<String> replacements = stream
            		.flatMap(line -> templateExpand(line, fieldValueMap))  // Expand any lists
            		.map(line -> templateReplace(line, fieldValueMap))     // Replace the fields and add meta data
            		.collect(Collectors.toList());

            Files.write(outputFilePath, replacements);
	    }



	}

	/**
	 * Updates a markdown file using the annotated fields in the {@code object}
	 * 
	 *
	 * @param dataObject The object containing the new data
	 * @param markdownFilePath A path to the markdown file to be updated 
	 */
	public void update(Object dataObject, Path markdownFilePath) {
		//TODO
	}


	/**
	 * Reads the specified markdown file and creates a new  object of class objectClass that contains the
	 * data semantically represented in the markdown file.
	 *
	 * @param objectClass  The class of the data object to be generated.
	 * @param markupFilePath The path of the markdown file.
	 * @return
	 */
	public Object read(Class<?> objectClass, Path markupFilePath) throws ReadException {
		Object dataObject;

		ValueMap valueMap = readValueMap(markupFilePath);

		dataObject = constructDataObject(objectClass, valueMap);

		return dataObject;
	}


	private ValueMap readValueMap(Path markupFilePath) throws ReadException {
		ValueMap valueMap;
		try (Stream<String> stream= Files.lines(markupFilePath, Charset.defaultCharset())) {
			 valueMap = stream.filter(fieldPattern.asPredicate())  // Only process lines that contain a field
			      .flatMap(line -> extractKeyValuePair(line))   // Now extract the key value pairs as Strings
			      .filter(s -> !s.contains("template.comment"))  // Filter out the template.comment directive TODO change
			      //.map(nameValuePair -> constructValueMap(nameValuePair))
			      .map(nameValuePair -> ValueMap.of(nameValuePair))
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
			dataObject = objectClass.getDeclaredConstructor().newInstance();  // Note: constructor can be private TODO check this
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
				| NoSuchMethodException | SecurityException e) {
			// TODO Auto-generated catch block
			throw new ReadException();
		}

		// Using the entries in the value map find the corresponding fields and set them.
		// TODO simple case first
		Set<String> fieldNames = valueMap.fieldNames();

		for (String fieldName : fieldNames) {
			// Get the value
			if (!valueMap.isValueMap(fieldName)) {
				Optional<Object> fieldValue = valueMap.getValue(fieldName);
				
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
					System.err.println("FIELD: fieldname " +  fieldName + " is unknown. " + e);

				}

			} else {
               //TODO --> value is a value map
				System.out.println(fieldName + "=" + valueMap.getValueMap(fieldName).orElse(ValueMap.empty()));
				Field field;
				
				try {
					field = dataObject.getClass().getDeclaredField(fieldName);
					if (field.getAnnotation(TemplateField.class) != null ) {
						if (field.getType() == List.class) {
							setListField(dataObject, field, valueMap.getValueMap(fieldName).orElse(ValueMap.empty()));
						}
						/*
					    Options
						1. target field is a list: determine list of what? (--> move all this code to setListField)
					          1.1 Create a list of this type and add the elements
					    2. target fieled is map: determine map of what? dot dot dot (--> move all of this code to setMapField)
					    3. target field is another type of object : 
					       3.1 unpack value map needes
					       3.2 fields of the object need to be filled. This should be recursive.
					   */

					} else {
						// If the field has not been annotated then silently ignore
					}
				} catch (NoSuchFieldException | SecurityException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				
			}
		}



		return dataObject;
	}


	private void setField(Object dataObject, Field field, Optional<Object> fieldValue) {

		if (fieldValue.isEmpty()) return;  // TODO replace with checkArgument?
		String valStr = fieldValue.orElseThrow().toString();

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

    
  /**
 * @param dataObject
 * @param field
 * @param valueMap
 * @throws IllegalArgumentException if the specified filedis not of type #
 */
private void setListField (Object dataObject, Field field, ValueMap valueMap) {
	  checkArgument(field.getType() == List.class, "The specified field name %s is not of type %s", field.getName(), List.class.getName());
	  
	 
	  //Class<?> fieldType = field.getType();
      field.setAccessible(true);

	  // Determine the type of list object in the data object ?
	  Type genericType = field.getGenericType();
	 
	  // Extract the parameterised type name
	  String paraTypeName = Splitter.on('<')
			  .trimResults(CharMatcher.is('>'))
			  .omitEmptyStrings()
			  .splitToList(genericType.getTypeName()).get(1);
	  	 
	 // Now construct data objects for that type and fill them out with the data in the value map entries.  
	 List<ValueMap> vmList = valueMap.getValueMaps();
	 for (ValueMap vmEntry : vmList) {
		try {
			Class<?> paramClass = Class.forName(paraTypeName); //TODO move outside try block
			
			// Now construct a data object of paramClass from the value map 
			Object listEntryDataObject = constructDataObject(paramClass,vmEntry);
			
			@SuppressWarnings("unchecked")
			List<Object> dataObjectList = (List<Object>)field.get(dataObject);
			
		    dataObjectList.add(listEntryDataObject)	;	
	
		
		} catch (ReadException | ClassNotFoundException | IllegalArgumentException | IllegalAccessException  e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		
	}
	

	  
  }



	private void accumulateList(ArrayList<String> r, String s) {
		r.add(s);
	}

	private void combineList(ArrayList<String> r1, ArrayList<String> r2) {
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



	/** Returns the path to the markdown file that has been configured. 
	 * 
	 * @return The path to the markdown file operated on. 
	 */
	public Path getTemplatePath() {

		return templatePath;
	}


	/** Returns the string used for starting comments in the markdown.
	 * 
	 * This is setup from the information in the template markdown file specified in during configuration. 
	 * If no start delimiter string has been specified then this returns Optional.empty(). 
	 * 
	 * @return An <code>Optional</code> to the string used for starting comments in the markdown. 
	 */
	public Optional<String> getCommentStartDelimiter() {
		return commentStartDelimiter;
	}


	/** Returns the string used for ending comments in the markdown.
	 * 
	 * This is setup from the information in the template markdown file specified in during configuration. 
	 * If no end delimiter string has been specified then this returns Optional.empty(). 
	 * 
	 * @return An <code>Optional</code> to the string used for ending comments in the markdown. 
	 */
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
