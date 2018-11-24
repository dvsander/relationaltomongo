package annotations;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "departments")
public class Department {

    @Id
    @Column(name = "dept_no")
    private String departmentNumber;

    @Column(name = "dept_name")
    private String getDepartmentName;

}
