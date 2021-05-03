/**
 * 
 */
package semplate;

import java.io.IOException;
import java.nio.file.Path;

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
	 * @throws ReadException if the template file could not be read for some cause
	 */
	public SemanticTemplateWriter usingTemplate(Path templateFile) throws ReadException {
		Template template = new Template();
		
		try {
			template.config(templateFile);
		} catch (IOException e) {
			String msg = "Cannot read template file" + templateFile.getFileName();
			throw new ReadException(msg, e);
		} 
		
		SemanticTemplateWriter writer = new SemanticTemplateWriter(dataObject, template);
		
		return writer;
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
		Template t = new Template();
		
		t.update(dataObject, inputFile, outputFile);
	}

}
