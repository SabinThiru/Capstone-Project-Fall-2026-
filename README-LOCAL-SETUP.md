# CampusPath — Local Setup and Sprint 1 Integration

This guide records the IntelliJ, frontend server, RDS, and Cognito setup used during the October 14–15, 2026 integration session. It describes the working development flow, not a public deployment. Menu labels may vary slightly between IntelliJ versions.

Suggested repository location: `docs/README-LOCAL-SETUP.md`. Link to it from the existing project README rather than replacing unrelated documentation.

## 1. What runs where

| Component | Location | Purpose |
| --- | --- | --- |
| HTML, CSS, JavaScript | Python server on `http://127.0.0.1:5500` | Serves the website to the browser |
| Spring Boot backend | IntelliJ, on `http://localhost:8080` | Handles authentication requests and database operations |
| Amazon Cognito | AWS | Stores login credentials, verifies email, and issues tokens |
| PostgreSQL | AWS RDS, database `campuspath` | Stores university records, student profiles, and settings |
| DataGrip | Developer computer | Inspects and queries the RDS database; it is not the database server |

Keep both Spring Boot and the Python server running while testing. Docker is not required for this setup because PostgreSQL is already running in RDS. The repository's Docker Compose file is a separate development option and is not used by this workflow.

## 2. Open the correct project

Use the repository containing the latest integrated changes. Avoid mixing files from an earlier practice copy with the active repository.

The relevant paths, relative to the repository root, are:

| Path | Purpose |
| --- | --- |
| `backend/pom.xml` | Maven dependencies and build configuration |
| `backend/src/main/java/com/campuspath/backend/BackendApplication.java` | Spring Boot entry point |
| `backend/src/main/resources/application.properties` | Backend configuration using environment variables |
| `login.html` | Signup, email verification, and login UI |
| `dashboard.html` | Dashboard markup |
| `js/login.js` | Authentication requests and initial profile creation |
| `js/dashboard.js` | Profile display, authentication guard, and logout |
| `css/dashboard.css` | Dashboard styling |
| `database/` | Database scripts and related project work |

`dashboard.html` belongs beside `login.html` at the repository root, not inside `css/`. Its stylesheet and script references should resolve to `css/dashboard.css` and `js/dashboard.js`.

## 3. Configure Java 21 and Maven in IntelliJ

The backend targets Java 21. A Java 17 SDK cannot compile this target.

1. Open the repository in IntelliJ.
2. Open **File → Project Structure → Platform Settings → SDKs**. Add or download a JDK 21 if it is not already listed.
3. Under **Project Settings → Project**, select JDK 21 as the Project SDK. Use the SDK default language level unless the project specifies otherwise.
4. Under **Modules → backend → Dependencies**, use the Project SDK or explicitly select JDK 21.
5. Right-click `backend/pom.xml` and choose **Add as Maven Project** if IntelliJ has not already imported it.
6. In **Settings/Preferences → Build, Execution, Deployment → Build Tools → Maven**, select the **Bundled Maven** installation. This was the Maven choice used in the session.
7. Reload/sync the Maven project and wait for successful completion.

If Maven itself reports a Java version mismatch, also check the Maven importer and runner JDK settings and align them with JDK 21. Keep the Java target in `pom.xml`; do not lower it to 17 merely to suppress the error.

## 4. Backend application properties

The connection settings belong in:

`backend/src/main/resources/application.properties`

The configuration used for the integration is:

```properties
spring.application.name=backend

spring.datasource.url=${DB_URL}
spring.datasource.username=${DB_USERNAME}
spring.datasource.password=${DB_PASSWORD}

spring.jpa.hibernate.ddl-auto=validate
spring.jpa.open-in-view=false
spring.sql.init.mode=never

cognito.user-pool-id=${COGNITO_USER_POOL_ID}
cognito.client-id=${COGNITO_CLIENT_ID}
cognito.client-secret=${COGNITO_CLIENT_SECRET}

app.cors.allowed-origins=${CORS_ALLOWED_ORIGINS:http://localhost:5500,http://127.0.0.1:5500,http://localhost:5173,http://localhost:3000}
```

Preserve any other required project settings when applying this configuration.

