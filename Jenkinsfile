pipeline {
  agent none
  tools {
    jdk 'JDK11'
  }
  
  stages {
    stage(' Pré-build tests ') {
      agent any
      steps {
        script {
          result = sh (script: "git log -1 | grep '\\[ci skip\\]'", returnStatus: true)
          if ( result == 0 ) {
            echo "Skipping Build due to commit msg [ci skip]..."
            currentBuild.result = 'ABORTED'
            error('Stopping early… due to commit msg [ci skip]')
          }
        }
      }
    }

    stage('Build et Tests unitaires') {
      agent any
      steps {  
	      echo 'Building and Unit tests'
        sh './mvnw -Dmaven.test.failure.ignore=true clean test' 
      }
      post { 
        always { 
            junit '**/target/surefire-reports/*.xml'
        }
        failure {
          mail body: 'It is very bad', from: 'oodoo@plb.fr', subject: 'Compilation or Unit test broken', to: 'david.thibau@gmail.com'
        }
      }
   }
stage('Parallel Stage') {
  parallel {
    stage('Intégration test') {
      agent any
       steps {  
           echo "Integration test"
           sh './mvnw clean integration-test'
        }
    }
    stage('Quality analysis') {
      agent any
      environment {
        SONAR_TOKEN = credentials('SONAR_TOKEN')
      }
      steps {  
          echo "Analyse sonar"
          sh './mvnw -Dsonar.login=${SONAR_TOKEN} clean verify sonar:sonar' 
          script {
            checkSonarQualityGate()
          }
      }
    }
  }
}

  stage('Déploiement jar SNAPSHOT vers Nexus') {
    agent any
    when {
      not { branch 'master' }
      beforeAgent true
    }

    steps {
      echo 'Deploying snapshot to Nexus.'
      sh './mvnw --settings settings.xml -DskipTests -Pprod clean deploy'
      dir('target/') {
        stash includes: '*.jar', name: 'service'
      }
      dir('src/main/docker') {
        stash includes: 'Dockerfile', name: 'dockerfile'
      }
    }
  }
  stage('Déploiement Image latest vers Nexus') {
    agent any
    when { 
        not { branch 'master' }
    } 
   steps {
     unstash 'service'
     unstash 'dockerfile'
     script {
        def mvnVersion = sh(returnStdout: true, script: './mvnw org.apache.maven.plugins:maven-help-plugin:3.1.0:evaluate -Dexpression=project.version -q -DforceStdout').trim()
        def dockerImage
        dockerImage = docker.build('delivery-service', '.')
        docker.withRegistry('http://localhost:10000', 'admin_nexus') {
          dockerImage.push "$mvnVersion"
        }
        dockerImage = docker.build('dthibau/delivery-service', '.')
        docker.withRegistry('https://registry.hub.docker.com', 'dthibau_docker') {
          dockerImage.push "$mvnVersion"
        }
     }
   }     
  }

  stage('Déploiement Kubernetes de recette') {
    agent any
    when { 
        not { branch 'master' }
    } 
    steps {

     script {
        def mvnVersion = sh(returnStdout: true, script: './mvnw org.apache.maven.plugins:maven-help-plugin:3.1.0:evaluate -Dexpression=project.version -q -DforceStdout').trim()
        // Deploy to kubernetes
        withEnv(["IMAGE=dthibau/delivery-service:$mvnVersion"]) {
        def testPresenceService = sh(returnStatus: true, script: "kubectl get service | grep delivery-service-${BRANCH_NAME}")
        echo "testPresenceService ${testPresenceService}"
        if ( testPresenceService.equals(0) ) {
          sh "kubectl delete service delivery-service-${BRANCH_NAME}"
        }
        sh ("envsubst < src/main/k8/postgres-config.yml | kubectl apply -f -")
        sh ("envsubst < src/main/k8/postgres-service.yml | kubectl apply -f -")
        sh ("envsubst < src/main/k8/delivery-service.yml | kubectl apply -f -")
        def portNumber = 80 + ("${BRANCH_NAME}".hashCode())%(65535-80)
        sh "kubectl expose deployment delivery-service-${BRANCH_NAME} --type LoadBalancer --port ${portNumber} --target-port 8080"
        
      }     
     }
   }     
  }


  stage('Test fonctionnel JMETER') {
    agent any
    when { not { branch 'master' } } 
    steps {
      script {
        def portNumber = 80 + ("${BRANCH_NAME}".hashCode())%(65535-80)
        sh "kubectl port-forward service/delivery-service-${BRANCH_NAME}  8080:${portNumber} &"
      }

      sleep 30 // Laisser le service redémarrer
      echo 'Démarrage 1 users effectuant les 4 appels REST'
      sh './apache-jmeter-5.2.1/bin/jmeter -JSERVEUR=localhost -n -t Fonctionnel.jmx -l fonc_result.jtl'
      perfReport 'fonc_result.jtl'
    }
  }

  stage('Test de charge JMETER') {
    agent any
    when { not { branch 'master' } } 
    steps {
      script {
        def portNumber = 80 + ("${BRANCH_NAME}".hashCode())%(65535-80)
        sh "kubectl port-forward service/delivery-service-${BRANCH_NAME}  8080:${portNumber} &"
      }
      echo 'Démarrage 100 users effectuant 50 fois le scénario de test'
      sh './apache-jmeter-5.2.1/bin/jmeter -JSERVEUR=localhost -n -t LoadTest.jmx -l result.jtl'
      perfReport 'result.jtl'
      // Génération de rapport 
      sh 'mkdir report'
      sh './apache-jmeter-5.2.1/bin/jmeter -Duser.language=fr -Duser.country -g result.jtl -o ./report'
      archiveArtifacts 'report/**/*.*'
      cleanWs()
    }
  }

  stage('Release') {
  agent any
    when { 
        branch 'master' 
        beforeInput true
    } 
    input {
        message 'Voulez-vous effectuer une release ?'
        ok 'Release !'
        parameters {
            string description: 'Prochain n° de version', name: 'newVersion'
        }
    }
    steps {
      echo 'Releasing app'
      sh 'pwd'
      sh 'ls -al'
      sh 'env'
      cleanWs()

    // Push on master
    script {
      sh "git clone $env.GIT_URL ."
      sh "git checkout master"
      result = sh (script: "git log -1 | grep '\\[ci skip\\]'", returnStatus: true) 
      if (result != 0) {
        def mvnVersion = sh(returnStdout: true, script: './mvnw org.apache.maven.plugins:maven-help-plugin:3.1.0:evaluate -Dexpression=project.version -q -DforceStdout').trim()
        sh ("echo $mvnVersion")
        mvnVersion = mvnVersion.replace('-SNAPSHOT','')
        sh ("echo $mvnVersion à partir de $env.BRANCH_NAME")
        sh ("./mvnw versions:set versions:commit -DnewVersion=\"$mvnVersion\" ")
     
        sh "git commit -a -m \'Release $mvnVersion [ci skip]\'"
        sh "git tag $mvnVersion"
        sh "git push origin $mvnVersion"
        sh "git push origin master:master"
        // Deploying to nexus release
        sh './mvnw --settings settings.xml -DskipTests -Pprod clean deploy' 

        dir('target/') {
          stash includes: '*.jar', name: 'service'
        }
        dir('src/main/docker') {
          stash includes: 'Dockerfile', name: 'dockerfile'
        }
        unstash 'service'
        unstash 'dockerfile'
        
        def dockerImage
        dockerImage = docker.build('delivery-service', '.')
        docker.withRegistry('http://localhost:10000', 'admin_nexus') {
            dockerImage.push "$mvnVersion"
        }
        docker.withRegistry('https://registry.hub.docker.com', 'dthibau_docker') {
            dockerImage.push "$mvnVersion"
        }
     

        echo 'Starting new version'
        // Setting new version 
        newVersion = newVersion.replace('-SNAPSHOT','') + '-SNAPSHOT'
        sh ("echo $newVersion")
        sh ("git checkout $env.BRANCH_NAME")
        sh ("./mvnw versions:set versions:commit -DnewVersion=\"$newVersion\" ")
        sh "git commit -a -m \'Starting $newVersion [ci skip]\'"
        sh "git push origin master"
      } else {
        echo "Skipping release step [ci skip]..."
      }
      
    }  
    }
  }

}
}

