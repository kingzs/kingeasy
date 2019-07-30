package king.test.entry;

import java.util.List;

import org.kingeasy.annotation.ColumnName;
import org.kingeasy.annotation.Immutable;
import org.kingeasy.annotation.Mapping;
import org.kingeasy.annotation.NoDeletion;
import org.kingeasy.annotation.NoRepeat;
import org.kingeasy.annotation.PrimaryKey;
import org.kingeasy.annotation.RoadMapping;
import org.kingeasy.annotation.Search;
import org.kingeasy.annotation.Statistic;
import org.kingeasy.annotation.TableName;
import org.kingeasy.base.Domain;
import org.kingeasy.base.Statistics;
import org.kingeasy.base.TableRelation;

/**
 * 路径注解
 * 在框架里，有一个注解扫描器，通过扫描，会把指定包下的所有类的这个注解扫描出来，加入到一个map中。
 * 当有一个请求过来，则会有一个拦截器把请求拦截下来，拦截器会把URI后面两部分截取出来，倒数第二部分，
 * 会与这个注解的value进行匹配，匹配上，则说明请求，是要访问这个类的，URI的最后部分，则是方法名，
 * 在框架内封装了一系列通用的方法，要调用哪个方法，就是通过URI的最后部分来指定。
 * 例如：请求的URI以“/goods/search”结尾，那么就会调用框架内的search方法，方法关联的类就是这个类，
 * 即：king.test.entry.Goods
 * 当然，这个注解不是必须的，如果没有加这个注解，则默认为类的简要名称，即goods，不区分大小写
 */
@RoadMapping("product")

/**
 * 表名注解
 * 指定数据库中与这个类关联的表的表名
 * 这个注解，并不是必须，如果没有添加，则会以类的简要名称，作为表名来映射，
 * 意味着，只有类名与数据库中表名不一致时，才需要添加这个注解
 * 数据库是不区分大小写的，所以goods和Goods，在数据库中是一样的
 */
@TableName("goods")
public class Goods {
	
	/**
	 * 主键注解
	 * 指定这个类的主键，一个类，必须要有一个主键
	 * 如果字段名与数据库的列名不一致，则还需要添加value来指名数据中的列名
	 * 如@PrimaryKey("goods_id")，则类的字段，与数据库中的goods_id进行关联
	 */
	@PrimaryKey
	
	/**
	 * 聚合函数注解，这是默认模式，具体的可以看后面的注释
	 */
	@Statistic
	
	/**
	 * 查询注解
	 * param为前端查询时，使用的键名，即表单元素的name的值
	 * exp为查询条件的关系表达式，注意"?"不能省略，通用方法里的所有sql都是使用的预编译
	 * 主要是考虑一些复杂的查询，比如模糊查询，不好确定"?"的位置，所以还是由用户自己写这个"?"
	 * 如果param与类的字段名一致，则param，可以不写
	 * 如果exp的值为"=?"，也可以省略不写
	 */
	@Search(param="goodsId", exp="=?")
	private int id;
	
	/**
	 * 不可重复注解
	 * 在插入和修改数据的时候，有些字段是不可以重复的，则添加这个注解
	 * 注解的value，为提示语，如果出现了重复，则会把提示语响应给前端，前端可以直接拿这个作为提示语显示给用户
	 * 这个后期会有功能的扩充
	 */
	@NoRepeat("商品名称重复")
	@Search(param="goods_name", exp="LIKE CONCAT('%', ?, '%')")
	private String name;
	
	/**
	 * 数据库列名注解
	 * 当类的字段名与数据中的列名不一致时，使用这个注解
	 * 如果一样，可以省略这个注解
	 */
	@ColumnName("image_addr")
	private String image_addr;
	
	/**
	 * 限制修改注解
	 * 在修改数据的时候，对于添加了这个注解的字段，会忽略这个字段的值
	 * 比如：现在price字段添加了这个注解，那么在调用修改商品数据的接口时，
	 * 即使传了price的值，也会直接忽略掉
	 */
	@Immutable
	private double price;
	@Search(param="minStock", exp=">=?")
	@Search(param="maxStock", exp="<=?")
	
	/**
	 * 聚合函数注解
	 * 添加这个注解，可以对数据库中对应的列进行聚合，一张表，可以有多个列进行聚合
	 * 聚合的结果，会放到一个map中
	 * key用于指定放入map中时的key值，默认值为"total"
	 * statistic指定用什么聚合函数。取值有COUNT、SUM、AVG、MAX、MIN，默认值为COUNT
	 * handleNull指定是否需要处理null值，默认为false，即不处理null值
	 */
	@Statistic(key="stocks", statistic=Statistics.SUM, handleNull=false)
	private int stock;
	
	/**
	 * 多表映射注解
	 * 用于描述多张表的关联关系
	 * tableRelation有四个取值：ONE_TO_ONE、ONE_TO_MANY、MANY_TO_ONE、MANY_TO_MANY
	 * value是一个数组，根据tableRelation的值不同，含义也不同
	 * 当tableRelation为MANY_TO_MANY时，value里的第一个值为中间表的表名，第二个值为中间表与当前类（表）主键关联的字段名，第三个值为中间表与另一个类（表）主键关联的字段名
	 * 注意：只要两张表关联时，使用了中间表，就是MANY_TO_MANY，即使具体的业务不是多对多
	 * 当tableRelation为ONE_TO_MANY时，value为空数组，可以省略
	 * 当tableRelation为MANY_TO_ONE时，value里只有一个值，即另一个表的主键关联当前表中的列名
	 * 当tableRelation为ONE_TO_ONE时，value里只有一个值，即另一个表，与本表关联的列名
	 */
	@Mapping(tableRelation=TableRelation.MANY_TO_MANY, value={"goods_category_mapping", "goods_id", "category_id"})
	
	/**
	 * 限制删除注解
	 * 这个注解通常用于关联其它类的字段，当该类有关联另一个类的数据时，限制用户不能删除
	 * 注解的value为提示语，value里面，允许显示另一个类的一个字段的值
	 * 比如，现在的这个注解，当商品关联了一个分类时，则删除商品时，就会不允许删除，提示语中的{{category_name}}，
	 * 会替换成关联的商品分类的分类名，如果关联多个分类，则多个分类的名称，以英文的逗号（暂定）分隔显示
	 */
	@NoDeletion("该商品关联了分类{{category_name}}，不允许删除")
	private List<Item> items;
	@Mapping(tableRelation=TableRelation.MANY_TO_MANY, value={"goods_group_mapping", "goods_id", "group_id"})
	private List<Group> groups;
	
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
	public String getImage_addr(){
		return image_addr;
	}
	public void setImage_addr(String image_addr){
		this.image_addr = image_addr;
	}
	public double getPrice() {
		return price;
	}
	public void setPrice(double price) {
		this.price = price;
	}
	public int getStock(){
		return stock;
	}
	public void setStock(int stock){
		this.stock = stock;
	}
	public List<Item> getItems(){
		Domain.getService().getRelationEntry(this);
		return items;
	}
	public void setItems(List<Item> items){
		this.items = items;
	}
	public List<Group> getGroups(){
		Domain.getService().getRelationEntry(this);
		return groups;
	}
	public void setGroups(List<Group> groups){
		this.groups = groups;
	}
	@Override
	public String toString() {
		return "Goods [id=" + id + ", name=" + name + ", image_addr=" + image_addr + ", price=" 
				+ price + ", stock=" + stock + ", \nitems=" + getItems() + ", \ngroups=" + getGroups() + "]";
	}
	
}
