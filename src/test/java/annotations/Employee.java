package annotations;

import javax.persistence.*;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "employees")
public class Employee {

    @Id
    @Column(name = "emp_no")
    private Integer employeeNumber;

    @Column(name="birth_date")
    private Date birthDate;

    @Column(name = "first_name")
    private String firstName;

    @Column(name = "last_name")
    private String lastName;

    @Column(name = "gender")
    private String gender;

    @Column(name = "hire_date")
    private Date hireDate;

    @OneToMany
    private List<Salary> salaries;

    @OneToMany
    private List<Title> titles;

    @OneToMany
    private List<DepartmentEmployee> departmentEmployees;


}
