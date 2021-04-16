package semplate;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.Optional;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.*;

import com.google.common.base.*;

import static com.google.common.base.Preconditions.*;
import static com.google.common.primitives.Primitives.*;

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
 *<br> -  {@linkplain #generate(Object, Path) generate} - Generate a markdown file using a template and the data in a POJO. The markdown file contains semantic about the data used. 
 * 
 *<br> - {@linkplain #read(Class, Path) read} - Read a previously generated markdown file and, using the semantic information in it, reconstruct a POJO. 
 * 
 *<br> - {@linkplain #update(Object, Path) update} - Read a previously generated markdown file and update it using data in a POJO. 
 * 
 * @author Andrew Doble
 *
 */
public class Template  {
	// Special fields are preceded with template
	final private String templateCommentField = "{@template.comment}}"; //TODO make static
	
    final private static Pattern directiveLine = Pattern.compile("\\{\\@[^}]*?\\}\\}");
	
	//final private static Pattern delimiterDirectivePattern = Pattern.compile("\\{@template.delimiter[^}]*\\}{2}");
	final private static Pattern delimiterDirectivePattern = Pattern.compile("\\{@template.delimiter.(?<type>.*?):(?<delim>.*?)\\}\\}");;
	
	final private static Pattern fieldPattern = Pattern.compile("\\{{2}(?<fieldname>[^\\}]*)\\}{2}");  
	//final private static Pattern listPattern = Pattern.compile("\\{{3}[^\\}]*\\}{3}");   //TODO check this - is it used?

	private Path templatePath;

	
	private StringBuffer block = new StringBuffer();
	
	private Delimiter commentDelimiter;

	private  Delimiters delimiters = new Delimiters();

	
    

	public Template() {
		
	}


	/**
	 * Specifies the template to be used in generating the markdown files.
	 *
	 * @param templatePath A path to the template file.
	 */
	public void config(Path templatePath) throws IOException, ReadException {
		this.templatePath = templatePath;

		try (Stream<String> stream = Files.lines(templatePath, Charset.defaultCharset())) {
 			commentDelimiter = stream.filter(line -> line.contains(templateCommentField))
 									 .map(line -> extractCommentDelimiter(line))
 									 .findFirst()
 									 .orElseThrow(() -> new ReadException("No template.comment directive found in template."));
		}

		
		try (Stream<String> stream = Files.lines(templatePath, Charset.defaultCharset())) {
			delimiters  = stream.filter(line -> line.contains("{@template.delimiter"))    // TODO is this strict enough?
					            .map(line -> extractDelimiters(line))
					            //.flatMap(delimiterList -> delimiterList.stream())    // Converts the list of delimiters to a stream of single delimiters
				                .collect(Delimiters::new, Delimiters::add, Delimiters::add);
		}


	}


	/**
	 * Specifies the name of a template file used for generating the markdown files.
     * @param templateFileName The name of the template file
	 */
	public void config(String templateFileName) throws IOException, ReadException {
		this.config(Path.of(templateFileName));

	}
	
	/** Parse a string containing delimiter specification and extract the delimiters. 
	 * 
	 * @param line The string contains  the specification
	 * @return A Delimiter object containing the delimiters 
	 */
	private Delimiter extractDelimiters(String line) {
		Matcher matcher = delimiterDirectivePattern.matcher(line);
		
		Delimiter delimiter = new Delimiter();  
		while(matcher.find()) {
			
		    // What type of delimiter directive is this?
		    String delimiterType = matcher.group("type");
		    String delimiterValue = matcher.group("delim");
		    if (delimiterValue.startsWith("\"")  && delimiterValue.endsWith("\"")) {
		      // Remove the quotes
		       delimiterValue = delimiterValue.substring(1, delimiterValue.length() - 1);
		       if (delimiterType.equals("start")) {
					delimiter.start(delimiterValue);
				} else if (delimiterType.equals("end")) {
					delimiter.end(delimiterValue);
				} else if (delimiterType.equals("pair")) {
					delimiter.pair(delimiterValue);
				}
				
		    } else {
		    	// This is reserved for special delimiters  such  as URL or DATE
		    	assert(false);
		    }
		    
		}
		
		return delimiter;
		
	}
	


	/**
	 * Generates a markdown file as specified by the template file using the information
	 * in the data object.

	 * @param dataObject An object annotated with template field information
	 * @param outputFilePath Path specifying the markdown file to be generated
	 */
	public void generate(Object dataObject, Path outputFilePath) throws IOException, CloneNotSupportedException {

		ValueMap valueMap = buildFieldValueMap(dataObject);

		// Expand the inline delimiters with the field delimiters so that only these are selected. 
		Delimiters expandedDelimiters = delimiters.clone().insertAll("{{", "}}"); 

		try (Stream<String> lines= Files.lines(templatePath, Charset.defaultCharset())) {
			List<String> replacements =  
					Stream.concat(lines, Stream.of("\n"))          // Add a blank line to the stream of lines so that all blocks are correctly terminated
					.map(chunk())                                                      // Map to Optional<StringBuffer> elements that either contain markdown text blocks or are empty
					.filter(optBlock -> optBlock.isPresent())                          // Filter out the empty blocks
					.map(optBlock -> optBlock.orElse(""))                              // Convert each block to a string
					.flatMap(block -> templateExpand(block, valueMap))                 // Expand any lists
					.map(line -> templateReplace(line, valueMap, expandedDelimiters))  // Replace the fields and add semantic information
					//::iterator;
					.collect(Collectors.toList());

			Files.write(outputFilePath, replacements);
		}

	}
	
	
    /** TODO Document!
     *  Using Optional as return type so that do not need to create empty strings in the lambda function as this is forbidden. */		
	public static Function <String, Optional<String>> chunk(){
		StringBuffer sb = new StringBuffer(80);  // This contains the state and is available for every element in the stream
		return s -> { if (s.isBlank()) { Optional<String> r = Optional.of(sb.toString()); sb.setLength(0); return r;}
		              else {sb.append(s).append('\n'); return  Optional.empty();
		            }  
		              
		};
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


	/**
	 * Updates a markdown file using the annotated fields in the {@code object}
	 * 
	 *
	 * @param dataObject The object containing the new data
	 * @param markdownFilePath A path to the markdown file to be updated
	 * @throws ReadException 
	 */
	public void update(Object dataObject, Path markdownFilePath) throws UpdateException {
		
		// Determine delimiters in the markdown file to be updated. 
		try {
			config(markdownFilePath);
		} catch (IOException | ReadException e) {
			// TODO Auto-generated catch block
			throw new UpdateException("Unable to read the file to be updated", e);
		}
		
		ValueMap updatedValueMap = buildFieldValueMap(dataObject); 

		// To be safe, copy the markdown file into a temp file in the same directory as the markdown files  before updating
		Path tempFile = copyToTempFile(markdownFilePath);
		
		// Update the contents with the data in the value map 
		List<String> blocks;
		try (Stream<String> lines = Files.lines(tempFile, Charset.defaultCharset())) {
			blocks = Stream.concat(lines, Stream.of("\n"))    // --> <String> : Add a blank lines to the stream of lines so that all blocks are correctly terminated
					.map(chunk())  
					.map(o -> o.orElse(""))
					.map(chunk -> updateBlock(chunk, updatedValueMap))
					.collect(Collectors.toList());
			
		} catch (IOException e)  {
			throw new UpdateException("Unable to update the file", e);
		}
		
		// Overwrite the original file with the new contents.  
		try {
			Files.write(markdownFilePath, blocks);
		} catch (IOException e) {
			String msg = "Cannot update the markdown file. A copy of the orignal file is in " + tempFile.toString();
			throw new UpdateException(msg, e);
		}
		
		
	}
    
	

	private Delimiter extractCommentDelimiter(String line) {
        checkArgument(line.contains(templateCommentField), "The line \"%s\" does not contain a template comment field", line);
        
        Delimiter delimiter = new Delimiter();
		
        List<String> preamble = Splitter.on("{@").trimResults().splitToList(line);
		delimiter.start(preamble.get(0));

		List<String> postamble = Splitter.on("}}").splitToList(line);
		delimiter.end(postamble.get(1));

		return delimiter;
		
	}

	
	/** Extract blocks of text from the markdown
	 *
	 * The blocks of text extracted are either 
	 * <p> a) the markdown text blocks that are separated by a new line. </p>
	 * <p> b) Same as (a), but appended with the semantic data that applies to it. The appended 
	 *        data can preceded by a new line. </p>
	 *    
	 * @param line The line being read from the markdown file
	 * @return A block of text with any semantic information appended. 
	 */
	private String extractBlock(String line) {
		String pipedBlock;

		if (line.trim().length() > 0 ) {  // Not an empty line
			block.append(line + "\n");
			pipedBlock = "";
		} else {
			pipedBlock = (block.toString());
			block.delete(0, block.length() - 1);
		}

		return pipedBlock;
	}
	
	/**
	 * See formal grammer  TODO
	 *
	 */
	
	private String updateBlock(String chunk, ValueMap valueMap) {
		ArrayList<FieldSpec> fieldSpecs = new ArrayList<>();
		
		// Extract the field specs 
        fieldSpecs.clear();
		
		Pattern fieldPattern = FieldSpec.pattern();
		Matcher fieldMatcher = fieldPattern.matcher(chunk);  

		while (fieldMatcher.find()) {
			FieldSpec field = FieldSpec.of(fieldMatcher.group());
			fieldSpecs.add(field); 
		}
		
		// Separate the chunk into the first line containing the semantics and
		// the rest containing the text.
		String semanticBlock = "";
		StringBuffer text  = new StringBuffer();
		if (!chunk.isEmpty()) {
		  List<String> parts = Splitter.on('\n').splitToList(chunk);
		  semanticBlock = parts.get(0);
		  parts.subList(1, parts.size() - 1).forEach(s -> text.append(s));;
		}
		String replacementChunk= text.toString();  
		
		for (FieldSpec fieldSpec : fieldSpecs) {
			Object value = valueMap.getValue(fieldSpec.fieldName()).orElse("");
			
			Optional<String> startDelimiter = fieldSpec.delimiter().start();
			Optional<String> endDelimiter = fieldSpec.delimiter().end();
			
			String regex = startDelimiter.map(d -> Pattern.quote(d)).orElse("^");
			regex += ".*?";
			regex += endDelimiter.map(d -> Pattern.quote(d)).orElse("$");
			replacementChunk = replacementChunk.replaceFirst(regex, startDelimiter.orElse("") + value.toString() + endDelimiter.orElse(""));
		}
		
		
		return semanticBlock + '\n' +  replacementChunk;
		
	}

	/** Copies a markdown file into a temp file in the same directory as the markdown file
	 * 
	 * @param markdownFilePath The path of the markdown file
	 * @throws ReadException 
	 */
	private Path copyToTempFile(Path markdownFilePath) throws UpdateException {  //TODO is a ReadException the correct way to do this? 
		Optional<Path> tempPath = Optional.empty();
		try  {
			tempPath = Optional.of(Files.createTempFile(markdownFilePath.getParent(), "semplate", ".tmp"));
			Files.copy(markdownFilePath, tempPath.orElseThrow(), StandardCopyOption.REPLACE_EXISTING);
		} catch (IOException e) {
			throw new UpdateException("Unable to create temporary files whilst updating " +  markdownFilePath.getFileName(), e);
		} finally {
			tempPath.ifPresent(p -> p.toFile().deleteOnExit());
        }
		
		return tempPath.orElseThrow(() -> new UpdateException("Unable to create temporary file for  " + markdownFilePath.getFileName()));
	}


	private ValueMap readValueMap(Path markupFilePath) throws ReadException {
		ValueMap valueMap;
		try (Stream<String> lines = Files.lines(markupFilePath, Charset.defaultCharset())) {

			valueMap = Stream.concat(lines, Stream.of("\n"))  // --> <String> : Add a blank lines to the stream of lines so that all blocks are correctly terminated 
							  .peek(l -> System.out.println("Line: " + l))
							  .map(Block.block())              // --> <block> : Create block = [semantic-block] text-value | text-block | empty.
							  .peek(b -> System.out.println("Block: " + b))
							  .filter(b -> !b.isEmpty())       // --> <block> : Filter out any empty blocks
							  .map(b -> b.toValueMap())        // --> <valueMap> : Read the values and create a value map 
							  .collect(ValueMap::new, ValueMap::merge, ValueMap::merge);  

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
					    2. target field is map: determine map of what? dot dot dot (--> move all of this code to setMapField)
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
 * @throws IllegalArgumentException if the specified field is not of type #
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
		//return delimiters.commentStartDelimiter();
		return commentDelimiter.start();
	}


	/** Returns the string used for ending comments in the markdown.
	 * 
	 * This is setup from the information in the template markdown file specified in during configuration. 
	 * If no end delimiter string has been specified then this returns Optional.empty(). 
	 * 
	 * @return An <code>Optional</code> to the string used for ending comments in the markdown. 
	 */
	public Optional<String> getCommentEndDelimiter() {
		return commentDelimiter.end();
	}

	
	/* Expands any markdown block that contains field names that refer to a list (i.e. have a '*' as part of their (compound) name
	 * to a stream of template blocks  each one of which represents one entry of the list.
	 * 
	 * For instance: the following template block:
	 * <code>
	 *    * Article number {{order.*.articleNumber}}
	 * </code>   
	 * will be expanded to a stream of the following blocks:
	 * <code>
	 *   * Article number {{order.0.articleNumber}}
	 *   
	 *   * Article number {{order.1.articleNumber}}
	 *   
	 *   * Article number {{order.2.articleNumber}}
	 *   
	 * </code>
	 * where the number of block is that contained in the value map.
	 * 
	 * If the block does not contain field names that contain a '*' then the block will be passed on without any changes. 
	 * 
	 * ---Precondition is that the blocks do not contains directives.
	 * 
	 * @param block A blank line delimited block of markdown text.
	 * @param valueMap The value map  
	 * @throws IllegalArgumentException if the block contains a directive
	 */
	private Stream<String> templateExpand(String block, ValueMap valueMap)  throws IllegalArgumentException {

        Stream.Builder<String> streamBuilder = Stream.builder();

		Matcher fieldMatcher = fieldPattern.matcher(block);

		String newBlock;	
		if (fieldMatcher.find()) {
			String fieldName = fieldMatcher.group("fieldname");
			if (fieldName.contains("*")) {
				// Extract the first part of the field name before the '*' character. 
				// Only indexed field names with the same first part before the '*' 
				// are allowed in one block.
				String firstPartFieldName = Splitter.on('*').trimResults(CharMatcher.is('.')).splitToList(fieldName).get(0);  //Included the periods, but that's ok. 

				ValueMap iteratedValueMap = valueMap.getValueMap(firstPartFieldName).orElse(ValueMap.empty());
				Set<String> fieldNameSet =  iteratedValueMap.fieldNames();
				for (String fieldNameEntry: fieldNameSet) {
					//newBlock = new String(block);
					String regex = "\\{\\{" + firstPartFieldName + "\\.\\*";
					String replacement = "{{" + firstPartFieldName + "." + fieldNameEntry;
					newBlock = block.replaceAll(regex, replacement);
					streamBuilder.add(newBlock);
				}                       

			} else {
				// Block does not contain any filed that represent a list, Just pass it on. 
				streamBuilder.add(block);  
			}
		} else {
			// Text block contains no fields or a directive. Just pass it on.
			streamBuilder.add(block);
		}


		return streamBuilder.build();
	}
	

	private String templateReplace(String inBlock, ValueMap valueMap, Delimiters delimiters) {
	   if (inBlock.contains("{@") && inBlock.contains("}}")) {
		   // Directives are passed through without any further processing
		   return inBlock;
		   
	   }
	   
	   if (!inBlock.contains("{{")) {
		   // Any text block without any fields is passed through without any further processing
		   return inBlock;
	   }
		
		StringBuilder semanticBlock = new StringBuilder();
		Matcher fieldMatcher = fieldPattern.matcher(inBlock);
				
		// Assemble the semantic block 
		// First assemble any inline field-specs and add them to the semantic block 
		Pattern delimiterPattern = delimiters.pattern();
		Matcher delimiterMatcher  = delimiterPattern.matcher(inBlock);
		semanticBlock = delimiterMatcher.results()
				                        .map(mr -> mr.group())   // Map to the string  s{{f}}e
						                .map(s -> mapInlineFieldSpec(s))
						                .collect(StringBuilder::new, StringBuilder::append, StringBuilder::append);
		
		
		boolean noInlineFieldsFound = (semanticBlock.length() == 0);
	    
		if (noInlineFieldsFound) {
			// A text block has the form
			//   a{{f}}b  where a, b are string with 0 or more characters, f is the field name
			// Need to map this to the outline field spec:
			//   {{f:pattern="a%s%b"}}
			if (fieldMatcher.find()) {
				List<String> parts = Splitter.onPattern("\\{\\{|\\}\\}").trimResults(CharMatcher.is('\n')).splitToList(inBlock);
				
				semanticBlock.append("{{").append(parts.get(1));
				String preamble = parts.get(0);
				String postamble = parts.get(2);
				
				if (!preamble.isEmpty() || !postamble.isEmpty()) {
					semanticBlock.append(":pattern=\"").append(preamble).append("%s").append(postamble).append("\"");
				}
				semanticBlock.append("}}");
			}
			
		}
		
		// Now surround the semantic block in comments
		if (semanticBlock.length() > 0) {
			semanticBlock.insert(0,  commentDelimiter.start().orElse(""))
			             .append(commentDelimiter.end().orElse(""));
		}
		
		// Now replace every thing in the in block that is a valid field using the value map
		String textValue =  fieldMatcher.replaceAll(mr -> fieldSubstitution(mr, valueMap));
		
		
		return semanticBlock + "\n" + textValue;
		
	}
	
	/* Takes string of the form 
	 *      <s>{{<f>}}<e>
	 * where <s> is the start delimiter, <f> is the field name and <e> is the end delimiter, and maps
	 * them to an inline field spec of the form:
	 * {{<f>:pattern="<s>%s<e>"}}
	 * 
	 * @param s The string to be mapped
	 * @return The inline field spec
	 * 
	 */
	private StringBuffer mapInlineFieldSpec(String s) {
		StringBuffer sb = new StringBuffer();
		List<String> parts = Splitter.onPattern("\\{\\{|\\}\\}").splitToList(s);
		
		checkArgument(parts.size() == 3, "The string \"%s\" is malformed", s); 
		
		sb.append("{{").append(parts.get(1)).append(":pattern=\"").append(parts.get(0)).append("%s").append(parts.get(2)).append("\"}}");
		
		return sb;
		
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
				
 		if (fieldValueMap.containsField(fieldName)) {
			valueObject =  fieldValueMap.getValue(fieldName);
			valueString = valueObject.orElse("ERROR").toString();
		} else {
			valueString ="UNKNOWN";
		}

		
		return valueString;
	}

	/**
     * Parser the stream of template lines and extracts the start and end delimiters of comments,
     * @param stream Stream of template lines.
     */
	private void determineCommentDelimiters(Stream<String> stream) {

    	String templateComment = stream.filter(line -> line.contains(templateCommentField)).findAny().orElse("");
    
		if (!templateComment.isEmpty()) {
			commentDelimiter.start(templateComment.substring(0,templateComment.indexOf(templateCommentField)));
			commentDelimiter.end(templateComment.substring(commentDelimiter.start().get().length() + templateCommentField.length(), templateComment.length()));

		} else {
			commentDelimiter.start("");
			commentDelimiter.end("");
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
					        Class<?> type = field.getType();
							// Scalar value
					        // TODO find a better way to do this. @See setField(...)
							if (type.isPrimitive() || isWrapperType(type) || type == String.class
									|| type == LocalDate.class || type == LocalDateTime.class || type == ZonedDateTime.class
									|| type == URL.class) {
								fieldValueMap.put(field.getName(), fieldValue);
							} else {
								ValueMap subValueMap = buildFieldValueMap(fieldValue);
								fieldValueMap.put(field.getName(), subValueMap);
							}
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
							
							ValueMap fieldIterationMap;
							for (Object listEntry: fieldIterable) {
								fieldIterationMap = buildFieldValueMap(listEntry);
								//listValues.add(fieldIterationMap);
								fieldValueMap.add(field.getName(), fieldIterationMap);

							}
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
