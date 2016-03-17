package ch.web.log;
import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
@Entity
public class StockReloadLog extends LogEntity{
	private static final long serialVersionUID = 1L;
	@Column(name="IS_LAST_TODAY")
	private Date isLastToday;
	private Date reload;
	@Column(name="LAST_TIME")
	private Date lastTime;
	@Column(name="IS_TODAY")
	private Boolean isToday;
	@Column(name="THREAD_ID")
	private long threadId;
	public StockReloadLog(){}
	public StockReloadLog(int symbol, int yearMonth, Date isLastToday, Date reload, Date lastTime, Boolean isToday){
		super();
		setSymbol(symbol);
		setYearMonth(yearMonth);
		this.isLastToday = isLastToday;
		this.reload = reload;
		this.lastTime = lastTime;
		this.isToday = isToday;
		this.threadId = Thread.currentThread().getId();
	}
	public Date getIsLastToday() {return isLastToday;}
	public void setIsLastToday(Date isLastToday) {this.isLastToday = isLastToday;}
	public Date getReload() {return reload;}
	public void setReload(Date reload) {this.reload = reload;}
	public Date getLastTime() {return lastTime;}
	public void setLastTime(Date lastTime) {this.lastTime = lastTime;}
	public Boolean getIsToday() {return isToday;}
	public void setIsToday(Boolean isToday) {this.isToday = isToday;}
	public long getThreadId() {return threadId;}
	public void setThreadId(long threadId) {this.threadId = threadId;}
}