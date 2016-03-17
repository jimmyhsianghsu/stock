package ch.web.stock;
@org.springframework.transaction.annotation.Transactional
@org.springframework.stereotype.Repository
public class StockRecordDaoImpl implements StockRecordDao {
	@javax.persistence.PersistenceContext
    private javax.persistence.EntityManager em;
	@Override
	public StockRecord saveStockRecord(StockRecord stockRecord) {
		em.persist(stockRecord);
		return stockRecord;
	}
	@Override
	public StockRecord updateStockRecord(StockRecord stockRecord) {
		StockRecord record = null;
		if (stockRecord != null){
			record = em.find(StockRecord.class, stockRecord.getId());
			if (record != null){
				record.setCounts(stockRecord.getCounts());
				record.setDateDay2(stockRecord.getDateDay2());
				record.setPrice2(stockRecord.getPrice2());
			}
		}
		return record;
	}
	@Override
	public StockRecord getStockRecord(long id) {
		return em.find(StockRecord.class, id);
	}
	@Override
	public java.util.List<StockRecord> getStockRecordAll() {
		return em.createQuery("SELECT s FROM StockRecord s", StockRecord.class).getResultList();
	}
	@Override
	public StockRecord removeStockRecord(long id) {
		StockRecord record = getStockRecord(id);
		if (record != null){
			em.remove(record);
		}
		return record;
	}
}