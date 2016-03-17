package ch.web.stock;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
@Service
@org.springframework.context.annotation.Scope("prototype")
public class StockJobServiceImpl implements StockJobService {
	private static final List<Thread> THREAD_LIST = new LinkedList<Thread>();
	private static final List<RunCeasable> RUNCEASABLE_LIST = new LinkedList<RunCeasable>();
	private Thread thread;
	private RunCeasable runCeasable;
	private Map<Integer, StockJobProgress> stockJobMap;
	@Autowired
	private StockService stockService;
	@Autowired
	private DateService dateService;
	private int symbol;
	private int defaultMonths = 24;
	private int failLimit = 5;
	private class StockJobRunCeasable implements RunCeasable {
		private boolean ceased = false;
		private boolean status = false;
		private String mark;
		@Override
		public void run() {
			while (!ceased){
				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
				}
				status = false;
				if (stockJobMap.size() > 0){
					status = true;
					java.util.Iterator<Integer> iterator = stockJobMap.keySet().iterator();
					List<Integer> symbols = new java.util.ArrayList<Integer>();
					while (iterator.hasNext()){
						symbols.add(iterator.next());
					}
					for (Integer symbol : symbols){
						long start = System.currentTimeMillis();
						try {
							process(stockJobMap.get(symbol));
						} catch (RuntimeException re){
							re.printStackTrace();
						}
						mark = symbol + "=" + (System.currentTimeMillis() - start) / 1000.0;
					}
				}
				status = false;
			}
		}
		@Override
		public void setCeased(boolean ceased) {
			this.ceased = ceased;			
		}
		@Override
		public boolean getStatus(){
			return status;
		}
		@Override
		public String toString(){
			return String.valueOf(ceased) + '/' + status + '(' + mark + "),";
		}
	}
	
	@Override
	synchronized public boolean start() {
		if (thread == null || !thread.isAlive()){
			stockJobMap = new java.util.concurrent.ConcurrentHashMap<Integer, StockJobProgress>();
			runCeasable = new StockJobRunCeasable();
			RUNCEASABLE_LIST.add(runCeasable);
			thread = new Thread(runCeasable);
			THREAD_LIST.add(thread);
			thread.start();
		}
		return true;
	}
	@Override
	public int prepare(int symbol) {
		this.symbol = symbol;
		if (!stockJobMap.containsKey(symbol)){
			StockJobProgress stockJobProgress = new StockJobProgress(symbol);
			List<Integer> list = dateService.getYearMonthList(defaultMonths);
			stockJobProgress.addYearMonth(list);
			for (int yearMonth : list){
				stockJobProgress.update(stockService.getStockStatusBySymbolMonth(symbol, yearMonth));
			}
			stockJobMap.put(symbol, stockJobProgress);
		}
		StockInfo stockInfo = stockService.getStockInfoBySymbol(symbol);
		if (stockInfo == null || !stockInfo.exist(symbol)){
			for (String exCh : new String[]{StockService.EX_CH_TSE, StockService.EX_CH_OTC}){
				List<StockNow> stockNows = stockService.getStockNows(new StockNow(symbol, exCh));
				long count = 0;
				if (stockNows != null && stockNows.size() > 0){
					for (StockNow stockNow : stockNows){
						if (stockNow.getC() == symbol){
							stockInfo = new StockInfo();
							stockInfo.setSymbol(symbol);
							stockInfo.setExCh(exCh);
							stockInfo.setName(stockNow.getN());
							count = stockService.saveStockInfo(stockInfo);
						}
					}
				}
				if (count > 0){
					break;
				}
			}
		}
		return stockJobMap.size();
	}
	@Override
	public boolean stop() {
		if (runCeasable != null){
			runCeasable.setCeased(true);
		}
		return true;
	}
	@Override
	public boolean stopAll() {
		if (RUNCEASABLE_LIST.size() > 0){
			for (RunCeasable runCeasable : RUNCEASABLE_LIST){
				runCeasable.setCeased(true);
			}
		}
		return true;
	}
	@Override
	public boolean process(StockJobProgress stockJobProgress) {
		stockJobProgress.setMark(String.valueOf(false));
		StockStatus stockStatus = null;
		while ((stockStatus = stockJobProgress.next(failLimit)) != null){
			stockJobProgress.setMark(String.valueOf(true));
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
			}
			if (stockStatus.getStatus() <= 1){ // <= 1
				stockService.updateStockBySymbolMonth(stockStatus);
				stockJobProgress.update(stockStatus);
			}
			if (stockJobProgress.getSymbol() != this.symbol){
				break;
			}
		}
		stockJobProgress.setMark(String.valueOf(false));
		return true;
	}
	@Override
	public boolean resetStatus(int symbol){
		if (stockJobMap.containsKey(symbol)){
			return stockJobMap.get(symbol).resetStatus();
		}
		return false;
	}
	@Override
	public void resetFailLimit(int failLimit){
		this.failLimit = failLimit;
	}
	@Override
	public java.util.Map<Integer, StockJobProgress> getStockJobMap(){
		return stockJobMap;
	}
	@Override
	public List<RunCeasable> getRunCeasableList(){
		return RUNCEASABLE_LIST;
	}
}