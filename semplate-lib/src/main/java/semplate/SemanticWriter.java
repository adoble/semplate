/**
 * 
 */
package semplate;

import static com.google.common.base.Preconditions.checkArgument;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.google.common.base.CharMatcher;
import com.google.common.base.Function;
import com.google.common.base.Splitter;

import semplate.valuemap.ConversionException;
import semplate.valuemap.ValueMap;

/** Updates existing semantically annotated markdown files using new data.
 * <p>
 * For instance, this statement:
 * <p>
 * {@code
 *     SemanticWriter.with(dataObject).usingFile(markdownFilePath).write(outputFilePath);
 * }
 * <p>
 * ... updates the semantically annotated markdown file at <code>markdownFilePath</code> using the 
 * data in <code>dataObject</code> and writes the updated file to <code>outputFilePath</code>
 * 
 * <p>
 * Whereas this statement: 
 * <p>
 * {@code
 *     SemanticWriter.with(dataObject).usingTemplate(templateFilePath).write(outputFilePath);
 * }
 * <p>
 * ... writes a semantically annotated markdown file at <code>outputFilePath</code> using the 
 * data in <code>dataObject</code> and the template file at <code>templateFilePath</code>.
 * 
 * 
 * @author Andrew Doble
 *
 */
public class SemanticWriter {
	private Object dataObject;
	private Path inputFile;
	
	private Delimiter commentDelimiter;

	private  Delimiters delimiters = new Delimiters();
	
	/* ------------------- PUBLIC API ------------------ */

	/** Set up the data object whose data is to be written to a semantically annotated markdown file. 
	 * 
	 * @see SemanticWriter
	 * @see #usingTemplate(Path)
	 * @see #usingFile(Path)
	 * @see #write(Path)
	 * 
	 * @param dataObject the data object whose data is to be written to a semantically annotated markdown file
	 * @return A SemanticWriter object setup with the data object
	 */
	public static SemanticWriter with(Object dataObject) {
		SemanticWriter semanticWriter = new SemanticWriter();
		semanticWriter.dataObject = dataObject;
		return semanticWriter;
	}

	
	/** Get the data object setup with the method {@link #with(Object)}
	 * 
	 * @return The data object 
	 */
	Object getDataObject() {
		return dataObject ;
	}

	
	/** Specifies the template file used to generate the semantically annotated markdown file.
	 * 
	 * @see SemanticWriter
	 * @see #with(Object)
	 * @see SemanticTemplateWriter
	 * @see SemanticTemplateWriter#write(Path)
	 * 
	 * @param templateFile The file containing the template for the generated markdown file
	 * @return A SemanticTemplateWriter object
	 * @throws ReadException if the template file could not be read for some reason
	 */
	public SemanticTemplateWriter usingTemplate(Path templateFile) throws ReadException {
		
		try {
			readDelimiters(templateFile);
		} catch (IOException e) {
			String msg = "Cannot read template file" + templateFile.getFileName();
			throw new ReadException(msg, e);
		} 
		
		SemanticTemplateWriter writer = new SemanticTemplateWriter(dataObject, templateFile, delimiters, commentDelimiter);
		
		return writer;
	}
	
	/** Specifies the name of the template file  used to generate the semantically annotated markdown file.
	 * 
	 * @see SemanticWriter
	 * @see #with(Object)
	 * @see SemanticTemplateWriter
	 * @see SemanticTemplateWriter#write(Path)
	 * 
	 * @param templateFileName The name of the file containing the template for the generated markdown file
	 * @return A SemanticTemplateWriter object
	 * @throws ReadException if the template file could not be read for some reason
	 */
	public SemanticTemplateWriter usingTemplate(String templateFileName) throws ReadException {
		return usingTemplate(Path.of(templateFileName));
	}

	
	/** Specifies the semantically annotated markdown file to be updated. 
	 * 
	 * @see SemanticWriter
	 * @see #write(Path)
	 * 
	 * @param inputFile Path to the semantically annotated markdown file to be updated
	 * @return A SemanticWriter object setup with semantically annotated markdown file to be updated 
	 */
	public SemanticWriter usingFile(Path inputFile) {
		this.inputFile = inputFile;
		return this;
	}

	
	/** Writes a semantically annotated markdown file.
	 * @param outputFile Path where the semantically annotated markdown file is written
	 * @throws UpdateException If the file could not be updated. 
	 */
	public void write(Path outputFile) throws UpdateException {
		//Template t = new Template();
		
		update(dataObject, inputFile, outputFile);
	}
	
	/* ------------------- SUPPORT FUNCTIONS ------------------ */
	
