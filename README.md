
=======
# Gamification-Framework

## Prerequisites
- Java 14 (or newer)
- Gradle 6.8 (exactly)
- PostgreSQL

## Database
The database used is PostgreSQL 9.5. SQL script to create the database and initialize the framework is located in psql folder.
The default framework database name is 'gamification'and user name is 'gamification'. Each created application will have its own schema. A schema named 'manager' is used to maintain application and user information globally. A schema named 'global_leaderboard' contains global leaderboard data with the table name in the schema is community_type name.

## Running the Gamification Framework through docker

  * The image is fairly large (~1.5GB), so it's advisable to first execute `docker pull registry.tech4comp.dbis.rwth-aachen.de/rwthacis/gamification:latest`
  * We can then run `docker run -p 8080:8080 registry.tech4comp.dbis.rwth-aachen.de/rwthacis/gamification:latest` to get a local installation of the gamification framework running under `localhost:8080/gamification`  

See the [Dockerfile](Dockerfile) and the [kubernetes folder](kubernetes/) for more info.

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

### Notes when running on Windows

When you run Gamification Framework on Windows, you can use `main.bat` instead of `main.sh`. It still requires a shell binary (so it only works in VSCode or a custom bash/shell). Also, the script only supports running the services in a single node, so the *join_node* arguments are not supported.

To run all services, in the main directory, execute:

```bat
./main.bat -r start_one_node
```

## API Documentation

Detailed generated documentation for the API of each service can be found in the following documents:

- [Game Service](docs/api/game-service/README.adoc)
- [Achievement Service](docs/api/acheivement-service/README.adoc)


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


## Local Setup

The web APIs for all services are deployed on port `8080`. You can access the different services at the following base URLS:

- Achievement Service: `http://localhost:8080/gamification/achievements/`
- Action Service: `http://localhost:8080/gamification/actions/`
- Badge Service: `http://localhost:8080/gamification/badges/`
- Game Service: `http://localhost:8080/gamification/games/`
- Gamifier Service: `http://localhost:8080/gamification/gamifier/`
- Level Service: `http://localhost:8080/gamification/levels/`
- Point Service: `http://localhost:8080/gamification/points/`
- Quest Service: `http://localhost:8080/gamification/quests/`
- Streak Service: `http://localhost:8080/gamification/streaks/`
- Visualization Service: `http://localhost:8080/gamification/visualization/`

When each service is started in an individual node, the following ports are used for the HTTP APIs:

- **ACHIEVEMENT_PORT**=8083
- **ACTION_PORT**=8084
- **BADGE_PORT**=8085
- **GAME_PORT**=8086
- **LEVEL_PORT**=8087
- **POINT_PORT**=8088
- **QUEST_PORT**=8089