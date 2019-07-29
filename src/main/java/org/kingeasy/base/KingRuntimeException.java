package org.kingeasy.base;

public class KingRuntimeException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public KingRuntimeException(){
		super();
	}
	
	public KingRuntimeException(String message){
		super(message);
	}
	
	public KingRuntimeException(Throwable t){
		super(t);
	}
	
	public KingRuntimeException(String message, Throwable t){
		super(message, t);
	}
}
