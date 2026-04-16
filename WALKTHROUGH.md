# Workforce Manager — App Modernization Demo Walkthrough

> **Purpose:** This document is the single end-to-end guide for delivering and exercising the Workforce Manager app modernization demo. It covers prerequisites, setup, running the application (backend + frontend), exploring every feature, executing the migration with GitHub Copilot App Modernization, and verifying the result. Proctors and attendees should follow it sequentially.

---

## Table of Contents

1. [What Is This Application?](#1--what-is-this-application)
2. [What Makes This a Good Migration Demo](#2--what-makes-this-a-good-migration-demo)
3. [Prerequisites](#3--prerequisites)
4. [Build and Run the Backend](#4--build-and-run-the-backend)
5. [Build and Run the React Frontend](#5--build-and-run-the-react-frontend)
6. [Explore the Application — Feature Guide](#6--explore-the-application--feature-guide)
7. [Verify the API (curl Quick-Check)](#7--verify-the-api-curl-quick-check)
8. [Run the Tests](#8--run-the-tests)
9. [Run the App Modernization Assessment](#9--run-the-app-modernization-assessment)
10. [Key Migration Points — What Changes and Why](#10--key-migration-points--what-changes-and-why)
11. [Execute the Migration with Copilot](#11--execute-the-migration-with-copilot)
12. [Post-Migration Verification Checklist](#12--post-migration-verification-checklist)
13. [Containerization & Azure Deployment (Optional)](#13--containerization--azure-deployment-optional)
14. [API Reference](#14--api-reference)
15. [Troubleshooting](#15--troubleshooting)

---

## 1 — What Is This Application?

Workforce Manager is a **Java 8 / Spring Boot 2.5.x monolith** that models a medium-sized company's employee, department, and project management workflows. It includes:

- **7 pre-loaded employees** across 4 departments with a manager hierarchy
- **3 projects** with budget tracking, team assignments, and time entries
- **Role-based security** (Admin, Manager, Employee) using HTTP Basic Auth
- **H2 in-memory database** — resets on every restart for clean demos
- **React frontend** — a full single-page application for hands-on exploration

The application is intentionally written with legacy Java 8 patterns and Spring Boot 2.x APIs so that GitHub Copilot App Modernization can demonstrate a realistic upgrade to Java 21 and Spring Boot 3.x.

### Architecture Overview

```
┌─────────────────────────────────┐
│  React Frontend (port 3000)     │  ← Vite dev server, proxies /api to 8080
│  - Dashboard, Employees,        │
│    Departments, Projects,        │
│    Time Entries, Reports         │
└──────────────┬──────────────────┘
               │ HTTP (JSON + Basic Auth)
┌──────────────▼──────────────────┐
│  Spring Boot Backend (port 8080)│
│  - REST Controllers              │
│  - Service Layer                 │
│  - Spring Security               │
│  - JPA / Hibernate               │
└──────────────┬──────────────────┘
               │
┌──────────────▼──────────────────┐
│  H2 In-Memory Database          │
│  - Auto-populated on startup     │
└─────────────────────────────────┘
```

> **Production build note:** Running `npm run build` in the `frontend/` folder outputs the compiled React app directly into `src/main/resources/static/`, so the Spring Boot JAR can serve the UI on its own (no separate Node process needed).

---

## 2 — What Makes This a Good Migration Demo

| Area | Current (Legacy) | Target (Modern) |
|---|---|---|
| Java version | Java 8 (1.8) | Java 21 |
| Spring Boot | 2.5.14 (EOL Nov 2023) | 3.x |
| Spring Framework | 5.3.x (EOL Dec 2024) | 6.x |
| JPA namespace | `javax.persistence.*` | `jakarta.persistence.*` |
| Validation namespace | `javax.validation.*` | `jakarta.validation.*` |
| Security config | `WebSecurityConfigurerAdapter` (removed in Boot 3) | `SecurityFilterChain` bean |
| Security matchers | `.antMatchers(...)` | `.requestMatchers(...)` |
| Date/time | `java.util.Date` + `@Temporal` | `java.time.LocalDate` |
| Language features | No `var`, no records, no text blocks | Modern Java 17–21 features |

**Why these matter:**
- The `javax.*` → `jakarta.*` namespace change is the single most universal breaking change when moving to Spring Boot 3 / Jakarta EE 10. Every entity and every controller in this app is affected.
- `WebSecurityConfigurerAdapter` was removed entirely in Spring Security 6. The app uses the old pattern, so the migration must refactor it to a `SecurityFilterChain` bean.
- `java.util.Date` with `@Temporal` is a common legacy pattern. JPA 3 handles `java.time.LocalDate` natively without annotations.
- The frontend exercises every endpoint, so you can visually confirm nothing broke.

---

## 3 — Prerequisites

### 3.1 Java 8 JDK (for pre-migration)

We start with Java 8 to demonstrate the legacy state. You will switch to Java 21 during the migration.

**Install (Ubuntu/WSL):**
```bash
sudo apt update && sudo apt install -y openjdk-8-jdk
```

**Windows (if not using WSL):**
Download from [Adoptium](https://adoptium.net/temurin/releases/?version=8) and add to PATH.

**Switch to Java 8 if multiple JDKs are installed:**
```bash
sudo update-alternatives --config java
# Select the entry for java-8-openjdk
```

**Verify:**
```bash
java -version
# Expected: openjdk version "1.8.0_xxx"
```

### 3.2 Java 21 JDK (for post-migration)

Have this ready but do NOT switch to it until the migration step.

**Install (Ubuntu/WSL):**
```bash
sudo apt install -y openjdk-21-jdk
```

**Windows:** Download from [Adoptium](https://adoptium.net/temurin/releases/?version=21).

### 3.3 Maven 3.6+

**Install (Ubuntu/WSL):**
```bash
sudo apt install -y maven
```

**Windows:** A local Maven (`apache-maven-3.9.9`) is included at `c:\javaappmod\apache-maven-3.9.9`. Add `c:\javaappmod\apache-maven-3.9.9\bin` to your PATH, or use the `mvnw` / `mvnw.cmd` wrapper scripts in the project root.

**Verify:**
```bash
mvn --version
# Expected: Apache Maven 3.6.x or higher, Java version 1.8
```

### 3.4 Node.js 18+ and npm (for the React frontend)

The frontend is a React application using Vite as the build tool. Node.js is required to run the dev server and build the frontend.

**Install (Windows):**
```powershell
winget install OpenJS.NodeJS.LTS
```

**Install (Ubuntu/WSL):**
```bash
curl -fsSL https://deb.nodesource.com/setup_lts.x | sudo -E bash -
sudo apt install -y nodejs
```

**Verify:**
```bash
node --version   # Expected: v18.x or higher
npm --version    # Expected: 9.x or higher
```

### 3.5 Git

```bash
git --version
```

### 3.6 VS Code Extensions

Install from the VS Code Extensions panel:

| Extension | Purpose |
|---|---|
| **GitHub Copilot** | AI code completion (required) |
| **GitHub Copilot Chat** | Chat-based AI assistance (required) |
| **GitHub Copilot App Modernization** | The primary migration tool — runs assessments and generates upgrade plans |
| **Extension Pack for Java** (Microsoft) | Java language support, debugging, Maven integration |

### 3.7 Pre-loaded Test Data

The application automatically populates itself on every startup via `DataInitializer.java`. No database setup is needed. The seed data includes:

| Data | Count | Details |
|---|---|---|
| Departments | 4 | Engineering, Marketing, Human Resources, Finance |
| Employees | 7 | Alice Johnson (VP), Bob Smith, Carol Williams, Dave Brown, Eve Davis, Frank Wilson, Grace Lee |
| Projects | 3 | Project Alpha (in progress), Project Beta (in progress), Project Gamma (completed) |
| Project Assignments | Multiple | Employees assigned to projects with roles and hours |
| Time Entries | Multiple | Hours logged against projects with billable status |

---

## 4 — Build and Run the Backend

### 4.1 Build

```bash
# Navigate to the project
cd workforce-manager

# Clean build, skip tests for speed during initial setup
mvn clean package -DskipTests

# Verify the JAR was created
ls -lh target/workforce-manager-1.0.0-SNAPSHOT.jar
```

**Expected output:** `BUILD SUCCESS`

### 4.2 Run

```bash
# Option A: via Maven (recommended for development)
mvn spring-boot:run

# Option B: via JAR
java -jar target/workforce-manager-1.0.0-SNAPSHOT.jar
```

**Windows (PowerShell):**
```powershell
cd c:\javaappmod\workforce-manager
.\mvnw.cmd spring-boot:run
```

**Expected startup log line:**
```
Started WorkforceManagerApplication in X.XXX seconds
```

The backend is now running at **http://localhost:8080**.

> **Leave this terminal running** and open a new terminal for the next steps.

---

## 5 — Build and Run the React Frontend

The frontend lives in the `frontend/` subfolder and uses Vite as the dev server. In development, Vite runs on port 3000 and proxies all `/api/*` requests to the Spring Boot backend on port 8080.

### 5.1 Install Dependencies (first time only)

```bash
cd workforce-manager/frontend
npm install
```

**Windows (PowerShell):**
```powershell
cd c:\javaappmod\workforce-manager\frontend
npm install
```

### 5.2 Start the Dev Server

```bash
npm run dev
```

**Expected output:**
```
  VITE v5.x.x  ready in XXX ms

  ➜  Local:   http://localhost:3000/
```

### 5.3 Open in Browser

Navigate to **http://localhost:3000** in your browser. You will see the **login screen**.

### 5.4 Production Build (optional)

To compile the React app into static files that the Spring Boot backend serves directly (no separate Node process):

```bash
npm run build
```

This outputs the compiled files to `src/main/resources/static/`. After rebuilding the Spring Boot JAR, the app at http://localhost:8080 will serve the React frontend automatically.

---

## 6 — Explore the Application — Feature Guide

This section walks through every part of the UI. Use it to understand the app before and after migration. After the migration, revisit each section to verify that behavior is identical.

### 6.1 Login Page

The login page presents three role-based sign-in buttons. No typing is needed — click one to log in immediately.

| Button | Username / Password | Role | Access Level |
|---|---|---|---|
| 🛡️ **Admin** | admin / admin123 | ADMIN | Full access — all pages, all actions |
| 📋 **Manager** | manager / manager123 | MANAGER | Manage employees, departments, projects, reports |
| 👤 **Employee** | employee / employee123 | EMPLOYEE | View projects, log time entries |

**What to show the audience:** Click each role and point out how the sidebar navigation changes — employees only see Projects and Time Entries, while admins see everything.

### 6.2 Sidebar Navigation

After login, a dark sidebar appears on the left with these sections:

| Section | Nav Items | Visible To |
|---|---|---|
| **Overview** | Dashboard | All roles |
| **Management** | Employees, Departments | Admin, Manager |
| **Work** | Projects, Time Entries | All roles |
| **Analytics** | Reports | Admin, Manager |

The bottom of the sidebar shows the current user's name, role, and a **Sign Out** button.

### 6.3 Dashboard

The dashboard provides an at-a-glance overview of the system.

**Admin / Manager view:**
- **4 summary cards** — Total Employees, Departments, Total Projects, Overdue Projects
- **Employees by Status** — horizontal bar chart showing Active, On Leave, etc.
- **Projects by Status** — horizontal bar chart showing Planning, In Progress, Completed, etc.
- **Recent Employees** — table with name, department, status (links to full list)
- **Active Projects** — table with project name, client, budget usage bar

**Employee view:**
- **1 summary card** — Available Projects count
- **Projects table** — name, status, client

**What to verify after migration:** All numbers should match. The bar charts should render. Clicking "View All" links should navigate correctly.

### 6.4 Employees Page (Admin/Manager only)

**List view (`/employees`):**
- **Search bar** — filters by name or email as you type
- **Status filter pills** — All, Active, Inactive, On Leave, Terminated
- **Table columns** — Employee (name + email), Department, Job Title, Salary, Status badge, Actions
- **Actions** — View, Edit, Delete buttons per row
- **"+ Add Employee"** button in the top right

**Detail view (`/employees/:id`):**
- **Personal Information card** — email, phone, job title, status badge
- **Employment Details card** — department, hire date, salary, manager (clickable link)
- **Direct Reports table** — lists employees who report to this person (if any)
- **Assigned Projects table** — lists projects this employee is on (if any)

**Create / Edit form (`/employees/new` or `/employees/:id/edit`):**
- Fields: First Name*, Last Name*, Email*, Phone, Hire Date*, Salary*, Job Title, Status, Department (dropdown), Manager (dropdown)
- Validates required fields before submission

**What to verify after migration:**
- Search still filters correctly
- Status filter pills work
- Creating a new employee with all fields succeeds
- Editing an existing employee preserves data
- Manager links in the detail view are clickable
- Direct reports appear for Alice Johnson (who manages Bob and Carol)

### 6.5 Departments Page (Admin/Manager only)

**List view (`/departments`):**
- **Search bar** — filters by name or location
- **Table columns** — Department name, Location, Budget, Head Count Limit, Actions
- **Actions** — Edit, Delete per row
- **"+ Add Department"** button in the top right

**Create / Edit form (`/departments/new` or `/departments/:id/edit`):**
- Fields: Name*, Location, Budget*, Head Count Limit

**What to verify after migration:**
- All 4 departments display with correct budgets
- Deleting a department with employees assigned shows an error message (this is expected — the backend prevents it)

### 6.6 Projects Page (All roles)

**List view (`/projects`):**
- **Search bar** — filters by project name or client name
- **Status filter pills** — All, Planning, In Progress, On Hold, Completed, Cancelled
- **Table columns** — Project (name + description excerpt), Client, Status badge, Budget, Budget Used (with color-coded progress bar)
- **Budget bar colors** — green (<60%), amber (60-85%), red (>85%)
- **Actions** — View, Edit, Delete (Edit/Delete visible to Admin/Manager only)

**Detail view (`/projects/:id`):**
- **Project Details card** — client, status badge, start/end dates, description, status change buttons (Admin/Manager only)
- **Budget card** — total budget, spent, remaining (green/red), usage percentage bar, total hours logged, "+ Expense" button
- **Team Assignments table** — employee name (clickable), role, hours allocated, assigned date, Remove button
- **"+ Assign" button** — opens a modal to assign an employee with role and hours
- **Recent Time Entries table** — employee, date, hours, description, billable status badge

**What to verify after migration:**
- Budget bars render correctly with accurate percentages
- Project Alpha shows ~31.7% budget used ($95K of $300K)
- Assigning an employee via the modal works
- Recording an expense updates the budget card in real time
- Changing project status via the buttons works

### 6.7 Time Entries Page (All roles)

**List view (`/time-entries`):**
- **Billable filter pills** — All, Billable, Non-Billable, Internal
- **Table columns** — Employee, Project (clickable link), Date, Hours (bold), Description, Type badge, Actions
- **Actions** — Edit, Delete per row
- **"+ Log Time"** button in the top right

**Create form (`/time-entries/new`):**
- Fields: Employee* (dropdown), Project* (dropdown — only shows non-completed/cancelled projects), Date*, Hours Worked* (1–24), Billable Status, Description
- Employee and project dropdowns are populated from the backend

**What to verify after migration:**
- Filter pills correctly show only matching billable types
- New time entry creation succeeds and appears in the list
- Hours validation (1-24) works in the form

### 6.8 Reports Page (Admin/Manager only)

**Summary cards row:**
- Total Headcount, Total Projects, Overdue Projects, Departments count

**Charts:**
- **Headcount by Status** — horizontal bar chart (Active, Inactive, On Leave, Terminated)
- **Headcount by Department** — horizontal bar chart (Engineering, Marketing, HR, Finance)

**Department Summary table:**
- Columns: Department, Total Employees, Active (badge), On Leave (badge)

**Employee Utilization section:**
- Select an employee + date range and click **Run Report**
- Returns: Employee Name, Total Hours Logged, Active Project Count

**Project Status Overview:**
- Horizontal bar chart showing project count per status

**What to verify after migration:**
- All charts render with the correct numbers
- Employee utilization report returns data for any employee in the seeded date range
- Department summary shows correct counts (Engineering: 4 employees, Marketing: 2, HR: 1, Finance: 0)

---

## 7 — Verify the API (curl Quick-Check)

These curl commands verify the backend independently of the frontend. All API endpoints require HTTP Basic Auth except the health check.

### Health check (no auth required)
```bash
curl http://localhost:8080/actuator/health
# Expected: {"status":"UP",...}
```

### List departments
```bash
curl -u admin:admin123 http://localhost:8080/api/departments
# Expected: JSON array with Engineering, Marketing, HR, Finance
```

### List employees
```bash
curl -u admin:admin123 http://localhost:8080/api/employees
# Expected: JSON array with 7 pre-loaded employees
```

### Search employees by name
```bash
curl -u manager:manager123 "http://localhost:8080/api/employees/search?name=alice"
# Expected: Array with Alice Johnson
```

### List projects
```bash
curl -u admin:admin123 http://localhost:8080/api/projects
# Expected: 3 projects — Alpha, Beta, Gamma
```

### Get overdue projects
```bash
curl -u admin:admin123 http://localhost:8080/api/projects/overdue
# Expected: Projects past their end date that are still IN_PROGRESS
```

### Headcount report
```bash
curl -u admin:admin123 http://localhost:8080/api/reports/headcount
# Expected: {"total":7,"byStatus":{...},"byDepartment":{...}}
```

### Project summary report
```bash
curl -u admin:admin123 http://localhost:8080/api/reports/project-summary
# Expected: {"total":3,"byStatus":{...},"overdueCount":...}
```

### H2 Database Console (browser)

Open: **http://localhost:8080/h2-console**
- JDBC URL: `jdbc:h2:mem:workforcedb`
- Username: `sa`
- Password: *(leave blank)*

This lets you browse the raw database tables to verify entity data.

---

## 8 — Run the Tests

```bash
mvn test
```

**Expected:** All 8 tests pass. Review the test output to see which services and controllers are covered.

> **Important:** Run tests both before AND after migration. The test results should be identical. If any test fails after migration, it means a breaking change was introduced.

---

## 9 — Run the App Modernization Assessment

> This is where the GitHub Copilot App Modernization extension takes center stage.

### 9.1 Open the Project in VS Code

Open the `workforce-manager` folder in VS Code (not the parent `javaappmod` folder).

### 9.2 Run the Assessment

1. Open the Command Palette (`Ctrl+Shift+P`)
2. Search for **"App Modernization: Assess"** and run it
3. Select **Java upgrade** or **Spring Boot upgrade** as the assessment type
4. Wait for the assessment to complete (usually 30–60 seconds)

### 9.3 Review the Assessment Report

The assessment will identify these issues across the codebase:

| Finding | Files Affected | Description |
|---|---|---|
| `javax.persistence.*` imports | All 5 entity classes, controllers | Must change to `jakarta.persistence.*` for Jakarta EE 10 |
| `javax.validation.*` imports | All entity classes | Must change to `jakarta.validation.*` |
| `WebSecurityConfigurerAdapter` | `SecurityConfig.java` | Removed in Spring Security 6 — must refactor to `SecurityFilterChain` |
| `.antMatchers()` | `SecurityConfig.java` | Replaced by `.requestMatchers()` in Spring Security 6 |
| `java.util.Date` + `@Temporal` | `Employee`, `Project`, `ProjectAssignment`, `TimeEntry` | Should migrate to `java.time.LocalDate` |
| Java version in `pom.xml` | `pom.xml` | Must update from `1.8` to `21` |
| Spring Boot version in `pom.xml` | `pom.xml` | Must update from `2.5.14` to `3.x` |

**Proctor note:** Walk through each finding and explain *why* it matters, not just *what* it is. The audience should understand that these are not cosmetic changes — the old APIs literally do not exist in Spring Boot 3.

---

## 10 — Key Migration Points — What Changes and Why

These are the high-value talking points for the presentation. Show the before/after for each.

### 10a. `pom.xml` — Java & Boot version

This is the root change that triggers everything else. Spring Boot 3.x *requires* Jakarta EE 10 and Java 17+.

```xml
<!-- BEFORE -->
<java.version>1.8</java.version>
<artifactId>spring-boot-starter-parent</artifactId>
<version>2.5.14</version>

<!-- AFTER -->
<java.version>21</java.version>
<artifactId>spring-boot-starter-parent</artifactId>
<version>3.3.x</version>
```

### 10b. `javax.*` → `jakarta.*` (every model and controller file)

Spring Boot 3 ships with Jakarta EE 10, which renamed every `javax.*` package to `jakarta.*`. This affects every file that uses JPA or Bean Validation.

```java
// BEFORE
import javax.persistence.*;
import javax.validation.constraints.*;

// AFTER
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
```

**Files affected:** `Employee.java`, `Department.java`, `Project.java`, `ProjectAssignment.java`, `TimeEntry.java`, and all controllers/services that reference `javax` annotations.

### 10c. Security config — `WebSecurityConfigurerAdapter` removal

The `WebSecurityConfigurerAdapter` class was deprecated in Spring Security 5.7 and **removed entirely** in Security 6 (shipped with Boot 3). The migration must replace it with a `SecurityFilterChain` bean.

```java
// BEFORE (Spring Boot 2.x) — SecurityConfig.java
@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.authorizeRequests()
            .antMatchers("/api/**").authenticated()
            ...
    }
}

// AFTER (Spring Boot 3.x)
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(auth -> auth
            .requestMatchers("/api/**").authenticated()
            ...
        );
        return http.build();
    }
}
```

**Key changes:** `extends WebSecurityConfigurerAdapter` is removed, `configure()` becomes a `@Bean` method, `authorizeRequests()` becomes `authorizeHttpRequests()`, `antMatchers()` becomes `requestMatchers()`.

### 10d. Date/time migration (every model with `java.util.Date`)

The old `java.util.Date` + `@Temporal` pattern is verbose and error-prone. JPA 3 supports `java.time.LocalDate` natively.

```java
// BEFORE — Employee.java
import java.util.Date;

@Temporal(TemporalType.DATE)
private Date hireDate;

// AFTER
import java.time.LocalDate;

private LocalDate hireDate;
// No @Temporal needed — JPA 3 handles LocalDate natively
```

**Files affected:** `Employee.java` (hireDate), `Project.java` (startDate, endDate), `ProjectAssignment.java` (assignedDate), `TimeEntry.java` (date).

> **Frontend impact:** The frontend parses dates using `.substring(0, 10)` to extract `YYYY-MM-DD`, which works for both `java.util.Date` (ISO timestamp) and `java.time.LocalDate` (ISO date) serialization. No frontend changes are needed.

### 10e. Modern Java features (optional, post-migration)

These are not required for the migration to succeed, but they demonstrate the value of being on a modern Java version.

```java
// BEFORE — Java 8 verbose loop
Map<String, Long> byStatus = new HashMap<>();
for (Employee e : all) {
    String key = e.getStatus().name();
    byStatus.put(key, byStatus.getOrDefault(key, 0L) + 1);
}

// AFTER — Java 21 streams
Map<String, Long> byStatus = all.stream()
    .collect(Collectors.groupingBy(e -> e.getStatus().name(), Collectors.counting()));
```

---

## 11 — Execute the Migration with Copilot

### 11.1 Switch to Java 21

Before the migration, switch your JDK:

```bash
sudo update-alternatives --config java
# Select java-21-openjdk
java -version
# Expected: openjdk version "21.x.x"
```

### 11.2 Run the Migration

1. In VS Code, open the **App Modernization** panel
2. Run **"App Modernization: Migrate"** (Command Palette → search "Migrate")
3. Select the migration targets identified in the assessment
4. Copilot will generate updated files — **review every diff before accepting**
5. Accept the changes

### 11.3 Rebuild

```bash
mvn clean package -DskipTests
```

**Expected:** `BUILD SUCCESS` with the new Spring Boot 3.x dependencies.

### 11.4 Run Tests

```bash
mvn test
```

**Expected:** All 8 tests pass, same as before.

### 11.5 Restart and Verify

```bash
mvn spring-boot:run
```

Then verify in the browser at http://localhost:3000 (or http://localhost:8080 if using the production build). Walk through the entire Feature Guide (Section 6) again to confirm everything still works.

---

## 12 — Post-Migration Verification Checklist

Use this checklist to confirm the migration was successful. Walk through each item in the UI.

### Backend Verification

- [ ] `mvn clean package` completes with `BUILD SUCCESS`
- [ ] `mvn test` — all 8 tests pass
- [ ] `curl http://localhost:8080/actuator/health` returns `{"status":"UP"}`
- [ ] `curl -u admin:admin123 http://localhost:8080/api/employees` returns 7 employees
- [ ] `curl -u admin:admin123 http://localhost:8080/api/departments` returns 4 departments
- [ ] `curl -u admin:admin123 http://localhost:8080/api/projects` returns 3 projects
- [ ] `curl -u admin:admin123 http://localhost:8080/api/reports/headcount` returns correct totals

### Frontend Verification

- [ ] Login page loads at http://localhost:3000
- [ ] Can sign in as Admin — all 6 nav items visible
- [ ] Can sign in as Employee — only Dashboard, Projects, Time Entries visible
- [ ] **Dashboard** — summary cards show correct numbers, charts render
- [ ] **Employees** — list loads with 7 employees, search works, status filters work
- [ ] **Employees** — click into Alice Johnson → see direct reports (Bob, Carol)
- [ ] **Employees** — create a new employee → appears in list
- [ ] **Departments** — list loads with 4 departments, budgets display correctly
- [ ] **Projects** — list loads, budget bars render with correct percentages
- [ ] **Projects** — click into Project Alpha → assignments and time entries appear
- [ ] **Projects** — assign an employee via the modal → appears in assignments table
- [ ] **Projects** — record an expense → budget card updates
- [ ] **Time Entries** — list loads, billable filter pills work
- [ ] **Time Entries** — log a new time entry → appears in list
- [ ] **Reports** — all charts render, department summary table populates
- [ ] **Reports** — run an employee utilization report → results appear

---

## 13 — Containerization & Azure Deployment (Optional)

After the migration, you can optionally demonstrate containerization and cloud deployment. This shows the full modernization journey from legacy code to a cloud-native container.

### 13.1 Generate a Dockerfile with Copilot

Use GitHub Copilot Chat or the App Modernization extension:

```
@workspace Generate a multi-stage Dockerfile for this Spring Boot 3 application using Java 21
```

### 13.2 Build and Run the Container

```bash
docker build -t workforce-manager .
docker run -p 8080:8080 workforce-manager
```

### 13.3 Deploy to Azure (App Service or AKS)

Use the App Modernization extension to generate:
- **Terraform or Bicep** templates for Azure App Service or AKS
- **CI/CD pipeline** definitions for GitHub Actions
- **Kubernetes manifests** (if targeting AKS)

> Detailed Azure deployment steps depend on the target service and are covered in the broader workshop materials.

---

## 14 — API Reference

### Employee Endpoints (`/api/employees`) — Admin, Manager

| Method | Endpoint | Description |
|---|---|---|
| GET | `/api/employees` | List all employees |
| GET | `/api/employees/{id}` | Get employee by ID |
| GET | `/api/employees/email/{email}` | Get employee by email |
| POST | `/api/employees` | Create new employee |
| PUT | `/api/employees/{id}` | Update employee |
| PATCH | `/api/employees/{id}/status?status=X` | Update employee status |
| DELETE | `/api/employees/{id}` | Delete employee |
| GET | `/api/employees/search?name=X` | Search by name (case-insensitive) |
| GET | `/api/employees/department/{deptId}` | List employees in department |
| GET | `/api/employees/status/{status}` | Filter by status |
| GET | `/api/employees/{id}/direct-reports` | Get direct reports |

### Department Endpoints (`/api/departments`) — Admin, Manager

| Method | Endpoint | Description |
|---|---|---|
| GET | `/api/departments` | List all departments |
| GET | `/api/departments/{id}` | Get department by ID |
| GET | `/api/departments/{id}/employees` | Get department with employees |
| POST | `/api/departments` | Create department |
| PUT | `/api/departments/{id}` | Update department |
| DELETE | `/api/departments/{id}` | Delete (fails if employees assigned) |
| GET | `/api/departments/by-location?location=X` | Filter by location |

### Project Endpoints (`/api/projects`) — All Roles

| Method | Endpoint | Description |
|---|---|---|
| GET | `/api/projects` | List all projects |
| GET | `/api/projects/{id}` | Get project by ID |
| POST | `/api/projects` | Create project |
| PUT | `/api/projects/{id}` | Update project |
| PATCH | `/api/projects/{id}/status?status=X` | Update project status |
| POST | `/api/projects/{id}/expenses?amount=X` | Record expense |
| DELETE | `/api/projects/{id}` | Delete (fails if IN_PROGRESS) |
| GET | `/api/projects/{id}/assignments` | List team assignments |
| POST | `/api/projects/{id}/assignments?employeeId=X&role=Y&hoursAllocated=Z` | Assign employee |
| DELETE | `/api/projects/{id}/assignments/{employeeId}` | Remove assignment |
| GET | `/api/projects/status/{status}` | Filter by status |
| GET | `/api/projects/overdue` | List overdue projects |
| GET | `/api/projects/employee/{employeeId}` | Projects for an employee |

### Time Entry Endpoints (`/api/time-entries`) — All Roles

| Method | Endpoint | Description |
|---|---|---|
| GET | `/api/time-entries` | List all time entries |
| GET | `/api/time-entries/{id}` | Get time entry by ID |
| POST | `/api/time-entries/employee/{eId}/project/{pId}` | Create time entry |
| PUT | `/api/time-entries/{id}` | Update time entry |
| DELETE | `/api/time-entries/{id}` | Delete time entry |
| GET | `/api/time-entries/employee/{eId}` | Entries for employee |
| GET | `/api/time-entries/project/{pId}` | Entries for project |
| GET | `/api/time-entries/range?start=X&end=Y` | Entries in date range |
| GET | `/api/time-entries/project/{pId}/total-hours` | Total hours for project |
| GET | `/api/time-entries/employee/{eId}/hours?start=X&end=Y` | Employee hours in range |

### Report Endpoints (`/api/reports`) — Admin, Manager

| Method | Endpoint | Description |
|---|---|---|
| GET | `/api/reports/headcount` | Headcount by status & department |
| GET | `/api/reports/project-summary` | Project totals by status + overdue count |
| GET | `/api/reports/employee/{eId}/utilization?start=X&end=Y` | Employee utilization |
| GET | `/api/reports/department/{dId}/summary` | Department employee summary |

### Infrastructure Endpoints

| Method | Endpoint | Auth | Description |
|---|---|---|---|
| GET | `/actuator/health` | Public | Health check |
| GET | `/actuator/info` | Admin | App info |
| GET | `/actuator/metrics` | Admin | Metrics list |
| GET | `/h2-console` | Public | H2 database browser |

---

## 15 — Troubleshooting

### Backend won't start

| Symptom | Cause | Fix |
|---|---|---|
| `UnsupportedClassVersionError` | Wrong Java version | Verify `java -version` matches pre/post migration (Java 8 before, Java 21 after) |
| Port 8080 already in use | Previous instance still running | Kill the process: `lsof -i :8080` (Linux) or `netstat -ano \| findstr :8080` (Windows) |
| `BUILD FAILURE` on `mvn package` | Dependencies not resolved | Check internet connectivity; run `mvn dependency:resolve` |

### Frontend won't start

| Symptom | Cause | Fix |
|---|---|---|
| `npm: command not found` | Node.js not installed | Install Node.js LTS (see Prerequisites) |
| Port 3000 already in use | Previous dev server running | Kill it or change port in `vite.config.js` |
| API calls return 401 | Backend not running | Start the backend first (`mvn spring-boot:run`) |
| CORS errors in console | Not using the proxy | Use http://localhost:3000 (Vite proxy), not opening index.html directly |

### After migration

| Symptom | Cause | Fix |
|---|---|---|
| `ClassNotFoundException: javax.persistence.Entity` | Incomplete namespace migration | Search for remaining `javax.` imports and change to `jakarta.` |
| `NoSuchMethodError` on security config | Old `WebSecurityConfigurerAdapter` pattern | Verify `SecurityConfig.java` uses `SecurityFilterChain` bean pattern |
| Date fields return `null` | `@Temporal` removed but `java.util.Date` still used | Ensure all date fields are `java.time.LocalDate`, remove `@Temporal` |
| Tests fail with `ApplicationContext` errors | Incomplete migration | Review Spring Boot migration guide; ensure all dependencies are Boot 3 compatible |
| Frontend dates display incorrectly | Unexpected date format from API | `LocalDate` serializes as `"2024-01-15"` — if you see a different format, check Jackson config |
