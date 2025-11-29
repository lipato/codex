pipeline {
    agent any

    parameters {
        string(name: 'GIT_REPO', defaultValue: 'git@github.com:your-org/spsmaxbot.git', description: 'Git repository URL for maxbot')
        string(name: 'GIT_BRANCH', defaultValue: 'main', description: 'Branch to deploy')
        string(name: 'TARGET_HOST', defaultValue: 'maxbot.example.com', description: 'Target host for deployment')
        credentials(name: 'SSH_CREDENTIALS_ID', defaultValue: 'jenkins-ssh-key', description: 'SSH key for Git/host access')
        string(name: 'SSH_USER', defaultValue: 'jenkins', description: 'SSH user used to connect to the target host')
    }

    stages {
        stage('Load deploy script') {
            steps {
                script {
                    deploy = load('jenkins/projects/maxbot/scripts/deploy.groovy')
                }
            }
        }

        stage('Deploy maxbot') {
            steps {
                script {
                    deploy.deploy(
                        gitRepo: params.GIT_REPO,
                        gitBranch: params.GIT_BRANCH,
                        targetHost: params.TARGET_HOST,
                        sshCredentialsId: params.SSH_CREDENTIALS_ID,
                        sshUser: params.SSH_USER
                    )
                }
            }
        }
    }
}
