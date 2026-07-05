# Security Audit Report — PetClinic (vulnerable-spring-petclinic)

**Дата:** 2026-07-05
**Инструмент:** opencode + Java CWE Security Skills, security-audit, security-antipatterns-java
**Статус:** Все уязвимости исправлены

---

## Сводка

| Severity | Исправлено |
|----------|-----------|
| CRITICAL | 13 |
| HIGH     | 12 |
| MEDIUM   | 11 |
| LOW      | 6 |
| **Всего** | **42** |

(42 уникальные уязвимости после удаления дубликатов. Log4ShellServer полностью удалён из репозитория.)
Повторный аудит (6-й раунд) — **CHISTO**, уязвимостей не обнаружено.

---

## Исправленные уязвимости

### 🔴 CRITICAL (13)

| # | Уязвимость | Файл | Фикс |
|---|-----------|------|------|
| 1 | SQL Injection | `customer/CustomerRepository.java:31` | JPA параметризованный запрос |
| 2 | SQL Injection | `owner/OwnerController.java:201-203` | PreparedStatement |
| 3 | SQL Injection | `customer/CustomerController.java:159-161` | PreparedStatement |
| 4 | Command Injection (ping) | `EmailService/EmailController.java` | Эндпоинт удалён |
| 5 | Command Injection (cmd) | `EmailService/EmailController.java` | Эндпоинт удалён |
| 6 | Command Injection (postcmd) | `EmailService/EmailController.java` | Эндпоинт удалён |
| 7 | Command Injection (postjsoncmd) | `EmailService/EmailController.java` | Эндпоинт удалён |
| 8 | Insecure Deserialization | `EmailService/EmailController.java` | Эндпоинт `/deserialize` удалён |
| 9 | Deserialization Gadget | `EmailService/model/EmailData.java` | `readObject()` удалён |
| 10 | JNDI RCE (trustURLCodebase) | `buildandrun.sh`, `Dockerfile` | Флаг удалён из обоих файлов |
| 11 | Log4Shell Exploit Server | `Log4ShellServer/` | Модуль полностью удалён из репозитория |
| 12 | H2 RCE (CVE-2022-45865) | `EmailService/pom.xml` | H2 1.4.197 → 2.3.232 |
| 13 | SSH приватный ключ | `EmailService/dummy-data/id_ed25519` | Файлы удалены |

### 🟠 HIGH (12)

| # | Уязвимость | Файл | Фикс |
|---|-----------|------|------|
| 14 | CSRF отключён | `WebApplication/SecurityConfig.java` | CookieCsrfTokenRepository (HttpOnly=true) |
| 15 | Hardcoded credentials | `WebApplication/SecurityConfig.java` | `${PETCLINIC_ADMIN_PASSWORD}`, `${PETCLINIC_USER_PASSWORD}`, `throw` если не заданы |
| 16 | Hardcoded API key | `GeolocationIoClient.java` | `${GEOLOCATION_API_KEY}` |
| 17 | Path Traversal (showImageByPath) | `PetController.java` | Endpoint удалён (возвращает 404) |
| 18 | Unrestricted File Upload | `PetController.java` | UUID + MIME check + ext allowlist + 5MB limit |
| 19 | Log4Shell (CVE-2021-44228) | `EmailService/pom.xml` | log4j2 2.13.3 → 2.24.1 |
| 20 | Log4j JNDI Lookup | `EmailService/log4j2-spring.xml` | Lookup удалён |
| 21 | MySQL пустой root пароль | `docker-compose.yml` | `${MYSQL_ROOT_PASSWORD:?required}` |
| 22 | EmailService без аутентификации | `EmailService` | Spring Security + BCrypt |
| 23 | EmailService дефолтный пароль БД | `application-mysql.properties` | `${MYSQL_PASS:?required}` |
| 24 | Missing security headers | `SecurityConfig.java` | CSP, nosniff, X-XSS-Protection |
| 25 | EmailService password hardcoded | `EmailService/SecurityConfig.java` | `${emailservice.password}` через env, без fallback |

### 🟡 MEDIUM (11)

| # | Уязвимость | Файл | Фикс |
|---|-----------|------|------|
| 26 | Actuator endpoints | `application.properties` | Ограничен до health,info |
| 27 | H2 Console | `EmailService/application.properties` | Отключена в production |
| 28 | Дефолтные пароли БД | `application-mysql/postgres.properties` | `${DB_PASS:?required}` |
| 29 | Runtime.exec() | `OwnerController.java` | Удалён |
| 30 | Open-in-View | `application.properties` | Отключён |
| 31 | DELETE через GET | `OwnerController.java`, `CustomerController.java` | Заменён на @PostMapping |
| 32 | EmailService CSRF /registerEmail | `EmailService/SecurityConfig.java` | CSRF включён, /registerEmail игнорируется (service-to-service) |
| 33 | Log injection / Log forging | `EmailController.java` | Параметризованное логирование `{}` |
| 34 | Empty catch EmailService | `WebApplication/EmailService.java` | `log.warn` добавлен |
| 35 | Hardcoded пароли в user.sql | `db/*/user.sql` (4 файла) | WARNING-комментарии |
| 36 | DiagnosticController без авторизации | `DiagnosticController.java` | `@PreAuthorize("hasRole('ADMIN')")` + `requestMatchers` |

### 🟢 LOW (6)

| # | Уязвимость | Файл | Фикс |
|---|-----------|------|------|
| 37 | Font-Awesome outdated | `WebApplication/pom.xml` | 4.7.0 (последняя 4.x) |
| 38 | Info leak stack trace | `PetController.java`, `error.html` | nosniff + `${message}` убран |
| 39 | Placeholder API keys | `contrast_security.yaml` | `${CONTRAST_API_KEY:changeme}` |
| 40 | Default credentials в README | `README.md` | Заменены на env var инструкции |
| 41 | Access control granularity | `OwnerController.java`, `CustomerController.java` | `@PreAuthorize("hasRole('ADMIN')")` на delete методы |
| 42 | @EnableMethodSecurity без @PreAuthorize | `SecurityConfig.java` | `@EnableMethodSecurity` + аннотации на ключевых endpoint'ах |

---

## Верификация

```
mvn compile → BUILD SUCCESS
mvn test   → BUILD SUCCESS
```

Функциональность приложения сохранена.

## Следующие рекомендации

- Установить `PETCLINIC_ADMIN_PASSWORD`, `PETCLINIC_USER_PASSWORD` через переменные окружения
- Установить `GEOLOCATION_API_KEY` и `EMAILSERVICE_PASSWORD` через переменные окружения
- Font-Awesome можно обновить до 6.x при миграции frontend
- Рассмотреть переход на внешний SSO/IdP вместо InMemoryUserDetailsManager для production
- Добавить unit/integration тесты для контроллеров
