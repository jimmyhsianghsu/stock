package ch.web.stock.cache;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ch.web.stock.Stock;
import ch.web.stock.StockService;
import ch.web.stock.StockStatus;
import com.google.common.cache.CacheLoader;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListenableFutureTask;
@Service
public class StockCacheLoader extends CacheLoader<StockKey, List<Stock>>{
	private static org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(StockCacheLoader.class);
	@Autowired
	private StockService stockService;
	private Map<StockKey, StockStatus> map = new HashMap<StockKey, StockStatus>();
	private Executor executor = Executors.newFixedThreadPool(10);
	private Map<StockKey, Date> lastTimeMap = new HashMap<StockKey, Date>();
	@Autowired private ch.web.log.LogService logService;
	@javax.annotation.PostConstruct public void init(){logService.start();}
	@Override
	public List<Stock> load(StockKey stockKey) {
		List<Stock> stockList = stockService.getStockBySymbolMonth(stockKey.getSymbol(), stockKey.getYearMonth());
		map.put(stockKey, stockService.getStockStatusBySymbolMonth(stockKey.getSymbol(), stockKey.getYearMonth()));
		return stockList;
	}
	@Override
	public ListenableFuture<List<Stock>> reload(final StockKey stockKey, List<Stock> stockList) {
		if (map.get(stockKey) != null && map.get(stockKey).getStatus() > 2 || isLastToday(stockList)){
			return Futures.immediateFuture(stockList);
		} else {
			ListenableFutureTask<List<Stock>> task = ListenableFutureTask.create(new Callable<List<Stock>>() {
				@Override
				public List<Stock> call() {
					LOG.info("reload.task = " + stockKey);
					lastTimeMap.put(stockKey, new Date());
					log(stockKey.getSymbol(), stockKey.getYearMonth(), null, lastTimeMap.get(stockKey), null, null);
					return load(stockKey);
				}
			});
			executor.execute(task);
			return task;
		}
	}
	private boolean isLastToday(List<Stock> stockList){
		boolean isToday = false;
		if (stockList != null && stockList.size() > 0){
			java.util.Date lastDateDay = stockList.get(stockList.size() - 1).getDateDay();
			long turnover = stockList.get(stockList.size() - 1).getTurnover();
			for (int i = stockList.size() - 1; i >= 0; i--){
				if (stockList.get(i).getDateDay().after(lastDateDay)){
					lastDateDay = stockList.get(i).getDateDay();
					turnover = stockList.get(i).getTurnover();
				}
			}
			int symbol = stockList.get(0).getSymbol();
			int yearMonth = Integer.parseInt(new SimpleDateFormat("yyyyMM").format(lastDateDay));
			Date now = new Date();
			isToday = lastDateDay.getTime() == getCurrentDay().getTime();
			isToday |= nearLastTime(symbol, yearMonth, now, 0.5);
			isToday &= (getCurrentHour() < 1400 || turnover > 0);
			LOG.info("isLastToday = [" + stockList.get(0).getSymbol() + "] " + lastDateDay + " : " + isToday + " (" + turnover + ')');
			log(symbol, yearMonth, now, null, lastTimeMap.get(new StockKey(symbol, yearMonth)), isToday);
		}
		return isToday;
	}
	private Date getCurrentDay(){
		DateFormat format = new SimpleDateFormat("yyyyMMdd");
		try {
			return format.parse(format.format(new Date()));
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return null;
	}
	private int getCurrentHour(){return Integer.parseInt(new SimpleDateFormat("HHmm").format(new Date()));}
	private boolean nearLastTime(int symbol, int yearMonth, Date now, double minute){
		Date lastTime = lastTimeMap.get(new StockKey(symbol, yearMonth));
		return lastTime != null ? now.getTime() - lastTime.getTime() < 1000 * 60 * minute : false;
	}
	private void log(int symbol, int yearMonth, Date isLastToday, Date reload, Date lastTime, Boolean isToday){
		logService.addLog(new ch.web.log.StockReloadLog(symbol, yearMonth, isLastToday, reload, lastTime, isToday));
		executor.execute(new Runnable(){
			@Override
			public void run() {
				logService.start();
				try{Thread.sleep(10000);}catch(InterruptedException e){e.printStackTrace();}
				logService.stop();
			}
		});
	}
}