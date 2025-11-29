pipeline {
    agent any

    parameters {
        choice(name: 'TARGET_ENV', choices: ['dev', 'stage', 'prod'], description: 'Environment to deploy to')
        string(name: 'APP_VERSION', defaultValue: 'latest', trim: true, description: 'Application version or image tag to deploy')
        string(name: 'TARGET_HOST', defaultValue: 'deploy.example.com', trim: true, description: 'SSH host used for deployment commands')
        string(name: 'TARGET_USER', defaultValue: 'deployer', trim: true, description: 'User for SSH deployment')
        string(name: 'SSH_CREDENTIALS_ID', defaultValue: 'maxbot-ssh', trim: true, description: 'Jenkins credentials ID for sshagent')
    }

    options {
        timestamps()
        ansiColor('xterm')
        buildDiscarder(logRotator(numToKeepStr: '20'))
    }

    environment {
        // Expose credentials ID as an environment variable if the shared script prefers it.
        SSH_CREDENTIALS_ID = "${params.SSH_CREDENTIALS_ID}"
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Deploy') {
            steps {
                script {
                    def deploy = load("${env.WORKSPACE}/jenkins/pipelines/deploy.groovy")

                    sshagent(credentials: [params.SSH_CREDENTIALS_ID]) {
                        deploy.runDeploy(
                            targetEnv: params.TARGET_ENV,
                            appVersion: params.APP_VERSION,
                            targetHost: params.TARGET_HOST,
                            targetUser: params.TARGET_USER
                        )
                    }
                }
            }
        }
    }

    post {
        always {
            archiveArtifacts artifacts: 'deploy.log', allowEmptyArchive: true, fingerprint: true
        }
    }
}
