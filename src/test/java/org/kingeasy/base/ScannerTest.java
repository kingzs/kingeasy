package org.kingeasy.base;

import java.util.Map;

import org.junit.Test;

/**
* @author King
* @email 281586342@qq.com
* @version 创建时间：2019年7月30日 下午5:36:11
* @ClassName 类名称
* @Description 类描述
*/
public class ScannerTest {

	@Test
	public void scanTest(){
//		AnnotationScanner as = new AnnotationScanner("king.test.entry,org.kingeasy.annotation");
		AnnotationScanner as = new AnnotationScanner("king.test.entry");
		Map<String, Class<?>> roadAsClassMap = as.getRoadAsClassMap();
		System.out.println(roadAsClassMap);
	}
}
