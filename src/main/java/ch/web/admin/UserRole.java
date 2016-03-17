package ch.web.admin;
@javax.persistence.Entity
@javax.persistence.Table(name="USER_ROLES")
public class UserRole {
	@javax.persistence.Id
	@javax.persistence.GeneratedValue
	@javax.persistence.Column(name="USER_ROLE_ID")
    private int userRoleId;
	private String userName;
	private String role;
	public int getUserRoleId() {
		return userRoleId;
	}
	public void setUserRoleId(int userRoleId) {
		this.userRoleId = userRoleId;
	}
	public String getUserName() {
		return userName;
	}
	public void setUserName(String userName) {
		this.userName = userName;
	}
	public String getRole() {
		return role;
	}
	public void setRole(String role) {
		this.role = role;
	}
}