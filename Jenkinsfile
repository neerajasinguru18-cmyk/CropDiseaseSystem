
pipeline {
    agent any

    stages {
        stage('Checkout') {
            steps {
                echo 'Downloading Crop Disease System from GitHub'
                checkout scm
            }
        }

        stage('Build') {
            steps {
                echo 'Building Spring Boot Application'
                bat 'cd backend\\backend && mvn clean package -DskipTests'
            }
        }

        stage('Test') {
            steps {
                echo 'Running Application Tests'
                bat 'cd backend\\backend && mvn test'
            }
        }
    }

    post {
        success {
            echo 'Crop Disease System Build Successful!'
        }
        failure {
            echo 'Build Failed! Check Console Output.'
        }
    }
}
