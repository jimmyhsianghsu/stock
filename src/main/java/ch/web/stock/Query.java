package ch.web.stock;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;
public class Query extends HttpServlet {
	/** serialVersionUID */
	private static final long serialVersionUID = -898464044289862613L;
	@Autowired
	private StockService stockService;
	@Autowired
	private StockJobService stockJobService;
	@Autowired
	private DataSource dataSource;
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse res)throws ServletException,IOException {
		String action = req.getParameter("action");
		if ("getStockBySymbolMonth".equals(action)){
			printList(res.getWriter(), stockService.getStockBySymbolMonth(getSymbol(req), getYearMonth(req)), stockService.getStockStatusBySymbol(getSymbol(req)));
			res.getWriter().println(new DateServiceImpl().getYearMonth(getYearMonth(req)));
		} else if ("createTable".equals(action)){
			stockService.createTable();
		} else if ("getAll".equals(action)){
			printList(res.getWriter(), stockService.getStocksAll(), stockService.getStockStatusAll());
		} else if ("fromUrl".equals(action)){
			List<Stock> stocks = stockService.getStockBySymbolMonthUrl(getSymbol(req), getYearMonth(req));
			if (stocks != null){
				for (Stock stock : stocks){
					res.getWriter().println(stock);
					res.getWriter().println(stockService.saveStockTx(stock));
				}
			}
		} else if ("fromDb".equals(action)){
			printList(res.getWriter(), stockService.getStockBySymbolMonthDb(getSymbol(req), getYearMonth(req)), stockService.getStockStatusBySymbol(getSymbol(req)));
		} else {
		}
		if (stockJobService.start()){
			int symbol = getSymbol(req);
			if (symbol > 0){
				stockJobService.prepare(symbol);
			}
		}
		if ("resetStatus".equals(action)){
			stockJobService.resetStatus(getSymbol(req));
		} else if ("resetFailLimit".equals(action)){
			stockJobService.resetFailLimit(Integer.parseInt(req.getParameter("failLimit")));
		} else if ("getStockJobMap".equals(action)){
			res.getWriter().println(dataSource + " = " + stockService.testSqliteCount());
			res.getWriter().println(stockJobService.getRunCeasableList());
			res.getWriter().println(stockJobService.getStockJobMap());
			res.getWriter().println(new org.json.JSONArray(stockService.getStockBySymbolMonthDb(getSymbol(req), getYearMonth(req)).toArray()));
		}
		if ("getStockNows".equals(action)){
			String[] symbols = req.getParameterValues("symbols[]");
			if (symbols != null && symbols.length > 0){
				StockNow[] stockNowArray = new StockNow[symbols.length + 1];
				for (int i = 0; i < symbols.length; i++){
					stockNowArray[i] = new StockNow(Integer.parseInt(symbols[i]), StockService.STOCK_INFO_MAP.get(Integer.parseInt(symbols[i])).getExCh());
				}
				stockNowArray[symbols.length] = new StockNow(8086, StockService.EX_CH_OTC);
				res.setContentType("text/html; charset=UTF-8"); // text/html; charset=UTF-8
				Object[] stockNows = stockService.getStockNows(stockNowArray).toArray();
				JSONObject jObj = new org.json.JSONObject();
				jObj.put("total", stockNows.length);
				jObj.put("rows", stockNows);
				res.getWriter().println(jObj);
			}
		} else if ("getStockInfoAll".equals(action)){
			res.setContentType("text/html; charset=UTF-8"); // text/html; charset=UTF-8
			res.getWriter().print(stockService.getStockInfoAll());
		} else if ("getStockJobMapJson".equals(action)){
			res.getWriter().print(getStockJobMapJson());
		} else if ("getStockStatusBySymbolJson".equals(action)){
			int symbol = getSymbol(req);
			if (symbol > 0){
				res.getWriter().print(getStockStatusBySymbolJson(symbol));
			}
		} else if ("getStockBySymbolMonthJson".equals(action)){
			int symbol = getSymbol(req);
			int yearMonth = getYearMonth(req);
			if (symbol > 0){
				if (yearMonth == 0){
					yearMonth = Integer.parseInt(new java.text.SimpleDateFormat("yyyyMM").format(new java.util.Date()));
				}
				res.getWriter().print(getStockBySymbolMonthJson(symbol, yearMonth));
			}
		}
		if ("updateStockInfoBySymbol".equals(action)){
			int symbol = getSymbol(req);
			if (symbol > 0){
				res.setContentType("text/html; charset=UTF-8"); // text/html; charset=UTF-8
				res.getWriter().println(stockService.updateStockInfoBySymbol(symbol));
			}
		} else if ("saveStockInfoAll".equals(action)){
			res.getWriter().println(stockService.saveStockInfoAll());
		} else if ("getStockInfoAllHtml".equals(action)){
			res.setContentType("text/html; charset=UTF-8"); // text/html; charset=UTF-8
			res.getWriter().println(getStockInfoAllHtml());
		}
		if ("saveLevel".equals(action)){
			int symbol = getSymbol(req);
			int dateDay1 = 0;
			int dateDay2 = 0;
			try {
				dateDay1 = Integer.parseInt(req.getParameter("dateDay1"));
				dateDay2 = Integer.parseInt(req.getParameter("dateDay2"));
			} catch (RuntimeException re){
			}
			if (symbol > 0 && dateDay1 > 0 && dateDay2 > 0){
				StockInfo stockInfo = stockService.saveLevel(symbol, dateDay1, dateDay2);
				JSONObject jObj = new org.json.JSONObject();
				jObj.put("level", new org.json.JSONObject(stockInfo));
				res.getWriter().println(jObj);
			}
		} else if ("getInfo".equals(action)){
			int symbol = getSymbol(req);
			if (symbol > 0){
				StockInfo stockInfo = stockService.getStockInfoBySymbol(symbol);
				JSONObject jObj = new org.json.JSONObject();
				jObj.put("info", new org.json.JSONObject(stockInfo));
				res.getWriter().println(jObj);
			}
		} else if ("saveBsMax".equals(action)){
			int symbol = getSymbol(req);
			int bsMaxDateDay = 0;
			double bsMax = 0;
			try {
				bsMaxDateDay = Integer.parseInt(req.getParameter("bsMaxDateDay"));
				bsMax = Double.parseDouble(req.getParameter("bsMax"));
			} catch (RuntimeException re){
			}
			if (symbol > 0 && bsMaxDateDay > 0 && bsMax > 0){
				stockService.saveBsMax(symbol, bsMaxDateDay, bsMax);
			}
		}
		if("getGroup".equals(action)){
			String group = req.getParameter("group");
			if("T50".equals(group)){
				List<StockInfo> infos = stockService.getStockInfoAll();
				List<Integer> list = new ArrayList<Integer>();
				if (infos != null && infos.size() > 0){
					for (StockInfo stockInfo : infos){
						if (stockInfo.getName().indexOf("¡¯") > 0){
							list.add(stockInfo.getSymbol());
						}
					}
				}
				JSONObject jObj = new org.json.JSONObject();
				jObj.put("symbols", new org.json.JSONArray(list.toArray()));
				res.getWriter().println(jObj);
			}
		}
	}
	private int getSymbol(HttpServletRequest req){
		String symbol = req.getParameter("symbol");
		return symbol != null ? Integer.parseInt(symbol) : 0;
	}
	private int getYearMonth(HttpServletRequest req){
		String yearMonth = req.getParameter("yearMonth");
		return yearMonth != null ? Integer.parseInt(yearMonth) : 0;
	}
	private String getStockJobMapJson(){
		java.util.Map<Integer, StockJobProgress> stockJobMap = stockJobService.getStockJobMap();
		List<Map<String, String>> stockJobMapRows = new ArrayList<Map<String, String>>();
		if (stockJobMap != null && stockJobMap.size() > 0){
			for (Integer symbol : stockJobMap.keySet()){
				stockJobMapRows.add(stockJobMap.get(symbol).getMap());
			}
		}
		List<RunCeasable> runCeasableList = stockJobService.getRunCeasableList();
		String[] runCeasableArray = new String[runCeasableList.size()];
		for (int i = 0; i < runCeasableList.size(); i++){
			runCeasableArray[i] = runCeasableList.get(i).toString();
		}
		JSONObject jObj = new org.json.JSONObject();
		jObj.put("stockJobMapRows", stockJobMapRows.toArray());
		jObj.put("runCeasableList",runCeasableArray);
		return jObj.toString();
	}
	private String getStockStatusBySymbolJson(int symbol){
		List<StockStatus> list = stockService.getStockStatusBySymbol(symbol);
		if (list != null && list.size() > 0){
			JSONObject jObj = new org.json.JSONObject();
			jObj.put("total", list.size());
			jObj.put("rows", list.toArray());
			return jObj.toString();
		}
		return null;
	}
	private String getStockBySymbolMonthJson(int symbol, int yearMonth){
		List<Stock> stockList = stockService.getStockBySymbolMonth(symbol, yearMonth);
		if (stockList != null && stockList.size() > 0){
			StockStatus stockStatus = stockService.getStockStatusBySymbolMonth(symbol, yearMonth);
			JSONObject jObj = new org.json.JSONObject();
			jObj.put("total", stockList.size());
			jObj.put("rows", stockService.getStockListMap(stockList).toArray());
			jObj.put("status", stockStatus != null ? stockStatus.getStatus() : 0);
			return jObj.toString();
		}
		return null;
		
	}
	private String getStockInfoAllHtml(){
		List<StockInfo> list = stockService.getStockInfoAll();
		if (list != null && list.size() > 0){
			StringBuffer buffer = new StringBuffer("<table border='1' style='border-collapse:collapse'>");
			int i = 0;
			for (StockInfo info : list){
				buffer.append("<tr>").append("<td>").append(++i).append("</td>").append("<td>").append(info.getSymbol()).append("</td>")
					.append("<td>").append(info.getName()).append("</td>").append("<td>").append(info.getExCh()).append("</td>")
					.append("<td>").append(info.getDateDay1()).append("</td>").append("<td>").append(info.getDateDay2()).append("</td>")
					.append("<td>").append(info.getLevel1()).append("</td>").append("<td>").append(info.getLevel2()).append("</td>")
					.append("<td>").append(info.getLevel3()).append("</td>").append("<td>").append(info.getBsMaxDateDay()).append("</td>")
					.append("<td>").append(info.getBsMax()).append("</td>").append("</tr>");
			}
			buffer.append("</table>");
			return buffer.toString();
		}
		return null;
	}
	private void printList(java.io.PrintWriter writer, List<Stock> stocks, List<StockStatus> statusList){
		if (stocks != null){
			for (Stock stock : stocks){
				writer.println(stock);
			}
		}
		if (statusList != null){
			for (StockStatus status : statusList){
				writer.println(status);
			}
		}
	}
	@Override
	public void init(ServletConfig config) throws ServletException {
	    super.init(config);
	    SpringBeanAutowiringSupport.processInjectionBasedOnServletContext(this, config.getServletContext());
	  }
}