package ch.web.stock;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
public class StockJobProgress {
	private int symbol;
	private Map<Integer, StockStatus> stockStatusMap;
	private Map<Integer, Integer> stockFailMap;
	private java.util.Comparator<Integer> comparator = new java.util.Comparator<Integer>(){
		@Override
		public int compare(Integer int1, Integer int2) {
			return int2 - int1;
		}
	};
	public StockJobProgress(int symbol){
		this.symbol = symbol;
		this.stockStatusMap = new HashMap<Integer, StockStatus>();
		this.stockFailMap = new HashMap<Integer, Integer>();
	}
	public int getSymbol(){
		return symbol;
	}
	public int addYearMonth(List<Integer> list){
		if (list != null && list.size() > 0){
			for (Integer yearMonth : list){
				if (!stockStatusMap.containsKey(yearMonth)){
					StockStatus stockStatus = new StockStatus();
					stockStatus.setSymbol(symbol);
					stockStatus.setYearMonth(yearMonth);
					stockStatusMap.put(yearMonth, stockStatus);
					stockFailMap.put(yearMonth, 0);
				}
			}
		}
		return stockStatusMap.size();
	}
	public StockStatus next(int failLimit){
		StockStatus stockStatus = null;
		int fails = failLimit;
		List<Integer> list = new java.util.LinkedList<Integer>(stockStatusMap.keySet());
		java.util.Collections.sort(list, comparator);
		for (Integer yearMonth : list){
			StockStatus status = stockStatusMap.get(yearMonth);
			int fail = stockFailMap.get(yearMonth);
			if (status.getStatus() <= 1 && fail < fails){ // <= 1
				stockStatus = status;
				fails = fail;
				if (yearMonth == list.get(0) && fail <= 1){ // <= 1
					break;
				}
			}
		}
		return stockStatus;
	}
	public boolean update(StockStatus stockStatus){
		if (stockStatus != null){
			int symbol = stockStatus.getSymbol();
			int yearMonth = stockStatus.getYearMonth();
			int status = stockStatus.getStatus();
			if (this.symbol == symbol && stockStatusMap.containsKey(yearMonth)){
				if (status > 1){ // <= 1
					stockStatusMap.get(yearMonth).setStatus(status);
				} else {
					stockStatusMap.get(yearMonth).setStatus(status); // <= 1
					stockFailMap.put(yearMonth, stockFailMap.get(yearMonth) + 1);
				}
				return true;
			}
		}
		return false;
	}
	public boolean resetStatus(){
		if (stockStatusMap.size() > 0){
			for (Integer yearMonth : stockStatusMap.keySet()){
				stockStatusMap.get(yearMonth).setStatus(0);
				stockFailMap.put(yearMonth, 0); // <= 1
			}
		}
		return true;
	}
	@Override
	public String toString(){
		StringBuffer buffer = new StringBuffer(Integer.toString(symbol)).append('\n');
		if (stockStatusMap.size() > 0){
			List<Integer> list = new java.util.ArrayList<Integer>(stockStatusMap.keySet());
			java.util.Collections.sort(list);
			for (Integer yearMonth : list){
				buffer.append(stockStatusMap.get(yearMonth)).append("=").append(stockFailMap.get(yearMonth)).append('\n');
			}
			buffer.append("/").append(mark).append('\n');
		}
		return buffer.toString();
	}
	private String mark;
	public String getMark() {
		return mark;
	}
	public void setMark(String mark) {
		this.mark = mark;
	}
	public Map<String, String> getMap(){
		Map<String, String> map = new HashMap<String, String>();
		List<Integer> list = new java.util.ArrayList<Integer>(stockStatusMap.keySet());
		java.util.Collections.sort(list);
		map.put("symbol", Integer.toString(symbol));
		map.put("list", list.toString());
		for (Integer yearMonth : list){
			map.put(yearMonth.toString(), "[" + stockStatusMap.get(yearMonth).getStatus() + ']' + stockFailMap.get(yearMonth));
		}
		map.put("mark", String.valueOf(mark));
		return map;
	}
}