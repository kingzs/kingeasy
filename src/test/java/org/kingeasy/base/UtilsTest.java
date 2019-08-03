package org.kingeasy.base;

import java.util.List;

import org.junit.Test;

/**
* @author King
* @email 281586342@qq.com
* @version 创建时间：2019年8月2日 下午6:01:21
* @ClassName 类名称
* @Description 类描述
*/
public class UtilsTest {

	@Test
	public void testArrayToList(){
		Integer[] arr = new Integer[10];
		List<Integer> list = KingUtils.arrayToList(arr);
		System.out.println(list);
	}
	
}
