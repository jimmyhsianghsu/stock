package ch.web.log;
@org.springframework.stereotype.Repository
public class LogDaoImpl implements LogDao{
	@javax.persistence.PersistenceContext
	private javax.persistence.EntityManager em;
	@Override
	@org.springframework.transaction.annotation.Transactional
	public LogEntity saveLog(LogEntity log){
		em.persist(log);
		return log;
	}
}