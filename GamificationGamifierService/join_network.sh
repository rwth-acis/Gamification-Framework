#!/bin/bash

# this script is autogenerated by 'ant startscripts'
# it starts a LAS2peer node providing the service 'i5.las2peer.services.gamificationBadgeService.GamificationBadgeService' of this project
# pls execute it from the root folder of your deployment, e. g. ./bin/start_network.sh

java -cp "lib/*:service/*" i5.las2peer.tools.L2pNodeLauncher -p 9115 -b 137.226.232.171:9010 --observer uploadStartupDirectory\(\'etc/startup\'\) \
startService\(\'i5.las2peer.services.gamificationGamifierService.GamificationGamifierService@0.1\',\'gamificationgamifierpass\'\) startWebConnector interactive
