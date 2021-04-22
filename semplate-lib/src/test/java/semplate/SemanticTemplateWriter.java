package semplate;

import java.io.IOException;
import java.nio.file.Path;

public class SemanticTemplateWriter {
	private Template template;
	private Object dataObject;

	public SemanticTemplateWriter(Object dataObject, Template template) {
		this.dataObject = dataObject;
		this.template = template;
	}

	public void write(Path outputFile) throws WriteException {
		
		try {
			template.generate(dataObject, outputFile);
		} catch (IOException | CloneNotSupportedException e) {
			String msg = "Unable to generate " + outputFile.getFileName() + " from data object of type " + dataObject.getClass();
			throw new WriteException(msg, e); 
		}
		
	}

}
