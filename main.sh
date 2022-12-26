#!/bin/bash

function configure_function {
	echo "[Configure]"
   	echo "Reading config"
	source config/gamification.config

	CONF_PROPERTIES="jdbcDriverClassName=$jdbcDriverClassName\njdbcUrl=$jdbcUrl\njdbcSchema=$jdbcSchema\njdbcLogin=$jdbcLogin\njdbcPass=$jdbcPass\nmonitor=TRUE"

	CONF_APP_PROPERTIES="gitHubUser=$gitHubCAEUser\ngitHubUserMail=$gitHubCAEUserMail\ngitHubOrganizationOrigin=$gitHubCAEOrganizationOrigin\ngitHubUserNewRepo=$gitHubUserNewRepo\ngitHubUserMailNewRepo=$gitHubUserMailNewRepo\ngitHubOrganizationNewRepo=$gitHubOrganizationNewRepo\ngitHubPasswordNewRepo=$gitHubPasswordNewRepo\nmonitor=TRUE"

	echo "Writing properties onfiguration "
	printf $CONF_PROPERTIES > GamificationGameService/etc/i5.las2peer.services.gamificationGameService.GamificationGameService.properties
	printf $CONF_PROPERTIES > GamificationAchievementService/etc/i5.las2peer.services.gamificationAchievementService.GamificationAchievementService.properties
	printf $CONF_PROPERTIES > GamificationActionService/etc/i5.las2peer.services.gamificationActionService.GamificationActionService.properties
	printf $CONF_PROPERTIES > GamificationBadgeService/etc/i5.las2peer.services.gamificationBadgeService.GamificationBadgeService.properties
	printf $CONF_PROPERTIES > GamificationBotWrapperService/etc/i5.las2peer.services.gamificationBotWrapperService.GamificationBadgeService.properties
	printf $CONF_PROPERTIES > GamificationLevelService/etc/i5.las2peer.services.gamificationLevelService.GamificationLevelService.properties
	printf $CONF_PROPERTIES > GamificationPointService/etc/i5.las2peer.services.gamificationPointService.GamificationPointService.properties
	printf $CONF_PROPERTIES > GamificationStreakService/etc/i5.las2peer.services.gamificationStreakService.GamificationStreakService.properties
	printf $CONF_PROPERTIES > GamificationQuestService/etc/i5.las2peer.services.gamificationQuestService.GamificationQuestService.properties
	printf $CONF_PROPERTIES > GamificationVisualizationService/etc/i5.las2peer.services.gamificationVisualizationService.GamificationVisualizationService.properties
	printf $CONF_APP_PROPERTIES > GamificationGamifierService/etc/i5.las2peer.services.gamificationGamifierService.GamificationGamifierService.properties

	echo "Copying executable shell"
	cp -f GamificationAchievementService/etc/bin/join_network.sh GamificationAchievementService/join_network.sh
	cp -f GamificationActionService/etc/bin/join_network.sh GamificationActionService/join_network.sh
	cp -f GamificationBadgeService/etc/bin/join_network.sh GamificationBadgeService/join_network.sh
	cp -f GamificationBotWrapperService/etc/bin/join_network.sh GamificationBotWrapperService/join_network.sh
	cp -f GamificationLevelService/etc/bin/join_network.sh GamificationLevelService/join_network.sh
	cp -f GamificationPointService/etc/bin/join_network.sh GamificationPointService/join_network.sh
	cp -f GamificationQuestService/etc/bin/join_network.sh GamificationQuestService/join_network.sh
	cp -f GamificationStreakService/etc/bin/join_network.sh GamificationStreakService/join_network.sh
	cp -f GamificationVisualizationService/etc/bin/join_network.sh GamificationVisualizationService/join_network.sh
	cp -f GamificationGamifierService/etc/bin/join_network.sh GamificationGamifierService/join_network.sh
	cp -f GamificationGameService/etc/bin/join_network.sh GamificationGameService/join_network.sh
	cp -f GamificationGameService/etc/bin/start_network.sh GamificationGameService/start_network.sh

	echo "Adjusting shell configuration"

	#Configure Individual Join script
	sed -i 's/\-p \([0-9]*\) /-p '$ACHIEVEMENT_PORT' /' GamificationAchievementService/join_network.sh
	sed -i 's/\-p \([0-9]*\) /-p '$ACTION_PORT' /' GamificationActionService/join_network.sh
	sed -i 's/\-p \([0-9]*\) /-p '$BADGE_PORT' /' GamificationBadgeService/join_network.sh
	sed -i 's/\-p \([0-9]*\) /-p '$BOT_PORT' /' GamificationBotWrapperService/join_network.sh
	sed -i 's/\-p \([0-9]*\) /-p '$GAME_PORT' /' GamificationGameService/join_network.sh
	sed -i 's/\-p \([0-9]*\) /-p '$GAMIFIER_PORT' /' GamificationGamifierService/join_network.sh
	sed -i 's/\-p \([0-9]*\) /-p '$LEVEL_PORT' /' GamificationLevelService/join_network.sh
	sed -i 's/\-p \([0-9]*\) /-p '$POINT_PORT' /' GamificationPointService/join_network.sh
	sed -i 's/\-p \([0-9]*\) /-p '$QUEST_PORT' /' GamificationQuestService/join_network.sh
	sed -i 's/\-p \([0-9]*\) /-p '$STREAK_PORT' /' GamificationStreakService/join_network.sh
	sed -i 's/\-p \([0-9]*\) /-p '$VISUALIZATION_PORT' /' GamificationVisualizationService/join_network.sh
	sed -i 's/\-p \([0-9]*\) /-p '$GAME_PORT' /' GamificationGameService/join_network_in_a_node.sh
	

	#Configure Bootstrap Node
	sed -i 's/\-b \(.*\):\([0-9]*\) /-b '$BASE_NODE_IP':'$BASE_NODE_PORT' \-\-observer /' GamificationAchievementService/join_network.sh
	sed -i 's/\-b \(.*\):\([0-9]*\) /-b '$BASE_NODE_IP':'$BASE_NODE_PORT' \-\-observer /' GamificationActionService/join_network.sh
	sed -i 's/\-b \(.*\):\([0-9]*\) /-b '$BASE_NODE_IP':'$BASE_NODE_PORT' \-\-observer /' GamificationBadgeService/join_network.sh
	sed -i 's/\-b \(.*\):\([0-9]*\) /-b '$BASE_NODE_IP':'$BASE_NODE_PORT' \-\-observer /' GamificationBotWrapperService/join_network.sh
	sed -i 's/\-b \(.*\):\([0-9]*\) /-b '$BASE_NODE_IP':'$BASE_NODE_PORT' \-\-observer /' GamificationLevelService/join_network.sh
	sed -i 's/\-b \(.*\):\([0-9]*\) /-b '$BASE_NODE_IP':'$BASE_NODE_PORT' \-\-observer /' GamificationPointService/join_network.sh
	sed -i 's/\-b \(.*\):\([0-9]*\) /-b '$BASE_NODE_IP':'$BASE_NODE_PORT' \-\-observer /' GamificationQuestService/join_network.sh
	sed -i 's/\-b \(.*\):\([0-9]*\) /-b '$BASE_NODE_IP':'$BASE_NODE_PORT' \-\-observer /' GamificationVisualizationService/join_network.sh
	sed -i 's/\-b \(.*\):\([0-9]*\) /-b '$BASE_NODE_IP':'$BASE_NODE_PORT' \-\-observer /' GamificationGamifierService/join_network.sh
	sed -i 's/\-b \(.*\):\([0-9]*\) /-b '$BASE_NODE_IP':'$BASE_NODE_PORT' \-\-observer /' GamificationGameService/join_network.sh
	sed -i 's/\-b \(.*\):\([0-9]*\) /-b '$BASE_NODE_IP':'$BASE_NODE_PORT' \-\-observer /' GamificationStreakService/join_network.sh


	echo "Make it executable"
	chmod +x GamificationAchievementService/join_network.sh
	chmod +x GamificationActionService/join_network.sh
	chmod +x GamificationBadgeService/join_network.sh
	chmod +x GamificationBotWrapperService/join_network.sh
	chmod +x GamificationLevelService/join_network.sh
	chmod +x GamificationPointService/join_network.sh
	chmod +x GamificationQuestService/join_network.sh
	chmod +x GamificationVisualizationService/join_network.sh
	chmod +x GamificationGamifierService/join_network.sh
	chmod +x GamificationGameService/join_network.sh
	chmod +x GamificationStreakService/join_network.sh
	chmod +x GamificationGameService/join_network_in_a_node.sh

	echo "Done !!"

}



