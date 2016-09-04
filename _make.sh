cd GamificationApplicationService && \
if [ "$1" == "build" ]; then
	echo "[Build only]"
  pwd
  ant build_all_projects_only
  # ant -Dbasedir=$(pwd)/GamificationBadgeService -f GamificationBadgeService/build.xml build_only
  # ant -Dbasedir=$(pwd)/GamificationPointService -f GamificationPointService/build.xml build_only
  # ant -Dbasedir=$(pwd)/GamificationApplicationService -f GamificationApplicationService/build.xml build_only
  # ant -Dbasedir=$(pwd)/GamificationAchievementService -f GamificationAchievementService/build.xml build_only
  # ant -Dbasedir=$(pwd)/GamificationActionService -f GamificationActionService/build.xml build_only
  # ant -Dbasedir=$(pwd)/GamificationLevelService -f GamificationLevelService/build.xml build_only
  # ant -Dbasedir=$(pwd)/GamificationQuestService -f GamificationQuestService/build.xml build_only
  # ant -Dbasedir=$(pwd)/GamificationVisualizationService -f GamificationVisualizationService/build.xml build_only
elif [ "$1" == "clean" ]; then
  echo "[Clean only]"
  ant clean_all_projects
  # ant -Dbasedir=$(pwd)/GamificationApplicationService -f GamificationApplicationService/build.xml clean_all_projects
  # ant -Dbasedir=$(pwd)/GamificationBadgeService -f GamificationBadgeService/build.xml clean_all
  # ant -Dbasedir=$(pwd)/GamificationPointService -f GamificationPointService/build.xml clean_all
  # ant -Dbasedir=$(pwd)/GamificationApplicationService -f GamificationApplicationService/build.xml clean_all
  # ant -Dbasedir=$(pwd)/GamificationAchievementService -f GamificationAchievementService/build.xml clean_all
  # ant -Dbasedir=$(pwd)/GamificationActionService -f GamificationActionService/build.xml clean_all
  # ant -Dbasedir=$(pwd)/GamificationLevelService -f GamificationLevelService/build.xml clean_all
  # ant -Dbasedir=$(pwd)/GamificationQuestService -f GamificationQuestService/build.xml clean_all
  # ant -Dbasedir=$(pwd)/GamificationVisualizationService -f GamificationVisualizationService/build.xml clean_all
elif [ "$1" == "all" ]; then
  echo "[Build all]"
  ant build_all_projects
  #ant -Dbasedir=$(pwd)/GamificationApplicationService -f GamificationApplicationService/build.xml build_all_projects
  #ant -Dbasedir=$(pwd)/GamificationApplicationService -f GamificationApplicationService/build.xml test_all_projects
  # ant -Dbasedir=$(pwd)/GamificationBadgeService -f GamificationBadgeService/build.xml all
  # ant -Dbasedir=$(pwd)/GamificationPointService -f GamificationPointService/build.xml all
  # ant -Dbasedir=$(pwd)/GamificationApplicationService -f GamificationApplicationService/build.xml all
  # ant -Dbasedir=$(pwd)/GamificationAchievementService -f GamificationAchievementService/build.xml all
  # ant -Dbasedir=$(pwd)/GamificationActionService -f GamificationActionService/build.xml all
  # ant -Dbasedir=$(pwd)/GamificationLevelService -f GamificationLevelService/build.xml all
  # ant -Dbasedir=$(pwd)/GamificationQuestService -f GamificationQuestService/build.xml all
  # ant -Dbasedir=$(pwd)/GamificationVisualizationService -f GamificationVisualizationService/build.xml all
else
  echo "Argument is not recognized!, try \"build\",\"clean\", or \"all\" "
fi
