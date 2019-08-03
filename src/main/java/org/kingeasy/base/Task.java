package org.kingeasy.base;
/**
* @author King
* @email 281586342@qq.com
* @version v1.0
* @ClassName Runnable
* @Description ...
*/
public class Task implements Runnable {
	
	private int index;
	private Object[] objs;
	private Object obj;
	private SyncObject oprate;
	
	public Task(){}
	
	public Task(int index, Object[] objs, Object obj, SyncObject oprate){
		this.index = index;
		this.objs = objs;
		this.obj = obj;
		this.oprate = oprate;
	}
	
	public void setIndex(int index){
		this.index = index;
	}
	
	public void setObjs(Object[] objs){
		this.objs = objs;
	}
	
	public void setObj(Object obj){
		this.obj = obj;
	}
	
	public void setOprate(SyncObject oprate){
		this.oprate = oprate;
	}

	@Override
	public void run() {
		objs[index] = obj;
		oprate.addCount();
	}

}
