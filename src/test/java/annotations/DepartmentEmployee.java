package annotations;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "dept_emp")
public class DepartmentEmployee {

    @EmbeddedId
    private DepartmentEmployeePk departmentEmployeePk;

    @Column(name = "from_date")
    private Date fromDate;

    @Column(name = "to_date")
    private Date toDate;


}
