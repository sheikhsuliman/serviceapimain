#!/usr/bin/env bash
#
# This script will fail CI build, if there is a conflict with FlyWay version files
# e.g. current branch contains FlyWay script with the same number but different name than master.
#

GIT_OAUTH_TOKEN=${GIT_OAUTH_TOKEN:-none}

function queryMasterFlyWayScripts() {
    curl -s -H "Authorization: token ${GIT_OAUTH_TOKEN}" https://api.github.com/repos/siryus-ag/swisscon-service-api/contents/src/main/resources/db/migrations/common | \
      jq -r ".[]|[.path,.sha] | @tsv" | grep "/V" | cut -dV -f 2- | sort -n
}

function queryCurrentBranchFlyWayScripts() {
  (
    cd ./src/main/resources/db/migrations/common/
    for f in $( ls -1 V* )
    do
      echo -e "$f\t$( git hash-object $f )"
    done
  ) | cut -c 2- | sort -n
}

queryMasterFlyWayScripts > /tmp/master-flyway-content
queryCurrentBranchFlyWayScripts > /tmp/branch-flyway-content

diff /tmp/master-flyway-content /tmp/branch-flyway-content > /tmp/diff-flyway-content && true

if grep -q "^<"  /tmp/diff-flyway-content
then
  echo "Fail"
  cat /tmp/diff-flyway-content
  exit 1
else
  echo "a OK!"
  exit 0
fi