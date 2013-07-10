package lexent.resource;

public class LexicalResourceException extends Exception {

	public LexicalResourceException() {
	}

	public LexicalResourceException(String message) {
		super(message);
	}

	public LexicalResourceException(Throwable cause) {
		super(cause);
	}

	public LexicalResourceException(String message, Throwable cause) {
		super(message, cause);
	}

	public LexicalResourceException(String message, Throwable cause,
			boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
