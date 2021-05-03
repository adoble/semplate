package semplate;

/** Thrown when either a markdown template or semantically annotated markdown file cannot be read. 
 * 
 * 
 * @author Andrew Doble
 *
 */
public class ReadException extends Exception {

	private static final long serialVersionUID = -7355467559376185515L;

	/** Constructs a ReadException with null as its error message string.
	 * 
	 */
	public ReadException() {
		super(); 
	}

	/** Constructs a ReadException, saving a reference to the error message string for later retrieval by the getMessage method.
	 * @param message the detail message
	 */
	public ReadException(String message) {
		super(message);
	}
	
	/** Constructs a ReadException, saving a reference to the original cause of the exception.
	 * @param cause the original cause of the exception
	 */
	public ReadException(Throwable cause) {
		super(cause);
	}
	
	/** Constructs a new ReadException with the specified detail message and cause. 
	 * @param message the detail message
	 * @param cause the original cause of the exception
	 */
	public ReadException(String message, Throwable cause) {
		super(message, cause);
	}

}
