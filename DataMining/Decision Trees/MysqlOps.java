package DataMining.Assignment;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;



public class MysqlOps {
	
	Connection con = null;
    Statement st = null;
    ResultSet rs = null;
    String url = "", user = "",passwd = "";
    
    
	public MysqlOps() {
		
		url = "jdbc:mysql://localhost:3306/Classification";
		user = "root";
		passwd = "quititdude";
	}
	
	public void connect() throws SQLException{
		
		con = DriverManager.getConnection(url,user,passwd);
		
	}
	
	public void LoadDataIntoDB(String path,List<String> columns) {
		
		Statement st;
		try {
			
			st = con.createStatement();
			st.execute("DROP TABLE IF EXISTS table1");
			st.execute(createTableQuery(columns));
			String loadQuery = String.format("LOAD DATA LOCAL INFILE '%s' INTO TABLE table1 FIELDS TERMINATED BY '\\t' LINES TERMINATED BY '\\r\\n' IGNORE 1 LINES (%s)", path,String.join(",", columns));
			st.execute(loadQuery);
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
	}
	
	public String createTableQuery(List<String> columns) {
		StringBuilder query = new StringBuilder();
		query.append("CREATE TABLE table1(");
		query.append(" tupleId MEDIUMINT NOT NULL AUTO_INCREMENT,");
		query.append(String.join(" varchar(20)," , columns));
		query.append(" varchar(20), primary key(tupleId) )");
		return query.toString();	
	}
	
	
	
	public List<String> GetUniqueValues(String column,String tableName) {
		
		List<String> resList = new ArrayList<String>();
		String query = String.format("SELECT DISTINCT(%s) from %s",column,tableName);
		try {
			st = con.createStatement();
			ResultSet rs = st.executeQuery(query);
			while(rs.next())
				resList.add(rs.getString(1));
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return resList;
	}
	
	public int getCount(String queryCond,String tableName){
		Integer count = 0;
		String query = String.format("SELECT count(*) from (%s) WHERE %s",tableName, queryCond);
		try {
			st = con.createStatement();
			ResultSet rs = st.executeQuery(query);
			while(rs.next()) {
				count = rs.getInt(1); break;
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return count;
	}
	
	public void createView(String queryCond,String view) {
		
		String query = String.format("CREATE OR REPLACE VIEW %s AS SELECT * FROM table1 WHERE tupleId in (%s)",view,queryCond);
		try {
			st = con.createStatement();
			st.executeUpdate(query);		
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public List<String> GetData(String tableName,String column) {
		
		List<String> resList = new ArrayList<String>();
		String query = String.format("SELECT %s from %s",column,tableName);
		try {
			st = con.createStatement();
			ResultSet rs = st.executeQuery(query);
			while(rs.next())
				resList.add(rs.getString(1));
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return resList;
	}
	
}
