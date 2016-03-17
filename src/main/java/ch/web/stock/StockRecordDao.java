package ch.web.stock;
public interface StockRecordDao {
	StockRecord saveStockRecord(StockRecord stockRecord);
	StockRecord updateStockRecord(StockRecord stockRecord);
	StockRecord getStockRecord(long id);
	java.util.List<StockRecord> getStockRecordAll();
	StockRecord removeStockRecord(long id);
}