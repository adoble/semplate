/**
 * 
 */
package semplate;

/** Thrown when a semantically annotated markdown file cannot be updated. 
 * 
 * 
 * @author Andrew Doble
 *
 */
public class UpdateException extends Exception {

	private static final long serialVersionUID = -8786596580870125199L;

	/** Constructs a UpdateException with null as its error message string.
	 * 
	 */public UpdateException() {
		super();
	}

	 /** Constructs a new UpdateException with the specified detail message and cause. 
		 * @param message the detail message
		 * @param cause the original cause of the exception
		 */
		public UpdateException(String message, Throwable cause) {
		super(message, cause);
	}

	/** Constructs a UpdateException, saving a reference to the error message string for later retrieval by the getMessage method.
	 * @param message the detail message
	 */
	public UpdateException(String message) {
		super(message);
	}

}
