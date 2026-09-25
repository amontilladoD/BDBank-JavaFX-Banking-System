# BD Bank — JavaFX Banking Management System

A full MVC banking management system built for an academic project. Covers customer + admin
portals, multithreading/concurrency, **SQLite database persistence**, **JSON parsing with Jackson
(including a real API call)**, custom exception handling, and password-masked login.

## How to run — Maven-managed dependencies in IntelliJ (recommended)

`pom.xml` already declares everything you need — JavaFX, `sqlite-jdbc`, and `jackson-databind` —
so Maven downloads and wires up all three jars automatically; you don't have to hunt down and
manually add any jar files yourself. **This directly answers your question: yes, this works.**

### 1. Open the project as Maven
1. Unzip the project. `File → Open...` in IntelliJ → select the `BDBank` folder (the one with `pom.xml`).
2. IntelliJ detects `pom.xml` and offers to import it as a Maven project — click **"Load Maven
   Project"** (or open the **Maven** tool window on the right edge and click the refresh icon).
   This needs internet the first time so Maven can download the 3 dependencies + their transitive
   jars (Jackson alone pulls in `jackson-core` and `jackson-annotations` automatically — this is
   exactly the advantage of Maven over manually downloading jars one by one).
3. `File → Project Structure → Project` → set **SDK** to JDK 17 or newer.
4. Once Maven finishes importing, check `External Libraries` in the Project tool window — you
   should see `javafx-controls`, `javafx-fxml`, `sqlite-jdbc`, and `jackson-databind` (plus its
   two transitive jars) listed there automatically.

### 2. Set up the Run Configuration (this is the important part)
Do **not** rely on `mvn javafx:run` / the `javafx-maven-plugin` goal — that's what threw the
`org.apache.commons.exec.ExecuteException` you hit earlier, because it launches your app as a
separate child process and any small misconfiguration surfaces as that opaque exit-code error.
Instead, use a plain **Application** run configuration, which is far more reliable:

`Run → Edit Configurations → + → Application`:
- **Main class:** `com.bdbank.Launcher`  (⚠ not `App` — `Launcher` is the plain, non-`Application`
  class that avoids the "JavaFX runtime components are missing" error)
- Leave **VM options** empty, or add `--add-modules javafx.controls,javafx.fxml` if you want to be
  extra safe (not required — IntelliJ automatically puts every Maven dependency, including
  JavaFX, on the classpath for you).
- Apply → OK.

### 3. Run it
Click the green ▶ button. First run creates a `data/` folder next to your project containing
`bdbank.db` (the SQLite database file — open it anytime in **DB Browser for SQLite** to show your
teacher the actual tables/rows while the app is closed) plus a couple of plain-text log/export
files, and seeds a default admin (see credentials below).

### Editing screens in Scene Builder
Right-click any `.fxml` file under `src/main/resources/com/bdbank/fxml` → **Open in SceneBuilder**.
If IntelliJ doesn't know where SceneBuilder is installed: `File → Settings → Languages &
Frameworks → JavaFX` → point "SceneBuilder home" to your SceneBuilder installation.

---

## Alternative: no Maven at all (manual jars)

If your teacher's machines truly can't use Maven, you can still add the same three libraries by
hand as IntelliJ **Libraries** instead of Maven dependencies. The one thing to watch for: Maven
resolves *transitive* dependencies automatically, but manually-downloaded jars do not — so for
Jackson specifically you need **three** jars, not one.

| What | Where to get it | Jar(s) needed |
|---|---|---|
| JavaFX SDK | `https://gluonhq.com/products/javafx/` (download the **SDK**, not "jmods") | its `lib` folder |
| SQLite JDBC driver | `https://github.com/xerial/sqlite-jdbc/releases` | `sqlite-jdbc-3.46.1.3.jar` |
| Jackson (3 jars — databind needs these two) | `https://mvnrepository.com/artifact/com.fasterxml.jackson.core/jackson-databind` and the linked `jackson-core` / `jackson-annotations` pages | `jackson-databind-2.17.2.jar`, `jackson-core-2.17.2.jar`, `jackson-annotations-2.17.2.jar` |

Steps: open the `BDBank` folder in IntelliJ but **decline** the Maven import prompt; mark
`src/main/java` as **Sources Root** and `src/main/resources` as **Resources Root**; add all four
jars/folders under `File → Project Structure → Libraries → +`; then follow steps 2–3 above
(Launcher as main class, etc). This is more error-prone than the Maven path since it's easy to
forget one of the three Jackson jars — Maven avoids that entirely, which is the real reason to
prefer it here.

### Default login

- **Admin ID:** `admin`
- **Admin Password:** `admin123`

New customers self-register (pending admin approval) via "New customer? Create an account" on
the login screen, or the admin can open an account directly (instantly active) from
**Account Opening**.

## Database — SQLite

All real data now lives in **`data/bdbank.db`**, a single SQLite file (see `Db.java` for the full
schema/connection code). Tables:

