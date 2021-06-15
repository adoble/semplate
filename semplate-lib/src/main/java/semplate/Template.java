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
import semplate.valuemap.ConversionException;
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
 *<br> - {@linkplain #update(Object, Path, Path) update} - Read a previously generated markdown file and update it using data in a POJO. 
 * 
 * @author Andrew Doble
 *
 */
class Template  {
	// Special fields are preceded with template
	final private static String templateCommentField = "{@template.comment}}"; 
	
    final private static Pattern delimiterDirectivePattern = Pattern.compile("\\{@template.delimiter.(?<type>.*?):(?<delim>.*?)\\}\\}");;
	
	final private static Pattern fieldPattern = Pattern.compile("\\{{2}(?<fieldname>[^\\}]*)\\}{2}");  
	
	final private static Pattern iteratedFieldPattern = Pattern.compile("\\{\\{(?<fieldname>.*?\\*.*?)\\}\\}");
	
	private Path templatePath;

	private Delimiter commentDelimiter;

	private  Delimiters delimiters = new Delimiters();
	
	/**
	 * Specifies the template to be used in generating the markdown files.
	 *
	 * @param templatePath A path to the template file.
	 */
	void config(Path templatePath) throws IOException, ReadException {
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
	void config(String templateFileName) throws IOException, ReadException {
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
	


	private Stream<String> extractIteratedFieldNames(String line) {
		 boolean  lineContainsIteratedField = false;
		
		 Stream.Builder<String> streamBuilder = Stream.builder();
		 
		 Matcher matcher = iteratedFieldPattern.matcher(line);  
		 while (matcher.find()) {
			 streamBuilder.add(matcher.group("fieldname"));
			
			 lineContainsIteratedField = true;
		 }
		 
		 checkState(lineContainsIteratedField, "The line passed does not contain an iterated field name.", line);
		 
		 return streamBuilder.build();
		 
	}

	/**
	 * Generates a markdown file as specified by the template file using the information
	 * in the data object.

	 * @param dataObject An object annotated with template field information
	 * @param outputFilePath Path specifying the markdown file to be generated
	 */
	 void generate(Object dataObject, Path outputFilePath) throws IOException, CloneNotSupportedException {

		ValueMap valueMap = ValueMap.from(dataObject);

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
					.collect(Collectors.toList());
			
			Files.write(outputFilePath, replacements);
		}

	}
	
	
    /** Converts lines of markdown into blocks of markdown that are separated by two new lines. 
     *  
     *  Using Optional as return type so that do not need to create empty strings in the lambda 
     *  function as this is forbidden. 
     */		
	static Function <String, Optional<String>> chunk(){
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
	 * @return  The object created from the data in the markdown file.
	 */
	 Object read(Class<?> objectClass, Path markupFilePath) throws ReadException {
		Object dataObject = null;
	
		ValueMap valueMap = readValueMap(markupFilePath);
	
		try {
			dataObject = valueMap.toObject(objectClass);
		} catch (ConversionException e) {
			throw new ReadException(e.getMessage(), e);
		}
	
		return dataObject;
	}


	/**
	 * Read a markdown file, updates it with the information in annotated {@code dataObject} and then outputs the 
	 * results to a the specified output markdown file. 
	 *
	 * @param dataObject The object containing the new data
	 * @param inputFile A path to the markdown file to be updated. 
	 * @param outputFile A path to the markdown file to be updated
	 * @throws UpdateException 
	 */
	void update(Object dataObject, Path inputFile, Path outputFile) throws UpdateException {
		
		// Determine delimiters in the markdown file to be updated. 
		try {
			config(inputFile);
		} catch (IOException | ReadException e) {
			throw new UpdateException("Unable to read the file to be updated", e);
		}
		
		ValueMap updatedValueMap = ValueMap.from(dataObject); 

		// Update the contents with the data in the value map 
		List<String> blocks;
		try (Stream<String> lines = Files.lines(inputFile, Charset.defaultCharset())) {
			blocks = Stream.concat(lines, Stream.of("\n"))    // --> <String> : Add a blank lines to the stream of lines so that all blocks are correctly terminated
					.map(chunk())  
					.map(o -> removeListElement(o))  // Remove any blocks that have list elements in them
					.map(o -> o.orElse(""))  // Remove any empty blocks
					.flatMap(chunk -> updateList(chunk, updatedValueMap))  
					.map(chunk -> updateBlock(chunk, updatedValueMap))
					.collect(Collectors.toList());
			
		} catch (IOException e)  {
			throw new UpdateException("Unable to update the file", e);
		}
		
		// Create the output file with the new contents.  
		try {
			Files.write(outputFile, blocks);
		} catch (IOException e) {
			String msg = "Cannot update the markdown file";
			throw new UpdateException(msg, e);
		}
		
		
	}
    
	// TODO don't use optional use a stream builder and flatmap()
	private Optional<String> removeListElement(Optional<String> optBlock) {
		String block = optBlock.orElse("");
		
		// Regular expression to find field names that are part of a iteration. i.e field names of the form 
		//  a.D.b  
		//  D.b
		//  a.D
		//  D
		// Where a and b are field names made of non-numeric characters
		// and D is a number. 
		String regex = "\\{{2}"
				+ "(?<fieldname>.*?\\.\\d+\\..*?|"
				+ ".*?\\.\\d+|"
				+ "\\d+\\..+?|"
				+ "\\d+?)"
				+ "\\:.*?\\}{2}";
		
		Matcher iteratedFieldNameMatcher = Pattern.compile(regex).matcher(block);
		
		if (iteratedFieldNameMatcher.find()) {
			return Optional.empty();
		}
		return optBlock;
		
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
	
	/** If the chunk is a list directive then expends this into a list and stream each list entry
	 * 
	 * @param chunk  Contains some text 
	 * @param valueMap The updated value map
	 * @return A stream of either text blocks or added list entries
	 */
	private Stream<String> updateList(String chunk, ValueMap valueMap) {
		Stream.Builder<String> streamBuilder = Stream.builder();

		Pattern listDirectivePattern  = Pattern.compile("\\{@list-template=\\\"(?<template>.*?)\\\"\\}\\}"); 
		Matcher listDirectiveMatcher = listDirectivePattern.matcher(chunk);

		if (listDirectiveMatcher.find()) {
			String template = listDirectiveMatcher.group("template");

			streamBuilder.add(chunk);  // Add the list directive

			// Get the fields specified in the template
			Matcher templateFieldMatcher = fieldPattern.matcher(template);
			if (templateFieldMatcher.find()) {
				String fieldName = templateFieldMatcher.group("fieldname");
				if (fieldName.contains("*")) {
					// Extract the first part of the field name before the '*' character. 
					// Only indexed field names with the same first part before the '*' 
					// are allowed in one block.
					String firstPartFieldName = Splitter.on('*').trimResults(CharMatcher.is('.')).splitToList(fieldName).get(0);  
					// Expand the list fields
					ValueMap iteratedValueMap = valueMap.getValueMap(firstPartFieldName).orElse(ValueMap.empty());
					Set<String> fieldNameSet =  iteratedValueMap.fieldNames();
					for (String fieldNameEntry: fieldNameSet) {
						String regex = "\\{\\{" + firstPartFieldName + "\\.\\*";
						String replacement = "{{" + firstPartFieldName + "." + fieldNameEntry;
						String listTextValue = template.replaceAll(regex, replacement);
						
						// Create the semantic block
						StringBuilder semanticBlock = assembleSemanticBlock(listTextValue);
						
						String block = semanticBlock + "\n" + listTextValue + "\n\n";
						
						streamBuilder.add(block);
					}
				}
			}


		} else {
			streamBuilder.add(chunk);
		}

		return streamBuilder.build();
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
							  .map(Block.block())              // --> <block> : Create block = [semantic-block] text-value | text-block | empty.
							  .filter(b -> !b.isEmpty())       // --> <block> : Filter out any empty blocks
							  .map(b -> b.toValueMap())        // --> <valueMap> : Read the values and create a value map 
							  .collect(ValueMap::new, ValueMap::merge, ValueMap::merge);  

		} catch (IOException e) {
			throw new ReadException(e);
		}


		return valueMap;
	}


	/** Returns the path to the markdown file that has been configured. 
	 * 
	 * @return The path to the markdown file operated on. 
	 */
	Path getTemplatePath() {

		return templatePath;
	}


	/** Returns the string used for starting comments in the markdown.
	 * 
	 * This is setup from the information in the template markdown file specified in during configuration. 
	 * If no start delimiter string has been specified then this returns Optional.empty(). 
	 * 
	 * @return An <code>Optional</code> to the string used for starting comments in the markdown. 
	 */
	Optional<String> getCommentStartDelimiter() {
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
	Optional<String> getCommentEndDelimiter() {
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

				// Add the list directive  
				//TODO add list directive to BNF
				String listDirective = commentDelimiter.start().orElse("") 
						+ "{@list-template=\"" + block.replace("\n", "") 
						+ "\"}}"
						+ commentDelimiter.end().orElse("")
				        + "\n";
				streamBuilder.add(listDirective);
				
				// Expand the list fields
				ValueMap iteratedValueMap = valueMap.getValueMap(firstPartFieldName).orElse(ValueMap.empty());
				Set<String> fieldNameSet =  iteratedValueMap.fieldNames();
				for (String fieldNameEntry: fieldNameSet) {
					String regex = "\\{\\{" + firstPartFieldName + "\\.\\*";
					String replacement = "{{" + firstPartFieldName + "." + fieldNameEntry;
					newBlock = block.replaceAll(regex, replacement);
					
					streamBuilder.add(newBlock);   
				}                       

			} else {
				// Block does not contain any field that represents a list, Just pass it on. 
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
		
		Matcher fieldMatcher = fieldPattern.matcher(inBlock);
		
		StringBuilder semanticBlock = assembleSemanticBlock(inBlock);
		
		// Now replace every thing in the in block that is a valid field using the value map
		String textValue =  fieldMatcher.replaceAll(mr -> fieldSubstitution(mr, valueMap));
		
		
		return semanticBlock + "\n" + textValue;
		
	}

	private StringBuilder assembleSemanticBlock(String inBlock) {
		StringBuilder semanticBlock = new StringBuilder();
						
		// Assemble the semantic block 
		// First assemble any inline field-specs and add them to the semantic block 
		Pattern delimiterPattern = delimiters.pattern();
		Matcher delimiterMatcher  = delimiterPattern.matcher(inBlock);
		semanticBlock = delimiterMatcher.results()
				                        .map(mr -> mr.group())   // Map to the string  s{{f}}e
						                .map(s -> mapInlineFieldSpec(s))
						                .collect(StringBuilder::new, StringBuilder::append, StringBuilder::append);
		
		
		boolean noInlineFieldsFound = (semanticBlock.length() == 0);
		
		Matcher fieldMatcher = fieldPattern.matcher(inBlock);
	    
		if (noInlineFieldsFound) {
			// A text block has the form
			//   a{{f}}b  where a, b are strings with 0 or more characters, f is the field name
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
		return semanticBlock;
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

}
