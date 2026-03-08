#!/usr/bin/env bash
set -euo pipefail

BASE_URL="${BASE_URL:-http://localhost:${APP_PORT:-8180}}"
SUFFIX="$(date +%s)"
USERNAME="smoke_${SUFFIX}"
EMAIL="${USERNAME}@test.local"
PASSWORD="SmokePass123!"

echo "Smoke tests against ${BASE_URL}"

wait_for_api() {
  local max_attempts=60
  local attempt=1

  while [ "${attempt}" -le "${max_attempts}" ]; do
    local code
    code="$(curl -s -o /dev/null -w "%{http_code}" -X POST "${BASE_URL}/api/auth/refresh" || true)"

    if [ "${code}" = "401" ] || [ "${code}" = "200" ]; then
      echo "API is ready (received HTTP ${code} on POST /api/auth/refresh)."
      return 0
    fi

    echo "Waiting for API... attempt ${attempt}/${max_attempts} (HTTP ${code})"
    attempt=$((attempt + 1))
    sleep 2
  done

  echo "API did not become ready in time."
  return 1
}

assert_http_code() {
  local expected="$1"
  local actual="$2"
  local context="$3"

  if [ "${actual}" != "${expected}" ]; then
    echo "Unexpected HTTP status for ${context}: expected ${expected}, got ${actual}"
    return 1
  fi
}

wait_for_api

register_payload="$(cat <<JSON
{"name":"Smoke Test","username":"${USERNAME}","email":"${EMAIL}","password":"${PASSWORD}","phone":"+212600000000","gender":"MALE","address":"Smoke Street","role":"ROLE_CLIENT"}
JSON
)"

register_code="$(curl -s -o /tmp/register_response.json -w "%{http_code}" \
  -X POST "${BASE_URL}/api/auth/register" \
  -H "Content-Type: application/json" \
  -d "${register_payload}")"

assert_http_code "200" "${register_code}" "POST /api/auth/register"
echo "Register OK"

login_payload="{\"username\":\"${USERNAME}\",\"password\":\"${PASSWORD}\"}"
login_code="$(curl -s -o /tmp/login_response.json -w "%{http_code}" \
  -X POST "${BASE_URL}/api/auth/login" \
  -H "Content-Type: application/json" \
  -d "${login_payload}")"

assert_http_code "200" "${login_code}" "POST /api/auth/login"

token="$(sed -n 's/.*"token":"\([^"]*\)".*/\1/p' /tmp/login_response.json)"
if [ -z "${token}" ]; then
  echo "No access token found in login response."
  cat /tmp/login_response.json
  exit 1
fi
echo "Login OK"

me_code="$(curl -s -o /tmp/me_response.json -w "%{http_code}" \
  -X GET "${BASE_URL}/api/users/me" \
  -H "Authorization: Bearer ${token}")"

assert_http_code "200" "${me_code}" "GET /api/users/me"
grep -q "\"username\":\"${USERNAME}\"" /tmp/me_response.json
echo "Get current user OK"

admin_check_code="$(curl -s -o /tmp/admin_check_response.json -w "%{http_code}" \
  -X GET "${BASE_URL}/api/users")"

assert_http_code "403" "${admin_check_code}" "GET /api/users without admin"
echo "Admin protection OK"

reset_code="$(curl -s -o /tmp/password_reset_request_response.json -w "%{http_code}" \
  -X POST "${BASE_URL}/api/auth/password-reset/request" \
  -H "Content-Type: application/json" \
  -d "{\"email\":\"${EMAIL}\"}")"

assert_http_code "200" "${reset_code}" "POST /api/auth/password-reset/request"
echo "Password reset request OK"

echo "Smoke tests passed."
