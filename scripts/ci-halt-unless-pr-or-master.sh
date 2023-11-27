#!/usr/bin/env bash
set -Eeuo pipefail # https://vaneyckt.io/posts/safer_bash_scripts_with_set_euxo_pipefail/

#
# This script will stop CI build which was not result of:
# - merge to master
# - non Draft PR
#

CIRCLE_PR_NUMBER=$( echo "${CIRCLE_PULL_REQUEST:-}" | cut -d/ -f 7 )
COMMIT_TAG="$(git tag -l --points-at HEAD 2>/dev/null || echo '')"
CURRENT_BRANCH="$(git rev-parse --abbrev-ref HEAD 2>/dev/null || echo '')"

GIT_OAUTH_TOKEN=${GIT_OAUTH_TOKEN:-none}

function isPR() {
  [ -n "$CIRCLE_PR_NUMBER" ]
}

function isMaster() {
   [[ "$CURRENT_BRANCH" == "master" ]]
}

function isDraftPR() {
  [[ `curl -s -H "Authorization: token ${GIT_OAUTH_TOKEN}" https://api.github.com/repos/siryus-ag/swisscon-service-api/pulls/${CIRCLE_PR_NUMBER} | grep "\"draft\":" | cut -d\   -f 4` == "true," ]]
}

function isRelease() {
  [[ "$CURRENT_BRANCH" == "HEAD" && "$COMMIT_TAG" != "" ]]
}

if isPR ; then
  if isDraftPR ; then
    echo "Draft PR"
    circleci-agent step halt
  else
    echo "Pull Requet"
    true
  fi
elif isMaster ; then
  echo "Meger to Master"
  true
elif isRelease ; then
  echo "Release tag : ${COMMIT_TAG}"
  true
else
  echo "Random commit?"
  circleci-agent step halt
fi