package org.kingeasy.base;

import java.util.HashMap;
import java.util.List;
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
	public void testService() {
		Service service = Domain.getService();
		Goods goods = new Goods();
		goods.setName("商品q4");
		goods.setImage_addr("sdjfwoe.jpg");
		goods.setPrice(34.5);
		goods.setStock(345);
		Goods resultGoods = null;
		try{
			resultGoods = (Goods) service.save(goods);
			System.out.println("添加的数据是：" + resultGoods);
		}catch(KingException e){
			System.out.println(e.getMessage());
		}
		
		goods.setName("goods223");
		goods.setId(1);
		try{
			resultGoods = (Goods) service.update(goods);
			System.out.println("修改后的数据为：" + resultGoods);
		}catch(KingException e){
			System.out.println(e.getMessage());
		}
		
		//删除时，实际上只要主键属性有值就可以
		Goods delGoods = new Goods();
		delGoods.setId(23);
		try {
			int count = service.delete(delGoods);
			int num = service.delete(24, Goods.class);
			System.out.println("删除了 " + count + " 条数据。");
			System.out.println("删除了 " + num + " 条数据。");
		} catch (KingException e) {
			System.out.println(e.getMessage());
		}
		
		Map<String, String[]> map = new HashMap<String, String[]>();
		map.put("itemName", new String[]{"戒"});
		
		List<Item> list;
		try {
			list = service.search(map, Item.class);
			System.out.println(list);
			System.out.println(list.get(0).getCategory_name());
		} catch (KingException e) {
			System.out.println(e.getMessage());
		}
		
		map.clear();

		map.put("itemId", new String[]{"6"});
		map.put("minStock", new String[]{"100"});
		map.put("groupName", new String[]{"主打"});
		try {
			System.out.println(service.search(map, Goods.class));
		} catch (KingException e) {
			System.out.println(e.getMessage());
		}
		
		System.out.println("-----------分割线------------");
		
		map.clear();
		map.put("goodsId", new String[]{"6"});
		map.put("itemName", new String[]{"戒"});
		try {
			System.out.println(service.search(map, Item.class));
		} catch (KingException e) {
			System.out.println(e.getMessage());
		}
	}

}
