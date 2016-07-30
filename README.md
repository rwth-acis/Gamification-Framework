
=======
# Gamification-Framework

## Database
The database that is used is PostgreSQL 9.5. SQL script to create the database and initialize the framework is located in ./etc/psql
The default framework database name is 'gamificationdb'. Each created application will have its own schema. A database named 'manager' is used to maintain application and user information globally.

## Starting Projects

1. Configure Database

* Go to psql folder in the main program.
* Create database with the name *gamificationdb* or run sql script in dbcreation.sql
* Initialize the database by running sql script in db.sql

2. Configure las2peer project properties

All projects have the same properties configuration. All of las2peer project properties (database driver name and credentials) can be configured by changing the content of *gamification.properties*. Then, run ```
```
set_properties(.bat|.sh)
```

3. Build the code

To build all projects in Gamification-Framework, go to GamificationApplicationService and run
```
ant build_all_projects
```

To test all projects, run
```
ant test_all_projects
```

To clean all projects, run
```
ant clean_all_projects
```


4. Run

To run all services, in main folder project execute 
```
start.bat
```