	/**
	 * Read a markdown file, updates it with the information in annotated {@code dataObject} and then outputs the 
	 * results to a the specified output markdown file. 
	 * 
	 * @param dataObject The object containing the new data
	 * @param inputFile A path to the markdown file to be updated. 
	 * @param outputFile A path to the markdown file to be updated
	 * @throws UpdateException If the output file cannot be updated for some reason
	 */
	void update(Object dataObject, Path inputFile, Path outputFile) throws UpdateException {
		
		// Determine delimiters in the markdown file to be updated. 
		try {
			readDelimiters(inputFile);
		} catch (IOException | ReadException e) {
			throw new UpdateException("Unable to read the file to be updated", e);
		}
		
		ValueMap updatedValueMap;
		try {
			updatedValueMap = ValueMap.from(dataObject);
		} catch (ConversionException e) {
			throw new UpdateException("Cannot read the supplied data object", e);
		} 

		// Update the contents with the data in the value map 
		List<String> blocks;
		try (Stream<String> lines = Files.lines(inputFile, Charset.defaultCharset())) {
			blocks = Stream.concat(lines, Stream.of("\n"))    // --> <String> : Add a blank lines to the stream of lines so that all blocks are correctly terminated
					.map(chunk())  
					.map(o -> o.orElse(""))  // Remove any empty blocks
					.flatMap(b -> removeListElement(b))  // Remove any blocks that have list elements in them
					.flatMap(chunk -> updateList(chunk, updatedValueMap))  
					.map(chunk -> updateBlock(chunk, updatedValueMap))
					.collect(Collectors.toList());
			
		} catch (IOException  e)  {
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
	
	/** Converts lines of markdown into blocks of markdown that are separated by two new lines. 
	 *  
	 *  Using Optional as return type so that do not need to create empty strings in the lambda 
	 *  function as this is forbidden. 
	 *  
	 */	
	static Function <String, Optional<String>> chunk(){
		StringBuffer sb = new StringBuffer(80);  // This contains the state and is available for every element in the stream
		return s -> { if (s.isBlank()) { Optional<String> r = Optional.of(sb.toString()); sb.setLength(0); return r;}
		              else {sb.append(s).append('\n'); return  Optional.empty();
		            }  
		              
		};
	}
	
	
	private Stream<String> removeListElement(String block) {
		Stream.Builder<String> streamBuilder = Stream.builder();
		
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
		
		if (!iteratedFieldNameMatcher.find()) {
			streamBuilder.add(block);
		}
		return streamBuilder.build();
		
	}
	
	/** If the chunk is a list directive then expends this into a list and streams each list entry
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
			Matcher templateFieldMatcher = Patterns.FIELD_PATTERN.matcher(template);
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
	
	/** Updates all fields in a block of markdown with the values in the value map
	 * 
	 * @param block  A block of markdown separated with two newlines. May contain fields with the form {{<i>fieldname</i>}}
	 * @param valueMap A value map containing the field values. 
	 * @returns The updated chunk. 
	 */
	private String updateBlock(String block, ValueMap valueMap) {
		ArrayList<FieldSpec> fieldSpecs = new ArrayList<>();
		
		// Extract the field specs 
        fieldSpecs.clear();
		
		Pattern fieldPattern = FieldSpec.pattern();
		Matcher fieldMatcher = fieldPattern.matcher(block);  

		while (fieldMatcher.find()) {
			FieldSpec field = FieldSpec.of(fieldMatcher.group());
			fieldSpecs.add(field); 
		}
		
		// Separate the chunk into the first line containing the semantics and
		// the rest containing the text.
		String semanticBlock = "";
		StringBuffer text  = new StringBuffer();
		if (!block.isEmpty()) {
		  List<String> parts = Splitter.on('\n').splitToList(block);
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
		
		Matcher fieldMatcher = Patterns.FIELD_PATTERN.matcher(inBlock);
	    
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
	
	/**
	 * Reads the delimiters specified in the template file. 
	 * 
	 * @param templatePath A path to the template file.
	 */
	
	void readDelimiters(Path templatePath) throws IOException, ReadException {
		
		try (Stream<String> stream = Files.lines(templatePath, Charset.defaultCharset())) {
 			commentDelimiter = stream.filter(Patterns.COMMENT_DIRECTIVE_PATTERN.asPredicate())
						 			 .map(line -> extractCommentDelimiter(line))
 									 .findFirst()
 									 .orElseThrow(() -> new ReadException("No template.comment directive found in template."));
		}

		
		try (Stream<String> stream = Files.lines(templatePath, Charset.defaultCharset())) {
			delimiters  = stream.filter(Patterns.DELIMITER_DIRECTIVE_PATTERN.asPredicate())
							    .map(line -> extractDelimiters(line))
					            .collect(Delimiters::new, Delimiters::add, Delimiters::add);
		}
		
		
	}
	
	//TODO move to Delimiter
	private Delimiter extractCommentDelimiter(String line) {
		checkArgument(Patterns.COMMENT_DIRECTIVE_PATTERN.asPredicate().test(line), "The line \"%s\" does not contain a template comment field", line);
        
        Delimiter delimiter = new Delimiter();
		
        List<String> preamble = Splitter.on("{@").trimResults().splitToList(line);
		delimiter.start(preamble.get(0));

		List<String> postamble = Splitter.on("}}").splitToList(line);
		delimiter.end(postamble.get(1));

		return delimiter;
		
	}
	
	//TODO Move to Delimiter
	/** Parse a string containing delimiter specification and extract the delimiters. 
	 * 
	 * @param line The string contains  the specification
	 * @return A Delimiter object containing the delimiters 
	 */
	private Delimiter extractDelimiters(String line) {
		Matcher matcher = Patterns.DELIMITER_DIRECTIVE_PATTERN.matcher(line);
		
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
		    	assert(false);  //TODO 
		    }
		    
		}
		
		return delimiter;
		
	}

}
