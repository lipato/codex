# Maxbot Jenkins Deployment

Этот каталог содержит скрипты и конфигурацию для деплоя приложения **spsmaxbot** через Jenkins.

## Параметры pipeline
- **GIT_REPO** – URL Git-репозитория (ssh/https) с кодом `spsmaxbot`.
- **GIT_BRANCH** – ветка для деплоя.
- **TARGET_HOST** – хост/VM, где развёрнут сервис `maxbot`.
- **SSH_CREDENTIALS_ID** – Jenkins credentials (SSH key) для Git и SSH.
- **SSH_USER** – SSH-пользователь (по умолчанию `jenkins`).

## Поток деплоя
1. Checkout указанной ветки из репозитория.
2. Подключение к `TARGET_HOST` по SSH под пользователем `SSH_USER` (через `sshagent`).
3. Остановка сервиса `systemctl stop maxbot` (если запущен).
4. Бэкап каталога `/opt/data/maxbot` → `/opt/data/maxbot_bak_<timestamp>`.
5. Создание чистого каталога `/opt/data/maxbot` и выдача прав `SSH_USER:SSH_USER`.
6. Инициализация git, fetch указанной ветки и reset на `origin/<branch>`.
7. **npm install / npm run build** выполняются пользователем `SSH_USER` (jenkins).
8. После сборки права меняются на `maxbot:maxbot` (`chown -R`).
9. Запуск и проверка `systemctl status maxbot`.
10. Healthcheck `curl http://localhost:3000/health` и проверка `"status":"ok"`.

## Требования
- На целевой VM установлены `git`, `node`, `npm`, `systemd` сервис `maxbot`.
- Jenkins user имеет доступ по SSH и sudo на команды `systemctl`, `mv`, `chown`, `mkdir`.
- В `SSH_CREDENTIALS_ID` хранится ключ, подходящий и для Git (если используется SSH-URL), и для подключения к `TARGET_HOST`.

## Откат
- Текущий релиз сохраняется в `/opt/data/maxbot_bak_<timestamp>`. Для отката:
  - остановить сервис `maxbot`;
  - удалить текущий `/opt/data/maxbot`;
  - вернуть бэкап `mv /opt/data/maxbot_bak_<timestamp> /opt/data/maxbot`;
  - запустить сервис.
