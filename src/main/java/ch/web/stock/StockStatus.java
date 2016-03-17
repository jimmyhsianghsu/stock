package ch.web.stock;
import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Table;
@Entity
@Table(name="stock_status")
@IdClass(StockStatus.StockStatusPK.class)
public class StockStatus {
	@Id
	private int symbol;
	@Id
	@Column(name="year_month")
	private int yearMonth;
	private int status;
	@Column(name="date_day")
	private java.util.Date dateDay;
	@SuppressWarnings("serial")
	public static class StockStatusPK implements Serializable{
		protected int symbol;
		protected int yearMonth;
		public StockStatusPK(){}
		public StockStatusPK(int symbol, int yearMonth){
			this.symbol = symbol;
			this.yearMonth = yearMonth;
		}
		@Override
		public boolean equals(Object obj) {
			if (obj instanceof StockStatusPK){
				StockStatusPK pk = (StockStatusPK) obj;
				return symbol == pk.symbol && yearMonth == pk.yearMonth;
			}
			return false;
		}
		@Override
		public int hashCode() {
			return symbol * yearMonth;
		}
	}
	@Override
	public String toString(){
		return Integer.toString(symbol) + ':' + yearMonth + '[' + status + "][" + dateDay + ']';
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
	public int getStatus() {
		return status;
	}
	public void setStatus(int status) {
		this.status = status;
	}
	public java.util.Date getDateDay() {
		return dateDay;
	}
	public void setDateDay(java.util.Date dateDay) {
		this.dateDay = dateDay;
	}
}