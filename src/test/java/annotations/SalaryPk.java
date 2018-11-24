package annotations;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Embeddable
public class SalaryPk implements Serializable {

    @JoinColumn(name = "emp_no")
    @ManyToOne
    private Employee employee;

    @Column(name="from_date")
    private Date fromDate;


}
