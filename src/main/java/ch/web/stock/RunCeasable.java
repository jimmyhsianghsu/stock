package ch.web.stock;
public interface RunCeasable extends Runnable {
	void setCeased(boolean ceased);
	boolean getStatus();
}