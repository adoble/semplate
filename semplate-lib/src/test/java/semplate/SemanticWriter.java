/**
 * 
 */
package semplate;

import java.io.IOException;
import java.nio.file.Path;

/**
 * @author Andrew Doble
 *
 */
public class SemanticWriter {
	private Object dataObject;

	public static SemanticWriter with(Object dataObject) {
		SemanticWriter semanticWriter = new SemanticWriter();
		semanticWriter.dataObject = dataObject;
		return semanticWriter;
	}

	Object getDataObject() {
		return dataObject ;
	}

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

}
