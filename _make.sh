cd GamificationApplicationService && \
if [ "$1" == "build" ]; then
	echo "[Build only]"
  pwd
  ant build_all_projects_only
elif [ "$1" == "clean" ]; then
  echo "[Clean only]"
  ant clean_all_projects
elif [ "$1" == "all" ]; then
  echo "[Build all]"
	ant get_deps_all_projects
  ant build_all_projects
else
  echo "Argument is not recognized!, try \"build\",\"clean\", or \"all\" "
fi