| Setting | Meaning |
| --- | --- |
| `${VARIABLE_NAME}` | Spring reads the value from the backend process environment |
| `ddl-auto=validate` | Checks that existing database tables match the Java entities; does not create the schema |
| `open-in-view=false` | Disables keeping the JPA persistence context open for the whole web request |
| `sql.init.mode=never` | Prevents automatic execution of Spring's SQL initialization scripts |
| `app.cors.allowed-origins` | Allows browser requests from the listed frontend origins |

The schema and university seed records must already exist in RDS. The session used UNT, UTD, SMU, and UTA. Do not change `ddl-auto` to `create` or `create-drop` on the shared database.

## 5. Create the IntelliJ run configuration

1. Open **Run → Edit Configurations**.
2. Create or select the configuration for `BackendApplication`. An **Application** configuration can run the Spring Boot main class; a Spring Boot configuration may also be available in your edition.
3. Set these values:

| Field | Value |
| --- | --- |
| Configuration name | `BackendApplication` — a descriptive label, not a database name |
| Main class | `com.campuspath.backend.BackendApplication` |
| Module/classpath | The imported `backend` module |
| JRE/JDK | JDK 21 |
| Working directory | The repository's `backend` folder |

If the module selector is unavailable or empty, finish importing `backend/pom.xml` as a Maven project first. IntelliJ may label the selector **Use classpath of module** or show it alongside the Java runtime selector.

4. Open the configuration's **Environment variables** editor. If hidden, use **Modify options → Environment variables**.
5. Enter the following variables individually, replacing the placeholders with your team's actual values:

| Variable | Example/description |
| --- | --- |
| `DB_URL` | `jdbc:postgresql://<RDS_ENDPOINT>:5432/campuspath?sslmode=verify-full&sslrootcert=/absolute/path/to/global-bundle.pem` |
| `DB_USERNAME` | The PostgreSQL login configured for RDS; the session used `postgres` |
| `DB_PASSWORD` | Password for that database login |
| `COGNITO_USER_POOL_ID` | User pool ID from the cloud teammate, such as `us-east-1_EXAMPLE` |
| `COGNITO_CLIENT_ID` | App client ID belonging to that user pool |
| `COGNITO_CLIENT_SECRET` | Secret for that same app client |
| `CORS_ALLOWED_ORIGINS` | Optional override; for this frontend, `http://127.0.0.1:5500,http://localhost:5500` |

Use the actual AWS RDS endpoint hostname, not the instance label or a URL starting with `https://`. The URL must end in the database name `campuspath` before any query parameters. An RDS instance identifier such as `campuspath-dev` is not the database username.

