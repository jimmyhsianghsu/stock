package ch.web.log;
public interface LogService {
	void addLog(LogEntity log);
	boolean start();
	void stop();
}