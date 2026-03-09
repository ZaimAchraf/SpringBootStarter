param(
    [string]$Port = "8180",
    [string]$SpringProfile = "local",
    [string]$DbHost = "127.0.0.1",
    [string]$DbPort = "5432",
    [string]$DbName = "starter_db",
    [string]$DbUsername = "starter_user",
    [string]$DbPassword = "change_me_db_password",
    [string]$JwtSecret = "change_me_very_long_random_secret_at_least_32_bytes_1234567890",
    [string]$CorsAllowedOrigins = "http://127.0.0.1:4200",
    [string]$AppEmailFrom = "no-reply@starter.com",
    [string]$FrontendBaseUrl = "http://127.0.0.1:4200",
    [string]$MailHost = "127.0.0.1",
    [string]$MailPort = "1025",
    [string]$MailUsername = "",
    [string]$MailPassword = ""
)

$env:PORT = $Port
$env:SPRING_PROFILES_ACTIVE = $SpringProfile

$env:DB_URL = "jdbc:postgresql://${DbHost}:${DbPort}/${DbName}"
$env:DB_USERNAME = $DbUsername
$env:DB_PASSWORD = $DbPassword

$env:JWT_SECRET = $JwtSecret
$env:CORS_ALLOWED_ORIGINS = $CorsAllowedOrigins
$env:APP_EMAIL_FROM = $AppEmailFrom
$env:FRONTEND_BASE_URL = $FrontendBaseUrl

$env:MAIL_HOST = $MailHost
$env:MAIL_PORT = $MailPort
$env:MAIL_USERNAME = $MailUsername
$env:MAIL_PASSWORD = $MailPassword

Write-Host "Local environment variables are set for this shell session." -ForegroundColor Green
Write-Host "PORT=$($env:PORT)"
Write-Host "SPRING_PROFILES_ACTIVE=$($env:SPRING_PROFILES_ACTIVE)"
Write-Host "DB_URL=$($env:DB_URL)"
Write-Host "DB_USERNAME=$($env:DB_USERNAME)"
Write-Host "MAIL_HOST=$($env:MAIL_HOST)"
Write-Host "MAIL_PORT=$($env:MAIL_PORT)"
Write-Host ""
Write-Host "Next command:" -ForegroundColor Yellow
Write-Host "mvn -pl app-api spring-boot:run"
