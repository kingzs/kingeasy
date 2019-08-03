package org.kingeasy.base;
/**
* @author King
* @email 281586342@qq.com
* @version v1.0
* @ClassName ...
* @Description ...
*/
public class SyncObject {

	private int count = 0;
	private int result = 0;
	
	public SyncObject(){}
	
	public void setResult(int result){
		this.result = result;
	}
	
	public synchronized void addCount(){
		if(++count == result){
			notifyAll();
		}
	}
}
