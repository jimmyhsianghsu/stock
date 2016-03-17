package ch.web.database;
import java.util.List;
import java.util.Map;
public interface DatabaseDao {
	String SQL_TABLES = "select table_name from INFORMATION_SCHEMA.tables where table_schema='PUBLIC' order by table_name";
	String SQL_COLUMNS = "select c.table_name, c.column_name, c.ordinal_position, c.data_type, " +
			"pks.constraint_type PK, fks.constraint_type FK, fks.p_table_name, fks.p_column_name from INFORMATION_SCHEMA.columns c " +
			"left join " +
			"(select kcu.table_name, kcu.column_name, tc.constraint_type from INFORMATION_SCHEMA.KEY_COLUMN_USAGE kcu " +
			"join INFORMATION_SCHEMA.TABLE_CONSTRAINTS tc on tc.constraint_name = kcu.constraint_name and tc.constraint_type = 'PRIMARY KEY') pks " +
			"on pks.table_name = c.table_name and pks.column_name = c.column_name " +
			"left join " +
			"(select kcu.table_name, kcu.column_name, tc.constraint_type, kcu1.table_name p_table_name, kcu1.column_name p_column_name " +
			"from INFORMATION_SCHEMA.KEY_COLUMN_USAGE kcu " +
			"join INFORMATION_SCHEMA.TABLE_CONSTRAINTS tc on tc.constraint_name = kcu.constraint_name and tc.constraint_type = 'FOREIGN KEY' " +
			"join INFORMATION_SCHEMA.REFERENTIAL_CONSTRAINTS rc on rc.constraint_name = kcu.constraint_name " +
			"join INFORMATION_SCHEMA.KEY_COLUMN_USAGE kcu1 on kcu1.constraint_name = rc.unique_constraint_name " +
			"and kcu1.ORDINAL_POSITION = kcu.POSITION_IN_UNIQUE_CONSTRAINT) fks " +
			"on fks.table_name = c.table_name and fks.column_name = c.column_name " +
			"where table_schema='PUBLIC' and table_name = ? order by table_name, ordinal_position";
	List<String> getTables();
	List<Map<String, String>> getColumns(String table);
	List<Map<String, String>> getRows(String table);
	List<Map<String, String>> getRows(String table, String column, String value);
	List<Map<String, String>> sqlExecute(String sql);
	String SQL_SQL = "SQL";
	String SQL_SUCCESS = "SUCCESS";
	String SQL_FAIL = "FAIL";
	String SQL_SEPARATOR = " => ";
	String SQL_NEWLINE = "<br/>";
}