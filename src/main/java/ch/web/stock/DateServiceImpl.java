package ch.web.stock;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import org.springframework.stereotype.Service;
@Service
public class DateServiceImpl implements DateService{
	private String cookie;
	private Comparator<Stock> stockComparator = new Comparator<Stock>(){
		@Override
		public int compare(Stock stock1, Stock stock2) {
			if (stock1.getSymbol() != stock2.getSymbol()){
				return stock1.getSymbol() - stock2.getSymbol();
			} else {
				if (stock1.getDateDay().equals(stock2.getDateDay())) return 0;
				return stock1.getDateDay().after(stock2.getDateDay()) ? 1 : -1;
			}
		}
	};
	private Comparator<StockStatus> stockStatusComparator = new Comparator<StockStatus>(){
		@Override
		public int compare(StockStatus status1, StockStatus status2) {
			if (status1.getSymbol() != status2.getSymbol()){
				return status1.getSymbol() - status2.getSymbol();
			} else {
				return status1.getYearMonth() - status2.getYearMonth();
			}
		}
	};
	@Override
	public Date getDateDay(String strDate) {
		Date date = null;
		if (strDate != null) {
			strDate = strDate.trim();
			strDate = strDate.replace("��", "");
			if (strDate.matches("\\d{2}/\\d{2}/\\d{2}")) {
				strDate = '0' + strDate;
			}
			if (strDate.matches("\\d{3}/\\d{2}/\\d{2}")) {
				DateFormat format = new SimpleDateFormat("yyyyMMdd");
				int year = Integer.parseInt(strDate.substring(0, 3)) + 1911;
				String month = strDate.substring(4, 6);
				String day = strDate.substring(7, 9);
				try {
					date = format.parse(year + month + day);
				} catch (ParseException e) {
					e.printStackTrace();
				}
			} else if (strDate.matches("\\d{8}")) {
				try {
					date = new SimpleDateFormat("yyyyMMdd").parse(strDate);
				} catch (ParseException e) {
					e.printStackTrace();
				}
			}
		}
		return date;
	}
	@Override
	public Date getYearMonth(int yearMonth){
		String strYearMonth = Integer.toString(yearMonth);
		if (strYearMonth.length() == 6){
			int month = Integer.parseInt(strYearMonth.substring(4, 6));
			if (month >= 1 && month <= 12){
			} else if (month == 0){
				strYearMonth = Integer.toString(Integer.parseInt(strYearMonth.substring(0, 4)) - 1) + "12";
			} else if (month == 13){
				strYearMonth = Integer.toString(Integer.parseInt(strYearMonth.substring(0, 4)) + 1) + "01";
			} else {
				return null;
			}
			DateFormat format = new SimpleDateFormat("yyyyMM");
			Date date = null;
			try {
				date = format.parse(strYearMonth);
			} catch (ParseException e) {
				e.printStackTrace();
			}
			return date;
		}
		return null;
	}
	@Override
	public Date getCurrentYearMonth(){
		DateFormat format = new SimpleDateFormat("yyyyMM");
		try {
			return format.parse(format.format(new Date()));
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return null;
	}
	@Override
	public Date getCurrentDay(){
		DateFormat format = new SimpleDateFormat("yyyyMMdd");
		try {
			return format.parse(format.format(new Date()));
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return null;
	}
	@Override
	public int getRocDate(Date date) {
		if (date != null){
			DateFormat format = new SimpleDateFormat("yyyyMMdd");
			String strDate = format.format(date);
			String year = strDate.substring(0, 4);
			int rocYear = Integer.parseInt(year) - 1911;
			return Integer.parseInt(rocYear + strDate.substring(4, 8));
		}
		return 0;
	}
	@Override
	public String getHtml(String url, String format) {
		if (url.startsWith("http://mis.twse.com.tw/stock/api/getStockInfo.jsp"))
			return "";
		StringBuffer sb = new StringBuffer();
		BufferedReader br = null;
		try {
			URL stockUrl = new URL(url);
			java.net.URLConnection urlConnection = stockUrl.openConnection();
			if (cookie != null)
				urlConnection.setRequestProperty("Cookie", cookie);
			String setCookie = urlConnection.getHeaderField("Set-Cookie");
			if (setCookie != null && setCookie.indexOf("JSESSIONID") != -1)
				cookie = setCookie.substring(0, setCookie.indexOf(";"));
			System.out.println("[cookie]=" + cookie);
			br = new BufferedReader(new InputStreamReader(urlConnection.getInputStream(), format));
			String line = null;
			while ((line = br.readLine()) != null) {
				sb.append(line);
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (br != null){
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		String html = sb.toString();
		return html != null && !html.trim().isEmpty() ? html : null;
	}
	@Override
	public List<Integer> getYearMonthList(int months){
		List<Integer> list = new ArrayList<Integer>();
		DateFormat format = new SimpleDateFormat("yyyyMM");
		int yearMonth = Integer.parseInt(format.format(new Date()));
		list.add(yearMonth);
		for (int i = 1; i < months; i++){
			yearMonth = rightYearMonth(yearMonth - 1);
			list.add(yearMonth);
		}
		return list;
	}
	private int rightYearMonth(int yearMonth){
		String strYearMonth = Integer.toString(yearMonth);
		if (strYearMonth.length() == 6){
			int month = Integer.parseInt(strYearMonth.substring(4, 6));
			if (month >= 1 && month <= 12){
			} else if (month == 0){
				strYearMonth = Integer.toString(Integer.parseInt(strYearMonth.substring(0, 4)) - 1) + "12";
			} else if (month == 13){
				strYearMonth = Integer.toString(Integer.parseInt(strYearMonth.substring(0, 4)) + 1) + "01";
			}
			return Integer.parseInt(strYearMonth);
		}
		return 0;
	}
	@Override
	public List<Stock> sortStock(List<Stock> stocks){
		if (stocks != null && stocks.size() > 0){
			synchronized (stocks){
			Collections.sort(stocks, stockComparator);
			}
		}
		return stocks;
	}
	@Override
	public List<StockStatus> sortStockStatus(List<StockStatus> stockStatus){
		if (stockStatus != null && stockStatus.size() > 0){
			Collections.sort(stockStatus, stockStatusComparator);
		}
		return stockStatus;
	}
	@Override
	public Date getDateDay(int dateDay){
		Date date = null;
		String strDate = Integer.toString(dateDay);
		if (strDate.length() < 7)strDate = "0" + strDate;
		if (strDate.matches("\\d{7}")) {
			DateFormat format = new SimpleDateFormat("yyyyMMdd");
			int year = Integer.parseInt(strDate.substring(0, 3)) + 1911;
			String month = strDate.substring(3, 5);
			String day = strDate.substring(5, 7);
			try {
				date = format.parse(year + month + day);
			} catch (ParseException e) {
				e.printStackTrace();
			}
		}
		return date;
	}
	@Override
	public String getYesterday(String today){
		DateFormat sdf = new SimpleDateFormat("yyyyMMdd");
		if (today == null){
			return sdf.format(new Date());
		}
		today = today.trim();
		if (today.matches("\\d{8}")){
			int dayTime = 1000 * 60 * 60 * 24;
			try {
				return sdf.format(new Date(sdf.parse(today).getTime() - dayTime));
			} catch (ParseException e) {
			}
		}
		return null;
	}
	@Override
	public java.util.Map<Integer, List<Integer>> getYearWeekMap(int year){
		java.util.Map<Integer, List<Integer>> yearWeekMap = new java.util.LinkedHashMap<Integer, List<Integer>>();
		DateFormat sdf = new SimpleDateFormat("yyyyMMdd");
		int dayTime = 1000 * 60 * 60 * 24;
		java.util.Calendar cal = java.util.Calendar.getInstance();
		Date date = null;;
		try {
			date = new SimpleDateFormat("yyyy").parse(Integer.toString(year));
		} catch (ParseException e) {
		}
		for (int i = 0; i < 10; i++){
			Date oDate = new Date(date.getTime() - dayTime);
			cal.setTime(oDate);
			if (cal.get(java.util.Calendar.WEEK_OF_YEAR) == 1){
				date = oDate;
			} else {
				break;
			}
		}
		int temp = 0;
		int theYear = year;
		int theYearWeek = 0;
		for (int i = 0; i < 400; i++){
			if (i > 0){
				date = new Date(date.getTime() + dayTime);
			}
			cal.setTime(date);
			if (cal.get(java.util.Calendar.YEAR) !=  year){
				// break;
			}
			int week = cal.get(java.util.Calendar.WEEK_OF_YEAR);
			theYear = week < temp ? theYear + 1: theYear;
			int yearWeek = Integer.parseInt(Integer.toString(theYear) + (week < 10 ? "0" : "") + week);
			if (cal.get(java.util.Calendar.YEAR) >  year && yearWeek != theYearWeek){
				break; //
			}
			if (!yearWeekMap.containsKey(yearWeek)){
				yearWeekMap.put(yearWeek, new ArrayList<Integer>());
			}
			yearWeekMap.get(yearWeek).add(Integer.parseInt(sdf.format(date)));
			temp = week;
			theYearWeek = yearWeek;
		}
		return yearWeekMap;
	}
	@Override
	public int getYearMonthByDateDay(Date dateDay){
		return Integer.parseInt(new SimpleDateFormat("yyyyMM").format(dateDay));
	}
	@Override
	public int getRightYearMonth(int yearMonth){
		return rightYearMonth(yearMonth);
	}
	private void computeStock(Stock... stocks){
		if (stocks != null && stocks.length > 0){
			double ma = 0;
			for (Stock stock : stocks){
				ma+= stock.getClose();
			}
			ma = ma / stocks.length;
			double bb = 0;
			for (Stock stock : stocks){
				bb += Math.pow(stock.getClose() - ma, 2);
			}
			bb = Math.pow(bb / stocks.length, 0.5);
			double bbh = ma + bb * 2;
			double bbl = ma - bb * 2;
			Stock stock = stocks[stocks.length - 1];
			stock.setM20(ma);
			stock.setBbh(bbh);
			stock.setBbl(bbl);
		}
	}
	@Override
	public void computeStocksAll(List<Stock> stocks){
		if (stocks != null && stocks.size() > 0){
			int p20 = 20;
			for (int i = stocks.size() - 1; i >= 0; i--){
				int cursor = i + 1 - p20;
				if (cursor >= 0){
					Stock[] stockArry = new Stock[p20];
					for (int i1 = 0; i1 < p20; i1++){
						stockArry[i1] = stocks.get(cursor + i1); 
					}
					computeStock(stockArry);
				} else {
					break;
				}
			}
		}
	}
	@Override
	public void computeStockLast(List<Stock> stocks, Stock lastStock){
		if (stocks != null && stocks.size() > 0 && lastStock != null){
			for (int i = 0; i < stocks.size(); i++){
				if (stocks.get(i).getDateDay().equals(lastStock.getDateDay())){
					stocks.set(i, lastStock);
					break;
				} else if (stocks.get(i).getDateDay().after(lastStock.getDateDay())){
					stocks.add(i, lastStock);
					break;
				}
				if (i == stocks.size() - 1){
					stocks.add(lastStock);
				}
			}
		}
	}
	@Override
	public String strSymbol(int symbol, int digit){
		StringBuffer buffer = new StringBuffer(Integer.toString(symbol));
		while (buffer.length() < digit)
			buffer.insert(0, 0);
		return buffer.toString();
	}
}