pipeline {
    agent any

    stages {
        stage('Build') {
            steps {
                echo 'Building..'
                sh './gradlew clean build -x test'
                archiveArtifacts artifacts: 'build/libs/*.jar' fingerprint: true
            }
        }
        stage('Test') {
            steps {
                echo 'Testing..'
                sh './gradlew test'
                junit 'test-results/test/*.xml'
            }
        }
        stage('Deploy') {
            steps {
                echo 'Deploying....'
                echo 'Nothing to dpeloy yet'
            }
        }
    }
}