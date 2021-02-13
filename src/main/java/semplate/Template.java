/**
 * TODO
 */
package semplate;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
<<<<<<< HEAD
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;
=======
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
>>>>>>> value-map-experiment

/**
 * TODO
 * @author Andrew
 *
 */
public class Template  {
	// Special fields are preceded with template
	final private String templateCommentField = "${template.comment}";
	
	private Path templatePath;
	String commentStartDelimiter;
	String commentEndDelimiter;

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
	 * @param object An object annotated with template field information
	 * @param outputFilePath
	 */
	public void generate(Object object, Path outputFilePath) {
		//TODO 
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

	public String getCommentStartDelimiter() {
		return commentStartDelimiter;
	}

	public String getCommentEndDelimiter() {
		return commentEndDelimiter;
	}
	
    /**
     * Parser the stream of template lines and extracts the start end end delimiters of comments,
     * @param stream Stream of template lines.
     */
	private void parseTemplateStream(Stream<String> stream) {
    	String templateComment = stream.filter(line -> line.contains(templateCommentField)).findAny().orElse("");
		if (!templateComment.isEmpty()) {
			commentStartDelimiter = templateComment.substring(0,templateComment.indexOf(templateCommentField));
			commentEndDelimiter = templateComment.substring(commentStartDelimiter.length() + templateCommentField.length(), templateComment.length());

		}
    }
	
	
	
}