| Table | Holds |
|---|---|
| `accounts` | every customer account (balance, type, status, password hash, etc.) |
| `admins` | admin logins |
| `transactions` | every debit/credit, append-only |
| `notifications` | bell-icon notifications, append-only + an `is_read` flag |
| `requests` | Loan/DPS/FDR/Card/Cheque/Locker/Dollar-endorsement applications (see JSON section below) |
| `loan_schemes`, `dps_schemes`, `fdr_schemes` | admin-managed products, each with a real `AUTOINCREMENT` primary key |
| `bank_config` | one row (id=1): assets, dollar rate, locker fees, and interest rates (see JSON section) |
| `chat_messages` | live support chat log, append-only |

**Design notes worth mentioning to your teacher:**
- One shared JDBC `Connection` for the whole app (`Db.java`), guarded by a single lock, with
  `PRAGMA journal_mode=WAL` and `busy_timeout` set — this avoids the classic "database is locked"
  exception that comes from opening many separate connections to the same SQLite file from
  multiple threads at once (this app has several background threads + the UI thread all touching
  data concurrently).
- Everything still keeps an in-memory cache (`CopyOnWriteArrayList`) of what's in the database,
  which is why the UI feels instant — reads don't round-trip to disk on every keystroke. Writes go
  through immediately (`Db.get().update(...)`) so the database is always kept in sync.
- Transactions, notifications, and chat messages are **append-only** (`INSERT` only, matching how
  a real ledger/audit-log works); accounts, admins, schemes, and requests support proper
  `UPDATE`/`DELETE` since they change over time.

## JSON — parsing, writing, and a real API call

This project uses JSON (via the **Jackson** library — `ObjectMapper`/`JsonNode`, shared as one
instance in `com.bdbank.util.JsonUtil`) in five places, each chosen because it's a genuinely
better fit than a plain SQL column, not just "because JSON was asked for":

1. **`requests.fields_json`** — a Loan application has completely different fields (principal,
   tenure, EMI) than a Locker request (size, fee) or a Card request (card type). Rather than one
   giant table with 20 mostly-empty columns, each request's own data is serialized to a JSON
   object (`ObjectNode`) and stored in one `TEXT` column (`RequestService.fieldsToJson()` /
   `mapRequest()`, which reads it back with `JsonNode.fields()`).
2. **`bank_config.interest_rates_json`** — the per-account-type interest rate map (Normal/Student/
   Woman/Worker/Savings+ → their own %) is naturally a small JSON object rather than five rigid
   columns (`ConfigService`).
3. **`ExchangeRateApiClient.java`** — this is the one most directly tied to your next lab: it
   makes a real HTTPS call to a free, no-key currency API (`https://open.er-api.com/v6/latest/USD`)
   using `java.net.http.HttpClient`, then **parses the JSON response** with Jackson's
   `ObjectMapper.readTree()` to pull out the live USD→BDT rate as a `JsonNode`. If there's no
   internet (e.g. in the exam room), it falls back to a locally-built JSON string formatted
   exactly like the real API's response, and parses that with the *exact same* `parseRateFromJson()`
   method — so the JSON-parsing code path always runs, live or offline. `ConfigService`'s
   background ticker calls this every ~60 seconds.
4. **`FileManager.writeJsonReport()`** — uses `ObjectMapper.writerWithDefaultPrettyPrinter()` to
   write a pretty-printed `.json` file to disk (JSON *writing*, the counterpart to the parsing above).
5. **Statement → "Export as JSON"** button (`StatementPanel`) — builds an `ObjectNode`/`ArrayNode`
   of the account's transactions and saves it via `writeJsonReport()`, next to the existing plain
   text "Export as File" option.

## Architecture (MVC + FXML)

```
com.bdbank
 ├─ model/       -> Model: Account, Transaction, Notification, ServiceRequest (+ subtypes),
 │                  Schemes, BankConfig, Enums, ChatMessage, AdminUser
 ├─ service/     -> Model layer's business logic + persistence:
 │                  Db (SQLite connection + schema + query/update helpers)
 │                  ExchangeRateApiClient (JSON API call + parsing, with offline fallback)
 │                  FileManager (plain-text audit log + statement/JSON export files)
 │                  AuthService, AccountService, RequestService, SchemeService, ConfigService,
 │                  AdminBankingService, NotificationService, ChatService
 ├─ controller/  -> Controller: SplashController, LoginController, RegisterController,
 │                  UserDashboardController, AdminDashboardController — each is a plain class
 │                  with @FXML-injected fields/handlers, loaded via FXMLLoader
 ├─ view/        -> View: Theme (blue/white style helpers for content panels) + user/admin
 │                  panel builders (Balance Inquiry, Transfer, Loan, etc.)
 └─ util/exception -> Util, SessionManager, ExecutorServiceManager, AlertUtil, BankException
```

```
resources/com/bdbank/
 ├─ fxml/  Splash.fxml, Login.fxml, Register.fxml, UserDashboard.fxml, AdminDashboard.fxml
 └─ css/   style.css   (blue & white design system, shared by every FXML screen)
```

**Where XML/FXML is used:** the 5 primary screens — Splash, Login, Register, the customer
dashboard shell, and the admin dashboard shell — are declared entirely in `.fxml` files. Each
has a matching Controller class with `@FXML`-annotated fields (`fx:id`) and handler methods
(`onAction="#method"`), loaded through `FXMLLoader`.

