package ch.web.stock;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
@Entity
@javax.persistence.Table(name="STOCK_RECORD")
public class StockRecord {
	@Id
    @GeneratedValue
	private Long id;
	private Integer symbol;
	@Column(name="date_day1")
	private Integer dateDay1;
	private Double price1;
	private Integer counts;
	@Column(name="date_day2")
	private Integer dateDay2;
	private Double price2;
	@Column(name="buy_sell")
	private Integer buySell;
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public Integer getSymbol() {
		return symbol;
	}
	public void setSymbol(Integer symbol) {
		this.symbol = symbol;
	}
	public Integer getDateDay1() {
		return dateDay1;
	}
	public void setDateDay1(Integer dateDay1) {
		this.dateDay1 = dateDay1;
	}
	public Double getPrice1() {
		return price1;
	}
	public void setPrice1(Double price1) {
		this.price1 = price1;
	}
	public Integer getCounts() {
		return counts;
	}
	public void setCounts(Integer counts) {
		this.counts = counts;
	}
	public Integer getDateDay2() {
		return dateDay2;
	}
	public void setDateDay2(Integer dateDay2) {
		this.dateDay2 = dateDay2;
	}
	public Double getPrice2() {
		return price2;
	}
	public void setPrice2(Double price2) {
		this.price2 = price2;
	}
	public Integer getBuySell() {
		return buySell;
	}
	public void setBuySell(Integer buySell) {
		this.buySell = buySell;
	}
}