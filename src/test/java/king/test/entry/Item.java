package king.test.entry;

import java.util.List;

import org.kingeasy.annotation.Mapping;
import org.kingeasy.annotation.PrimaryKey;
import org.kingeasy.annotation.Search;
import org.kingeasy.annotation.TableName;
import org.kingeasy.base.Domain;
import org.kingeasy.base.KingException;
import org.kingeasy.base.TableRelation;

@TableName("goods_category")
public class Item {
	@PrimaryKey
	@Search(param="itemId", exp="=?")
	private int id;
	@Search(param="itemName", exp="LIKE CONCAT('%', ?, '%')")
	private String category_name;
	private int lev;
	private int parent;
	@Mapping(tableRelation=TableRelation.MANY_TO_MANY, value={"goods_category_mapping", "category_id", "goods_id"})
	private List<Goods> goodses;
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getCategory_name() throws KingException {
		if(parent != 0){
			return Domain.getService().search(parent, Item.class).getCategory_name() + "/" + category_name;
		}
		return category_name;
	}
	public void setCategory_name(String category_name) {
		this.category_name = category_name;
	}
	public int getLev() {
		return lev;
	}
	public void setLev(int lev) {
		this.lev = lev;
	}
	public int getParent() {
		return parent;
	}
	public void setParent(int parent) {
		this.parent = parent;
	}
	public List<Goods> getGoodses(){
		return goodses;
	}
	public void setGoodses(List<Goods> goodses){
		this.goodses = goodses;
	}
	@Override
	public String toString() {
		return "Item [id=" + id + ", category_name=" + category_name + ", lev=" + lev + ", parent=" + parent + "]";
	}
}