function run_one_node_function {
	echo "[Start One Node]"
	cd GamificationGameService && sh start_network_in_a_node.sh
}

function run_join_one_node_function {
	echo "[Join One Node]"
	if screen -list | grep -q "gamification"; then
		screen -S gamification -X quit
	fi
	echo "run Gamification.."
	cd GamificationGameService &&  screen -dmS gamification sh join_network_in_a_node.sh
	screen -r gamification
}

function stop_join_one_node_function {
	echo "[Stopping Gamification..["
	if screen -list | grep -q "gamification"; then
		screen -S gamification -X quit
	else
		echo "No screen for Gamification found"
	fi
}

function run_join_node_function {
	echo "[Join Node]"
	cd script && sh run_join.sh
}

function stop_join_node_function {
	echo "[Stop Join Node]"
	cd script && sh stop_join.sh
}
function set_in_service_config {
	sed -i "s?${1}[[:blank:]]*=.*?${1}=${2}?g" ${SERVICE_PROPERTY_FILE}
}

function replace_properties_function {
	echo "[replacing properties file]"
	set_in_service_config jdbcSchema ${DATABASE_NAME}
	set_in_service_config jdbcUrl ${DATABASE_HOST}
	set_in_service_config jdbcLogin ${DATABASE_USER}
	set_in_service_config jdbcPass ${DATABASE_PASSWORD}
}

