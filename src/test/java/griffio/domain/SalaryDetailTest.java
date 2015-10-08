package griffio.domain;

import com.google.common.collect.ImmutableList;
import com.mysema.query.BooleanBuilder;
import com.mysema.query.types.expr.BooleanExpression;

import com.mysema.query.types.expr.CaseBuilder;
import com.mysema.query.types.expr.StringExpression;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static com.google.common.truth.Truth.assertThat;
import static com.mysema.query.collections.CollQueryFactory.from;
import static com.mysema.query.group.GroupBy.groupBy;
import static com.mysema.query.group.GroupBy.sum;
import static griffio.domain.QEmployeeSalary.employeeSalary;
import static griffio.domain.QSalaryDetail.salaryDetail;

@Test
public class SalaryDetailTest {

  SalaryDetail bigBonus = new SalaryDetail("Bonus", new BigDecimal("25000"));
  SalaryDetail commission = new SalaryDetail("Commission", new BigDecimal("1000"));
  SalaryDetail gross = new SalaryDetail("Gross", new BigDecimal("75000"));
  SalaryDetail smallBonus = new SalaryDetail("Bonus", new BigDecimal("5000"));

  List<SalaryDetail> details;
  List<String> relevantSalaryNames = ImmutableList.of("Bonus", "Commission", "Gross");

  @BeforeTest
  public void setUp() throws Exception {
    details = ImmutableList.of(bigBonus, commission, gross, smallBonus);
  }

  public void big_bonus_salary_details() throws Exception {

    BigDecimal payThreshold = new BigDecimal("20000");
    BooleanExpression isBonusSalary = salaryDetail.salaryName.equalsIgnoreCase("Bonus");
    BooleanExpression isGreaterThanThreshold = salaryDetail.salary.goe(payThreshold);
    BooleanExpression isBonusAboveThreshold = isBonusSalary.and(isGreaterThanThreshold);

    List<SalaryDetail> actual = from(salaryDetail, details).where(isBonusAboveThreshold).list(salaryDetail);

    assertThat(actual).has().exactly(bigBonus);
  }

  public void relevant_salary_details_for_threshold() throws Exception {

    BooleanBuilder isRelevantSalaryName = new BooleanBuilder();

    for (String salaryName : relevantSalaryNames) {
      isRelevantSalaryName.or(salaryDetail.salaryName.eq(salaryName));
    }

    //mutable boolean builder
    isRelevantSalaryName.and(salaryDetail.salary.gt(new BigDecimal("50000")));

    List<SalaryDetail> actual = from(salaryDetail, details).where(isRelevantSalaryName).list(salaryDetail);

    assertThat(actual).has().exactly(gross);

  }

  public void sum_relevant_salaries() {

    BigDecimal actual = from(salaryDetail, details).singleResult(salaryDetail.salary.sum());

    assertThat(actual).is(new BigDecimal("106000.00"));
  }

  public void unique_salaries_from_employees_salaries() {

    ImmutableList<EmployeeSalary> employeesSalaries = ImmutableList.of(new EmployeeSalary(details), new EmployeeSalary(details));

    List<String> actual = from(employeeSalary, employeesSalaries)
        .innerJoin(employeeSalary.salaryDetails, salaryDetail)
        .where(salaryDetail.isSalaryRelevant())
        .distinct()
        .list(salaryDetail.salaryName);

    assertThat(actual).has().exactly("Bonus", "Commission", "Gross");
  }

  public void aggregated_by_salary_name() {

    ImmutableList<SalaryDetail> salaryDetails = ImmutableList.of(smallBonus, commission, bigBonus);

    List<SalaryDetail> actual = from(salaryDetail, salaryDetails)
        .orderBy(salaryDetail.salaryName.asc())
        .transform(groupBy(salaryDetail.salaryName)
            .list(create(salaryDetail.salaryName, sum(salaryDetail.salary))));

    assertThat(actual).has().exactly(smallBonus.add(bigBonus), commission);
  }

  public void sum_relevant_salary() throws Exception {

    ImmutableList<SalaryDetail> salaryDetails = ImmutableList.of(smallBonus, commission, bigBonus);

    BigDecimal thresholdForPayPeriod = new BigDecimal("12500");

    StringExpression caseSalaryName = new CaseBuilder()
        .when(salaryDetail.isSalaryRelevant().and(salaryDetail.salary.goe(thresholdForPayPeriod)))
        .then(salaryDetail.salaryName)
        .otherwise("Not Relevant");

    Map<String, BigDecimal> actual = from(salaryDetail, salaryDetails)
        .transform(groupBy(caseSalaryName).as(sum(salaryDetail.salary)));

    assertThat(actual).hasKey(bigBonus.getSalaryName()).withValue(bigBonus.getSalary());
    assertThat(actual).hasKey("Not Relevant").withValue(smallBonus.getSalary().add(commission.getSalary()));
  }
}