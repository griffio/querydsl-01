package griffio.domain;

import com.mysema.query.annotations.QueryDelegate;
import com.mysema.query.types.expr.BooleanExpression;

/**
 * isSalaryRelevant is added to the QSalaryDetail generated class
 */
public final class QueryExtensions {

  @QueryDelegate(SalaryDetail.class)
  public static BooleanExpression isSalaryRelevant(QSalaryDetail detail) {
    return detail.salaryName.notEqualsIgnoreCase("other");
  }
}
