package annotations;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name="salaries")
public class Salary {

    @EmbeddedId
    private SalaryPk salaryPk;

    @Column(name = "salary")
    private Integer salary;

    @Column(name = "to_date")
    private Date toDate;


}
