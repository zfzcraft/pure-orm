package cn.zfz.pureorm.core;

public class PureOrmException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2348661179136070540L;

	public PureOrmException() {
		super();
	}

	public PureOrmException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public PureOrmException(String message, Throwable cause) {
		super(message, cause);
	}

	public PureOrmException(String message) {
		super(message);
	}

	public PureOrmException(Throwable cause) {
		super(cause);
	}

}
