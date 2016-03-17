package ch.web.stock;
public interface StockJobService {
	boolean start();
	boolean stop();
	boolean stopAll();
	int prepare(int symbol);
	boolean process(StockJobProgress stockJobProgress);
	boolean resetStatus(int symbol);
	void resetFailLimit(int failLimit);
	java.util.Map<Integer, StockJobProgress> getStockJobMap();
	java.util.List<RunCeasable> getRunCeasableList();
}