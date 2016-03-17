package ch.web.stock;
import java.util.Date;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;
@StaticMetamodel(Stock.class)
public class Stock_ {
  public static volatile SingularAttribute<Stock, Integer> symbol;
  public static volatile SingularAttribute<Stock, Date> dateDay;
  public static volatile SingularAttribute<Stock, Double> open;
  public static volatile SingularAttribute<Stock, Double> high;
  public static volatile SingularAttribute<Stock, Double> low;
  public static volatile SingularAttribute<Stock, Double> close;
  public static volatile SingularAttribute<Stock, Long> volume;
  public static volatile SingularAttribute<Stock, Long> turnover;
  public static volatile SingularAttribute<Stock, Double> change;
  public static volatile SingularAttribute<Stock, Long> counts;
  public static volatile SingularAttribute<Stock, Double> m20;
  public static volatile SingularAttribute<Stock, Double> bbh;
  public static volatile SingularAttribute<Stock, Double> bbl;
}