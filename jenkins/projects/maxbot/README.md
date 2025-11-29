# Maxbot Jenkins Jobs

This document describes the Jenkins jobs used to build and deploy Maxbot.

## Job parameters
- **GIT_BRANCH**: Git branch or tag to check out (e.g., `main`, `release/*`).
- **ENVIRONMENT**: Target environment (e.g., `dev`, `stage`, `prod`).
- **DEPLOY_VERSION**: Version or image tag to promote. Defaults to the latest artifact built from `GIT_BRANCH` if empty.
- **DRY_RUN**: When `true`, renders the pipeline steps without executing changes.
- **EXTRA_CONFIG**: Optional path in `jenkins/projects/maxbot/config/` with environment-specific overrides.

## Required credentials
- **maxbot-repo-ssh-key** (SSH key): Access to clone the private repository.
- **maxbot-container-registry** (username/password or token): Push/pull container images.
- **maxbot-kubeconfig** (secret file): Kubeconfig with access to target namespaces.
- **slack-notifications** (token): Posting build and deploy status notifications.

## Deploy steps
1. Checkout code from `GIT_BRANCH` using `maxbot-repo-ssh-key`.
2. Build and publish the container image, tagging it with `DEPLOY_VERSION` via `maxbot-container-registry`.
3. Load pipeline shared libraries from `jenkins/shared` and job-specific scripts from `jenkins/projects/maxbot/scripts`.
4. Apply environment overrides from `jenkins/projects/maxbot/config/` (and optional `EXTRA_CONFIG`).
5. Deploy manifests/Helm chart to the selected `ENVIRONMENT` cluster using `maxbot-kubeconfig`.
6. Run post-deploy checks and announce results through `slack-notifications`.
