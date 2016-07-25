#!/bin/bash

# this script is autogenerated by 'ant startscripts'
# it starts a LAS2peer node providing the service 'i5.las2peer.services.gamificationQuestService.GamificationQuestService' of this project
# pls execute it from the root folder of your deployment, e. g. ./bin/start_network.sh

java -cp "lib/*:service/*" i5.las2peer.tools.L2pNodeLauncher -p 9015 -b 192.168.56.1:9011 uploadStartupDirectory\(\'etc/startup\'\) \
startService\(\'i5.las2peer.services.gamificationQuestService.GamificationQuestService@0.1\'\) startWebConnector interactive
