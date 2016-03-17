package ch.web.database;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
@Repository
public class DatabaseDaoImpl implements DatabaseDao {
	private static org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(DatabaseDaoImpl.class);
	@Autowired
	private DataSource dataSource;
	private void closeConn(Connection conn){
		if (conn != null){
			try {
				conn.close();
			} catch (SQLException e) {
				LOG.error("closeConn", e);
			} 
		}
	}
	@Override
	public List<String> getTables() {
		List<String> list = null;
		Connection conn = null;
		try {
			conn = dataSource.getConnection();
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
	@Override
	public List<Map<String, String>> getColumns(String table) {
		if (table != null){
			table = table.trim().toUpperCase();
		} else {
			return null;
		}
		List<Map<String, String>> list = null;
		Connection conn = null;
		try {
			conn = dataSource.getConnection();
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
	@Override
	public List<Map<String, String>> getRows(String table) {
		if (table == null){
			return null;
		}
		List<Map<String, String>> list = null;
		Connection conn = null;
		try {
			conn = dataSource.getConnection();
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
	@Override
	public List<Map<String, String>> getRows(String table, String column, String value) {
		return null;
	}
	@Override
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
				conn = dataSource.getConnection();
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