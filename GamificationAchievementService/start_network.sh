#!/bin/bash
java -cp "lib/*:service/*" i5.las2peer.tools.L2pNodeLauncher -p 9011 uploadStartupDirectory\(\'etc/startup\'\) \
startService\(\'i5.las2peer.services.gamificationAchievementService.GamificationAchievementService@0.1\',\'gamificationachievementpass\'\)
startWebConnector interactive
		