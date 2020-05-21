pipeline {
  agent none
  tools {
    jdk 'JDK11'
  }
  
  stages {
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

  stage('Déploiement SNAPSHOT Nexus') {
    agent any
    when {
      not { branch 'master' }
      beforeAgent true
    }

    steps {
      echo 'Deploying snapshot to Nexus.'
      sh './mvnw --settings settings.xml -DskipTests clean deploy'
      dir('target/') {
        stash includes: '*.jar', name: 'service'
      }
    }
  }

  stage('Deploiement Intégration Ansible') {
    agent any
    when { not { branch 'master' } } 
    steps {
      echo 'Deploiement en intégration via Ansible'
      unstash 'service'
      sh 'cp *SNAPSHOT.jar delivery-service.jar'
      dir ('ansible') {
        sh 'ansible-playbook delivery.yml -i hosts'
      }
    }
  }
  stage('Test fonctionnel JMETER') {
    agent any
    when { not { branch 'master' } } 
    steps {
      sleep 30 // Laisser le service redémarrer
      echo 'Démarrage 1 users effectuant les 4 appels REST'
      sh './apache-jmeter-5.2.1/bin/jmeter -n -t Fonctionnel.jmx -l fonc_result.jtl'
      perfReport 'fonc_result.jtl'
    }
  }

  stage('Test de charge JMETER') {
    agent any
    when { not { branch 'master' } } 
    steps {
      echo 'Démarrage 100 users effectuant 50 fois le scénario de test'
      sh './apache-jmeter-5.2.1/bin/jmeter -n -t LoadTest.jmx -l result.jtl'
      perfReport 'result.jtl'
      // Génération de rapport 
      sh 'mkdir report'
      sh './apache-jmeter-5.2.1/bin/jmeter -Duser.language=fr -Duser.country -g result.jtl -o ./report'
      archiveArtifacts 'report/**/*.*'
      cleanWs()
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

