/**
 * 
 */
package semplate;

import java.nio.file.Path;

/**
 * @author Andrew Doble
 *
 */
public class SemanticReader {
	Class<?> dataObjectClass;
	Path inputFile;

	public static SemanticReader with(Class<?> dataObjectClass) {
		SemanticReader reader = new SemanticReader();

		reader.dataObjectClass = dataObjectClass;

		return reader;
	}

	public SemanticReader usingFile(Path inputFile) {
		this.inputFile = inputFile;
		
		return this;
	}

	public Object read() throws ReadException {
       Template t = new Template();
       
       Object o = t.read(dataObjectClass, inputFile);
       
       return o;
	}



}
