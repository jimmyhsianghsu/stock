package ch.web.stock;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;
@StaticMetamodel(StockStatus.class)
public class StockStatus_ {
	public static volatile SingularAttribute<StockStatus, Integer> symbol;
	public static volatile SingularAttribute<StockStatus, Integer> yearMonth;
	public static volatile SingularAttribute<StockStatus, Integer> status;
	public static volatile SingularAttribute<StockStatus, java.util.Date> dateDay;
}