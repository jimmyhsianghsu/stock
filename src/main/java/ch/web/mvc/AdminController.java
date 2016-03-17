package ch.web.mvc;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ch.web.admin.AdminService;
import ch.web.admin.User;
import ch.web.admin.UserRole;
@RestController
public class AdminController {
	@Autowired
	private AdminService adminService;
	@RequestMapping(value = "/admin/users/{userName}", method = RequestMethod.POST)
	public User createUser(@PathVariable("userName") String userName, @RequestParam(value = "password", required = true) String password){
		return adminService.createUser(userName, password);
	}
	@RequestMapping(value = "/admin/users/{userName}", method = RequestMethod.DELETE)
	public User removeUser(@PathVariable("userName") String userName){
		return adminService.removeUser(userName);
	}
	@RequestMapping(value = "/admin/roles/{userName}", method = RequestMethod.POST)
	public UserRole createUserRole(@PathVariable("userName") String userName, @RequestParam(value = "role", required = true) String role){
		return adminService.createUserRole(userName, role);
	}
	@RequestMapping(value = "/admin/roles/{userName}", method = RequestMethod.DELETE)
	public Map<String, Integer> removeUserRole(@PathVariable("userName") String userName, @RequestParam(value = "role", required = true) String role){
		Map<String, Integer> map = new HashMap<String, Integer>();
		map.put("count", adminService.removeUserRole(userName, role));
		return map;
	}
	@RequestMapping(value = "/admin/users/{userName}/prop", method = RequestMethod.POST)
	public User setUserProp(@PathVariable("userName") String userName,
			@RequestParam(value = "enabled", required = false) String enabled,
			@RequestParam(value = "password", required = false) String password){
		User user = null;
		if (enabled != null)
			user = adminService.setUserEnabled(userName, new Boolean(enabled));
		else if(password != null){
			user = adminService.changeUserPassword(userName, password);
		}
		if (user != null){
			return user;
		}
		throw new RuntimeException("user not exist!");
	}
	@RequestMapping(value = "/admin/users/", method = RequestMethod.GET)
	public Object getUsers(){
		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
		List<User> userList = adminService.getUsers();
		if (userList != null){
			for (User user : userList){
				Map<String, Object> map = new HashMap<String, Object>();
				map.put("user", user);
				List<UserRole> roleList = adminService.getUserRoles(user);
				if (roleList != null){
					List<String> roles = new ArrayList<String>();
					for (UserRole role : roleList)
						roles.add(role.getRole());
					map.put("roles", roles);
				}
				list.add(map);
			}
		}
		return list;
	}
}