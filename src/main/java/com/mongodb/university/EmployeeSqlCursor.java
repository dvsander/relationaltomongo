package com.mongodb.university;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

@Repository
public class EmployeeSqlCursor {

    @Autowired
    private DataSource dataSource;

    private Logger logger = LoggerFactory.getLogger(EmployeeSqlCursor.class);

    private static final String STATEMENT =
            "select\n" +
            "   e.emp_no,\n" +
            "   e.birth_date,\n" +
            "   e.first_name,\n" +
            "   e.last_name,\n" +
            "   e.gender,\n" +
            "   e.hire_date,\n" +
            "   t.title,\n" +
            "   t.from_date,\n" +
            "   t.to_date,\n" +
            "   s.salary,\n" +
            "   s.from_date,\n" +
            "   s.to_date,\n" +
            "   de.dept_no,\n" +
            "   de.from_date,\n" +
            "   de.to_date,\n" +
            "   d.dept_name,\n" +
            "   dm.dept_no,\n" +
            "   dm.from_date,\n" +
            "   dm.to_date,\n" +
            "   d2.dept_name\n" +
            "from\n" +
            "    employees e\n" +
            "    left join titles t on t.emp_no = e.emp_no\n" +
            "    left join salaries s on s.emp_no = e.emp_no\n" +
            "    left join dept_emp de on de.emp_no = e.emp_no\n" +
            "    left join departments d on d.dept_no = de.dept_no\n" +
            "    left join dept_manager dm on dm.emp_no = e.emp_no\n" +
            "    left join departments d2 on d2.dept_no = dm.dept_no\n" +
            "order by e.emp_no asc";

    public ResultSet buildIterator(){
        try {
            final Statement st = this.dataSource.getConnection().createStatement(java.sql.ResultSet.TYPE_FORWARD_ONLY,
                    java.sql.ResultSet.CONCUR_READ_ONLY);
            st.setFetchSize(Integer.MIN_VALUE);
            return st.executeQuery(STATEMENT);
        } catch (SQLException e){
            throw new RuntimeException(e);
        }

    }


}
