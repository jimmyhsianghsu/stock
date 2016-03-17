package ch.web.database;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import static ch.web.database.DatabaseDao.*; 
public class DatabaseDaoExt {
	private static String syntaxDateFormat = "yyyy-MM-dd HH:mm:ss.SSS";
	private static String syntaxDateSql = "YYYY-MM-DD HH:MI:SS.FF";
	private static String syntaxDateFormatShort = "yyyy-MM-dd";
	private static String syntaxDateSqlShort = "YYYY-MM-DD";
	private String url;
	private String username;
	private String password;
	public DatabaseDaoExt(String url, String username, String password){
		this.url = url;
		this.username = username;
		this.password = password;
	}
	private Connection getConn(){
		try {
			return DriverManager.getConnection(url, username, password);
		} catch (SQLException e) {
			LOG.error("getConn", e);
		}
		return null;
	}
	private List<String> insertSqls(String table, Map<String, String> colMap) {
		List<String> insertSqls = new ArrayList<String>();
		List<Map<String, String>> cols = getColumns(table);
		List<Map<String, String>> rows = null;
		if (cols != null && cols.size() > 0) {
			StringBuffer buffer = new StringBuffer("SELECT * FROM ").append(table).append(" WHERE ");
			boolean flag = false;
			for (Map<String, String> col : cols) {
				String colName = col.get("COLUMN_NAME");
				if (colMap.containsKey(colName) && colMap.get(colName) != null && !colMap.get(colName).trim().isEmpty()) {
					flag = true;
					buffer.append(colName).append("%".equals(colMap.get(colName)) ? " like '" : "='").append(colMap.get(colName)).append("' and ");
				}
			}
			if (!flag){
				flag = true;
				buffer.setLength(buffer.length() - 2);
			}
			if (flag) {
				buffer.setLength(buffer.length() - 4);
				rows = sqlExecute(buffer.toString() + ';');
				if(rows != null && rows.size() > 1)rows.remove(0);
			}
		}
		if (rows != null && rows.size() > 0) {
			for (Map<String, String> row : rows) {
				StringBuffer buffer3 = new StringBuffer("INSERT INTO ").append(table).append('(');
				StringBuffer buffer4 = new StringBuffer("VALUES(");
				for (Map<String, String> col : cols) {
					String colName = col.get("COLUMN_NAME");
					String colValue = row.get(colName);
					if (colValue != null && !"null".equals(colValue)) {
						if ("TIMESTAMP".equals(col.get("DATA_TYPE"))) {
							DateFormat format = new SimpleDateFormat(syntaxDateFormat);
							try {
								colValue = "to_date('" + format.format(format.parse(colValue)) + "','" + syntaxDateSql + "')";
							} catch (ParseException pe) {
								throw new RuntimeException(pe);
							}
						} else if ("DATE".equals(col.get("DATA_TYPE"))) {
							DateFormat format = new SimpleDateFormat(syntaxDateFormatShort);
							try {
								colValue = "to_date('" + format.format(format.parse(colValue)) + "','" + syntaxDateSqlShort + "')";
							} catch (ParseException pe) {
								throw new RuntimeException(pe);
							}
						} else {
							colValue = "'" + colValue + "'";
						}
					}
					buffer3.append(colName).append(',');
					buffer4.append(colValue).append(',');
				}
				buffer3.setLength(buffer3.length() - 1);
				buffer4.setLength(buffer4.length() - 1);
				buffer3.append(')');
				buffer4.append(");");
				buffer3.append(buffer4);
				insertSqls.add(buffer3.toString());
			}
		}
		return insertSqls;
	}
	public String createSqls(String... tables) {
		List<String> insertSqls = null;
		if (tables != null && tables.length > 0){
			for (String table : tables){
				List<Map<String, String>> cols = getColumns(table);
				Map<String, String> colMap = new HashMap<String, String>();
				if (cols != null && cols.size() > 0) {
					for (Map<String, String> map : cols)
						if (map.get("PK") != null && !map.get("PK").trim().isEmpty())
							colMap.put(map.get("COLUMN_NAME"), "%");
					if (insertSqls == null) {
						insertSqls = new ArrayList<String>();
					}
					insertSqls.addAll(insertSqls(table, colMap));
				}
			}
			if (insertSqls != null && insertSqls.size() > 0) {
				StringBuffer buffer = new StringBuffer();
				for (String sql : insertSqls) {
					buffer.append(sql).append('\n');
				}
				return buffer.toString();
			}
		}
		return null;
	}
	private static org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(DatabaseDaoExt.class);
	private void closeConn(Connection conn){
		if (conn != null){
			try {
				conn.close();
			} catch (SQLException e) {
				LOG.error("closeConn", e);
			} 
		}
	}
	public List<String> getTables() {
		List<String> list = null;
		Connection conn = null;
		try {
			conn = getConn();
			if (conn != null){
				ResultSet rs = conn.prepareStatement(SQL_TABLES).executeQuery();
				if (rs != null){
					list = new ArrayList<String>();
					while (rs.next())
						list.add(rs.getString(1));
				}
			}
		} catch (SQLException e) {
			LOG.error("getTables", e);
		} finally {
			closeConn(conn);
		}
		return list;
	}
	public List<Map<String, String>> getColumns(String table) {
		if (table != null){
			table = table.trim().toUpperCase();
		} else {
			return null;
		}
		List<Map<String, String>> list = null;
		Connection conn = null;
		try {
			conn = getConn();
			if (conn != null){
				PreparedStatement pstmt = conn.prepareStatement(SQL_COLUMNS);
				pstmt.setString(1, table);
				ResultSet rs = pstmt.executeQuery();
				if (rs != null){
					list = new ArrayList<Map<String, String>>();
					ResultSetMetaData rsmd = rs.getMetaData();
					while (rs.next()){
						Map<String, String> map = new LinkedHashMap<String, String>();
						for (int i = 1; i <= rsmd.getColumnCount(); i++){
							map.put(rsmd.getColumnLabel(i), rs.getString(i));
						}
						list.add(map);
					}
				}
			}
		} catch (SQLException e) {
			LOG.error("getColumns", e);
		} finally {
			closeConn(conn);
		}
		return list;
	}
	public List<Map<String, String>> getRows(String table) {
		if (table == null){
			return null;
		}
		List<Map<String, String>> list = null;
		Connection conn = null;
		try {
			conn = getConn();
			if (conn != null){
				ResultSet rs = conn.prepareStatement("select * from " + table).executeQuery();
				if (rs != null){
					list = new ArrayList<Map<String, String>>();
					ResultSetMetaData rsmd = rs.getMetaData();
					while (rs.next()){
						Map<String, String> map = new LinkedHashMap<String, String>();
						for (int i = 1; i <= rsmd.getColumnCount(); i++){
							map.put(rsmd.getColumnName(i), rs.getString(i));
						}
						list.add(map);
					}
				}
			}
		} catch (SQLException e) {
			LOG.error("getRows", e);
		} finally {
			closeConn(conn);
		}
		return list;
	}
	public List<Map<String, String>> getRows(String table, String column, String value) {
		return null;
	}
	public List<Map<String, String>> sqlExecute(String sql) {
		if (sql == null || sql.indexOf(';') == -1){
			return null;
		}
		sql = sql.replace(";", "").replaceAll("[\\r\\n]", " ").replaceAll("[ \\xA0]+", " ").trim();
		List<Map<String, String>> list = new ArrayList<Map<String, String>>();
		Map<String, String> map = new LinkedHashMap<String, String>();
		map.put(SQL_SQL, sql + ";\n");
		list.add(map);
		Connection conn = null;
		if (!sql.isEmpty())
			try {
				conn = getConn();
				if (conn != null){
					PreparedStatement pstmt = conn.prepareStatement(sql);
					if (pstmt.execute()){
						ResultSet rs = pstmt.getResultSet();
						ResultSetMetaData rsmd = rs.getMetaData();
						int count = 0;
						while (rs.next()){
							count++;
							Map<String, String> row = new LinkedHashMap<String, String>();
							for (int i = 1; i <= rsmd.getColumnCount(); i++)
								row.put(rsmd.getColumnLabel(i), rs.getString(i));
							list.add(row);
						}
						map.put(SQL_SUCCESS, sql.substring(0, sql.indexOf(" ")) + SQL_SEPARATOR + count);
					} else
						map.put(SQL_SUCCESS, sql.substring(0, sql.indexOf(" ")) + SQL_SEPARATOR + pstmt.getUpdateCount());
				}
			} catch (SQLException sqle) {
				StringBuffer sqleSb = new StringBuffer(sqle.getLocalizedMessage()).append(SQL_NEWLINE).append(SQL_NEWLINE);
				do {
					sqleSb.append("ErrorCode : ").append(sqle.getErrorCode()).append(SQL_NEWLINE);
					sqleSb.append("SQLState : ").append(sqle.getSQLState()).append(SQL_NEWLINE);
					sqleSb.append("Message : ").append(sqle.getMessage()).append(SQL_NEWLINE).append(SQL_NEWLINE);
				} while ((sqle = sqle.getNextException()) != null);
				map.put(SQL_FAIL, sqleSb.toString());
				LOG.error("sqlExecute", sqle);
			} catch (RuntimeException re){
				LOG.error("sqlExecute", re);
			} finally {
				closeConn(conn);
			}
		return list;
	}
}