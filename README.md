
=======
# Gamification-Framework

## Database
The database that is used is PostgreSQL 9.5. SQL script to create the database and initialize the framework is located in ./etc/psql
The default framework database name is 'gamificationdb'. Each created application will have its own schema. A database named 'manager' is used to maintain application and user information globally.
## Front-End
The front-end is using Apache tomcat 9.0.0 in its development. The root of front-end application is located in ./frontend/webapps/ROOT
## Back-End
The back-end is using las2peer. To start the service, run bat script located in ./bin/start_network.bat

## Starting All Projects

To build all projects in Gamification-Framework, go to main project folder and run 
```
ant
```

To run all services, execute 
```
start.bat
```