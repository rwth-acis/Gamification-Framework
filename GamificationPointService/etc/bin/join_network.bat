:: this script is autogenerated by 'ant startscripts'
:: it starts a LAS2peer node providing the service 'i5.las2peer.services.gamificationPointService.GamificationPointService' of this project
:: pls execute it from the bin folder of your deployment by double-clicking on it

%~d0
cd %~p0
cd ..
set BASE=%CD%
        	
set CLASSPATH="%CD%/lib/*;%CD%/service/*;"

java -cp %CLASSPATH% i5.las2peer.tools.L2pNodeLauncher -w -p 9014 -b 192.168.56.1:9011 uploadStartupDirectory('etc/startup')^
 startService('i5.las2peer.services.gamificationPointService.GamificationPointService@0.1','gamificationpointpass') interactive
 pause

