
=======
# Gamification-Framework

## Database
The database used is PostgreSQL 9.5. SQL script to create the database and initialize the framework is located in psql folder.
The default framework database name is 'gamification'and user name is 'gamification'. Each created application will have its own schema. A schema named 'manager' is used to maintain application and user information globally. A schema named 'global_leaderboard' contains global leaderboard data with the table name in the schema is community_type name.

## Building the Projects

1. **Configure Database**
  * Go to psql folder in the main folder.
  * Create database with the name *gamification* or run sql script in dbcreation.sql
  * Initialize the database by running sql script in db.sql
2. **Configure las2peer project properties**
  * All of las2peer project properties (database driver name and credentials) can be configured by changing the content of *gamification.config* file in config folder. The content of gamification.config are described in **Content of gamification.config** section.
  * To apply the configuration to all services, run  ```bash main.sh -s or ./main.sh -s```
  
3. **Build the code**
	1. To build and test all projects in Gamification-Framework, run ```./main.sh -m all```
	2. To clean up the projects, run ```./main.sh -m clean```
	3. To only build the projects (without testing), run ```./main.sh -m build```

## Starting the Projects

To run all services, in main folder project execute

1. ```./main.sh -r start_one_node```, to run all services in a node in new network
1. ```./main.sh -r join_one_node```, to run all services in a node and join existing network
1. ```./main.sh -r join_node```
	1. ```./main.sh -r join_node start```, to run all services in different node and join existing network
	1. ```./main.sh -r join_node stop```, to stop all services in different node and join existing network

## Content of gamification.config

> - **jdbcDriverClassName**="org.postgresql.Driver"  (Database Driver for JAVA)
> - **jdbcUrl**="jdbc:postgresql://127.0.0.1:5432/" (Database URL)
> - **jdbcSchema**="gamification" (Database schema name)
> - **jdbcLogin**="" (Database login user name credential)
> - **jdbcPass**="" (Database login password credential)
> 
> - **gitHubCAEUser**="CAEGamifiedUser" (CAE repository user name)
> - **gitHubCAEUserMail**="CAEGamified@gmail.com" (CAE repository e-mail)
> - **gitHubCAEOrganizationOrigin**="CAE-Dev"  (CAE repository organization name)
> 
> - **gitHubUserNewRepo**="CAEGamifiedUser" (Gamification Framework repository user name)
> - **gitHubUserMailNewRepo**="CAEGamified@gmail.com" (Gamification Framework repository e-mail)
> - **gitHubOrganizationNewRepo**="CAE-Gamified" (Gamification Framework repository organization name)
> - **gitHubPasswordNewRepo**=""  (Gamification Framework repository password of user name)
> 
> - **BASE\_NODE_IP**="" (IP of bootstrap network)
> - **BASE\_NODE_PORT**="9010" (Port of bootstrap network)
> 
 Deployed port in las2peer :

> - **ACHIEVEMENT_PORT**="9111"
> - **ACTION_PORT**="9112"
> - **BADGE_PORT**="9113"
> - **GAME_PORT**="9114"
> - **GAMIFIER_PORT**="9115"
> - **LEVEL_PORT**="9116"
> - **POINT_PORT**="9117"
> - **QUEST_PORT**="9118"
> - **VISUALIZATION_PORT**="9119"

## HTTP PORT
> - **ACHIEVEMENT_PORT**=8083
> - **ACTION_PORT**=8084
> - **BADGE_PORT**=8085
> - **GAME_PORT**=8086
> - **LEVEL_PORT**=8087
> - **POINT_PORT**=8088
> - **QUEST_PORT**=8089