package semplate.valuemap;

public class ConversionException extends Exception {
	
	private static final long serialVersionUID = 3531449732819160190L;

	public ConversionException() {
		super(); 
	}

	public ConversionException(String message) {
		super(message);
	}
	
	public ConversionException(Throwable cause) {
		super(cause);
	}
	
	public ConversionException(String message, Throwable cause) {
		super(message, cause);
	}

}
