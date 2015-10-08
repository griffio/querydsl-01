package griffio.domain;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.mysema.query.annotations.QueryEntity;
import com.mysema.query.annotations.QueryProjection;

import java.math.BigDecimal;

@QueryEntity
public class SalaryDetail {

  private final String salaryName;
  private final BigDecimal salary;

  @QueryProjection
  public SalaryDetail(String salaryName, BigDecimal salary) {
    this.salaryName = salaryName;
    this.salary = salary.setScale(2, BigDecimal.ROUND_HALF_UP);
  }

  public String getSalaryName() {
    return salaryName;
  }

  public BigDecimal getSalary() {
    return salary;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    SalaryDetail that = (SalaryDetail) o;
    return Objects.equal(salaryName, that.salaryName) &&
        Objects.equal(salary, that.salary);
  }

  public SalaryDetail add(SalaryDetail other) {
    Preconditions.checkArgument(salaryName.equals(other.salaryName));
    return new SalaryDetail(salaryName, salary.add(other.salary));
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(salaryName, salary);
  }

}
