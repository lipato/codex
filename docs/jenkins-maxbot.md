# Jenkins плейбук для maxbot

Этот документ описывает требования к окружению и порядок деплоя приложения **spsmaxbot** при помощи Jenkins.

## Требования к целевой VM
- Установлены: `git`, `node`, `npm`, `systemd`.
- Сервис `maxbot` зарегистрирован в systemd.
- Пользователь Jenkins (SSH_USER) может подключаться по SSH и выполнять `sudo systemctl`, `sudo mv`, `sudo chown`, `sudo mkdir`.
- Доступ Jenkins к Git-репозиторию (SSH или HTTPS). Один credentials ID используется и для Git, и для SSH.

## Параметры pipeline
- `GIT_REPO` — URL репозитория `spsmaxbot`.
- `GIT_BRANCH` — ветка для деплоя (по умолчанию `main`).
- `TARGET_HOST` — адрес сервера с `maxbot`.
- `SSH_CREDENTIALS_ID` — Jenkins credentials (SSH key).
- `SSH_USER` — SSH пользователь (по умолчанию `jenkins`).

## Последовательность деплоя
1. Checkout указанной ветки (локально на Jenkins agent для валидации доступа).
2. SSH на `TARGET_HOST` под `SSH_USER`.
3. Остановка сервиса: `systemctl stop maxbot` (если активен).
4. Бэкап текущей версии: `/opt/data/maxbot` → `/opt/data/maxbot_bak_<timestamp>`.
5. Создание каталога `/opt/data/maxbot` и выдача прав `SSH_USER:SSH_USER` для дальнейших git/npm операций.
6. `git init` + `git fetch` указанной ветки и `git reset --hard origin/<branch>`.
7. **npm install** и **npm run build** выполняются пользователем Jenkins (SSH_USER).
8. После сборки выполняется `sudo chown -R maxbot:maxbot /opt/data/maxbot`.
9. Запуск: `systemctl start maxbot` и проверка `systemctl status maxbot`.
10. Healthcheck: `curl --fail --silent http://localhost:3000/health` и проверка `"status":"ok"`.

## Откат
- Сохранённая папка `/opt/data/maxbot_bak_<timestamp>` содержит предыдущий билд.
- Для отката: остановить сервис, удалить текущий `/opt/data/maxbot`, вернуть бэкап и запустить сервис.

## Настройка Jenkins job
- Включить параметризацию (см. `Jenkinsfile`).
- Добавить `sshagent` с `SSH_CREDENTIALS_ID`.
- Позволить шагу shell выполнять многострочный SSH-сценарий (set -euo pipefail).
- Логи healthcheck и `systemctl status` попадут в консоль вывода билда.
