#!/bin/bash
java -cp "lib/*:service/*" i5.las2peer.tools.L2pNodeLauncher -p 9011 uploadStartupDirectory\(\'etc/startup\'\) \
startService\(\'i5.las2peer.services.gamificationSuccessAwarenessModelService.GamificationSuccessAwarenessModelService@0.1\',\'gamificationsuccessmodelpass\'\) startWebConnector interactive
