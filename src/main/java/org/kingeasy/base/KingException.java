package org.kingeasy.base;

public class KingException extends Exception {

	private static final long serialVersionUID = 211L;

	public KingException(){
		super();
	}
	
	public KingException(String message){
		super(message);
	}
	
	public KingException(Throwable t){
		super(t);
	}
	
	public KingException(String message, Throwable t){
		super(message, t);
	}
}
