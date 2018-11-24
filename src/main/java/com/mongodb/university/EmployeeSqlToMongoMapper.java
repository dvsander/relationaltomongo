package com.mongodb.university;

import org.bson.Document;
import org.springframework.stereotype.Component;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;

@Component
public class EmployeeSqlToMongoMapper {

    public Document sqlToDocument(ResultSet rs) throws SQLException {
        Document root = new Document();

        Document emp = new Document();
        emp.append("emp_no", rs.getInt(1));
        emp.append("birth_date", getAsNullOrLocalDate(rs, 2));
        emp.append("first_name", rs.getString(3));
        emp.append("last_name", rs.getString(4));
        emp.append("gender", rs.getString(5));
        emp.append("hire_date", getAsNullOrLocalDate(rs,6));
        root.append("emp", emp);

        Document title = new Document();
        title.append("title", rs.getString(7));
        title.append("from_date", getAsNullOrLocalDate(rs, 8));
        title.append("to_date", getAsNullOrLocalDate(rs, 9));
        root.append("title", title);

        Document salary = new Document();
        salary.append("salary", rs.getInt(10));
        salary.append("from_date", getAsNullOrLocalDate(rs, 11));
        salary.append("to_date", getAsNullOrLocalDate(rs, 12));
        root.append("salary", salary);

        Document dept = new Document();
        dept.append("dept_no", rs.getString(13));
        dept.append("from_date", getAsNullOrLocalDate(rs, 14));
        dept.append("to_date", getAsNullOrLocalDate(rs, 15));
        dept.append("name", rs.getString(16));
        root.append("dept", dept);

        Document manager = new Document();
        manager.append("dept_no", rs.getString(17));
        manager.append("from_date", getAsNullOrLocalDate(rs, 18));
        manager.append("to_date", getAsNullOrLocalDate(rs, 19));
        manager.append("dept_name", rs.getString(20));
        root.append("manager", manager);

        return root;
    }

    public static java.util.Date getAsNullOrLocalDate(ResultSet rs, int idx) throws SQLException{
        Date sqlDate = rs.getDate(idx);
        return sqlDate != null ? new java.util.Date(sqlDate.getTime()) : null;
    }

}
