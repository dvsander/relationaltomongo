package annotations;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import java.io.Serializable;
import java.util.Date;

@Embeddable
public class TitlePk implements Serializable {

    @ManyToOne
    @JoinColumn(name = "emp_no")
    private Employee employee;

    @Column(name = "title")
    private String title;

    @Column(name = "from_date")
    private Date fromDate;

}
