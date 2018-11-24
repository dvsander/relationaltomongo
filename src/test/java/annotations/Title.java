package annotations;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "titles")
public class Title {

    @EmbeddedId
    private TitlePk titlePk;

    @Column(name = "to_date")
    private Date toDate;

}
