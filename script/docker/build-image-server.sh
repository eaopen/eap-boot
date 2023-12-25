#!/bin/bash
DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

# DIR 改为项目目录
if [ -d "${DIR}/eap-boot" ]; then
  cd "${DIR}/eap-boot"
  DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
fi

# Change to the parent directory if in */script/docker
if [[ "$DIR" == *"script/docker"* ]]; then
  cd ./../..
  DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
fi

# 回到项目的上一级目录
EAP_PROJ_DIR=${DIR}/..

# Function to update a Git project
update_git_project() {
  local project_dir="$1"
  if [ -d "$project_dir" ]; then
    cd "$project_dir"
    git checkout .
    git pull
    mvn clean install -Dgpg.skip=true -Dmaven.javadoc.skip=true -DskipTests=true
  else
    echo "$project_dir not exist."
  fi
}

# Check and update eap-base
update_git_project "${EAP_PROJ_DIR}/eap-base"

# Check and update eap-boot
update_git_project "${EAP_PROJ_DIR}/eap-boot"

# change to top dir
cd ${DIR}

# change to eap-server dir
APPDIR=${DIR}/eap-server
cd ${APPDIR}

# 2 maven in dir
mvn clean
mvn install -Dgpg.skip=true -Dmaven.javadoc.skip=true -DskipTests=true

# 3 docker build
# docker image vars
artifactId=eap-server
version=$(date +%F)

docker build . --tag ${artifactId}:${version}
docker tag  ${artifactId}:${version}  ${artifactId}:latest

