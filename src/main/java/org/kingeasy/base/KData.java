package org.kingeasy.base;
/**
* @author King
* @email 281586342@qq.com
* @version 创建时间：2019年7月26日 下午3:00:15
* @ClassName 类名称
* @Description 类描述
*/

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import javax.sql.DataSource;

import com.mchange.v2.c3p0.ComboPooledDataSource;

public class KData {

	private static DataSource ds = new ComboPooledDataSource();
	private Connection conn;
	private PreparedStatement pstmt;
	private ResultSet resultSet;
	
	public KData() {
		try {
			conn = ds.getConnection();
		} catch (SQLException e) {
			throw new KingRuntimeException("数据库连接出现异常", e);
		}
	}
	
	public KData(String sql, List<Object> paramList){
		this();
		try {
			pstmt = conn.prepareStatement(sql);
			int index = 0;
			for(Object obj : paramList){
				pstmt.setObject(++index, obj);
			}
		} catch (SQLException e) {
			close();
			throw new KingRuntimeException("预编译sql出现异常", e);
		}
	}
	
	public void executeQuery(){
		try {
			resultSet = pstmt.executeQuery();
		} catch (SQLException e) {
			close();
			throw new KingRuntimeException("ִ执行sql出现异常", e);
		}
	}
	
	public int executeUpdate() {
		try {
			return pstmt.executeUpdate();
		} catch (SQLException e) {
			close();
			throw new KingRuntimeException("执行sql出现异常", e);
		}
	}
	
	public int executeUpdate(String sql, List<Object> paramList){
		try {
			pstmt = conn.prepareStatement(sql,  Statement.RETURN_GENERATED_KEYS);
			int index = 0;
			for(Object obj : paramList){
				pstmt.setObject(++index, obj);
			}
			pstmt.executeUpdate();
			resultSet = pstmt.getGeneratedKeys();
			resultSet.next();
			return resultSet.getInt(1);
		} catch (SQLException e) {
			close();
			throw new KingRuntimeException("编译或执行sql出现异常", e);
		}
	}
	
	public ResultSet getResultSet(){
		return resultSet;
	}
	
	public void close(){
		if(resultSet != null){
			try {
				resultSet.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		if(pstmt != null){
			try {
				pstmt.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		if(conn != null){
			try {
				conn.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}
}

