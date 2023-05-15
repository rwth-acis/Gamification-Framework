#!/bin/bash -f

program="screen"
condition=$(which $program 2>/dev/null | grep -v "not found" | wc -l)
if [ $condition -eq 0 ] ; then
  echo "This task uses GNU screen"
  echo "$program is not installed"
  echo "run apt-get install screen"
else
  # Achievement
  if screen -list | grep -q "g-achievement"; then
    screen -S g-achievement -X quit
  fi
  echo "run Achievement.."
  cd ../GamificationAchievementService && screen -dmS g-achievement sh join_network.sh
  #screen -S g-achievement -p 0 -X stuff "cd ../GamificationAchievementService"
  # screen -r g-achievement -X stuff "cd ../GamificationAchievementService"
  # screen -r g-achievement -X eval "stuff \015"

  # screen -r g-achievement -p0 -X stuff "sh join_network.sh"
  # screen -r g-achievement -p0  -X eval "stuff \015"

  # Action
  if screen -list | grep -q "g-action"; then
    screen -S g-action -X quit
  fi
  echo "run Action.."
  cd ../GamificationActionService && screen -dmS g-action sh join_network.sh

  #screen -dmS g-action
  # screen -r g-action -p0  -X stuff "cd ../GamificationActionService && sh join_network.sh"
  # screen -r g-action -p0  -X eval "stuff \015"

  # # Badge
  if screen -list | grep -q "g-badge"; then
    screen -S g-badge -X quit
  fi
  echo "run Badge.."
  cd ../GamificationBadgeService && screen -dmS g-badge sh join_network.sh

    # # Bot
  if screen -list | grep -q "g-bot"; then
    screen -S g-bot -X quit
  fi
  echo "run Bot.."
  cd ../GamificationBotWrapperService && screen -dmS g-bot sh join_network.sh

  # screen -mS g-badge
  # screen -r g-badge -p0  -X stuff "cd ../GamificationBadgeService && sh join_network.sh"
  # screen -r g-badge -p0  -X eval "stuff \015"

  # Gamifier
  if screen -list | grep -q "g-gamifier"; then
    screen -S g-gamifier -X quit
  fi
  echo "run Gamifier.."
  cd ../GamificationGamifierService && screen -dmS g-gamifier sh join_network.sh
  # screen -mS g-gamifier
  # screen -r g-gamifier -p0  -X stuff "cd ../GamificationGamifierService && sh join_network.sh"
  # screen -r g-gamifier -p0  -X eval "stuff \015"
  #
  # Level
  if screen -list | grep -q "g-level"; then
    screen -S g-level -X quit
  fi
  echo "run Level.."
  cd ../GamificationLevelService && screen -dmS g-level sh join_network.sh
  # screen -mS g-level
  # screen -r g-level -p0  -X stuff "cd ../GamificationLevelService && sh join_network.sh"
  # screen -r g-level -p0  -X eval "stuff \015"
  #
  # Point
  if screen -list | grep -q "g-point"; then
    screen -S g-point -X quit
  fi
  echo "run Point.."
  cd ../GamificationPointService && screen -dmS g-point sh join_network.sh
  # screen -mS g-point
  # screen -r g-point -p0  -X stuff "cd ../GamificationPointService && sh join_network.sh"
  # screen -r g-point -p0  -X eval "stuff \015"
  #
  # Quest
  if screen -list | grep -q "g-quest"; then
    screen -S g-quest -X quit
  fi
  echo "run Quest.."
  cd ../GamificationQuestService && screen -dmS g-quest sh join_network.sh
  # screen -mS g-quest
  # screen -r g-quest -p0  -X stuff "cd ../GamificationQuestService && sh join_network.sh"
  # screen -r g-quest -p0  -X eval "stuff \015"
  #
  # Visualization
  if screen -list | grep -q "g-vis"; then
    screen -S g-vis -X quit
  fi
  echo "run Visualization.."
  cd ../GamificationVisualizationService && screen -dmS g-vis sh join_network.sh
  # screen -mS g-vis
  # screen -r g-vis -p0  -X stuff "cd ../GamificationVisualizationService && sh join_network.sh"
  # screen -r g-vis -p0  -X eval "stuff \015"
  #
  # Game
  if screen -list | grep -q "g-game"; then
    screen -S g-game -X quit
  fi
  echo "run Game.."
  cd ../GamificationGameService && screen -dmS g-game sh join_network.sh
  # screen -mS g-game
  # screen -r g-game -p0  -X stuff "cd ../GamificationGameService && sh join_network.sh"
  # screen -r g-game -p0  -X eval "stuff \015"

  echo "This may take a long time, please check the screens and wait until all the services are running.."

fi
