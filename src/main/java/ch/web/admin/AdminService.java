package ch.web.admin;
import java.util.List;
public interface AdminService {
	User createUser(String userName, String password);
	User removeUser(String userName);
	UserRole createUserRole(String userName, String role);
	int removeUserRole(String userName, String role);
	User setUserEnabled(String userName, boolean enabled);
	User changeUserPassword(String userName, String password);
	List<User> getUsers();
	List<UserRole> getUserRoles(User user);
}