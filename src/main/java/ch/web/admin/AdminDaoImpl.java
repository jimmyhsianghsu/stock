package ch.web.admin;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import org.springframework.transaction.annotation.Transactional;
@Transactional
@org.springframework.stereotype.Repository
public class AdminDaoImpl implements AdminDao {
	@PersistenceContext
    private EntityManager em;
	@Override
	public void saveUser(User user) {
		em.persist(user);
	}
	@Override
	public User deleteUser(String userName){
		User user = getUser(userName);
		em.remove(user);
		return user;
	}
	@Override
	public void saveUserRole(UserRole userRole){
		em.persist(userRole);
	}
	@Override
	public int deleteUserRole(UserRole userRole){
		Query q = em.createQuery("DELETE FROM UserRole u WHERE u.userName = :userName and u.role = :role");
		q.setParameter("userName", userRole.getUserName());
		q.setParameter("role", userRole.getRole());
		return q.executeUpdate();
	}
	@Override
	public User getUser(String userName){
		return em.find(User.class, userName);
	}
	@Override
	public List<User> getUsers() {
		return em.createQuery("SELECT u FROM User u", User.class).getResultList();
	}
	@Override
	public List<UserRole> getUserRoles(User user){
		return em.createQuery("SELECT u FROM UserRole u WHERE u.userName = :userName", UserRole.class)
				.setParameter("userName", user.getUserName()).getResultList();
	}
}