# Relation to MongoDB migration proof of concept

In this example we will export data from a relational system and import it into MongoDB.

Online resources

- [MongoDB RDBMS migration guide](https://www.mongodb.com/collateral/rdbms-mongodb-migration-guide)

## Set-up and prerequisites

Set-up:

- Host the employees sample database provided by MySQL as per instructions on [the website](https://dev.mysql.com/doc/employee/en/). This work is licensed under the Creative Commons Attribution-Share Alike 3.0 Unported License. To view a copy of this license, visit http://creativecommons.org/licenses/by-sa/3.0/ or send a letter to Creative Commons, 171 Second Street, Suite 300, San Francisco, California, 94105, USA.

- Host a MongoDB replica set or set one up in the cloud via [MongoDB Atlas](https://cloud.mongodb.com).

## Description of the use cases

1. Use case: Custom Developed Software

This project hosts a Java project built using the Spring Boot framework.
It offers most fine grained control over the migration of data, it's however not suitable for very large amounts of datasets and may need to be repeated with multiple queries over several domain tables residing in the same database.

1. Use case: Scripted with MongoDB tooling

This is best suitable for large result sets, where data is extracted to disk and loaded into mongodb as fast as possible. Transforming the data is done once loaded inside MongoDB with the aggregation framework.

### Scripted use case

Run the MigratorApp main executable after having updated the application.properties file to suit your environment.

### Scripted with mongo tooling

THe following process may be followed:

1. Export the (entire) dataset to CSV.

An example query for MySQL can be:

    select
       e.emp_no,
       e.birth_date,
       e.first_name,
       e.last_name,
       e.gender,
       e.hire_date,
       t.title,
       t.from_date,
       t.to_date,
       s.salary,
       s.from_date,
       s.to_date,
       de.dept_no,
       de.from_date,
       de.to_date,
       d.dept_name,
       dm.dept_no,
       dm.from_date,
       dm.to_date,
       d2.dept_name
    from
        employees e
        left join titles t on t.emp_no = e.emp_no
        left join salaries s on s.emp_no = e.emp_no
        left join dept_emp de on de.emp_no = e.emp_no
        left join departments d on d.dept_no = de.dept_no
        left join dept_manager dm on dm.emp_no = e.emp_no
        left join departments d2 on d2.dept_no = dm.dept_no
    INTO OUTFILE '/var/lib/mysql-files/employees.csv'
    FIELDS TERMINATED BY ','
    ENCLOSED BY '"'
    LINES TERMINATED BY '\n';

2. (Optional) Sync the fieldFile.txt contents with your query

This file contains the field type mappings when importing the data.
For basic types there is automatic field detection, for date time information it's recommended to provide this information.

3. Zip the CSV and unzip the CSV to where you have shell access.

Highly recommended to run this on the database server itself.

4. Import !

- --db: the target database
- --collection: the target collection
- --type: the type of file
- --fieldFile: the file containing the field type mappings
- --file: the file to import
- --columnsHaveTypes: enable the type conversion during import

  mongoimport --db dev-app --collection stg_employees --type csv --fieldFile /shared/fieldFile.txt --file employees.csv --columnsHaveTypes --parseGrace skipField

5. Statistics

   Query OK, 5124191 rows affected (16.46 sec)

   2018-11-26T18:59:28.775+0000 connected to: localhost

   2018-11-26T19:02:18.189+0000 imported 5124191 documents

6. Aggregate example - use Compass's visual pipeline builder for this for this!

The below example groups all documents by emp_no and adds all related information to arrays within the same document.

    var pipeline = [
                     {
                       $group: {
                         _id: "$emp_no",
                         birth_date: {
                           $first: "$birth_date"
                         },
                         first_name: {
                           $first: "$first_name"
                         },
                         last_name: {
                           $first: "$last_name"
                         },
                         gender: {
                           $first: "$gender"
                         },
                         hire_date: {
                           $first: "$hire_date"
                         },
                         titles: {
                           $addToSet: {
                             title: "$title",
                             from_date: "$title_from_date",
                             to_date: "$title_to_date"
                           }
                         },
                         salaries: {
                           $addToSet: {
                             salary: "$salary",
                             from_date: "$salary_from_date",
                             to_date: "$salary_to_date"
                           }
                         },
                         departments: {
                           $addToSet: {
                             dept_no: "$dept_no",
                             dept_name: "$dept_name",
                             from_date: "$dept_from_date",
                             to_date: "$dept_to_date"
                           }
                         },
                         managing: {
                           $addToSet: {
                             dept_no: "$managing_dept_no",
                             dept_name: "$managing_dept_no"
                           }
                         }
                       }
                     }
                   ]


    db.scanned_letter_stg.aggregate(pipeline, {'allowDiskUse' : true})
