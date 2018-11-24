package annotations;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.util.Date;

@Entity
@Table(name="dept_manager")
public class DepartmentManager {

    @EmbeddedId
    private DepartmentManagerPk departmentManagerPk;

    @Column(name = "from_date")
    private Date fromDate;

    @Column(name = "to_date")
    private Date toDate;

}
