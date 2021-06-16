/**
 * 
 */
package semplate;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

import semplate.valuemap.ConversionException;
import semplate.valuemap.ValueMap;

/** Reads semantically annotated markdown files and constructs data objects from them.
 * <p>
 * For example, this expression:
 * <pre>
 * {@code
 * DataObjectType dataObject = (DataObjectType) SemanticReader.with(DataObjectType.class)
				               .usingFile(sourceFilePath)
				               .read();   
 *}
 * </pre>
 * ... reads the semantically annotated markdown file at <code>sourceFilePath</code> and creates the
 * data object <code>dataObject</code> using the data in the markdown file. 
 * 
 * @author Andrew Doble
 *
 */
public class SemanticReader {
	Class<?> dataObjectClass;
	Path inputFile;
	
	/** Private constructor as object are created with the factory method 
	 *  {@link #with(Class)}
	 */
	private SemanticReader() {}

	/** ----------------------  PUBLIC API -------------------------------------------- */
	
	/** Specifies the class of the data object that is created when the semantically annotated markdown file is read. 
	 * 
	 * @see SemanticReader
	 * @see #usingFile(Path)
	 * @see #read()
	 * 
	 * @param dataObjectClass The class of the data object to be created
	 * @return A SemanticReader configured with the data object class 
	 */
	public static SemanticReader with(Class<?> dataObjectClass) {
		SemanticReader reader = new SemanticReader();

		reader.dataObjectClass = dataObjectClass;

		return reader;
	}

	/** Specifies the semantically annotated markdown file to be read. 
	 * 
	 * @param inputFile Path to the semantically annotated markdown file
	 * @return A SemanticReader configured with the path to the markdown file. 
	 */
	public SemanticReader usingFile(Path inputFile) {
		this.inputFile = inputFile;
		
		return this;
	}

	/**  Reads a semantically annotated markdown file and creates a data object populated with 
	 * the data in the file.
	 * <p>
	 * 
	 * @see SemanticReader
	 * @see #usingFile(Path)
	 * @see #with(Class) 
	 * 
	 * @return An object populated with the data from the markdown file. 
	 * @throws ReadException when the markdown file cannot be read
	 */
	public Object read() throws ReadException {
       //Template t = new Template();
       
       //Object o = t.read(dataObjectClass, inputFile);
       
		ValueMap valueMap = readValueMap(inputFile);
		
		Object dataObject = null;
		try {
			dataObject = valueMap.toObject(dataObjectClass);
		} catch (ConversionException e) {
			throw new ReadException(e.getMessage(), e);
		}
	
		return dataObject;
	}
	
	
	/** ----------------------  SUPPORT FUNCTIONS -------------------------------------------- */
	

	private ValueMap readValueMap(Path markupFilePath) throws ReadException {
		ValueMap valueMap;
		try (Stream<String> lines = Files.lines(markupFilePath, Charset.defaultCharset())) {

			valueMap = Stream.concat(lines, Stream.of("\n"))  // --> <String> : Add a blank lines to the stream of lines so that all blocks are correctly terminated 
							  .map(Block.block())              // --> <block> : Create block = [semantic-block] text-value | text-block | empty.
							  .filter(b -> !b.isEmpty())       // --> <block> : Filter out any empty blocks
							  .map(b -> b.toValueMap())        // --> <valueMap> : Read the values and create a value map 
							  .collect(ValueMap::new, ValueMap::merge, ValueMap::merge);  

		} catch (IOException e) {
			throw new ReadException(e);
		}


		return valueMap;
	}

}
