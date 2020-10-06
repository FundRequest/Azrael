pipeline {
    agent any
    environment {
        GITHUB_CREDS = credentials('GITHUB_CRED')
        CODECOV_TOKEN = credentials('PLATFORM_CODECOV_TOKEN')
    }
    options {
        disableConcurrentBuilds()
        timeout(time: 15, unit: 'MINUTES')
    }
    stages {
        stage('Build') {
            steps {
                sh './gradlew clean build'
            }
            post {
                always {
                    junit '**/target/surefire-reports/*.xml'
                    jacoco(
                          execPattern: '**/target/*.exec',
                          classPattern: '**/target/classes',
                          sourcePattern: '**/src/main/java',
                          exclusionPattern: '**/src/test*,**/*Exception*,**/*Config*'
                    )

                }
            }
        }
        stage('Docker Build') {
          steps {
            sh 'docker build -t fundrequestio/azrael:${BRANCH_NAME} worker'
          }
        }
        stage('Docker Push') {
          steps {
            withCredentials([usernamePassword(credentialsId: 'dockerHub', passwordVariable: 'dockerHubPassword', usernameVariable: 'dockerHubUser')]) {
              sh "docker login -u ${env.dockerHubUser} -p ${env.dockerHubPassword}"
              sh "docker push fundrequestio/azrael:${BRANCH_NAME} && echo 'pushed'"
            }
          }
        }
    }
}
