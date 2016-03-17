package ch.web.stock.cache;
public class StockKey {
	private int symbol;
	private int yearMonth;
	public StockKey(int symbol, int yearMonth){
		this.symbol = symbol;
		this.yearMonth = yearMonth;
	}
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof StockKey){
			StockKey stockKey = (StockKey) obj;
			return symbol == stockKey.symbol && yearMonth == stockKey.yearMonth;
		}
		return false;
	}
	@Override
	public String toString() {
		return "[" + symbol + ':' + yearMonth + ']';
	}
	@Override
	public int hashCode() {
		return symbol * yearMonth;
	}
	public int getSymbol() {
		return symbol;
	}
	public void setSymbol(int symbol) {
		this.symbol = symbol;
	}
	public int getYearMonth() {
		return yearMonth;
	}
	public void setYearMonth(int yearMonth) {
		this.yearMonth = yearMonth;
	}
}