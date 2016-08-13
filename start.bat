
start call GamificationApplicationService/bin/start_network.bat
echo "Press any key after application service already up"
pause
start call GamificationPointService/bin/join_network.bat
start call GamificationBadgeService/bin/join_network.bat
start call GamificationAchievementService/bin/join_network.bat
start call GamificationActionService/bin/join_network.bat
start call GamificationLevelService/bin/join_network.bat
start call GamificationQuestService/bin/join_network.bat
start call GamificationVisualizationService/bin/join_network.bat