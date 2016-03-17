package ch.web.stock;
import java.util.Date;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
@Transactional
@org.springframework.stereotype.Repository
public class StockDaoImpl implements StockDao{
	@PersistenceContext
    private EntityManager em;
	@Autowired
	private DateService dateService;
	@Override
	public Stock saveStock(Stock stock) { // synchronized
		Stock oriStock = getOriStock(stock.getSymbol(), stock.getDateDay());
		if (oriStock != null){
			oriStock.setOpen(stock.getOpen());
			oriStock.setHigh(stock.getHigh());
			oriStock.setLow(stock.getLow());
			oriStock.setClose(stock.getClose());
			oriStock.setVolume(stock.getVolume());
			oriStock.setTurnover(stock.getTurnover());
			oriStock.setChange(stock.getChange());
			oriStock.setCounts(stock.getCounts());
			return oriStock;
		} else {
			em.persist(stock);
			return stock;
		}
		// return em.merge(stock);
	}
	@Override
	public Stock saveStockComputeValue(Stock stock){
		Stock oriStock = getOriStock(stock.getSymbol(), stock.getDateDay());
		if (oriStock != null){
			oriStock.setM20(stock.getM20());
			oriStock.setBbh(stock.getBbh());
			oriStock.setBbl(stock.getBbl());
			return oriStock;
		} else {
			em.persist(stock);
			return stock;
		}
	}
	private Stock getOriStock(int symbol, Date dateDay){
		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<Stock> cq = cb.createQuery(Stock.class);
		Root<Stock> stock = cq.from(Stock.class);
		cq.where(cb.equal(stock.get(Stock_.symbol), symbol), cb.and(cb.equal(stock.get(Stock_.dateDay), dateDay)));
		System.out.println("oooooooooo1=" + symbol + ':' + dateDay);
		List<Stock> list = em.createQuery(cq).getResultList();
		System.out.println("oooooooooo2=" + symbol + ':' + dateDay);
		return list != null && list.size() == 1 ? list.get(0) : null;
	}
	@Override
	public List<Stock> getStockBySymbolMonth(int symbol, int yearMonth){
		List<Stock> list = null;
		Date date1 = dateService.getYearMonth(yearMonth);
		Date date2 = dateService.getYearMonth(yearMonth + 1);
		if (date1 != null && date2 != null){
			CriteriaBuilder cb = em.getCriteriaBuilder();
			CriteriaQuery<Stock> cq = cb.createQuery(Stock.class);
			Root<Stock> stock = cq.from(Stock.class);
			cq.where(cb.equal(stock.get(Stock_.symbol), symbol),
					cb.and(cb.greaterThanOrEqualTo(stock.get(Stock_.dateDay), date1)),
					cb.and(cb.lessThan(stock.get(Stock_.dateDay), date2)));
			list = em.createQuery(cq).getResultList();
		}
		return list;
	}
	@Override
	public List<Stock> getStockAll() {
		return em.createQuery("SELECT s FROM Stock s", Stock.class).getResultList();
	}
	@Override
	public long getStockCount(){
		CriteriaBuilder qb = em.getCriteriaBuilder();
		CriteriaQuery<Long> cq = qb.createQuery(Long.class);
		cq.select(qb.count(cq.from(Stock.class)));
		return em.createQuery(cq).getSingleResult();
	}
	@Override
	public StockStatus saveStockStatus(StockStatus stockStatus) {
		StockStatus mergeStatus = em.merge(stockStatus);
		if (mergeStatus != null){
			stockStatus.setSymbol(mergeStatus.getSymbol());
			stockStatus.setYearMonth(mergeStatus.getYearMonth());
			stockStatus.setStatus(mergeStatus.getStatus());
			stockStatus.setDateDay(mergeStatus.getDateDay());
		}
		return mergeStatus;
	}
	@Override
	public List<StockStatus> getStockStatusBySymbol(int symbol){
		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<StockStatus> cq = cb.createQuery(StockStatus.class);
		Root<StockStatus> stockStatus = cq.from(StockStatus.class);
		cq.where(cb.equal(stockStatus.get(StockStatus_.symbol), symbol));
		return em.createQuery(cq).getResultList();
	}
	@Override
	public StockStatus getStockStatusBySymbolMonth(int symbol, int yearMonth){
		return em.find(StockStatus.class, new StockStatus.StockStatusPK(symbol, yearMonth));
	}
	@Override
	public List<StockStatus> getStockStatusAll() {
		return em.createQuery("SELECT s FROM StockStatus s", StockStatus.class).getResultList();
	}
	@Override
	public long getStockStatusCount() {
		CriteriaBuilder qb = em.getCriteriaBuilder();
		CriteriaQuery<Long> cq = qb.createQuery(Long.class);
		cq.select(qb.count(cq.from(StockStatus.class)));
		return em.createQuery(cq).getSingleResult();
	}
	@Override
	public StockInfo saveStockInfo(StockInfo stockInfo){
		return em.merge(stockInfo);
	}
	@Override
	public StockInfo getStockInfoBySymbol(int symbol){
		return em.find(StockInfo.class, symbol);
	}
	@Override
	public List<StockInfo> getStockInfoAll(){
		return em.createQuery("SELECT s FROM StockInfo s", StockInfo.class).getResultList();
	}
	@Override
	public List<Stock> saveLevel(int symbol, int dateDay1, int dateDay2){
		List<Stock> list = null;
		Date date1 = dateService.getDateDay(dateDay1);
		Date date2 = dateService.getDateDay(dateDay2);
		if (date1 != null && date2 != null){
			CriteriaBuilder cb = em.getCriteriaBuilder();
			CriteriaQuery<Stock> cq = cb.createQuery(Stock.class);
			Root<Stock> stock = cq.from(Stock.class);
			cq.where(cb.equal(stock.get(Stock_.symbol), symbol),
					cb.and(cb.greaterThanOrEqualTo(stock.get(Stock_.dateDay), date1)),
					cb.and(cb.lessThan(stock.get(Stock_.dateDay), date2)));
			list = em.createQuery(cq).getResultList();
		}
		return list;
	}
	@Override
	public int computeBySymbol(int symbol){
		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<Stock> cq = cb.createQuery(Stock.class);
		Root<Stock> root = cq.from(Stock.class);
		cq.where(cb.equal(root.get(Stock_.symbol), symbol), cb.and(cb.isNull(root.get(Stock_.m20))));
		List<Stock> stockList = em.createQuery(cq).getResultList();
		if (stockList != null && stockList.size() > 0){
			List<StockStatus> statusList = getStockStatusBySymbol(symbol);
			if (statusList != null && statusList.size() > 0){
				java.util.Map<Integer, Integer> statusMap = new java.util.HashMap<Integer, Integer>();
				for (StockStatus status : statusList){
					statusMap.put(status.getYearMonth(), status.getStatus());
				}
				java.util.LinkedList<Stock> stocks = new java.util.LinkedList<Stock>();
				dateService.sortStock(stockList);
				int atYearMonth = dateService.getYearMonthByDateDay(stockList.get(stockList.size() - 1).getDateDay());
				for (int i = stockList.size() - 1; i >= 0; i--){
					int yearMonth = dateService.getYearMonthByDateDay(stockList.get(i).getDateDay());
					if (yearMonth != atYearMonth){
						atYearMonth = dateService.getRightYearMonth(atYearMonth - 1);
					}
					if (yearMonth == atYearMonth && statusMap.get(atYearMonth) > 0){ // > 0
						stocks.addFirst(stockList.get(i));
					} else {
						break;
					}
				}
				if (stocks.size() > 0){
					dateService.computeStocksAll(stocks);
				}
				return atYearMonth;
			}
		} else {
			List<StockStatus> statusList = getStockStatusBySymbol(symbol);
			if (statusList != null && statusList.size() > 0){
				dateService.sortStockStatus(statusList);
				int atYearMonth = statusList.get(statusList.size() - 1).getYearMonth();
				for (int i = statusList.size() - 2; i >= 0; i--){
					atYearMonth = dateService.getRightYearMonth(atYearMonth - 1);
					if (statusList.get(i).getYearMonth() != atYearMonth){
						break;
					}
				}
				return atYearMonth;
			}
		}
		return 0;
	}
}