#!/usr/bin/env groovy

def deploy(Map config = [:]) {
    def gitRepo = config.get('gitRepo', env.GIT_REPO ?: '')
    def gitBranch = config.get('gitBranch', env.GIT_BRANCH ?: 'main')
    def targetHost = config.get('targetHost', env.TARGET_HOST ?: '')
    def sshCredentialsId = config.get('sshCredentialsId', env.SSH_CREDENTIALS_ID ?: '')
    def sshUser = config.get('sshUser', env.SSH_USER ?: 'jenkins')

    if (!gitRepo?.trim()) {
        error 'gitRepo is required'
    }
    if (!targetHost?.trim()) {
        error 'targetHost is required'
    }
    if (!sshCredentialsId?.trim()) {
        error 'sshCredentialsId is required'
    }

    stage('Checkout (pipeline context)') {
        checkout([$class: 'GitSCM', branches: [[name: gitBranch]], userRemoteConfigs: [[url: gitRepo, credentialsId: sshCredentialsId]]])
    }

    stage('Deploy on remote host') {
        sshagent(credentials: [sshCredentialsId]) {
            sh """
                set -euo pipefail
                ssh -o BatchMode=yes -o StrictHostKeyChecking=no ${sshUser}@${targetHost} <<'REMOTE_DEPLOY'
                set -euo pipefail

                SERVICE_NAME=maxbot
                APP_DIR=/opt/data/maxbot
                BACKUP_DIR=/opt/data/maxbot_bak_$(date +%Y%m%d%H%M%S)
                HEALTH_URL=http://localhost:3000/health

                echo "[stop] Stopping service ${SERVICE_NAME}"
                if systemctl is-active --quiet ${SERVICE_NAME}; then
                    sudo systemctl stop ${SERVICE_NAME}
                else
                    echo "Service ${SERVICE_NAME} not running or already stopped"
                fi

                if [ -d "${APP_DIR}" ]; then
                    echo "[backup] Moving current app to ${BACKUP_DIR}"
                    sudo mv ${APP_DIR} ${BACKUP_DIR}
                fi

                echo "[prepare] Creating clean app directory"
                sudo mkdir -p ${APP_DIR}
                sudo chown ${sshUser}:${sshUser} ${APP_DIR}

                echo "[git] Cloning ${gitRepo} (${gitBranch})"
                cd ${APP_DIR}
                git init
                git remote add origin ${gitRepo}
                git fetch origin ${gitBranch}
                if git show-ref --verify --quiet refs/heads/${gitBranch}; then
                    git checkout ${gitBranch}
                else
                    git checkout -b ${gitBranch}
                fi
                git reset --hard origin/${gitBranch}

                echo "[npm] Installing dependencies as ${sshUser}"
                npm install
                echo "[npm] Building application"
                npm run build

                echo "[permissions] Setting owner to maxbot:maxbot"
                sudo chown -R maxbot:maxbot ${APP_DIR}

                echo "[start] Starting service ${SERVICE_NAME}"
                sudo systemctl start ${SERVICE_NAME}
                sudo systemctl status ${SERVICE_NAME} --no-pager

                echo "[health] Checking ${HEALTH_URL}"
                curl --fail --silent ${HEALTH_URL} | grep '"status":"ok"'

                echo "Deployment completed successfully"
REMOTE_DEPLOY
            """
        }
    }
}

return this
