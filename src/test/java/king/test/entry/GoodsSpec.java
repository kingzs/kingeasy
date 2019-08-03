package king.test.entry;

import org.kingeasy.annotation.TableName;

/**
* @author King
* @email 281586342@qq.com
* @version 创建时间：2019年8月2日 上午10:12:55
* @ClassName 类名称
* @Description 类描述
*/

@TableName("goods_spec")
public class GoodsSpec {

	private int id;
	private Goods goods;
	private String name;
	private int sort;
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public Goods getGoods() {
		return goods;
	}
	public void setGoods(Goods goods) {
		this.goods = goods;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public int getSort() {
		return sort;
	}
	public void setSort(int sort) {
		this.sort = sort;
	}
	
}
