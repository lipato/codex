# Deployment Structure and Approach

## Repository structure for Jenkins
- `jenkins/shared/`: Shared pipeline libraries, common stages, and utility scripts reused across projects.
- `jenkins/pipelines/`: Declarative or scripted pipelines that orchestrate builds and deployments.
- `jenkins/projects/<project>/`: Project-specific assets.
  - `vars/`: Reusable Groovy steps or shared library entries scoped to the project.
  - `scripts/`: Shell, Python, or other helper scripts executed by the pipeline.
  - `config/`: Environment-specific values, templates, and overrides.
  - `README.md`: Job parameters, required credentials, and deploy notes.

## General deployment approach
1. **Plan**: Select branch/tag, target environment, and the artifact or image version to deploy.
2. **Prepare**: Ensure credentials (repository access, registry, cluster context) are available in Jenkins and referenced by pipeline parameters.
3. **Build**: Checkout the repository, build artifacts or container images, and push them to the registry with a traceable tag.
4. **Configure**: Load shared libraries, project vars, and environment configs to render manifests or Helm values. Keep overrides scoped per environment in `config/`.
5. **Deploy**: Apply manifests or Helm releases to the selected environment using the provided kubeconfig or equivalent access token. Support dry-run when possible.
6. **Verify**: Run smoke checks, health probes, and roll back on failure if necessary. Publish results to monitoring and chat notifications.
7. **Document**: Update job READMEs and config samples whenever parameters or credentials change.

## Best practices
- Keep secrets in Jenkins credentials, not in the repository. Reference by ID only.
- Default pipelines to safe operations (e.g., dry-run) and require explicit promotion to production.
- Reuse `jenkins/shared` steps for logging, notifications, and standardized error handling.
- Store environment-specific settings under `config/` and avoid branching logic in pipeline code.
- Tag images and releases with immutable versions to ensure repeatable deployments.
