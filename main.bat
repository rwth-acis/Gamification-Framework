@echo off

IF "%~1" == "-m" (
  call :configure_function
  IF "%~2" == "build" (
    echo "[Build only]"
    call ./gradlew "build" "--exclude-task" "test"
  ) ELSE (
    IF "%~2" == "clean" (
      echo "[Clean only]"
      call ./gradlew "clean"
    ) ELSE (
      IF "%~2" == "test" (
        echo "[Test only]"
        call ./gradlew "test"
      ) ELSE (
        IF "%~2" == "all" (
          echo "[Build all]"
          call ./gradlew "build"
        ) ELSE (
          call :help
        )
      )
    )
  )
) ELSE (
  IF "%~1" == "-s" (
    call :configure_function
  ) ELSE (
    IF "%~1" == "-r" (
      echo "[Run]"
      IF "%~2" == "start_one_node" (
        call :run_one_node_function
      ) ELSE (
        IF "%~2" == "join_one_node" (
          IF "%~3" == "start" (
            echo join_node is currently NOT supported by this wrapper script!
          ) ELSE (
            IF "%~3" == "stop" (
              echo join_node is currently NOT supported by this wrapper script!
            ) ELSE (
              echo join_node is currently NOT supported by this wrapper script!
            )
          )
        ) ELSE (
          IF "%~2" == "join_node" (
            IF "%~3" == "start" (
              echo join_node is currently NOT supported by this wrapper script!
            ) ELSE (
              IF "%~3" == "stop" (
                echo join_node is currently NOT supported by this wrapper script!
              ) ELSE (
                echo join_node is currently NOT supported by this wrapper script!
              )
            )
          ) ELSE (
            call :help
          )
        )
      )
    ) ELSE (
      call :help
    )
  )
)
GOTO:EOF


:configure_function
echo configure...
echo Calling the configure option of the original main.sh file. You need to have a shell binary (sh) available in order to use this wrapper script!
sh main.sh -s
EXIT /B 0

:run_one_node_function
echo "[Start One Node]"
echo Calling the configure option of the original main.sh file. You need to have a shell binary (sh) available in order to use this wrapper script!
cd GamificationGameService && call start_network_in_a_node.bat
EXIT /B 0

:help 
echo "Argument is not recognized!, the format should be:"
echo "main.bat <Option> <Arguments>"
echo "--------------------------------------------------"
echo "NOTE: This is only a Windows wrapper around the main.sh file! You still need to have bash available (e.g., in VS Code Terminal)"
echo "--------------------------------------------------"
echo "<Option> <Arguments>:"
echo "-s : Set the configuration to all services"
echo "-m build : Build all services only"
echo "   clean : Clean all services"
echo "   all   : Build and test all services"
echo "-r start_one_node  : Run all services in a node in new network"
EXIT /B 0