function help {
	  echo "Argument is not recognized!, the format should be:"
	  echo "main.sh <Option> <Arguments>"
	  echo "--------------------------------------------------"
	  echo "<Option> <Arguments>:"
	  echo "-s : Set the configuration to all services"
	  echo "-m build : Build all services only"
	  echo "   clean : Clean all services"
	  echo "   all   : Build and test all services"
	  echo "-r start_one_node  : Run all services in a node in new network"
	  echo "   join_one_node   : Run all services in a node and join existing network"
	  echo "   join_node start : Run all services in different node and join existing network"
	  echo "   join_node stop  : Stop all services in different node and join existing network"

}

if [ "$1" == "-m" ]; then
	configure_function
#	cd GamificationGameService && \

	if [ "$2" == "build" ]; then
		echo "[Build only]"
	  ./gradlew build --exclude-task test
	elif [ "$2" == "clean" ]; then
	  echo "[Clean only]"
	  ./gradlew clean
	elif [ "$2" == "test" ]; then
	  echo "[Test only]"
	  ./gradlew test
	elif [ "$2" == "all" ]; then
	  echo "[Build all]"
	  ./gradlew build
	else
	  help
	fi
elif [ "$1" == "-s" ]; then
  configure_function
elif [ "$1" == "-r" ]; then
	echo "[Run]"
	if [ "$2" == "start_one_node" ]; then
		export SERVICE_PROPERTY_FILE='GamificationGameService/etc/i5.las2peer.services.gamificationVisualizationService.GamificationVisualizationService.properties'
		replace_properties_function
		export SERVICE_PROPERTY_FILE='GamificationGameService/etc/i5.las2peer.services.gamificationLevelService.GamificationLevelService.properties'
		replace_properties_function
		export SERVICE_PROPERTY_FILE='GamificationGameService/etc/i5.las2peer.services.gamificationActionService.GamificationActionService.properties^'
		replace_properties_function
		export SERVICE_PROPERTY_FILE='GamificationGameService/etc/i5.las2peer.services.gamificationAchievementService.GamificationAchievementService.properties'
		replace_properties_function
		export SERVICE_PROPERTY_FILE='GamificationGameService/etc/i5.las2peer.services.gamificationGameService.GamificationGameService.properties'
		replace_properties_function
		export SERVICE_PROPERTY_FILE='GamificationGameService/etc/i5.las2peer.services.gamificationBadgeService.GamificationBadgeService.properties'
		replace_properties_function	
		export SERVICE_PROPERTY_FILE='GamificationGameService/etc/i5.las2peer.services.gamificationQuestService.GamificationQuestService.properties'
		replace_properties_function
		export SERVICE_PROPERTY_FILE='GamificationGameService/etc/i5.las2peer.services.gamificationStreakService.GamificationStreakService.properties'
		replace_properties_function
		export SERVICE_PROPERTY_FILE='GamificationGameService/etc/i5.las2peer.services.gamificationBotWrapperService.GamificationBotWrapperService.properties'
		replace_properties_function
		export SERVICE_PROPERTY_FILE='GamificationGameService/etc/i5.las2peer.services.gamificationPointService.GamificationPointService.properties'
		replace_properties_function
		export SERVICE_PROPERTY_FILE='GamificationGameService/etc/i5.las2peer.services.gamificationGamifierService.GamificationGamifierService.properties'
		replace_properties_function
		echo ${DATABASE_NAME}
		run_one_node_function
	elif [ "$2" == "join_one_node" ]; then
		if [ "$3" == "start" ]; then
			run_join_one_node_function
		elif [ "$3" == "stop" ]; then
			stop_join_one_node_function
		else
			run_join_one_node_function
		fi
	elif [ "$2" == "join_node" ]; then
		if [ "$3" == "start" ]; then
			run_join_node_function
		elif [ "$3" == "stop" ]; then
			stop_join_node_function
		else
			run_join_node_function
		fi
	else
	  help
	fi
else
  help
fi