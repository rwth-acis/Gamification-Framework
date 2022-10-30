
java -cp "lib/*:service/*" i5.las2peer.tools.L2pNodeLauncher -p 9115 -b 127.0.0.1:9010 --observer uploadStartupDirectory\(\'etc/startup\'\) \
startService\(\'i5.las2peer.services.gamificationGameService.GamificationGameService@0.1\',\'gamificationgamepass\'\) startWebConnector interactive
