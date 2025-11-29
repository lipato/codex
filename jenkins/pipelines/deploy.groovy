#!/usr/bin/env groovy

/**
 * Minimal reusable deployment step for Jenkins Pipeline examples.
 */
def runDeploy(Map config = [:]) {
    echo "Deploying ${config.appVersion ?: 'unknown'} to ${config.targetEnv ?: 'unknown'}"

    // Generate a local deployment log to demonstrate artifact collection.
    writeFile file: 'deploy.log', text: "Deployment ${config.appVersion ?: 'unknown'} -> ${config.targetEnv ?: 'unknown'} at ${new Date()}\n"

    // In a real pipeline you would run your deployment commands here (ssh, rsync, etc.).
    sh label: 'Trigger remote deploy', script: """
        set -euo pipefail
        ssh ${config.targetUser}@${config.targetHost} "echo Deploying ${config.appVersion} to ${config.targetEnv}"
    """
}

return this
