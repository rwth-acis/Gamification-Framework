#!/bin/bash -f

program="screen"
condition=$(which $program 2>/dev/null | grep -v "not found" | wc -l)
if [ $condition -eq 0 ] ; then
  echo "This task uses GNU screen"
  echo "$program is not installed"
  echo "run apt-get install screen"
else
  # Achievement
  echo "Stopping Achievement.."
  if screen -list | grep -q "g-achievement"; then
    screen -S g-achievement -X quit
  else
    echo "No screen for Achievement found"
  fi
  # Action
  echo "Stopping Action.."
  if screen -list | grep -q "g-action"; then
    screen -S g-action -X quit
  else
    echo "No screen for Action found"
  fi

  # Badge
  echo "Stopping Badge.."
  if screen -list | grep -q "g-badge"; then
    screen -S g-badge -X quit
  else
    echo "No screen for Badge found"
  fi

  # Gamifier
  echo "Stopping Gamifier.."
  if screen -list | grep -q "g-gamifier"; then
    screen -S g-gamifier -X quit
  else
    echo "No screen for Gamifier found"
  fi

  # Level
  echo "Stopping Level.."
  if screen -list | grep -q "g-level"; then
    screen -S g-level -X quit
  else
    echo "No screen for Level found"
  fi

  # Point
  echo "Stopping Point.."
  if screen -list | grep -q "g-point"; then
    screen -S g-point -X quit
  else
    echo "No screen for Point found"
  fi

  # Quest
  echo "Stopping Quest.."
  if screen -list | grep -q "g-quest"; then
    screen -S g-quest -X quit
  else
    echo "No screen for Quest found"
  fi

  # Visualization
  echo "Stopping Visualization.."
  if screen -list | grep -q "g-vis"; then
    screen -S g-vis -X quit
  else
    echo "No screen for Visualization found"
  fi

  # Game
  echo "Stopping Game.."
  if screen -list | grep -q "g-game"; then
    screen -S g-game -X quit
  else
    echo "No screen for Game found"
  fi

fi
