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

/**
 * TODO
 * @author Andrew
 *
 */
public class Template  {
	// Special fields are preceded with template
	final private String templateCommentField = "{{template.comment}}"; //TODO make static
	
	final private static Pattern fieldPattern = Pattern.compile("\\{{2}[^\\}]*\\}{2}"); 
	
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
		
		Map<String, String> valueMap = buildValueMap(dataObject);
	    
		try (Stream<String> stream= Files.lines(templatePath, Charset.defaultCharset())) {
            List<String> replacements = stream
            		.map(line -> templateReplace(line, valueMap))
            		.collect(Collectors.toList());
            
            System.out.println(String.join("\n", replacements));
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
	 * @param objectClass
	 * @param markupFilePath
	 * @return
	 */
	public Object read(Class<Object> objectClass, Path markupFilePath) {
		//TODO 
		return null;
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


	private String templateReplace(String line, Map<String, String> valueMap) {

		Matcher templateMatcher = fieldPattern.matcher(line);
		
		//TODO need to handle the case for {{template.comment}}.

		if (templateMatcher.find() &&  !line.contains(templateCommentField)) {
			// Line contains a field
			String replacedTemplateLine =  templateMatcher.replaceAll(mr -> templateSubstitution(mr, valueMap)); //TODO

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
	
	private String templateSubstitution(MatchResult mr, Map<String, String> valueMap) {
	
		String fieldName = mr.group().replaceAll("\\{|\\}", "");  // Removed the field delimiters
		
		// Field names starting with "template." are ignored. Note that these are always alone on a line.
	    if (fieldName.startsWith("template.")) {
	    	if (fieldName.substring("template.".length()).equals("comment")) {
	          return "{{template.comment}}";		
	    	}
	    }
		return valueMap.getOrDefault(fieldName, "UNKNOWN");
		
		
		
	
		
//        StringBuilder builtString = new StringBuilder(fieldValue);
//        if (commentStartDelimiter.isPresent() ) {
//        	builtString.append(" ")
//                       .append(commentStartDelimiter.get())
//                       .append("{{")
//                       .append(fieldName)
//                       .append("=\"")
//                       .append(fieldValue)
//                       .append("\"}}");
//        	if (commentEndDelimiter.isPresent()) {
//        		builtString.append(commentEndDelimiter.get());
//        	} else {
//        		builtString.append("\n");
//        	}
//        }
//        
//		return builtString.toString();

	}
	
	private String metaDataSubstitution(MatchResult mr, Map<String, String> valueMap) {
	     String fieldName = mr.group().replaceAll("\\{|\\}", "");  // Removed the field delimiters
		
		// Field names starting with "template." are ignored. Note that these are always alone on a line.
	    if (fieldName.startsWith("template.")) {
	    	if (fieldName.substring("template.".length()).equals("comment")) {
	          return "{{template.comment}}";		
	    	}
	    }
	    
	    return "{{" + fieldName + "=\"" + valueMap.getOrDefault(fieldName, "UNKNOWN") + "\"}}";
		
	}
			
	
	
	/**
     * Parser the stream of template lines and extracts the start end end delimiters of comments,
     * @param stream Stream of template lines.
     */
	private void parseTemplateStream(Stream<String> stream) {
		
		
		
		/*
		 * fields = stream.collect(Collectors.toMap()) Map<Long, Employee> employeesMap
		 * = employeeList.stream() .collect( Collectors.toMap(Employee::getId,
		 * Function.identity()) );
		 */
		
		
    	String templateComment = stream.filter(line -> line.contains(templateCommentField)).findAny().orElse("");
		if (!templateComment.isEmpty()) {
			commentStartDelimiter = Optional.of(templateComment.substring(0,templateComment.indexOf(templateCommentField)));
			commentEndDelimiter = Optional.of(templateComment.substring(commentStartDelimiter.get().length() + templateCommentField.length(), templateComment.length()));

		} else {
			commentStartDelimiter = Optional.empty();
			commentStartDelimiter = Optional.empty();
		}
    }


	private Map<String, String> buildValueMap(Object dataObject) {
		String fieldValue;
		
		Map<String, String>  valueMap = new HashMap<String, String>();
		
		Class<?> c = dataObject.getClass();
	
		if (c.isAnnotationPresent(Templatable.class)) {
	
			for(Field field: c.getDeclaredFields()) {
				if (field.isAnnotationPresent(TemplateField.class)) {
					field.setAccessible(true);
					
						try {
							fieldValue = field.get(dataObject).toString();
						} catch (IllegalArgumentException | IllegalAccessException e) {
							fieldValue = "ERROR";
						} 
					
					
					valueMap.put(field.getName(), fieldValue);
				}
			}
		}
		
		return valueMap;
		
	
	}
	
	
	
}
