#!/bin/sh

TARGET_DIRECTORY=../delivery-service

git checkout $1

rm -rf ${TARGET_DIRECTORY}/src 
rm ${TARGET_DIRECTORY}/README.md
rm ${TARGET_DIRECTORY}/pom.xml
rm ${TARGET_DIRECTORY}/Jenkinsfile
rm -rf ${TARGET_DIRECTORY}/ansible
rm -rf ${TARGET_DIRECTORY}/tools

cp -r ./src ${TARGET_DIRECTORY}/
cp ./README.md ${TARGET_DIRECTORY}/
cp ./pom.xml ${TARGET_DIRECTORY}/
cp ./Jenkinsfile ${TARGET_DIRECTORY}/
cp ./.gitignore ${TARGET_DIRECTORY}/
cp ./mvn* ${TARGET_DIRECTORY}/
cp -r ./.mvn ${TARGET_DIRECTORY}/
cp ./settings.xml ${TARGET_DIRECTORY}/
cp -r ./ansible ${TARGET_DIRECTORY}/
cp -r apache-jmeter-5.2.1 ${TARGET_DIRECTORY}/
cp *.jmx ${TARGET_DIRECTORY}/
cp -r ./tools ${TARGET_DIRECTORY}/

git checkout master
