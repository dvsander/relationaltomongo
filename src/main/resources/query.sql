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
   dm.to_date
from
    employees e
    left join titles t on t.emp_no = e.emp_no
    left join salaries s on s.emp_no = e.emp_no
    left join dept_emp de on de.emp_no = e.emp_no
    left join departments d on d.dept_no = de.dept_no
    left join dept_manager dm on dm.emp_no = e.emp_no
    left join departments d2 on d2.dept_no = dm.dept_no