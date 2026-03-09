#!/usr/bin/env bash

# This script is meant to be sourced.
# Do not enable strict shell options here because it would affect the caller shell.

is_sourced=0
if [ -n "${BASH_SOURCE:-}" ] && [ "${BASH_SOURCE[0]}" != "$0" ]; then
  is_sourced=1
fi

if [ "$is_sourced" -ne 1 ]; then
  echo "Run this script with: source ./scripts/set-local-env.sh"
  exit 1
fi

# Optional overrides via environment variables before sourcing this script.
PORT="${PORT:-8180}"
SPRING_PROFILES_ACTIVE="${SPRING_PROFILES_ACTIVE:-local}"

DB_HOST="${DB_HOST:-127.0.0.1}"
DB_PORT="${DB_PORT:-5432}"
DB_NAME="${DB_NAME:-starter_db}"
DB_USERNAME="${DB_USERNAME:-starter_user}"
DB_PASSWORD="${DB_PASSWORD:-change_me_db_password}"

JWT_SECRET="${JWT_SECRET:-change_me_very_long_random_secret_at_least_32_bytes_1234567890}"
CORS_ALLOWED_ORIGINS="${CORS_ALLOWED_ORIGINS:-http://127.0.0.1:4200}"
APP_EMAIL_FROM="${APP_EMAIL_FROM:-no-reply@starter.com}"
FRONTEND_BASE_URL="${FRONTEND_BASE_URL:-http://127.0.0.1:4200}"

MAIL_HOST="${MAIL_HOST:-127.0.0.1}"
MAIL_PORT="${MAIL_PORT:-1025}"
MAIL_USERNAME="${MAIL_USERNAME:-}"
MAIL_PASSWORD="${MAIL_PASSWORD:-}"

export PORT
export SPRING_PROFILES_ACTIVE
export DB_URL="jdbc:postgresql://${DB_HOST}:${DB_PORT}/${DB_NAME}"
export DB_USERNAME
export DB_PASSWORD
export JWT_SECRET
export CORS_ALLOWED_ORIGINS
export APP_EMAIL_FROM
export FRONTEND_BASE_URL
export MAIL_HOST
export MAIL_PORT
export MAIL_USERNAME
export MAIL_PASSWORD

echo "Local environment variables are set for this shell session."
echo "PORT=${PORT}"
echo "SPRING_PROFILES_ACTIVE=${SPRING_PROFILES_ACTIVE}"
echo "DB_URL=${DB_URL}"
echo "DB_USERNAME=${DB_USERNAME}"
echo "MAIL_HOST=${MAIL_HOST}"
echo "MAIL_PORT=${MAIL_PORT}"
echo
echo "Next command:"
echo "mvn -pl app-api spring-boot:run"
