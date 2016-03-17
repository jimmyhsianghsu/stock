package ch.web.admin;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
@Service
public class AdminServiceImpl implements AdminService {
	@Autowired
	private AdminDao adminDao;
	@Override
	public User createUser(String userName, String password) {
		BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
		String hashedPassword = passwordEncoder.encode(password);
		User user = new User();
		user.setUserName(userName);
		user.setPassword(hashedPassword);
		user.setEnabled(false);
		adminDao.saveUser(user);
		return user;
	}
	@Override
	public User removeUser(String userName){
		return adminDao.deleteUser(userName);
	}
	@Override
	public UserRole createUserRole(String userName, String role) {
		UserRole userRole = new UserRole();
		userRole.setUserName(userName);
		userRole.setRole(role);
		adminDao.saveUserRole(userRole);
		return userRole;
	}
	@Override
	public int removeUserRole(String userName, String role){
		UserRole userRole = new UserRole();
		userRole.setUserName(userName);
		userRole.setRole(role);
		return adminDao.deleteUserRole(userRole);
	}
	@Override
	@Transactional
	public User setUserEnabled(String userName, boolean enabled) {
		User user = adminDao.getUser(userName);
		if (user != null){
			user.setEnabled(enabled);
		}
		return user;
	}
	@Override
	@Transactional
	public User changeUserPassword(String userName, String password){
		BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
		String hashedPassword = passwordEncoder.encode(password);
		User user = adminDao.getUser(userName);
		if (user != null){
			user.setPassword(hashedPassword);
		}
		return user;
	}
	@Override
	public List<User> getUsers(){
		return adminDao.getUsers();
	}
	@Override
	public List<UserRole> getUserRoles(User user){
		return adminDao.getUserRoles(user);
	}
}