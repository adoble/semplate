package semplate;

/** Thrown when either a  semantically annotated markdown file cannot be 
 * created or written. 
 * 
 * 
 * @author Andrew Doble
 *
 */
public class WriteException extends Exception {

	private static final long serialVersionUID = 6660379282078846242L;

	/** Constructs a WriteException with null as its error message string.
	 * 
	 */
	public WriteException() {
	}

	/** Constructs a WriteException, saving a reference to the error message string for later retrieval by the getMessage method.
	 * 
	 * @param message the detail message
	 */public WriteException(String message) {
		 super(message);
	 }

	 /** Constructs a WriteException, saving a reference to the original cause of the exception.
	  * 
	  * @param cause the original cause of the exception
	  */
	 public WriteException(Throwable cause) {
		 super(cause);
	 }

	 /** Constructs a new WriteException with the specified detail message and cause. 
	  * 
	  * @param message the detail message
	  * @param cause the original cause of the exception
	  */
	 public WriteException(String message, Throwable cause) {
		 super(message, cause);
	 }


}
