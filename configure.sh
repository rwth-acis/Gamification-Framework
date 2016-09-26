echo "Reading config"
source config

OLD_NODE_IP="192.168.56.1"

CONF_PROPERTIES="jdbcDriverClassName=$jdbcDriverClassName\n\
jdbcUrl=$jdbcUrl\n\
jdbcSchema=$jdbcSchema\n\
jdbcLogin=$jdbcLogin\n\
jdbcPass=$jdbcPass\n\
monitor=TRUE"

CONF_APP_PROPERTIES="gitHubUser=$gitHubUser\n\
gitHubUserMail=$gitHubUserMail\n\
gitHubOrganizationOrigin=$gitHubOrganizationOrigin\n\
gitHubUserNewRepo=$gitHubUserNewRepo\n\
gitHubUserMailNewRepo=$gitHubUserMailNewRepo\n\
gitHubOrganizationNewRepo=$gitHubOrganizationNewRepo\n\
gitHubPasswordNewRepo=$gitHubPasswordNewRepo\n\
monitor=TRUE"

echo "Writing properties onfiguration "
printf $CONF_PROPERTIES > GamificationApplicationService/etc/i5.las2peer.services.gamificationApplicationService.GamificationApplicationService.properties
printf $CONF_PROPERTIES > GamificationAchievementService/etc/i5.las2peer.services.gamificationAchievementService.GamificationAchievementService.properties
printf $CONF_PROPERTIES > GamificationActionService/etc/i5.las2peer.services.gamificationActionService.GamificationActionService.properties
printf $CONF_PROPERTIES > GamificationBadgeService/etc/i5.las2peer.services.gamificationBadgeService.GamificationBadgeService.properties
printf $CONF_PROPERTIES > GamificationLevelService/etc/i5.las2peer.services.gamificationLevelService.GamificationLevelService.properties
printf $CONF_PROPERTIES > GamificationPointService/etc/i5.las2peer.services.gamificationPointService.GamificationPointService.properties
printf $CONF_PROPERTIES > GamificationQuestService/etc/i5.las2peer.services.gamificationQuestService.GamificationQuestService.properties
printf $CONF_PROPERTIES > GamificationVisualizationService/etc/i5.las2peer.services.gamificationVisualizationService.GamificationVisualizationService.properties
printf $CONF_APP_PROPERTIES > GamificationGamifierService/etc/i5.las2peer.services.gamificationGamifierService.GamificationGamifierService.properties

echo "Copying executable shell"
cp -f GamificationAchievementService/bin/join_network.sh GamificationAchievementService/join_network.sh
cp -f GamificationActionService/bin/join_network.sh GamificationActionService/join_network.sh
cp -f GamificationBadgeService/bin/join_network.sh GamificationBadgeService/join_network.sh
cp -f GamificationLevelService/bin/join_network.sh GamificationLevelService/join_network.sh
cp -f GamificationPointService/bin/join_network.sh GamificationPointService/join_network.sh
cp -f GamificationQuestService/bin/join_network.sh GamificationQuestService/join_network.sh
cp -f GamificationVisualizationService/bin/join_network.sh GamificationVisualizationService/join_network.sh
cp -f GamificationGamifierService/bin/join_network.sh GamificationGamifierService/join_network.sh
cp -f GamificationApplicationService/bin/start_network.sh GamificationApplicationService/start_network.sh

echo "Adjusting shell configuration"
sed -i 's/'$OLD_NODE_IP'/'$NODE_IP'/g' GamificationAchievementService/join_network.sh
sed -i 's/'$OLD_NODE_IP'/'$NODE_IP'/g' GamificationActionService/join_network.sh
sed -i 's/'$OLD_NODE_IP'/'$NODE_IP'/g' GamificationBadgeService/join_network.sh
sed -i 's/'$OLD_NODE_IP'/'$NODE_IP'/g' GamificationLevelService/join_network.sh
sed -i 's/'$OLD_NODE_IP'/'$NODE_IP'/g' GamificationPointService/join_network.sh
sed -i 's/'$OLD_NODE_IP'/'$NODE_IP'/g' GamificationQuestService/join_network.sh
sed -i 's/'$OLD_NODE_IP'/'$NODE_IP'/g' GamificationVisualizationService/join_network.sh
sed -i 's/'$OLD_NODE_IP'/'$NODE_IP'/g' GamificationGamifierService/join_network.sh

echo "Make it executable"
chmod +x GamificationAchievementService/join_network.sh
chmod +x GamificationActionService/join_network.sh
chmod +x GamificationBadgeService/join_network.sh
chmod +x GamificationLevelService/join_network.sh
chmod +x GamificationPointService/join_network.sh
chmod +x GamificationQuestService/join_network.sh
chmod +x GamificationVisualizationService/join_network.sh
chmod +x GamificationGamifierService/join_network.sh

echo "Done !!"
