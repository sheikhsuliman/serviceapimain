#!/usr/bin/env bash
set -Eeuo pipefail # https://vaneyckt.io/posts/safer_bash_scripts_with_set_euxo_pipefail/

CIRCLE_PR_NUMBER=$( echo "${CIRCLE_PULL_REQUEST:-}" | cut -d/ -f 7 )
CIRCLE_BUILD_NUM=${CIRCLE_BUILD_NUM:-local}

COMMIT_TAG="$(git tag -l --points-at HEAD 2>/dev/null || echo '')"
BRANCH_TAG="$(git describe --abbrev=0 --tags 2>/dev/null || echo '' )"
# shellcheck disable=SC2046
REPO_TAG="$(git describe --tags $(git rev-list --tags --max-count=1 2>/dev/null) 2>/dev/null || echo '')"
CURRENT_BRANCH="$(git rev-parse --abbrev-ref HEAD 2>/dev/null || echo '')"

function validSemVer() {
  [[ "$1" =~ ^[0-9]+\.[0-9]+\.[0-9]$  ]]
}

function bumpPatch() {
  IFS='.' read -r -a SEM_VER <<< "$1"

  PATCH="${SEM_VER[2]}"
  echo "${SEM_VER[0]}.${SEM_VER[1]}.$(( PATCH + 1))"
}

function bumpMinor() {
  IFS='.' read -r -a SEM_VER <<< "$1"

  MINOR="${SEM_VER[1]}"
  echo "${SEM_VER[0]}.$(( MINOR + 1 )).0"
}

function bumpMajor() {
  IFS='.' read -r -a SEM_VER <<< "$1"

  MAJOR="${SEM_VER[0]}"
  echo "$((MAJOR + 1)).1.0"
}

function lessThan() {
  IFS='.' read -r -a SEM_VER_1 <<< "$1"
  IFS='.' read -r -a SEM_VER_2 <<< "$2"

  if ((  "${SEM_VER_1[0]}" > "${SEM_VER_2[0]}" )) ; then
    false
    elif ((  "${SEM_VER_1[1]}" > "${SEM_VER_2[1]}" )); then
      false
    elif  (( "${SEM_VER_1[2]}" > "${SEM_VER_2[2]}" )); then
      false
    else
      true
  fi
}

function isPR() {
  [ -n "$CIRCLE_PR_NUMBER" ]
}

function isMaster() {
   [[ "$CURRENT_BRANCH" == "master" ]]
}
#
# Strip 'v' from tag(s)
#
COMMIT_TAG="${COMMIT_TAG//v/}"
BRANCH_TAG="${BRANCH_TAG//v/}"
REPO_TAG="${REPO_TAG//v/}"

#
# Latest commit was tagged as release... return it as-is
#
if validSemVer "$COMMIT_TAG"
then
  echo "$COMMIT_TAG";
  exit 0;
fi

if [[ "$BRANCH_TAG" == "$REPO_TAG" ]] ; then
  BASE_VERSION=$( bumpMinor "$BRANCH_TAG" )
elif lessThan "$BRANCH_TAG" "$REPO_TAG" ; then
  BASE_VERSION=$( bumpPatch "$BRANCH_TAG" )
else
  BASE_VERSION=$( bumpMinor "$BRANCH_TAG" )
fi
export BASE_VERSION

if isPR ; then
  echo "${BASE_VERSION}-PR${CIRCLE_PR_NUMBER}-${CIRCLE_BUILD_NUM}"
elif isMaster ; then
  echo "${BASE_VERSION}-beta-${CIRCLE_BUILD_NUM}"
else
  echo "${BASE_VERSION}-build-${CIRCLE_BUILD_NUM}"
fi