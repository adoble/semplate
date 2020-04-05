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
	
	private Object dataObject;
	
	
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
		
		this.dataObject = dataObject;  
	    
	    try (Stream<String> stream= Files.lines(templatePath, Charset.defaultCharset())) {
            List<String> replacements = stream
            		.map(line -> templateReplace(line))
            		.collect(Collectors.toList());
            
            System.out.println(String.join("\n", replacements));
            Files.write(outputFilePath, replacements);
	    }
	    
	    

	}
	
	private String templateReplace(String line) {

		Matcher matcher = fieldPattern.matcher(line);

        return matcher.replaceAll(mr -> buildTemplateSubstitution(mr)); 
	}	
	
	private String buildTemplateSubstitution(MatchResult mr) {
	
		String fieldName = mr.group().replaceAll("\\{|\\}", "");  // Removed the field delimiters
		
		// Field names starting with "template." are ignored
	    if (fieldName.startsWith("template.")) {
	    	if (fieldName.substring("template.".length()).equals("comment")) {
	          return "{{template.comment}}";		
	    	}
	    }
		String fieldValue = "UNKNOWN";

		Class<?> c = dataObject.getClass();

		if (c.isAnnotationPresent(Templatable.class)) {

			Field field;
			try {
				field = c.getDeclaredField(fieldName);

				if (field.isAnnotationPresent(TemplateField.class)) {
					field.setAccessible(true);
					fieldValue = field.get(dataObject).toString();
				}
			}
			catch (NoSuchFieldException e) {
				fieldValue = "UNKNOWN";
			}
			catch (SecurityException | IllegalAccessException e) {
				fieldValue =  "ERROR";
			}
		}

        StringBuilder builtString = new StringBuilder(fieldValue);
        if (commentStartDelimiter.isPresent() ) {
        	builtString.append(" ")
                       .append(commentStartDelimiter.get())
                       .append("{{")
                       .append(fieldName)
                       .append("=")
                       .append(fieldValue)
                       .append("}}");
        	if (commentEndDelimiter.isPresent()) {
        		builtString.append(commentEndDelimiter.get());
        	} else {
        		builtString.append("\n");
        	}
        }
        
		return builtString.toString();

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
	
	
	
}
