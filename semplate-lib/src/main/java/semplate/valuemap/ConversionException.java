package semplate.valuemap;

/** Thrown when a {@link ValueMap} cannot be converted to an object
 * 
 * @author Andrew Doble
 *
 */
public class ConversionException extends Exception {

	private static final long serialVersionUID = 3531449732819160190L;

	/** Constructs a ConversionException with null as its error message string.
	 * 
	 */public ConversionException() {
		 super(); 
	 }

	 /** Constructs a ConversionException, saving a reference to the error message string for later retrieval by the getMessage method.
	  * @param message the detail message
	  */
	 public ConversionException(String message) {
		 super(message);
	 }

	 /** Constructs a ConversionException, saving a reference to the original cause of the exception.
	  * @param cause the original cause of the exception
	  */
	 public ConversionException(Throwable cause) {
		 super(cause);
	 }

	 /** Constructs a new ConversionException with the specified detail message and cause. 
	  * @param message the detail message
	  * @param cause the original cause of the exception
	  */
	 public ConversionException(String message, Throwable cause) {
		 super(message, cause);
	 }

}
