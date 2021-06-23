package semplate;

import static com.google.common.base.Preconditions.checkArgument;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.google.common.base.CharMatcher;
import com.google.common.base.Splitter;

import semplate.valuemap.ConversionException;
import semplate.valuemap.ValueMap;

/** Writes a semantically annotated markdown file using a data object containing the data and a template file.
 * <p>
 * This class is never directly instantiated by clients. Instead the factory methods in {@link SemanticWriter} are used:
 * 
 * <pre> <code>
 *      SemanticWriter.with(dataObject).usingTemplate(templateFilePath).write(outputFilePath);
 * </code> </pre>
 * 
 * @author Andrew Doble
 *
 */
public class SemanticTemplateWriter {
	private Path templatePath;
	private Object dataObject;
	private Delimiters delimiters;
	private Delimiter commentDelimiter;

	/** Construct a SemanticTemplateWriter object using the data object containing the data and the path to the template file. 
	 * <p>
	 * This constructor has package scope as it is never directly called by clients. Instead it is constructed using the 
	 * factory method {@link SemanticWriter#with(Object)}.
	 * 
	 * @param dataObject An object annotated with template field information
	 * @param templatePath Path to a template file
	 * @param delimiters List of the delimiters used in the template
	 * @param commentDelimiter The delimiter used for comments in the template
	 * 
	 *
	 */
	SemanticTemplateWriter(Object dataObject, Path templatePath, Delimiters delimiters, Delimiter commentDelimiter) {
		this.dataObject = dataObject;
		this.templatePath = templatePath;
		this.delimiters = delimiters;
		this.commentDelimiter = commentDelimiter;
	}
	
	/* -------------------  PUBLIC API ----------------- */

	/** Generates a semantically annotated markdown file from a template.
	 * 
	 * 
	 * @param outputFile Path specifying the markdown file to be generated
	 * @throws WriteException if the markdown file could no be created for some cause.
	 */
	public void write(Path outputFile) throws WriteException {
		
		try {
			generate(dataObject, outputFile);
		} catch (IOException | CloneNotSupportedException | ConversionException e) {
			String msg = "Unable to generate " + outputFile.getFileName() + " from data object of type " + dataObject.getClass();
			throw new WriteException(msg, e); 
		}
		
	}
	
	/* -------------------  SUPPORT FUNCTIONS ----------------- */
	/**
	 * Generates a markdown file as specified by the template file using the information
	 * in the data object.
     * 
     * @param dataObject An object annotated with template field information
	 * @param outputFilePath Path specifying the markdown file to be generated
	 */
	void generate(Object dataObject, Path outputFilePath) throws IOException, CloneNotSupportedException, ConversionException {

		ValueMap valueMap = ValueMap.from(dataObject);

		// Expand the inline delimiters with the field delimiters so that only these are selected. 
		Delimiters expandedDelimiters = delimiters.clone().insertAll("{{", "}}"); 

		try (Stream<String> lines= Files.lines(templatePath, Charset.defaultCharset())) {
			List<String> replacements =  
					Stream.concat(lines, Stream.of("\n"))          // Add a blank line to the stream of lines so that all blocks are correctly terminated
					.map(SemanticWriter.chunk())                                                      // Map to Optional<StringBuffer> elements that either contain markdown text blocks or are empty
					.filter(optBlock -> optBlock.isPresent())                          // Filter out the empty blocks
					.map(optBlock -> optBlock.orElse(""))                              // Convert each block to a string
					.flatMap(block -> templateExpand(block, valueMap))                 // Expand any lists
					.map(line -> templateReplace(line, valueMap, expandedDelimiters))  // Replace the fields and add semantic information
					.collect(Collectors.toList());
			
			Files.write(outputFilePath, replacements);
		}

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
	 * 
	 * @param block A blank line delimited block of markdown text.
	 * @param valueMap The value map  
	 * @throws IllegalArgumentException if the block contains a directive
	 */
	private Stream<String> templateExpand(String block, ValueMap valueMap)  throws IllegalArgumentException {

        Stream.Builder<String> streamBuilder = Stream.builder();

        Matcher fieldMatcher = Patterns.FIELD_PATTERN.matcher(block);

		String newBlock;	
		if (fieldMatcher.find()) {
			String fieldName = fieldMatcher.group("fieldname");
			if (fieldName.contains("*")) {
				// Extract the first part of the field name before the '*' character. 
				// Only indexed field names with the same first part before the '*' 
				// are allowed in one block.
				String firstPartFieldName = Splitter.on('*').trimResults(CharMatcher.is('.')).splitToList(fieldName).get(0);  //Included the periods, but that's ok. 

				// Add the list directive  
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

    /**
     * @deprecated This functionality is very similar to the updateBlock 
     * functionality in {@link SemanticWriter}.
     * TODO try and fund a less redundant solution
     * @param inBlock
     * @param valueMap
     * @param delimiters
     * @return
     */
	private String templateReplace(String inBlock, ValueMap valueMap, Delimiters delimiters) {
	   if (inBlock.contains("{@") && inBlock.contains("}}")) {
		   // Directives are passed through without any further processing
		   return inBlock;
		   
	   }
	   
	   if (!inBlock.contains("{{")) {
		   // Any text block without any fields is passed through without any further processing
		   return inBlock;
	   }
		
		Matcher fieldMatcher = Patterns.FIELD_PATTERN.matcher(inBlock);
		
		StringBuilder semanticBlock = assembleSemanticBlock(inBlock);
		
		// Now replace every thing in the in block that is a valid field using the value map
		String textValue =  fieldMatcher.replaceAll(mr -> fieldSubstitution(mr, valueMap));
		
		
		return semanticBlock + "\n" + textValue;
		
	}

	/**
	 * @deprecated This method is duplicated in {@link SemanticWriter}. 
	 * @param inBlock
	 * @return
	 */
	@Deprecated
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
	
	/** Takes string of the form 
	 *      <s>{{<f>}}<e>
	 * where <s> is the start delimiter, <f> is the field name and <e> is the end delimiter, and maps
	 * them to an inline field spec of the form:
	 * {{<f>:pattern="<s>%s<e>"}}
	 * 
	 * @deprecated This methid is duplicated in {@link SemanticWriter}
	 * 
	 * @param s The string to be mapped
	 * @return The inline field spec
	 * 
	 */
	@Deprecated
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
