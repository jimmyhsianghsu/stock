package ch.web.admin;
@javax.persistence.Entity
@javax.persistence.Table(name="USERS")
public class User {
	@javax.persistence.Id
	private String userName;
	private String password;
	private boolean enabled;
	public User(){}
	public User(String userName){
		this.userName = userName;
	}
	public String getUserName() {
		return userName;
	}
	public void setUserName(String userName) {
		this.userName = userName;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public boolean isEnabled() {
		return enabled;
	}
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}
}