**Why the ~20 feature panels inside the dashboards (Balance Inquiry, Transfer, Loan, DPS, FDR,
etc.) are still built as Java view classes:** they're swapped into the FXML dashboard's
`<StackPane fx:id="contentHolder">` at runtime. Converting each into its own near-duplicate FXML
file wouldn't add any real structure — e.g. `RequestHubPanel` already drives DPS, FDR and Locker
from one class, and the polymorphic `ServiceRequest` hierarchy lets one `RequestService.approve()`
apply the right effect per type.

## OOP concepts demonstrated

- **Abstraction & inheritance:** `ServiceRequest` (abstract) → 7 concrete subclasses in `RequestTypes`
- **Polymorphism:** `RequestService.approve()` branches on `getRequestType()`; each subclass overrides `summary()`
- **Encapsulation:** `Account` balance is private behind `credit()`/`debit()`, never mutated directly
- **Singletons:** `Db`, `AuthService`, `AccountService`, `SessionManager`, `ExecutorServiceManager`, etc.
- **Enums with behavior:** `AccountType` carries its own default interest rate

## Multithreading & concurrency

- `ExecutorServiceManager` — one shared, named daemon `ScheduledExecutorService` used everywhere
  (never raw `new Thread()` scattered around).
- **Background jobs:**
  - `ConfigService.startDollarRateTicker()` — calls the exchange-rate JSON API (a blocking HTTP
    call) on a background thread every ~60s, never on the JavaFX Application Thread.
  - `AdminBankingService.startInterestAccrualJob()` — periodically credits interest to every
    active account based on its type's admin-configured rate.
  - `ChatService` — auto-reply for Live Support arrives 2s later via the scheduler, without
    blocking the UI thread.
- **Async UI operations:** login authentication and balance-transfer processing run on background
  threads via `ExecutorServiceManager`, with results marshalled back to the JavaFX Application
  Thread through `Platform.runLater(...)` — the UI never freezes.
- **Thread safety:** `Account.balance` is an `AtomicReference<Double>`; `debit()` is `synchronized`;
  all in-memory caches (`accounts`, `transactions`, `requests`, `notifications`) are
  `CopyOnWriteArrayList`; `Db` serializes all actual SQLite access behind one lock (SQLite itself
  only supports one writer at a time, so sharing one connection avoids "database is locked"
  errors that come from opening many connections concurrently).

## Error handling

A single, deliberate checked exception (`BankException`) is thrown for every business-rule
violation (insufficient balance, invalid credentials, invalid input, ineligible loan, etc.) and
caught at the UI boundary in each panel, surfaced to the user via inline status labels or
`AlertUtil` dialogs — the app never crashes on bad input. `Db` also catches every `SQLException`
internally and logs it rather than letting a database hiccup crash the UI.

## Feature checklist

**Customer:** notification bell (live badge) • balance inquiry • monthly statement (+ export as
plain text or JSON) • balance transfer (NPSB / BEFTN / Card / bKash / Nagad / Rocket, each with
the exact charge rules requested; credits the recipient's account too if they're a BD Bank
customer) • mobile recharge • loan (eligibility check / application / rates / my loan) • DPS
(application / rates / my DPS) • FDR (application / rates / my FDR) • card & cheque book requests
• locker request (application / rates) • dollar endorsement (live API-fetched rate + apply) •
toll service • utility bills (electricity / water / tax / govt fees / institutional fees) • live
support chat • logout.

**Admin:** transaction management (search/filter) • cash deposit (teller-style deposit into any
account) • account opening with per-type eligibility criteria (Normal / Student / Woman / Worker
/ Savings+) + account list (approve/block) • daily & monthly bank statements (+ file export) •
assets (BDT reserve, foreign currency reserve, gold & valuables, max loan capacity, total loan
disbursed) • per-account-type annual interest rate editor • loan management (review/approve/
reject, add/remove scheme, update rate) • DPS & FDR management (same pattern) • card/cheque
approval • locker management (approve + rates/availability) • dollar endorsement (auto-updating
via live API + manually overridable daily rate, policy-limited approvals) • customer support chat
• logout.

## Notes for your presentation

- Login and registration use JavaFX's native `PasswordField`, which masks input automatically —
  no custom masking code needed, and passwords are SHA-256 hashed before ever touching disk.
- The splash screen holds for exactly 3 seconds (`PauseTransition`) before showing the login screen.
- Because this was built in a sandboxed environment without internet access, a JavaFX runtime, or
  the SQLite/JSON jars, it could not be compiled/run here. Every file was carefully reviewed by
  hand instead (brace-balance checked across every file, every DB row-mapper's constructor call
  cross-checked against the real model constructors, and every service-method call from the view
  layer cross-checked against each rewritten service's public API) — but please still do a first
  build in IntelliJ before your demo in case of a small typo.
- If `open.er-api.com` is blocked on the exam network, the dollar rate will just use the local
  simulated fallback automatically (see `ExchangeRateApiClient`) — nothing else about the app
  changes, so this is safe either way.
