package ch.web.stock;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
@Entity
@javax.persistence.Table(name="STOCK_INFO")
public class StockInfo {
	@Id
	private int symbol;
	@Column(name="ex_ch")
	private String exCh;
	private String name;
	@Column(name="date_day1")
	private Integer dateDay1;
	@Column(name="date_day2")
	private Integer dateDay2;
	@Column(name="level_1")
	private Double level1;
	@Column(name="level_2")
	private Double level2;
	@Column(name="level_3")
	private Double level3;
	@Column(name="bs_max_date_day")
	private Integer bsMaxDateDay;
	@Column(name="bs_max")
	private Double bsMax;
	public int getSymbol() {
		return symbol;
	}
	public void setSymbol(int symbol) {
		this.symbol = symbol;
	}
	public String getExCh() {
		return exCh;
	}
	public void setExCh(String exCh) {
		this.exCh = exCh;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	@Override
	public String toString(){
		return exCh + '_' + symbol + ':' + name;
	}
	public boolean exist(int symbol){
		return this.symbol == symbol && exCh != null && !exCh.trim().isEmpty() && name != null && !name.trim().isEmpty();
	}
	public Integer getDateDay1() {
		return dateDay1;
	}
	public void setDateDay1(int dateDay1) {
		this.dateDay1 = dateDay1;
	}
	public Integer getDateDay2() {
		return dateDay2;
	}
	public void setDateDay2(int dateDay2) {
		this.dateDay2 = dateDay2;
	}
	public Double getLevel1() {
		return level1;
	}
	public void setLevel1(double level1) {
		this.level1 = level1;
	}
	public Double getLevel2() {
		return level2;
	}
	public void setLevel2(double level2) {
		this.level2 = level2;
	}
	public Double getLevel3() {
		return level3;
	}
	public void setLevel3(double level3) {
		this.level3 = level3;
	}
	public Double getBsMax() {
		return bsMax;
	}
	public void setBsMax(double bsMax) {
		this.bsMax = bsMax;
	}
	public Integer getBsMaxDateDay() {
		return bsMaxDateDay;
	}
	public void setBsMaxDateDay(Integer bsMaxDateDay) {
		this.bsMaxDateDay = bsMaxDateDay;
	}
}