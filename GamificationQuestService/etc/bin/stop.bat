%~d0
cd %~p0
cd ..
set BASE=%CD%
        	
set CLASSPATH=%CD%/lib/*;%CD%/service/*;

java -cp %CLASSPATH% i5.las2peer.tools.L2pNodeLauncher -w -p 9001 -n 123456789^
 stopService('i5.las2peer.services.badgeService.BadgeService@0.1')^
 stopService('i5.las2peer.services.gamificationManagerService.GamificationManagerService@0.1')^
 stopService('i5.las2peer.services.fileService.FileService@1.0') stopWebConnector 