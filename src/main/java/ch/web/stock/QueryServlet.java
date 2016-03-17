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
public class QueryServlet extends HttpServlet {
	private static org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(QueryServlet.class);
	/** serialVersionUID */
	private static final long serialVersionUID = -898464044289862613L;
	@Autowired
	private StockService stockService;
	@Autowired
	private StockJobService stockJobService;
	@Autowired
	private DateService dateService;
	@Autowired
	private DataSource dataSource;
	@Autowired private ch.web.stock.cache.StockCacheService stockCacheService;
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse res)throws ServletException,IOException {
		String action = req.getParameter("action");
		int symbol = getSymbol(req);
		int yearMonth = getYearMonth(req);
		LOG.info("action=" + action + ",symbol=" + symbol + ",yearMonth=" + yearMonth);
		if (stockJobService.start()){
			if (symbol > 0){
				 // stockJobService.prepare(symbol);
			}
			if ("resetStatus".equals(action)){
				stockJobService.resetStatus(getSymbol(req));
			} else if ("resetFailLimit".equals(action)){
				stockJobService.resetFailLimit(Integer.parseInt(req.getParameter("failLimit")));
			} else if ("getStockJobMapJson".equals(action)){
				res.getWriter().print(getStockJobMapJson());
			} else if ("getStockNows".equals(action)){
				res.setContentType("text/html; charset=UTF-8");
				res.getWriter().print(getStockNowsJson(req.getParameterValues("symbols[]")));
			} else if ("getStockBySymbolMonthJson".equals(action) && symbol > 0){
				if (yearMonth == 0){
					yearMonth = Integer.parseInt(new java.text.SimpleDateFormat("yyyyMM").format(new java.util.Date()));
				}
				res.getWriter().print(getStockBySymbolMonthJson(symbol, yearMonth, 3));
			} else if ("getStockStatusBySymbolJson".equals(action) && symbol > 0){
				res.getWriter().print(getStockStatusBySymbolJson(symbol));
			} else if ("saveLevel".equals(action) && symbol > 0){
				int dateDay1 = 0;
				int dateDay2 = 0;
				try {
					dateDay1 = Integer.parseInt(req.getParameter("dateDay1"));
					dateDay2 = Integer.parseInt(req.getParameter("dateDay2"));
				} catch (RuntimeException re){
				}
				if (dateDay1 > 0 && dateDay2 > 0){
					StockInfo stockInfo = stockService.saveLevel(symbol, dateDay1, dateDay2);
					JSONObject jObj = new org.json.JSONObject();
					jObj.put("level", new org.json.JSONObject(stockInfo));
					res.getWriter().println(jObj);
				}
			} else if ("getInfo".equals(action) && symbol > 0){
					StockInfo stockInfo = stockService.getStockInfoBySymbol(symbol);
					JSONObject jObj = new org.json.JSONObject();
					jObj.put("info", new org.json.JSONObject(stockInfo));
					res.getWriter().println(jObj);
			} else if ("saveBsMax".equals(action) && symbol > 0){
				int bsMaxDateDay = 0;
				double bsMax = 0;
				try {
					bsMaxDateDay = Integer.parseInt(req.getParameter("bsMaxDateDay"));
					bsMax = Double.parseDouble(req.getParameter("bsMax"));
				} catch (RuntimeException re){
				}
				if (bsMaxDateDay > 0 && bsMax > 0){
					stockService.saveBsMax(symbol, bsMaxDateDay, bsMax);
				}
			} else if("getGroup".equals(action)){
				String group = req.getParameter("group");
				if (group == null || group.trim().isEmpty()){
					res.getWriter().println(new org.json.JSONArray(stockService.getGroup().toArray()));
				} else
				if("T50".equals(group)){
					List<StockInfo> infos = stockService.getStockInfoAll();
					List<Integer> list = new ArrayList<Integer>();
					if (infos != null && infos.size() > 0){
						for (StockInfo stockInfo : infos){
							if (stockInfo.getName().indexOf("＊") > 0){
								list.add(stockInfo.getSymbol());
							}
						}
					}
					JSONObject jObj = new org.json.JSONObject();
					jObj.put("symbols", new org.json.JSONArray(list.toArray()));
					res.getWriter().println(jObj);
				} else if("T100".equals(group)){
					List<StockInfo> infos = stockService.getStockInfoAll();
					List<Integer> list = new ArrayList<Integer>();
					if (infos != null && infos.size() > 0){
						for (StockInfo stockInfo : infos){
							if (stockInfo.getName().indexOf("＃") > 0){
								list.add(stockInfo.getSymbol());
							}
						}
					}
					JSONObject jObj = new org.json.JSONObject();
					jObj.put("symbols", new org.json.JSONArray(list.toArray()));
					res.getWriter().println(jObj);
				} else if("topV20".equals(group)){
					JSONObject jObj = new org.json.JSONObject();
					jObj.put("symbols", new org.json.JSONArray(stockService.getTopV20().toArray()));
					res.getWriter().println(jObj);
				} else {
					String value = stockService.getGroup(group);
					if (value != null && !value.trim().isEmpty()){
						java.util.Set<Integer> set = new java.util.HashSet<Integer>();
						String[] values = value.split(",");
						for (int i = 0; i < values.length; i++){
							try {
								set.add(Integer.parseInt(values[i].trim()));
							} catch (RuntimeException re){
							}
						}
						if (set != null && set.size() > 0){
							JSONObject jObj = new org.json.JSONObject();
							jObj.put("symbols", new org.json.JSONArray(set.toArray()));
							res.getWriter().println(jObj);
						}
					}
				}
			} else if("addGroup".equals(action)){
				stockService.addGroup(req.getParameter("group"), req.getParameter("value"));
				res.getWriter().println(getGroupMapJson());
			} else if("removeGroup".equals(action)){
				String group = req.getParameter("group");
				String value = req.getParameter("value");
				if (value != null){
					stockService.removeGroup(group, value);
				} else {
					stockService.removeGroup(group);
				}
				res.getWriter().println(getGroupMapJson());
			} else if ("saveRecord".equals(action) && symbol > 0){
				try {
					int dateDay1 = Integer.parseInt(req.getParameter("dateDay1"));
					double price1 = Double.parseDouble(req.getParameter("price1"));
					int counts = Integer.parseInt(req.getParameter("counts"));
					int buySell = Integer.parseInt(req.getParameter("buySell"));
					stockService.saveStockRecord(symbol, dateDay1, price1, counts, buySell);
					res.setContentType("text/html; charset=UTF-8");
					res.getWriter().print(getStockRecordAllJson());
				} catch (RuntimeException re){
					JSONObject jObj = new org.json.JSONObject();
					jObj.put("err", re.getLocalizedMessage());
					res.getWriter().print(jObj);
				}
			} else if ("getRecords".equals(action)){
				res.setContentType("text/html; charset=UTF-8");
				res.getWriter().print(getStockRecordAllJson());
			} else if ("removeRecord".equals(action)){
				StockRecord stockRecord = stockService.removeStockRecord(Integer.parseInt(req.getParameter("id")));
				res.getWriter().print(new org.json.JSONObject(stockRecord));
			} else if ("updateRecord".equals(action)){
				try {
					long id = Long.parseLong(req.getParameter("id"));
					int dateDay2 = Integer.parseInt(req.getParameter("dateDay2"));
					double price2 = Double.parseDouble(req.getParameter("price2"));
					int counts = Integer.parseInt(req.getParameter("counts"));
					stockService.updateStockRecord(id, dateDay2, price2, counts);
					res.setContentType("text/html; charset=UTF-8");
					res.getWriter().print(getStockRecordAllJson());
				} catch (RuntimeException re){
					JSONObject jObj = new org.json.JSONObject();
					jObj.put("err", re.getLocalizedMessage());
					res.getWriter().print(jObj);
				}
			} else if ("updateStockInfoBySymbol".equals(action) && symbol > 0){
				res.setContentType("text/html; charset=UTF-8");
				res.getWriter().println(stockService.updateStockInfoBySymbol(symbol));
			} else if ("saveStockInfoAll".equals(action)){
				res.getWriter().println(stockService.saveStockInfoAll());
			} else if ("getStockInfoAllHtml".equals(action)){
				res.setContentType("text/html; charset=UTF-8");
				res.getWriter().println(getStockInfoAllHtml());
			}
		}
		if ("createTable".equals(action)){
			stockService.createTable();
		} else if ("getAll".equals(action)){
			printList(res.getWriter(), stockService.getStocksAll(), stockService.getStockStatusAll());
		} else if ("getStockInfoAll".equals(action)){
			res.setContentType("text/html; charset=UTF-8");
			res.getWriter().print(stockService.getStockInfoAll());
		} else if ("getStockBySymbolMonth".equals(action) && symbol > 0 && yearMonth > 0){
			printList(res.getWriter(), stockService.getStockBySymbolMonth(symbol, yearMonth), stockService.getStockStatusBySymbol(symbol));
			res.getWriter().println(new DateServiceImpl().getYearMonth(getYearMonth(req)));
		} else if ("fromUrl".equals(action) && symbol > 0 && yearMonth > 0){
			List<Stock> stocks = stockService.getStockBySymbolMonthUrl(symbol, yearMonth);
			if (stocks != null){
				for (Stock stock : stocks){
					res.getWriter().println(stock);
					res.getWriter().println(stockService.saveStockTx(stock));
				}
			}
		} else if ("fromDb".equals(action) && symbol > 0 && yearMonth > 0){
			stockService.computeBySymbol(symbol);
			printList(res.getWriter(), stockService.getStockBySymbolMonthDb(symbol, yearMonth), stockService.getStockStatusBySymbol(symbol));
		} else if ("getStockJobMap".equals(action) && symbol > 0 && yearMonth > 0){
			res.getWriter().println(dataSource + " = " + stockService.testSqliteCount());
			res.getWriter().println(stockJobService.getRunCeasableList());
			res.getWriter().println(stockJobService.getStockJobMap());
			res.getWriter().println(new org.json.JSONArray(stockService.getStockBySymbolMonthDb(symbol, yearMonth).toArray()));
		} else if ("getYearWeekMap".equals(action) && yearMonth > 0){
			Map<Integer, List<Integer>> yearWeekMap = dateService.getYearWeekMap(yearMonth);
			if (yearWeekMap != null && yearWeekMap.size() > 0){
				for (int yearWeek : yearWeekMap.keySet()){
					res.getWriter().println(yearWeek + "=");
					for (int day : yearWeekMap.get(yearWeek)){
						res.getWriter().print(day + ",");
					}
					res.getWriter().println();
				}
			}
		} else if ("getComputeMap".equals(action)){
			Map<String, String> map = stockCacheService.getComputeMap();
			if (map != null && map.size() > 0){
				for (String key : map.keySet()){
					res.getWriter().println(key + ':' + map.get(key));
				}
			}
			String extend = req.getParameter("extend");
			if ("start".equals(extend)){
				stockCacheService.start();
			} else if ("stop".equals(extend)){
				stockCacheService.stop();
			}
		} else if ("getComputeMapJson".equals(action)){
			String extend = req.getParameter("extend");
			res.getWriter().print(new JSONObject().put("computeMap", stockCacheService.getComputeMap()).put("extend", extend).toString());
			if ("start".equals(extend)){
				stockCacheService.start();
			} else if ("stop".equals(extend)){
				stockCacheService.stop();
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
		List<Stock> stockList = stockCacheService.getComputeTx(symbol, yearMonth); // stockService.getStockBySymbolMonth(symbol, yearMonth);
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
	private String getStockBySymbolMonthJson(int symbol, int yearMonth, int month){
		List<Map<String, String>> stockList = stockService.getStockComputedNows(symbol, yearMonth, month);
		if (stockList != null && stockList.size() > 0){
			JSONObject jObj = new org.json.JSONObject();
			jObj.put("rows", stockList.toArray());
			return jObj.toString();
		}
		return null;
	}
	private String getStockNowsJson(String... symbols){
		if (symbols != null && symbols.length > 0){
			StockNow[] stockNowArray = new StockNow[symbols.length];
			for (int i = 0; i < symbols.length; i++){
				int symbol = Integer.parseInt(symbols[i]);
				stockNowArray[i] = new StockNow(symbol, StockService.STOCK_INFO_MAP.get(symbol).getExCh());
			}
			List<StockNow> stockNowList = stockService.getStockNows(stockNowArray);
			if (stockNowList == null || stockNowList.size() == 0) stockNowList = stockService.getStockNowsOffline(stockNowArray);
			Object[] stockNows = stockService.getStockNowsComputed(stockNowList).toArray();
			JSONObject jObj = new org.json.JSONObject();
			jObj.put("total", stockNows.length);
			jObj.put("rows", stockNows);
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
	private String getGroupMapJson(){
		JSONObject jObj = new org.json.JSONObject();
		List<String> list = stockService.getGroup();
		if (list != null && list.size() > 0){
			for (String group : list){
				jObj.put(group, stockService.getGroup(group));
			}
		}
		return jObj.toString();
	}
	private String getStockRecordAllJson(){
		JSONObject jObj = new org.json.JSONObject();
		List<StockRecord> list = stockService.getStockRecordAll();
		if (list != null && list.size() > 0){
			StockNow[] stockNowArray = new StockNow[list.size()];
			for (int i = 0; i < list.size(); i++){
				int symbol = list.get(i).getSymbol();
				stockNowArray[i] = new StockNow(symbol, StockService.STOCK_INFO_MAP.get(symbol).getExCh());
			}
			List<StockNow> stockNows = stockService.getStockNows(stockNowArray);
			Map<Integer, StockNow> stockNowMap = new java.util.HashMap<Integer, StockNow>();
			for (StockNow stockNow : stockNows){
				stockNowMap.put(stockNow.getC(), stockNow);
			}
			for (StockRecord stockRecord : list){
				org.json.JSONObject rObj = new org.json.JSONObject(stockRecord);
				StockNow stockNow = stockNowMap.get(stockRecord.getSymbol());
				if (stockNow != null){
					if (stockRecord.getDateDay2() == null || stockRecord.getDateDay2() == 0){
						rObj.put("price2", stockNow.getZ());
					}
					rObj.put("name", stockNow.getN());
				}
				jObj.put(stockRecord.getId().toString(), rObj);
			}
		}
		return jObj.toString();
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
	    System.setOut(createLoggingProxy(System.out));
        System.setErr(createLoggingProxy(System.err));
	  }
	private java.io.PrintStream createLoggingProxy(final java.io.PrintStream realPrintStream) {
		return new java.io.PrintStream(realPrintStream) {
			public void print(final String string) {
				realPrintStream.print(string);
				LOG.info(string);
			}
		};
	}
}