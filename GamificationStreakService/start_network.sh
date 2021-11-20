#!/bin/bash
java -cp "lib/*:service/*" i5.las2peer.tools.L2pNodeLauncher -p 9011 uploadStartupDirectory\(\'etc/startup\'\) \
startService\(\'i5.las2peer.services.gamificationStreakService.GamificationStreakService@0.1\',\'gamificationstreakpass\'\) startWebConnector interactive
