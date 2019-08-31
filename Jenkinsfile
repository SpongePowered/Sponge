pipeline {
    agent any

    stages {
        stage('Prepare') {
            steps {
                sh './gradlew clean setupDecompWorkspace --refresh-dependencies'
            }
        }

        stage('Build') {
            environment {
                MAVEN = credentials('maven')
            }
            steps {
                sh './gradlew -PspongeUsername=$MAVEN_USR -PspongePassword=$MAVEN_PSW ' +
                        'clean build :publish --refresh-dependencies'
            }
        }
    }
}
