package semplate;

import java.io.IOException;
import java.nio.file.Path;

/** Writes semantically annotated markdown files using a data object containing the data and a template file.
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
	private Template template;
	private Object dataObject;

	/** Construct a SemanticTemplateWriter object using the data object containing the data and the path to the template file. 
	 * <p>
	 * This constructor has package scope as it is never directly called by clients. Instead it is constructed using the 
	 * factory method {@link SemanticWriter#with(Object)}.
	 * 
	 * @param dataObject An object annotated with template field information
	 * @param template A template object containing the {@link Template#generate(Object, Path)} method
	 * 
	 * 
	 * */
	SemanticTemplateWriter(Object dataObject, Template template) {
		this.dataObject = dataObject;
		this.template = template;
	}

	/** Generates a semantically annotated markdown file from a template
	 * 
	 * 
	 * @param outputFile Path specifying the markdown file to be generated
	 * @throws WriteException if the markdown file could no be created for some cause.
	 */
	public void write(Path outputFile) throws WriteException {
		
		try {
			template.generate(dataObject, outputFile);
		} catch (IOException | CloneNotSupportedException e) {
			String msg = "Unable to generate " + outputFile.getFileName() + " from data object of type " + dataObject.getClass();
			throw new WriteException(msg, e); 
		}
		
	}

}
