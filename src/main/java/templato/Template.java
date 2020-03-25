/**
 * TODO
 */
package templato;

import java.io.InputStream;
import java.nio.file.Path;

/**
 * TODO
 * @author Andrew
 *
 */
public class Template  {
	private Path templateFile;

	/**
	 * TODO
	 * @param templateFile
	 */
	public Template() {
		super();
	} 

	/**
	 * TODO
	 * @param templateFile
	 */
	public Template(Path templateFile) {
		super();
		this.templateFile = templateFile;
	}
	
	/**
	 * TODO
	 * @param templatePath
	 */
	public void config(Path templatePath) {
		this.templateFile = templatePath;
	}
	
	/**
	 * TODO
	 * @param templateStream
	 */
	public void config(InputStream templateStream) { 
		//TODO
	}
	
	/**
	 * Generates a markdown file as specified by the template file using the information
	 * in the node object
	 * @param object An object annotated with template field information
	 * @param outputFile
	 */
	public void generate(Object object, Path outputFile) {
		//TODO 
	}
	
	/**
	 * Updates a markdown files using the annotated fields in the object .
	 * 
	 * @param object
	 * @param outputFile
	 */
	public void update(Object object, Path outputFile) {
		//TODO 
	}
	
	
	/**
	 * Reads the specified markup file and creates a new  object of class nodeClass that contains the
	 * data semantically represented in the file. 
	 * @param objectClass
	 * @param markupFile
	 * @return
	 */
	public Object read(Class<Object> objectClass, Path markupFile) {
		//TODO 
		return null;
	}
	
}
