#!/usr/bin/env groovy

pipeline {
    agent any
    tools {
        maven 'maven-latest'
        jdk 'openjdk11-zulu'
    }
    options {
        timeout(time: 1, unit: 'HOURS')
        timestamps()
    }
    parameters {
        booleanParam(defaultValue: false, description: 'Perform Maven Release?', name: 'RELEASE_OK')
        string(defaultValue: '', description: 'Release Version - Version ohne -SNAPSHOT', name: 'RELEASE_VERSION', trim: true)
        string(defaultValue: '', description: 'Development version - Version mit -SNAPSHOT, die zur Entwicklung genutzt werden soll', name: 'DEVELOPMENT_VERSION', trim: true)
    }
    stages {
        stage('Checking commit message') {
            when {
                allOf {
                    not {
                        buildingTag()
                    }
                    changelog '.*\\[maven-release-plugin\\].*'
                }
            }
            steps {
                script {
                    currentBuild.getRawBuild().getExecutor().interrupt(Result.SUCCESS)
                    sleep(1)
                }
            }
        }
        stage('Clone Devbasis') {
            steps {
                checkout([$class: 'GitSCM', branches: [[name: '*/master']], doGenerateSubmoduleConfigurations: false, extensions: [[$class: 'RelativeTargetDirectory', relativeTargetDir: 'devbasis']], submoduleCfg: [], userRemoteConfigs: [[credentialsId: 'github-ssh-key', url: 'git@github.com:WargearWorld/devbasis.git']]])
            }
        }
        stage('Release') {
            when {
                environment name: 'RELEASE_OK', value: 'true'
            }

            steps {
                withCredentials([
                        usernamePassword(credentialsId: 'wgw_nexus', usernameVariable: 'NEXUS_USERNAME', passwordVariable: 'NEXUS_TOKEN')
                ]) {
                    sshagent(['github-ssh-key']) {
                        sh '''
                        mvn --version
                        git config --global user.name "WargearWorld-GIT"
                        git config --global user.email "github@wargearworld.net"
                        git checkout ${BRANCH_NAME}
                        mvn -B release:prepare -DdevelopmentVersion=${DEVELOPMENT_VERSION} -DreleaseVersion=${RELEASE_VERSION} -s devbasis/maven/settings.xml
                        '''
                    }
                }
                script {
                    currentBuild.getRawBuild().getExecutor().interrupt(Result.SUCCESS)
                    sleep(1)
                }
            }
        }
        stage('Build') {
            when {
                changeRequest()
            }
            steps {
                withCredentials([
                        usernamePassword(credentialsId: 'wgw_nexus', usernameVariable: 'NEXUS_USERNAME', passwordVariable: 'NEXUS_TOKEN')
                ]) {
                    sh '''
                    mvn --version
                    mvn -B install -s devbasis/maven/settings.xml
                    '''
                }
                script {
                    currentBuild.getRawBuild().getExecutor().interrupt(Result.SUCCESS)
                    sleep(1)
                }
            }
        }
        stage('Build and Deploy') {
            when {
                not {
                    changeRequest()
                }
            }
            steps {
                withCredentials([
                        usernamePassword(credentialsId: 'wgw_nexus', usernameVariable: 'NEXUS_USERNAME', passwordVariable: 'NEXUS_TOKEN')
                ]) {
                    sh '''
                    mvn --version
                    mvn -B deploy -s devbasis/maven/settings.xml
                    '''
                }
            }
        }
    }
}
