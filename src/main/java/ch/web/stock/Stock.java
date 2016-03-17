package ch.web.stock;
import java.io.Serializable;
import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
@Entity
@IdClass(Stock.StockPK.class)
public class Stock {
	/**
	 * http://hsqldb.org/doc/2.0/guide/running-chapt.html#rgc_hsqldb_db
	 * http://hsqldb.org/doc/guide/dbproperties-chapt.html
	 * http://devcrumb.com/hibernate/hibernate-jpa-spring-and-hsqldb/
	 * http://stackoverflow.com/questions/3585034/how-to-map-a-composite-key-with-hibernate
	 * 
	 * http://en.wikibooks.org/wiki/Java_Persistence/Identity_and_Sequencing
	 * http://stackoverflow.com/questions/8469871/jpa-merge-vs-persist
	 * http://stackoverflow.com/questions/1069992/jpa-entitymanager-why-use-persist-over-merge
	 * http://spitballer.blogspot.tw/2010/04/jpa-persisting-vs-merging-entites.html
	 * 
	 * https://code.google.com/p/sqlite4java/wiki/UsingWithMaven
	 * Plugin execution not covered by lifecycle configuration
	 * draw.update.thread
	 * toString.thread.json
	 * 43=iterator.synchronized.transaction.reset+emv.van.m001
	 */
/**
	@Id
    @javax.persistence.GeneratedValue
	private Long id;
*/
	@Id
	private int symbol;
	@Id
	@Column(name="date_day")
	private Date dateDay;
	private double open;
	private double high;
	private double low;
	private double close;
	private long volume;
	private long turnover;
	private double change;
	private long counts;
	private Double m20;
	private Double bbh;
	private Double bbl;
	@SuppressWarnings("serial")
	public static class StockPK implements Serializable{
		protected int symbol;
		protected Date dateDay;
		public StockPK(){}
		public StockPK(int symbol, Date dateDay){
			this.symbol = symbol;
			this.dateDay = dateDay;
		}
		@Override
		public boolean equals(Object obj) {
			System.out.println("StockPK =" + obj);
			if (obj instanceof StockPK){
				StockPK pk = (StockPK) obj;
				return symbol == pk.symbol && dateDay.equals(pk.dateDay);
			}
			return false;
		}
		@Override
		public int hashCode() {
			return symbol;
		}
	}
	@Override
	public String toString(){
		return dateDay.toString() + '\t' + Integer.toString(symbol) + '\t' + volume + '\t' + turnover + '\t' +
				open + '\t' + high + '\t' + low + '\t' + close + '\t' + change + '\t' + counts
				+ "\t[" + m20 + "][" + bbh + "][" + bbl + ']';
	}
	public int getSymbol() {
		return symbol;
	}
	public void setSymbol(int symbol) {
		this.symbol = symbol;
	}
	public Date getDateDay() {
		return dateDay;
	}
	public void setDateDay(Date dateDay) {
		this.dateDay = dateDay;
	}
	public double getOpen() {
		return open;
	}
	public void setOpen(double open) {
		this.open = open;
	}
	public double getHigh() {
		return high;
	}
	public void setHigh(double high) {
		this.high = high;
	}
	public double getLow() {
		return low;
	}
	public void setLow(double low) {
		this.low = low;
	}
	public double getClose() {
		return close;
	}
	public void setClose(double close) {
		this.close = close;
	}
	public long getVolume() {
		return volume;
	}
	public void setVolume(long volume) {
		this.volume = volume;
	}
	public long getTurnover() {
		return turnover;
	}
	public void setTurnover(long turnover) {
		this.turnover = turnover;
	}
	public double getChange() {
		return change;
	}
	public void setChange(double change) {
		this.change = change;
	}
	public long getCounts() {
		return counts;
	}
	public void setCounts(long counts) {
		this.counts = counts;
	}
	public Double getM20() {
		return m20;
	}
	public void setM20(Double m20) {
		this.m20 = m20;
	}
	public Double getBbh() {
		return bbh;
	}
	public void setBbh(Double bbh) {
		this.bbh = bbh;
	}
	public Double getBbl() {
		return bbl;
	}
	public void setBbl(Double bbl) {
		this.bbl = bbl;
	}
}