For the TLS URL above, obtain the RDS CA bundle from AWS and place it on each developer's computer. Replace the certificate path with that developer's absolute local path. AWS publishes the bundle at [global-bundle.pem](https://truststore.pki.rds.amazonaws.com/global/global-bundle.pem).

Do not include quote marks or leading/trailing spaces around values entered in the environment-variable editor. A trailing space in the Cognito client ID caused an authentication failure during the session.

6. Click **Apply**, then **OK**.
7. Restart the backend after changing environment variables; an already-running process will retain its previous values.

### What belongs in Git

Commit the `${...}` placeholders in `application.properties` and this documentation. Each teammate supplies their own local runtime configuration with the agreed team credentials. Do not paste the password or client secret into JavaScript, Java source, a README, or a committed run configuration. Keep a secret-bearing IntelliJ run configuration local; review `.idea` or `.run` changes before committing them. Creating a `.env` file alone does not automatically make this setup load it.

## 6. RDS access prerequisites

For the direct local-development connection used in the session:

- The RDS instance must be available and reachable from the developer's network.
- Its attached security group must allow PostgreSQL TCP port **5432** from that developer's current public IP, typically as a `/32` rule.
- The instance was configured as publicly accessible for this development workflow. Public accessibility alone does not grant access; security-group rules and database credentials still apply.
- Each teammate may have a different public IP. A rule allowing one teammate does not automatically allow everyone else. Update the rule if a developer's public IP changes; avoid opening port 5432 to all addresses.
- The `campuspath` database and its tables must exist on the RDS instance, not only in a separate local PostgreSQL installation.

DataGrip can connect to the same endpoint, port, database, and credentials for inspection. A direct connection does not need an SSH tunnel. Changing a DataGrip connection does not automatically change Spring Boot's `DB_URL`.

To confirm the selected database in DataGrip:

```sql
SELECT current_database();
```

Expected value: `campuspath`. PostgreSQL does not use MySQL's `USE campuspath`; change the connection/database selection and open a console attached to that database.

## 7. How Cognito is connected

Use the existing user pool and app client configured by the cloud teammate. All three Cognito environment variables must refer to that same setup.

The current backend:

- Derives the AWS region from the prefix of the user pool ID.
- Uses `CognitoAuthService` for signup, confirmation, and login.
- Uses the app client secret to calculate Cognito's `SECRET_HASH` on the server.
- Logs in through `USER_PASSWORD_AUTH`; the app client must support that authentication flow.
- Uses `CognitoJwtConfig` to validate JWT signatures, issuer, lifetime, app-client audience, and `token_use`.

**This implementation expects a Cognito ID token for protected profile requests.** Although login returns both `accessToken` and `idToken`, the frontend sends the ID token in:

```http
Authorization: Bearer <ID_TOKEN>
```

This documents the current code contract. Do not substitute the access token without updating the backend validation and claim handling too.

The browser talks to Spring Boot, and Spring Boot calls Cognito and RDS. Cognito is not directly connected to PostgreSQL and does not automatically insert a row in `users`. The current signup/confirmation/login calls do not require developers to add AWS IAM access keys to the frontend or this run configuration.

## 8. Start Spring Boot

1. Open `BackendApplication.java`.
2. Click the green Run triangle beside the main class or `main` method, or select the saved `BackendApplication` configuration and press Run.
3. Check the **Run** console for successful startup, including `Started BackendApplication` and the server listening on port **8080**.
4. Open [http://localhost:8080/api/health](http://localhost:8080/api/health).

Expected response:

```json
{"status":"ok"}
```

The health endpoint confirms that the application is serving requests; it is not a dedicated database health query. Opening [http://localhost:8080/api/universities](http://localhost:8080/api/universities) should return the seeded university records and exercises database access.

Keep this process running. The backend root URL is not the frontend website in this setup.

## 9. Start the frontend Python server

Open IntelliJ's **Terminal** tool window in a separate tab from any other running command. The terminal must be in the repository root containing `login.html` and `dashboard.html`, not inside `backend/` or `css/`.

On macOS, check the current directory with:

```bash
pwd
ls
```

If necessary, change to your repository directory. Replace the example path:

```bash
cd "/absolute/path/to/your/Capstone-Project-repository"
```

Start the server:

```bash
python3 -m http.server 5500 --bind 127.0.0.1
```

Leave that terminal running. Python serves the files; it does not run Spring Boot or connect to RDS itself.

Open:

[http://127.0.0.1:5500/login.html](http://127.0.0.1:5500/login.html)

Use this same hostname consistently. `localhost` and `127.0.0.1` are different browser origins, so switching between them does not share the same session storage. Avoid opening the HTML as `file:///...` or through IntelliJ's separate preview server while following this setup.

The frontend JavaScript uses `http://localhost:8080` as its backend API base URL. If the Python port or frontend hostname changes, update the allowed CORS origins as needed and restart Spring Boot. This Python server is for local development, not production hosting.

## 10. Signup, login, and database flow

| Step | Request/behavior |
| --- | --- |
| Populate university dropdown | `GET /api/universities` |
| Create Cognito account | `POST /api/auth/register` with email, password, and fullName |
| Verify email | `POST /api/auth/confirm` with email and confirmationCode |
| Log in | `POST /api/auth/login` with email and password |
| Check for student profile | Authenticated `GET /api/users/me` |
| Create missing profile | Authenticated `POST /api/users/me` with universityId, fullName, and euid; success returns **201** |
| Display dashboard | Authenticated profile request supplies the student's name |
| Log out | Remove the locally stored ID token and return to login |

After signup, `login.js` temporarily stores the pending profile fields in `sessionStorage`. Following verification and the first successful login, it creates the RDS profile if one is missing. Keep the same browser tab/origin for this sequence. Closing the tab can lose the pending profile information before creation.

The backend takes `cognito_sub` and email from the validated token. Profile creation also creates default user settings in the same transaction. Passwords are managed by Cognito and are not inserted into the RDS profile table.

The university HTML field must be a `<select id="university">`, since the JavaScript populates selectable options. Leaving it as an `<input>` caused the locked university field encountered during integration.

Logout currently clears the local tab's token; it does not revoke all Cognito sessions on every device. Academic dashboard values and graduation data are not covered by the tested profile integration.

## 11. Verify the sprint demonstration

The session confirmed signup, email verification, login, a dashboard displaying the account name, logout, repeat login, and the new user's record in RDS.

To reproduce:

1. Start Spring Boot and Python as described above.
2. Register using an unused email whose inbox you can access and a password meeting the Cognito pool's requirements. Use an EUID not already registered for the selected university.
3. Enter the emailed confirmation code.
4. Log in in the same browser tab.
5. Confirm that the dashboard displays the correct name.
6. In DataGrip, on RDS database `campuspath`, run:

```sql
SELECT id, full_name, email, euid, university_id, cognito_sub
FROM public.users
WHERE email = 'replace-with-your-test-email@example.com';
```

Expect one matching profile row. To inspect its settings:

```sql
SELECT s.*
FROM public.user_settings AS s
JOIN public.users AS u ON u.id = s.user_id
WHERE u.email = 'replace-with-your-test-email@example.com';
```

7. Log out and verify that visiting the dashboard redirects to login.
8. Log in again and verify that the existing profile loads.

No manual user INSERT is needed. Save appropriate screenshots of the working UI and database result for sprint evidence without exposing passwords, client secrets, or tokens.

## 12. Troubleshooting from the integration session

| Symptom | What to check or do |
| --- | --- |
| JVM target 21 but SDK 17 | Select JDK 21 for the project, backend module, and run configuration; check Maven JDK settings if needed |
| Backend module/classpath missing | Import `backend/pom.xml` as Maven and wait for successful sync |
| RDS connection timeout | Check endpoint, port 5432, RDS availability, and the security-group rule for your current public IP |
| Password authentication failed | Check `DB_USERNAME` and `DB_PASSWORD`; after an RDS password reset, update local variables and restart |
| Connection uses `postgres` instead of `campuspath` | Check `/campuspath` in `DB_URL` and the database attached to the DataGrip console |
| Missing tables during startup validation | Confirm the schema was created in RDS `campuspath`, not only in local PostgreSQL |
| Cognito client ID fails a validation pattern | Remove accidental spaces from the environment variable and restart |
| `UsernameExistsException` | The email already has a Cognito account; log in or finish verification rather than registering it again |
| Signup shows a generic 401 | Read the latest IntelliJ backend exception; the existing error handling can mask the underlying Cognito error |
| Verification email missing | Confirm the exact email, check spam, and inspect the user's status in the correct Cognito pool; the current UI has no resend-code button |
| `NoClassDefFoundError` for `AuthController$LoginResponse` | Stop, rebuild the backend, and restart to refresh compiled classes |
| Conflicting `healthController` bean definitions | Check for duplicate source controllers; remove only an unintended duplicate. If sources are correct but stale classes remain, stop the backend, delete generated `backend/target`, rebuild, and restart |
| Website cannot be reached on port 5500 | Start Python from the correct repository root and keep its terminal open |
| API cannot be reached on port 8080 | Confirm Spring Boot actually finished starting and is still running |
| Dashboard blank or unstyled | Check that root `dashboard.html` is nonempty and its CSS/JS paths exist in the same active repository |
| Old blank dashboard persists after files are fixed | Open browser developer tools, select Network → Disable cache, keep the tools open, and reload |
| Dashboard redirects to login | Check whether the same tab/origin holds a valid ID token; redirects are expected when logged out |
| University field appears locked | Check that `id="university"` identifies a select element and that `/api/universities` returns successfully |
| Profile creation conflicts | Check for an existing email, Cognito subject, or EUID at that university |

When investigating a new error, clear only the displayed IntelliJ console output, repeat the failing action once, and read the newly generated exception. An old log entry can describe a previous attempt.

## 13. Daily startup and shutdown

**Start:** open the correct repository → run `BackendApplication` with its saved local variables → start Python from the repository root → open `http://127.0.0.1:5500/login.html`.

**Stop:** click Stop for the backend in IntelliJ and press `Ctrl+C` in the Python terminal. Stopping these local processes does not stop or delete the AWS RDS instance or Cognito user pool, and does not stop any ongoing AWS charges.

## 14. Contribution record

This guide documents the integration and local configuration work: importing Maven, resolving the JDK mismatch, configuring environment-based connections, connecting the existing backend/Cognito work to RDS and the UI, fixing frontend file placement and university selection, and testing the complete account flow. It builds on the backend, cloud, and UI components contributed by the team.

Suggested documentation commit:

```text
Document IntelliJ, RDS, Cognito, and local frontend setup
```
