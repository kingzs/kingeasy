package org.kingeasy.base;

import java.util.ArrayList;
import java.util.List;

import org.kingeasy.base.KingUtils;

import king.test.entry.Goods;

/**
* @author King
* @email 281586342@qq.com
* @version v1.0
* @ClassName Demo
* @Description 多线程的例子，当然，这只是很简单的例子，没有与线程池结合，也没有与框架做整合，而且现在还有一些问题没有处理
*/
public class Demo {

	public static void main(String[] args){
		List<Goods> list = new ArrayList<>();
		Goods[] result = new Goods[10];
		
		for(int i=0; i<6; ++i){
			Goods goods = new Goods();
			goods.setId(i+1);
			list.add(goods);
		}
		
		SyncObject so = new SyncObject();
		synchronized(so){
			int i = 0;
			for(Goods goods : list){
				new Thread(new Task(i++, result, goods, so)).start();
			}
			
			so.setResult(i);
			try {
				so.wait();
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
		}
		
		System.out.println(KingUtils.arrayToList(result));
	}
	
}