def checkSonarQualityGate(){
    // Get properties from report file to call SonarQube 
    def sonarReportProps = readProperties  file: 'target/sonar/report-task.txt'
    def sonarServerUrl = sonarReportProps['serverUrl']
    def ceTaskUrl = sonarReportProps['ceTaskUrl']
    def ceTask

    // Get task informations to get the status
    timeout(time: 4, unit: 'MINUTES') {
        waitUntil {
            withCredentials ([string(credentialsId: 'SONAR_TOKEN', variable : 'token')]) {
                def response = sh(script: "curl -u ${token}: ${ceTaskUrl}", returnStdout: true).trim()
                ceTask = readJSON text: response
            }

            echo ceTask.toString()
              return "SUCCESS".equals(ceTask['task']['status'])
        }
    }

    // Get project analysis informations to check the status
    def ceTaskAnalysisId = ceTask['task']['analysisId']
    def qualitygate

    withCredentials ([string(credentialsId: 'SONAR_TOKEN', variable : 'token')]) {
        def response = sh(script: "curl -u ${token}: ${sonarServerUrl}/api/qualitygates/project_status?analysisId=${ceTaskAnalysisId}", returnStdout: true).trim()
        qualitygate =  readJSON text: response
    }

    echo qualitygate.toString()
    if ("ERROR".equals(qualitygate['projectStatus']['status'])) {
        error "Quality Gate failure"
    }
}

