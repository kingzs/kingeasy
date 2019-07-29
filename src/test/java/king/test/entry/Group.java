package king.test.entry;

import java.util.List;

import org.kingeasy.annotation.ColumnName;
import org.kingeasy.annotation.Mapping;
import org.kingeasy.annotation.PrimaryKey;
import org.kingeasy.annotation.Search;
import org.kingeasy.annotation.TableName;
import org.kingeasy.base.TableRelation;

@TableName("goods_group")
public class Group {

	@PrimaryKey
	@Search(param="groupId", exp="=?")
	private int id;
	@ColumnName("group_name")
	@Search(param="groupName", exp="LIKE CONCAT('%', ?, '%')")
	private String name;
	@Mapping(tableRelation=TableRelation.MANY_TO_MANY, value={"goods_group_mapping", "group_id", "goods_id"})
	private List<Goods> goodsList;
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	
	public String toString(){
		return "Group [id=" + id + ", name=" + name + "]";
	}
	
}
