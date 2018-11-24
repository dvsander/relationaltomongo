package annotations;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.Serializable;

@Embeddable
public class DepartmentEmployeePk implements Serializable {

    @Column(name = "emp_no")
    private Integer employeeNumber;

    @Column(name="dept_no")
    private String departmentNumber;

}
