package templato;

public class ReadException extends Exception {

	private static final long serialVersionUID = -7355467559376185515L;

	public ReadException() {
		super(); 
	}

	public ReadException(String message) {
		super(message);
	}
	
	public ReadException(Throwable cause) {
		super(cause);
	}
	
	public ReadException(String message, Throwable cause) {
		super(message, cause);
	}

}
