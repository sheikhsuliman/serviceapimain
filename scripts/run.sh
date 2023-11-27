#!/usr/bin/env bash
set -Eeuxo pipefail # https://vaneyckt.io/posts/safer_bash_scripts_with_set_euxo_pipefail/

if [ -f "$HOME/.swisscon" ]
then
  echo "Loading defaults from $HOME/.swisscon"
  echo
  . "$HOME/.swisscon"
fi

touch -t $( git log -1 --date=short --pretty=format:%ci | sed -e "1,\$s/-//g" -e "1,\$s/ //g" -e "1,\$s/://" -e "1,\$s/:/./" | cut -c1-15 ) /tmp/git-last-commit

[ target/dependency-jars -ot /tmp/git-last-commit ] &&  ./mvnw -DskipTests -nsu clean package

java -Dspring.profiles.active=dev -cp src/main/resources:target/classes:target/dependency-jars/* com.siryus.swisscon.api.Application
