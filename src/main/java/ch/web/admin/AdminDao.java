package ch.web.admin;
import java.util.List;
public interface AdminDao {
	void saveUser(User user);
	User deleteUser(String userName);
	void saveUserRole(UserRole userRole);
	int deleteUserRole(UserRole userRole);
	User getUser(String userName);
	List<User> getUsers();
	List<UserRole> getUserRoles(User user);
}