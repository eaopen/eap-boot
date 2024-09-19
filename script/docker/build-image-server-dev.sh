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
    git checkout dev
    git checkout .
    git pull
    mvn clean install -Dgpg.skip=true -Dmaven.javadoc.skip=true -DskipTests=true
  else
    echo "$project_dir not exist."
  fi
}

# Check and update eap-common
update_git_project "${EAP_PROJ_DIR}/eap-common"

# Check and update eap-bpm
update_git_project "${EAP_PROJ_DIR}/eap-bpm"

# Check and update eap-boot
update_git_project "${EAP_PROJ_DIR}/eap-boot"

cd ${EAP_PROJ_DIR}/eap-common
mvn clean
mvn install -Dgpg.skip=true -Dmaven.javadoc.skip=true -DskipTests=true

cd ${EAP_PROJ_DIR}/eap-bpm
mvn clean
mvn install -Dgpg.skip=true -Dmaven.javadoc.skip=true -DskipTests=true

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
version=1.0.0
#version=$(date +%F)
# docker
docker build ../eap-server/. --tag ${artifactId}:${version}
#docker build -t ${artifactId}:${version} -f ../eap-server/Dockerfile .
#docker tag  ${artifactId}:${version}  10.9.8.162:9001/${artifactId}:latest
