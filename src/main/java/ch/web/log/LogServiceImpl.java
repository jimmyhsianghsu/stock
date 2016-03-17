package ch.web.log;
import java.util.LinkedList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
@Service
public class LogServiceImpl implements LogService{
	private final static List<LogEntity> logList = new LinkedList<LogEntity>();
	@Autowired
	private LogDao logDao;
	private Status status = new Status();
	private class Status {
		static final int OFF = 0;
		static final int RUN = 1;
		static final int STOP = 2;
		int code = OFF;
	}
	@Override
	public void addLog(LogEntity log){
		logList.add(log);
	}
	@Override
	public boolean start(){
		synchronized (status){
			if (status.code == Status.OFF){
				status.code = Status.RUN;
				new Thread(new Runnable(){
					@Override
					public void run() {
						try {
							while (status.code == Status.RUN){
								try {
									while (logList.size() > 0)
										logDao.saveLog(logList.remove(0));
								} catch (RuntimeException re){
									re.printStackTrace();
								}
							}
						} finally {
							status.code = Status.OFF;
						}
					}
				}).start();
				return true;
			}
		}
		return false;
	}
	@Override
	public void stop(){
		synchronized (status){
			if (status.code == Status.RUN)
			status.code = Status.STOP;
		}
	}
}