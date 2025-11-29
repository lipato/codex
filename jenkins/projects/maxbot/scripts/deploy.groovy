// Jenkins scripted pipeline for Maxbot deployment
properties([
  parameters([
    string(name: 'GIT_REPO', defaultValue: '', description: 'Git repository URL'),
    string(name: 'GIT_BRANCH', defaultValue: 'main', description: 'Branch to deploy'),
    string(name: 'TARGET_HOST', defaultValue: '', description: 'Target host for deployment'),
    string(name: 'SSH_CREDENTIALS_ID', defaultValue: '', description: 'Jenkins SSH credentials ID')
  ])
])

node {
  stage('Checkout') {
    checkout([
      $class: 'GitSCM',
      branches: [[name: params.GIT_BRANCH ?: 'main']],
      userRemoteConfigs: [[url: params.GIT_REPO]]
    ])
  }

  stage('Deploy') {
    sshagent([params.SSH_CREDENTIALS_ID]) {
      def remoteScript = """#!/bin/bash
set -euo pipefail

sudo systemctl stop maxbot

if [ -d /opt/data/maxbot ]; then
  sudo cp -a /opt/data/maxbot /opt/data/maxbot.bak_$(date +%Y%m%d%H%M%S)
fi

sudo rm -rf /opt/data/maxbot
sudo mkdir -p /opt/data/maxbot
sudo chown -R jenkins:jenkins /opt/data/maxbot

sudo -u jenkins git clone -b ${params.GIT_BRANCH} ${params.GIT_REPO} /opt/data/maxbot
sudo -u jenkins bash -lc 'cd /opt/data/maxbot && git pull --ff-only origin ${params.GIT_BRANCH}'

sudo chown -R maxbot:maxbot /opt/data/maxbot
sudo -u maxbot bash -lc 'cd /opt/data/maxbot && npm install'
sudo -u maxbot bash -lc 'cd /opt/data/maxbot && npm run build'

sudo systemctl start maxbot
sudo systemctl status maxbot --no-pager

curl -fsS http://localhost:3000/health | grep '\"status\":\"ok\"'
"""

      sh """
        set -euo pipefail
        ssh -o StrictHostKeyChecking=no ${params.TARGET_HOST} <<'REMOTE'
${remoteScript}
REMOTE
      """
    }
  }
}
