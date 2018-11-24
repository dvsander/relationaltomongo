package annotations;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.Serializable;

@Embeddable
public class DepartmentManagerPk implements Serializable {

    @Column(name = "dept_no")
    private String departmentNumber;

    @Column(name = "emp_no")
    private Integer employeeNumber;


}
