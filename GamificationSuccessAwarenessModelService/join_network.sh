#!/bin/bash

# this script is autogenerated by 'ant startscripts'
# it starts a LAS2peer node providing the service 'i5.las2peer.services.gamificationSuccessAwarenessModelService.GamificationSuccessAwarenessModelService' of this project
# pls execute it from the root folder of your deployment, e. g. ./bin/start_network.sh

java -cp "lib/*:service/*" i5.las2peer.tools.L2pNodeLauncher -p 9120 -b 0.0.0.0:9010 --observer uploadStartupDirectory\(\'etc/startup\'\) \
startService\(\'i5.las2peer.services.gamificationSuccessAwarenessModelService.GamificationSuccessAwarenessModelService@0.1\',\'gamificationsuccessmodelpass\'\) startWebConnector interactive