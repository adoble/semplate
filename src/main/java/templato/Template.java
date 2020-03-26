/**
 * TODO
 */
package templato;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

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

			String templateComment = stream.filter(line -> line.contains(templateCommentField)).findAny().orElse("");
			if (!templateComment.isEmpty()) {
				commentStartDelimiter = templateComment.substring(0,templateComment.indexOf(templateCommentField));
				commentEndDelimiter = templateComment.substring(commentStartDelimiter.length() + templateCommentField.length(), templateComment.length());
				
			}
		} catch (Exception e) {
			System.err.println(e.getMessage());
		}
			

	}
	
	
	/**
	 * TODO
	 * @param templateStream
	 */
	public void config(InputStream templateStream) { 
		//TODO
		
		/*
		 try (BufferedReader reader = new BufferedReader(new InputStreamReader(System.in))) {
			
			//Get Stream with lines from BufferedReader
			reader.lines()
			//Gives each line as string to the changeTrumpToDrumpf method of this.
			.map(this::changeTrumptoDrumpf)
			//Calls for each line the print method of this.
			.forEach(this::print);
			
		} catch (IOException ioException) {
			ioException.printStackTrace();
		}
		 */
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
	
	
	
}
