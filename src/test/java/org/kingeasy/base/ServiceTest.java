package org.kingeasy.base;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import king.test.entry.Goods;
import king.test.entry.Item;

/**
* @author King
* @version 创建时间：2019年7月26日 上午9:18:10
* @ClassName 类名称
* @Description 类描述
*/
public class ServiceTest {

	@Test
	public void testService() throws KingException{
		Service service = Domain.getService();
//		Goods goods = new Goods();
//		goods.setName("商品233q4");
//		goods.setImage_addr("sdjfwoe.jpg");
//		goods.setPrice(34.5);
//		goods.setStock(345);
//		
//		int key = save(goods);
//		System.out.println("添加的数据主键是："+key);
//		goods.setId(key);
//		
//		try{
//			int num = update(goods);
//			System.out.println("修改了 " + num + " 条数据。");
//		}catch(KingException e){
//			System.out.println(e.getMessage());
//		}
//		
//		//删除时，实际上只要主键属性有值就可以
//		int count = delete(goods);
//		System.out.println("删除了 " + count + " 条数据。");
		
//		Map<String, String[]> map = new HashMap<String, String[]>();
//		map.put("category_name", new String[]{"戒"});
//		
//		List<Item> list = search(map, Item.class);
//		System.out.println(list);
//		System.out.println(list.get(0).getCategory_name());
		
		Map<String, String[]> map = new HashMap<String, String[]>();

		map.put("itemId", new String[]{"6"});
		map.put("minStock", new String[]{"100"});
		map.put("groupName", new String[]{"主打"});
		System.out.println(service.search(map, Goods.class));
		
//		System.out.println("-----------分割线------------");
//		
//		long time = System.currentTimeMillis();
//		for(int i=0; i<10000; ++i){
//			service.search(map, Goods.class).get(0).toString();
//		}
//		System.out.println(System.currentTimeMillis() - time + " 毫秒");
		
		System.out.println("-----------分割线------------");
		
		map.clear();
		map.put("goodsId", new String[]{"6"});
		map.put("itemName", new String[]{"戒"});
		System.out.println(service.search(map, Item.class));
		
	}
}
