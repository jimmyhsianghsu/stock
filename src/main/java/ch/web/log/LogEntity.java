package ch.web.log;
import java.io.Serializable;
import javax.persistence.*;
@Entity
@javax.persistence.Table(name="LOG_ENTITY")
public abstract class LogEntity implements Serializable {
	private static final long serialVersionUID = 1L;
	@Id @GeneratedValue
	private Long id;
	private int symbol;
	@Column(name="YEAR_MONTH")
	private int yearMonth;
	public Long getId() {return id;}
	public void setId(Long id) {this.id = id;}
	public int getSymbol() {return symbol;}
	public void setSymbol(int symbol) {this.symbol = symbol;}
	public int getYearMonth() {return yearMonth;}
	public void setYearMonth(int yearMonth) {this.yearMonth = yearMonth;}
}