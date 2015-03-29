package dataMining.AssignmentTwo;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MysqlConnector {

	Connection con = null;
    Statement st = null;
    ResultSet rs = null;
    String url = "", user = "",passwd = "";
    
	public MysqlConnector(){
		
		url = "jdbc:mysql://localhost:3306/MovieLens";
		user = "root";
		passwd = "quititdude";
	}
	
	public void connect() throws SQLException{
		
		con = DriverManager.getConnection(url,user,passwd);
			
	}
	
	public List<Cluster> getData() throws SQLException {
				
		List<ResultSet> rsList = new ArrayList<ResultSet>();
		String query;
		for(int i = 0; i < 50; i++) {
			query = String.format("Select age,occupation,rating,genres,Gender from CongregatedData LIMIT 1000 OFFSET %d",i*1000);
			st = con.createStatement();
			rs = st.executeQuery(query);
			rsList.add(rs);
		}		
		return getListFromResultSet(rsList);
	}
	
	public List<Cluster> getListFromResultSet(List<ResultSet> rsList) throws SQLException{
		
		List<Cluster> items = new ArrayList<Cluster>();
		Entity entity;Cluster cluster;
		for(ResultSet rs1: rsList) {
			while (rs1.next()) {
				//entity = new Entity(rs1.getInt(1),rs1.getInt(2),rs1.getInt(3),rs1.getInt(4),rs1.getString(5),rs1.getInt(6));
				entity = new Entity(rs1.getInt(1),rs1.getInt(2),rs1.getDouble(3),rs1.getString(4),rs1.getString(5).charAt(0));
				cluster = new Cluster();
				cluster.clusterRepresentaive = entity; 
				items.add(cluster);
			}
		}
		return items;
	}
	
	
	public void disconnect() throws SQLException{
		con.close();
	}
}
