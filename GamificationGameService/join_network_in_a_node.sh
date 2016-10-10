
#!/bin/bash

# this script is autogenerated by 'ant startscripts'
# it starts a LAS2peer node providing the service 'i5.las2peer.services.gamificationApplicationService.GamificationApplicationService' of this project
# pls execute it from the root folder of your deployment, e. g. ./bin/start_network.sh

java -cp "lib/*:service/*" i5.las2peer.tools.L2pNodeLauncher -p 9011 -b 137.226.232.171:9010 --observer uploadStartupDirectory\(\'etc/startup\'\) \
startService\(\'i5.las2peer.services.gamificationGameService.GamificationGameService@0.1\',\'gamificationgamepass\'\) \
startService\(\'i5.las2peer.services.gamificationQuestService.GamificationQuestService@0.1\',\'gamificationquestpass\'\) \
startService\(\'i5.las2peer.services.gamificationBadgeService.GamificationBadgeService@0.1\',\'gamificationbadgepass\'\) \
startService\(\'i5.las2peer.services.gamificationPointService.GamificationPointService@0.1\',\'gamificationpointpass\'\) \
startService\(\'i5.las2peer.services.gamificationAchievementService.GamificationAchievementService@0.1\',\'gamificationachievementpass\'\) \
startService\(\'i5.las2peer.services.gamificationLevelService.GamificationLevelService@0.1\',\'gamificationlevelpass\'\) \
startService\(\'i5.las2peer.services.gamificationActionService.GamificationActionService@0.1\',\'gamificationactionpass\'\) \
startService\(\'i5.las2peer.services.gamificationVisualizationService.GamificationVisualizationService@0.1\',\'gamificationvisualizationpass\'\) \
startService\(\'i5.las2peer.services.gamificationGamifierService.GamificationGamifierService@0.1\',\'gamificationgamifierpass\'\) startWebConnector interactive