package ch.web.mvc;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ch.web.database.DatabaseDao;
import ch.web.database.DatabaseDaoExt;
@RestController
public class DatabaseController {
	@Autowired
	private DatabaseDao databaseDao;
	@RequestMapping(value = "/db/tables", method = RequestMethod.GET)
	public Object getTables(){
		return databaseDao.getTables();
	}
	@RequestMapping(value = "/db/{table}", method = RequestMethod.GET)
	public Object getColumns(@PathVariable("table") String table){
		return databaseDao.getColumns(table);
	}
	@RequestMapping(value = "/db/{table}/rows", method = RequestMethod.GET)
	public Object getRows(@PathVariable("table") String table){
		return databaseDao.getRows(table);
	}
	@RequestMapping(value = "/db/execute", method = RequestMethod.POST)
	public Object execute(@RequestParam(value = "sqls", required = true) String sqls){
		if (sqls != null && sqls.indexOf(';') != -1){
			List<List<Map<String, String>>> list = new ArrayList<List<Map<String, String>>>();
			for (String sql : sqls.split(";"))
				list.add(databaseDao.sqlExecute(sql + ';'));
			return list;
		}
		return null;
	}
	@RequestMapping(value = "/db/ext/tables", method = RequestMethod.GET)
	public Object getTablesExt(@RequestParam(value = "params", required = true) String params){
		return getDatabaseExt(params).getTables();
	}
	@RequestMapping(value = "/db/ext/createSqls", method = RequestMethod.POST)
	public List<String> createSqls(@RequestParam(value = "params", required = true) String params,
			@RequestParam(value = "table", required = true) String table){
		List<String> results = new ArrayList<String>();
		for (String sql : getDatabaseExt(params).createSqls(table).split(";"))
			results.add(sql + ';');
		return results;
	}
	@RequestMapping(value = "/db/ext/transfer", method = RequestMethod.POST)
	public List<String> transferExt(@RequestParam(value = "params1", required = true) String params1,
			@RequestParam(value = "params2", required = true) String params2,
			@RequestParam(value = "table1", required = true) String table1){
		List<String> results = new ArrayList<String>();
		DatabaseDaoExt dbExt2 = getDatabaseExt(params2);
		for (String sql : createSqls(params1, table1)){
			results.add(dbExt2.sqlExecute(sql).toString());
		}
		return results;
	}
	private DatabaseDaoExt getDatabaseExt(String params){
		String[] paramAry = params.split(",");
		return new DatabaseDaoExt(paramAry[0], paramAry[1], paramAry.length > 2 ? paramAry[2] : "");
	}
}