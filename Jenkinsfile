pipeline {
  agent none
  
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
      steps {  
          echo "Analyse sonar"
          sh './mvnw clean verify sonar:sonar' 
      }
    }
  }
}

stage('Déploiement artefact') {
  agent any
  when {
    not {
      branch 'master'
    }
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
  }
}
