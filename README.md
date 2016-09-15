
=======
# Gamification-Framework

## Database
The database that is used is PostgreSQL 9.5. SQL script to create the database and initialize the framework is located in psql folder.
The default framework database name is 'gamification'and user name is 'gamification'. Each created application will have its own schema. A schema named 'manager' is used to maintain application and user information globally. A schema named 'global_leaderboard' contains global leaderboard data with the table name in the schema is community_type name.

## Building the Projects

1. Configure Database
  Go to psql folder in the main folder.
  * Create database with the name *gamification* or run sql script in dbcreation.sql
  * Initialize the database by running sql script in db.sql
2. Configure las2peer project properties
  All of las2peer project properties (database driver name and credentials) can be configured by changing the content of *config* file. Then, run

  ```
  bash configure.sh
  ```
  
3. Build the code
  To build all projects in Gamification-Framework, run

  ```
  bash make.sh
  ```
  
  with these options :
  1.  To clean up the projects
    ```
    bash make.sh clean
    ```
  2. To only build the projects (without testing)
    ```
    bash make.sh build
    ```
  3. To build and test the projects
    ```
    bash make.sh all
    ```

## Starting the Projects

To run all services, in main folder project execute
```
bash start_in_a_node.sh
```
