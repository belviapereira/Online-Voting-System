# PROJECT_DOCUMENTATION.md
## Online Voting System ("iVOTE") — Complete Project Reference

> A Java Swing + SQLite desktop application for conducting secure online elections.
> This document is based entirely on the actual source code in this repository.
> Every statement below was verified against the code (or, where noted, by running it).

---

# Table of Contents

1. [Project Overview](#1-project-overview)
2. [Technology Stack & Dependencies](#2-technology-stack--dependencies)
3. [Project Structure](#3-project-structure)
4. [Architecture](#4-architecture)
5. [Database Design](#5-database-design)
6. [File-by-File Explanation](#6-file-by-file-explanation)
7. [Important Code Walkthroughs](#7-important-code-walkthroughs)
8. [Java Concepts Used in This Project](#8-java-concepts-used-in-this-project)
9. [Execution Flow (Step-by-Step Traces)](#9-execution-flow-step-by-step-traces)
10. [Setup & Running Instructions](#10-setup--running-instructions)
11. [Verification: How We Proved It Works](#11-verification-how-we-proved-it-works)
12. [Troubleshooting Common Errors](#12-troubleshooting-common-errors)
13. [Known Bugs, Errors & Missing Features](#13-known-bugs-errors--missing-features)
14. [Possible Improvements](#14-possible-improvements)

---

# 1. Project Overview

**What it is:** A desktop application that lets an administrator create elections with candidates,
and lets voters log in and cast exactly one vote per election — with results hidden from voters
until the election is concluded ("sealed ballot").

**Why it exists (the problem it solves):**
Traditional voting needs paper ballots, physical polling stations, and manual counting.
This project demonstrates how software can handle voter identity, prevent duplicate votes,
and publish verifiable results — all stored in a single local database file.

**Key features (all verified in code):**

| Feature | Where it is implemented |
|---|---|
| Register / login for two roles (ADMIN, VOTER) | `LoginFrame`, `UserDAO` |
| Passwords stored as BCrypt hashes (never plain text) | `PasswordUtil`, `UserDAO.registerUser` |
| Admin creates/concludes/deletes elections | `AdminDashboard`, `ElectionDAO` |
| Admin adds/removes candidates | `AdminDashboard`, `ElectionDAO` |
| Voter casts one vote per election | `VoteDAO.castVote` + DB `UNIQUE(election_id, voter_id)` |
| Duplicate vote blocked at database level | `votes` table constraint (verified by test) |
| Live tallies hidden from voters while election is active | `VoterDashboard.updateChartElectionDisplay` (shows "SEALED") |
| Results published after election is concluded | Same method shows chart when `isActive == false` |
| Admin sees results/charts any time | `AdminDashboard` Results & Analytics view |
| Global search (elections, candidates, users) | `performAdminSearch`, `performGlobalSearch` |
| Custom modern UI (rounded cards, pills, gradients) | `ui/components/*`, `Theme` |

**What it is NOT (honest scope):**
- Not a web application — it is a local desktop (Swing) program.
- No server, no networking: two users on two PCs would use two different database files.
- "Sealed" results are hidden by the UI, not encrypted in the database (see §13).

---

# 2. Technology Stack & Dependencies

| Technology | Version | Purpose | Declared in |
|---|---|---|---|
| Java (JDK) | release 25 (runs on 17+; verified on JDK 26) | Language & Swing GUI | `pom.xml` `<maven.compiler.release>25` |
| Maven | 3.9.16 (installed during setup) | Build tool, dependency manager | `pom.xml` |
| SQLite JDBC | org.xerial:sqlite-jdbc:3.45.1. | Embedded SQL database (single file `voting_system.db`) | `pom.xml` |
| jBCrypt | org.mindrot:jbcrypt:0.4 | BCrypt password hashing | `pom.xml` |
| JFreeChart | org.jfree:jfreechart:1.5.4 | **Declared but never used** (see §13) | `pom.xml` |
| SLF4J | 1.7.36 | Logging facade pulled in *transitively* by sqlite-jdbc | Not in `pom.xml` (only README) |
| Java Swing | part of JDK | All GUI windows and custom components | — |

**Why SQLite?** It needs no server installation — the entire database is one file
(`voting_system.db`) created automatically in the folder you run the program from.
Perfect for a mini project.

**Why BCrypt instead of SHA-256?** BCrypt is *adaptive*: it automatically adds a random
"salt" per password and runs the algorithm 2^12 times (cost factor `12` in
`PasswordUtil.hashPassword`). Two identical passwords produce different hashes, and
brute-force attacks become extremely slow.

---

# 3. Project Structure

```text
Online-Voting-System/
├── pom.xml                          # Maven config: dependencies, Java release 25
├── run.bat                          # Windows launcher (FIXED: see §13 — original pointed to another PC)
├── push_to_github.bat               # Helper script: git push to GitHub
├── README.md                        # Project readme (has some inaccuracies, see §13)
├── .gitignore                       # Ignores target/, *.db, IDE files
├── PROJECT_DOCUMENTATION.md         # This file
├── PROJECT_DOCUMENTATION.pdf        # PDF edition of this file
└── src/main/java/com/votingsystem/
    ├── Main.java                    # Entry point: main() starts DB + login window
    ├── config/
    │   └── Database.java            # JDBC connection factory + CREATE TABLE scripts
    ├── dao/                         # Data Access Objects (all SQL lives here)
    │   ├── UserDAO.java             # register, authenticate, count voters, list users
    │   ├── ElectionDAO.java         # elections + candidates CRUD, transactions
    │   └── VoteDAO.java             # cast vote, has-voted check, tallies
    ├── model/                       # Plain data objects (rows of the DB)
    │   ├── User.java                # id, username, password(hash), role
    │   ├── Election.java            # id, title, isActive
    │   └── Candidate.java           # id, electionId, name, party
    ├── util/
    │   └── PasswordUtil.java        # BCrypt hash + verify (2 static methods)
    └── ui/                          # Everything the user sees
        ├── LoginFrame.java          # Sign-in / Register screen
        ├── AdminDashboard.java      # Admin panel (7 views, ~1813 lines)
        ├── VoterDashboard.java      # Voter panel (5 views, ~1331 lines)
        ├── theme/
        │   └── Theme.java           # Colors, fonts, antialiasing hints (design system)
        └── components/              # 12 reusable custom Swing widgets
            ├── PillButton.java          # Capsule button, 5 styles
            ├── PillTextField.java       # Rounded input + placeholder + search icon
            ├── PillPasswordField.java   # Rounded password input + placeholder
            ├── RoundedPanel.java        # Card with rounded corners + shadow + border
            ├── SegmentedRoleSelector.java  # Voter/Admin toggle control
            ├── CandidateCard.java       # Candidate display with VOTE button
            ├── CircularAvatar.java      # Round avatar (initials or illustration)
            ├── HorizontalBarChart.java  # Custom-drawn horizontal bar chart
            ├── StatMetricCard.java      # Circle badge + label statistics tile
            ├── BrandBadge.java          # "iVOTE" gradient pill logo
            ├── HeaderIcon.java          # Hamburger / Bell / Help icons
            └── VotingIllustrationPanel.java  # Ballot-box artwork
```

Total: **25 Java files, ~4,700 lines of code.**

---

# 4. Architecture

The project follows a **layered architecture** — a classic beginner-friendly version of
 MVC. Each layer only talks to the layer below it:

```text
┌─────────────────────────────────────────────────────────────┐
│  UI LAYER (Java Swing)                                      │
│  LoginFrame, AdminDashboard, VoterDashboard, components/    │
│  • Builds the screens, captures button clicks               │
│  • Contains NO SQL                                          │
└──────────────────────────┬──────────────────────────────────┘
                           │ calls methods on
┌──────────────────────────▼──────────────────────────────────┐
│  DAO LAYER (Data Access Object)                             │
│  UserDAO, ElectionDAO, VoteDAO                              │
│  • Contains ALL SQL (as Strings)                            │
│  • Uses PreparedStatement (safe from SQL injection)         │
│  • Returns model objects / boolean / List / Map             │
└──────────────────────────┬──────────────────────────────────┘
                           │ java.sql (JDBC) API
┌──────────────────────────▼──────────────────────────────────┐
│  CONFIG LAYER                                               │
│  Database.java — one place that knows the JDBC URL          │
│  and creates the tables                                     │
└──────────────────────────┬──────────────────────────────────┘
                           │
┌──────────────────────────▼──────────────────────────────────┐
│  SQLite DB file: voting_system.db                           │
│  tables: users, elections, candidates, votes                │
└─────────────────────────────────────────────────────────────┘

  MODEL LAYER (User, Election, Candidate) = data carriers
  that travel between all layers (see arrows below):

  UI  ──reads/writes──▶  DAO  ──maps rows to──▶  Model objects ──▶ UI displays
```

**Why separate layers?**
- *Change one without breaking others*: swapping SQLite for MySQL means editing only
  `Database.java` + DAOs; the UI would not change.
- *Testable*: we tested the DAO layer with 29 assertions **without opening any window** (§11).
- *Single responsibility* ( SOLID "S"): `VoteDAO` only does votes; `Theme` only does colors.

**Relationships between classes:**

```text
Main ──creates──▶ LoginFrame ──on success──▶ AdminDashboard(currentUser)
                      │                         or VoterDashboard(currentUser)
                      │                              │
                      ▼                              ▼
                  UserDAO                    ElectionDAO, VoteDAO, UserDAO
                      │                              │
                      └───────────▶ Database.getConnection() ◀──────────┘
                                             │
                                       voting_system.db
```

Key detail: dashboards receive the logged-in `User` **through the constructor**
(`new AdminDashboard(user)`). That object is the "session" — there is no login token.

---

# 5. Database Design

Created automatically by `Database.initializeDatabase()` on every start
(`CREATE TABLE IF NOT EXISTS`, so existing data is never wiped).

```sql
users        ( id INTEGER PK AUTOINCREMENT,
               username TEXT UNIQUE NOT NULL,
               password TEXT NOT NULL,            -- BCrypt hash, never plain text
               role TEXT CHECK(role IN ('ADMIN','VOTER')) NOT NULL )

elections    ( id INTEGER PK AUTOINCREMENT,
               title TEXT NOT NULL,
               is_active INTEGER DEFAULT 1 )      -- 1 = ongoing, 0 = concluded

candidates   ( id INTEGER PK AUTOINCREMENT,
               election_id INTEGER NOT NULL
                   REFERENCES elections(id) ON DELETE CASCADE,
               name TEXT NOT NULL,
               party TEXT NOT NULL )

votes        ( id INTEGER PK AUTOINCREMENT,
               election_id INTEGER NOT NULL REFERENCES elections(id),
               voter_id    INTEGER NOT NULL REFERENCES users(id),
               candidate_id INTEGER NOT NULL REFERENCES candidates(id),
               UNIQUE(election_id, voter_id) )     -- ★ ONE VOTE PER ELECTION
```

**ER diagram:**

```text
 users 1 ────────< votes >──────── 1 elections 1 ────────< candidates
  │               │   │                │                        │
  │ id (PK)       │   │                │ id (PK)                │ id (PK)
  │ username(UQ)  │   │                │ title                  │ election_id (FK)
  │ password      │   └ candidate_id   │ is_active              │ name
  │ role          │        (FK)        └────────────────────────┘ party
  └───────────────┘ election_id (FK)
```

**The three integrity rules that matter most:**

1. `UNIQUE(election_id, voter_id)` — the database itself refuses a second vote by the
   same voter in the same election. This is *enforcement by the storage engine*, not by
   `if` statements — much harder to bypass.
2. `CHECK(role IN ('ADMIN','VOTER'))` — the database rejects any other role string.
3. Foreign keys link votes → election/candidate/user, so no vote can "float" without
   its parents.

> **Viva gold nugget (verified fact):** SQLite does **not** enforce foreign keys unless
> `PRAGMA foreign_keys=ON` is executed — and this project never executes that pragma.
> So `ON DELETE CASCADE` does *not* fire automatically. That is exactly why
> `ElectionDAO.deleteElection()` manually deletes votes → candidates → election inside
> one transaction (the code comment says: *"Delete votes and candidates first to ensure
> clean cascade in SQLite"*). Our smoke test confirmed children are removed correctly.

---

# 6. File-by-File Explanation

## 6.1 Entry point & config

### `Main.java` (15 lines)
The whole program starts here.
```java
public static void main(String[] args) {
    Database.initializeDatabase();          // 1) create tables if missing
    SwingUtilities.invokeLater(() -> {      // 2) schedule UI on Event-Dispatch Thread
        new LoginFrame().setVisible(true);  // 3) show login window
    });
}
```
- `SwingUtilities.invokeLater` is the **thread-safety rule of Swing**: all UI work must
  happen on the special Event-Dispatch Thread (EDT), not on `main`'s thread.

### `config/Database.java` (73 lines)
| Member | Purpose |
|---|---|
| `private static final String URL = "jdbc:sqlite:voting_system.db"` | JDBC address; *relative* path → DB file is created in the current working directory |
| `static { Class.forName("org.sqlite.JDBC"); }` | Static initializer: loads the driver class when `Database` is first touched; prints a message if missing |
| `getConnection()` | Opens a **new** connection each call (SQLite is lightweight); throws `SQLException` upward |
| `initializeDatabase()` | Executes 4 `CREATE TABLE IF NOT EXISTS` statements via `Statement`, wrapped in try-with-resources; catches `SQLException` and prints the stack trace |

## 6.2 Models (`model/`)

| Class | Fields (all `private` with getters) | Purpose |
|---|---|---|
| `User` | `id, username, password, role` | One row of `users`. Constructor sets everything once (immutable-ish). |
| `Election` | `id, title, isActive` | One election. `toString()` returns `title` — this is why it can be shown directly inside a `JComboBox<Election>`. |
| `Candidate` | `id, electionId, name, party` | One candidate. `toString()` returns `"Name (Party)"`. |

These are **POJOs** (plain old Java objects): no SQL, no UI — only data + getters.
They are the "currency" passed from DAO → UI.

## 6.3 Utility

### `util/PasswordUtil.java` (10 lines)
```java
hashPassword(plain)  -> BCrypt.hashpw(plain, BCrypt.gensalt(12))   // cost 12
checkPassword(plain, hash) -> BCrypt.checkpw(plain, hash)           // constant-time compare
```
Static utility class (cannot be instantiated meaningfully) — a common design for
stateless helpers.

## 6.4 DAO layer (`dao/`) — where all SQL lives

### `UserDAO.java`
| Method | Returns | What it does |
|---|---|---|
| `registerUser(username, plainPassword, role)` | `boolean` | INSERTs user; hashes password first; upper-cases role; **returns `false` if the username already exists** (catches the `UNIQUE` violation) |
| `authenticate(username, plainPassword)` | `User` or `null` | SELECT by username; if found, verifies BCrypt hash; returns a `User` object on success, `null` on any failure |
| `getTotalVotersCount()` | `int` | `SELECT COUNT(*) ... WHERE role='VOTER'` |
| `getAllUsers()` | `List<User>` | SELECT all users ordered by id (used by admin's Voter Registry) |

### `ElectionDAO.java` (largest DAO)
| Method | What it does |
|---|---|
| `createElection(title)` | INSERT with `is_active = 1` |
| `addCandidate(electionId, name, party)` | INSERT into `candidates` |
| `getActiveElections()` | `WHERE is_active = 1` → `List<Election>` (feeds the voter's election dropdown) |
| `getAllElections()` | all elections, newest first |
| `getCandidatesForElection(id)` | candidates of one election |
| `getElectionById(id)` | single election or `null` |
| `toggleElectionStatus(id, isActive)` | UPDATE `is_active` (Conclude / Reopen buttons) |
| `deleteElection(id)` | **Transaction**: delete votes → candidates → election; `commit()` on success, `rollback()` on error (lines 101–134) |
| `deleteCandidate(id)` | **Transaction**: delete that candidate's votes first, then the candidate |
| `getTotalCandidatesCount()` | `COUNT(*)` for statistics tiles |

### `VoteDAO.java`
| Method | What it does |
|---|---|
| `castVote(electionId, voterId, candidateId)` | INSERT; **returns `false` if the UNIQUE constraint fires** (duplicate vote) — the `catch (SQLException)` returns `false` instead of crashing |
| `hasVoted(electionId, voterId)` | `SELECT id ... LIMIT`-style existence check → used to grey out the VOTE button ("Voted ✓") |
| `getResults(electionId)` | SQL `GROUP BY` with `LEFT JOIN` so **candidates with 0 votes still appear**; returns `Map<String,Integer>` label → count |
| `getTotalVotesCount()` / `getTotalVotesForElection(id)` | counts for statistics and election cards |

## 6.5 UI layer (`ui/`)

### `LoginFrame.java` (~465 lines) — the first window
- **Layout**: top navbar (brand + Home/About/Security/Contact links) and a 2-column body:
  left hero (title, text, `VotingIllustrationPanel`), right the white `authCard`.
- **One window, two modes**: a boolean field `isRegisterMode` switches everything —
  titles, button text, and visibility of the role selector (`updateMode(boolean)`,
  lines 361–386). The "Sign In / Register" pill tabs and the bottom link both call it.
- **`handleLogin()`** (lines 388–417): validates non-empty → `userDAO.authenticate()` →
  on success `dispose()` this window and open `AdminDashboard` **or**
  `VoterDashboard` depending on `user.getRole()` → on failure shows a status label and
  a `JOptionPane` error dialog.
- **`handleRegister()`** (lines 419–459): validation rules — both fields required,
  username ≥ 3 characters — then `userDAO.registerUser()`; on success switches back to
  Sign-In mode with the username kept, password cleared.
- **Events**: `submitBtn` action chooses register vs login; pressing **Enter in the
  password field** submits (`passwordField.addActionListener(e -> submitBtn.doClick())`);
  a `DocumentListener` isn't used here but the dashboards use one for live search.

### `AdminDashboard.java` (~1813 lines) — admin home
- `extends JFrame`. Constructor receives the logged-in `User` and stores it (`currentUser`).
- Uses **`CardLayout`**: one container holds 7 "cards" — `DASHBOARD`, `ELECTIONS`,
  `CANDIDATES`, `RESULTS`, `VOTERS`, `SETTINGS`, `SEARCH` — and sidebar buttons call
  `contentCardLayout.show(viewsContainer, name)`. Only one card is visible at a time
  (like switching tabs of an app without opening new windows).
- **Sidebar** (created by `createSidebar()`): profile block, 6 nav buttons, Logout.
  `setActiveNav(String)` marks the active button and calls `refreshAllData()`.
- **`refreshAllData()`** (≈line 1471) is the *central refresh*: rebuilds the 4
  `StatMetricCard`s (voters, active contests, candidates, votes), the election list,
  syncs all `JComboBox<Election>`s (`syncDropdown` keeps the user's current selection),
  updates chart, analytics table, and the voter registry.
- **CRUD flows** (each is: validate → DAO call → clear fields → refresh):
  - Create election: `handleCreateElection()` ≈line 657
  - Add candidate: `handleAddCandidate()` ≈line 769 (empty party defaults to `"Independent"`)
  - Conclude/Reopen: `electionDAO.toggleElectionStatus(...)` (3 places: dashboard rows,
    election cards, search results)
  - Delete election/candidate: confirmation dialog → DAO delete → refresh
- **Results & Analytics**: `HorizontalBarChart` + a `JTable` (columns Candidate, Party,
  Votes, Share %) filled by `updateAnalyticsView()` ≈line 929, which also computes
  turnout = total votes / registered voters.
- **Global search**: typing in the top search box fires `triggerSearch()` →
  `performAdminSearch(query)` (≈line 1214) which searches **elections by title,
  candidates by name/party/election, and users by username/role**, then renders
  actionable result cards (toggle election, view results, remove candidate). It uses a
  **local class `CandidateMatch`** declared *inside* the method as a data holder.
- **No timers**: refresh happens on user actions (pull-based), not on a clock.

### `VoterDashboard.java` (~1331 lines) — voter home
- Same `CardLayout` pattern with 5 cards: `DASHBOARD`, `VOTE`, `GUIDELINES`,
  `SETTINGS`, `SEARCH`.
- **Vote view**: election dropdown (filled only with **active** elections) →
  `loadCandidatesForSelectedElection()` fetches candidates, checks
  `voteDAO.hasVoted(electionId, userId)`, and renders a `CandidateCard` per candidate.
  Live search box filters candidates by name/party (`filterCandidates()`).
- **`handleCastVote(candidate)`** (lines 922–952):
  1. Confirmation dialog (candidate, party, election, warning "cannot be undone")
  2. `voteDAO.castVote(...)` → success dialog + refresh **or** error dialog
     *"You may have already voted in this election."*
- **★ Sealed results (the project's signature feature)**
  `updateChartElectionDisplay()` (lines 1298–1317):
  ```java
  if (el.isActive()) {
      resultsCardLayout.show(resultsDisplayContainer, "SEALED");   // hide tally
  } else {
      Map<String,Integer> results = voteDAO.getResults(el.getId());
      liveResultsChart.setData(results);
      resultsCardLayout.show(resultsDisplayContainer, "CHART");    // publish
  }
  ```
  The sealed panel text explicitly states results are hidden to prevent the
  *Bandwagon Effect*, *voter suppression*, and *tactical voting*. Prev/Next arrows
  cycle through all elections (`cycleChartElection(int delta)`).
- **Live search**: the top search field has a `DocumentListener` → search runs as you
  type → `performGlobalSearch(query)` (lines 1087–1239) with a local class `Match`;
  active elections get a "Vote in this Election" button that jumps to the Vote view.
- **`refreshData()`**: sets the "ongoing election" hero title and rebuilds 3
  `StatMetricCard`s (registered voters, votes cast, candidates).

### `ui/theme/Theme.java`
A constants-only class: color palette (`PRIMARY_NAVY`, `BRAND_GRADIENT_START/END`…),
font factory methods (`fontHero()`, `fontTitle()`, `fontBody()`… all "Segoe UI"), and
`applyRenderHints(Graphics2D)` which switches on antialiasing so circles/curves look
smooth. Because every component asks `Theme`, the whole app is recolorable from one file.

### `ui/components/` — the 12 custom widgets

| Component | Extends | What it does (implementation) |
|---|---|---|
| `RoundedPanel` | `JPanel` | Draws shadow (offset rounded rect) → background → border, all in `paintComponent`; `setOpaque(false)` so the parent canvas shows through |
| `PillButton` | `JButton` | Capsule shape (`radius = height`); `enum Style {GRADIENT, OUTLINE, SECONDARY, DARK, PRIMARY}`; a `MouseAdapter` tracks hover/press and calls `repaint()`; text centered manually with `FontMetrics` |
| `PillTextField` | `JTextField` | Rounded background/border drawn in `paintComponent`, border turns blue on focus (`FocusAdapter`), grey placeholder drawn only when empty, optional magnifier icon drawn with an ellipse + line |
| `PillPasswordField` | `JPasswordField` | Same idea as above for passwords (calls `super.paintComponent` so the real text still renders) |
| `SegmentedRoleSelector` | `JPanel` | Two segment buttons ("Voter"/"Admin"); shared field `selectedRole` decides which one paints with gradient; `getSelectedRole()` returns `"VOTER"` or `"ADMIN"` |
| `CandidateCard` | `RoundedPanel` | Avatar + name + party + buttons. Constructor takes **`Consumer<Candidate> onVote`** and does `voteBtn.addActionListener(e -> onVote.accept(candidate))`; if `hasVoted`, button shows "Voted ✓" and is disabled |
| `CircularAvatar` | `JComponent` | Fills ellipse + ring; if diameter ≥ 48 draws a stylized illustration, else draws the person's initial |
| `HorizontalBarChart` | `JPanel` | Takes `Map<String,Integer>`; draws up to 4 bars: label, rounded track, gradient fill proportional to `votes/max`, % label, axis ticks (0–100%). Empty state message when no votes |
| `StatMetricCard` | `JPanel` | Circle badge with the count + two-line label (special-cases labels like "registered voters") |
| `BrandBadge` | `JComponent` | Gradient pill with centered "iVOTE" text |
| `HeaderIcon` | `JComponent` | enum `Type {HAMBURGER, BELL, HELP}`; draws lines/bell path/`?` glyph; hover changes color via `MouseAdapter`; bell can show a red badge |
| `VotingIllustrationPanel` | `JPanel` | Pure `Graphics2D` artwork: ballot box with gradient, paper with checkmark (`Path2D`), security shield — used on the login hero and voter hero |

**Why hand-draw everything?** Standard Swing has no rounded cards or pill buttons.
Overriding `paintComponent(Graphics)` is *the* standard way to customise look & feel —
and it is a strong topic for a viva on OOP/inheritance.

---

# 7. Important Code Walkthroughs

## 7.1 Program start (Main → Database → LoginFrame)
```text
main()
 ├─ Database.initializeDatabase()
 │    ├─ static{} loads org.sqlite.JDBC driver
 │    └─ 4 × stmt.execute("CREATE TABLE IF NOT EXISTS ...")
 │         (try-with-resources closes Connection & Statement automatically)
 └─ SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true))
```

## 7.2 Registration path
```text
User types username+password, picks role, clicks "Register Now"
 → LoginFrame.handleRegister()          [validates: non-empty, username ≥ 3]
 → UserDAO.registerUser(u, p, role)
      → PasswordUtil.hashPassword(p)    [BCrypt, salt, cost 12]
      → INSERT INTO users(?,?,?)        [PreparedStatement → no SQL injection]
      → true  |  false (e.g., username exists → SQLException caught)
 → success dialog, switch back to Sign In mode
```

## 7.3 Login path (role-based routing)
```text
handleLogin()
 → UserDAO.authenticate(u, p)
      → SELECT * FROM users WHERE username = ?
      → if row exists AND BCrypt.checkpw(plain, hash) → new User(...)
      → else null
 → user != null ?
      YES → dispose() login window
            role == "ADMIN" ? new AdminDashboard(user) : new VoterDashboard(user)
      NO  → status label + error dialog ("Invalid username or password.")
```

## 7.4 Casting a vote (the core business rule)
```text
Voter opens VOTE view
 → electionDAO.getActiveElections()            (dropdown only shows active elections)
 → electionDAO.getCandidatesForElection(id)
 → voteDAO.hasVoted(electionId, userId)        (if true → cards show "Voted ✓" disabled)
 → user clicks VOTE on a CandidateCard
      → JOptionPane confirm ("this ballot cannot be changed")
      → VoteDAO.castVote(...)
           INSERT INTO votes(...) 
           ├─ success → true  → success dialog → refresh (cards now disabled)
           └─ UNIQUE violation (already voted) → SQLException caught → false
                                              → "You may have already voted..."
```
**Two independent guards** protect one-vote-per-voter: the UI disables the button
(`hasVoted`) *and* the database rejects duplicates (`UNIQUE`). The DB guard is the
authoritative one — UI guards are for usability.

## 7.5 Sealed vs published results
```text
Voter presses < or > on the results card
 → cycleChartElection(delta)  → updateChartElectionDisplay()
      election is ACTIVE? ──YES──▶ show "SEALED" panel  (tally invisible)
                    │
                    NO ──▶ voteDAO.getResults(electionId)
                              SELECT c.name||' ('||c.party||')' label,
                                     COUNT(v.id)
                              FROM candidates c LEFT JOIN votes v ...
                              GROUP BY c.id
                           → chart.setData(map) → show "CHART" panel
```

## 7.6 Deleting an election (transaction & rollback)
```text
Admin clicks Delete → confirm dialog
 → ElectionDAO.deleteElection(id)
      conn.setAutoCommit(false);            // begin transaction manually
      try {
          DELETE FROM votes WHERE election_id = ?
          DELETE FROM candidates WHERE election_id = ?
          DELETE FROM elections WHERE id = ?
          conn.commit();   return affected > 0;
      } catch (SQLException ex) {
          conn.rollback();                  // all-or-nothing!
          return false;
      } finally { conn.setAutoCommit(true); }
```
If step 3 fails after steps 1–2 succeeded, rollback restores everything —
this is the **ACID "Atomicity"** property in action.

---

# 8. Java Concepts Used in This Project

All concepts below point to real code in this repo — ideal for viva answers.

### 8.1 Object-Oriented Programming — all four pillars
| Pillar | Where it appears here |
|---|---|
| **Encapsulation** | Models keep fields `private` and expose only getters; DAOs hide SQL; `Theme` hides palette. Outside code cannot corrupt a `User`'s id. |
| **Abstraction** | UI calls `voteDAO.castVote(...)` without knowing SQL exists. Complexity is hidden behind a one-line method signature. |
| **Inheritance** | Every component extends a Swing class: `PillButton extends JButton`, `LoginFrame extends JFrame`, `CandidateCard extends RoundedPanel` (2 levels: → JPanel). Code reuses Swing's behaviour, then *overrides* `paintComponent`. |
| **Polymorphism** | `paintComponent` is overridden differently in 10+ classes — same method call, different drawing. Also: `Election.toString()` vs `Candidate.toString()` both override Object's method, letting `JComboBox` print either type. |

### 8.2 Interfaces & event handling (a huge part of this codebase)
- `ActionListener` — buttons (`submitBtn.addActionListener(e -> ...)`)
- `MouseListener` via **`MouseAdapter`** — hover effects on nav links and icons
  (`mouseEntered` recolours, `mouseExited` restores)
- `DocumentListener` — live search: fires on every keystroke in the search fields
- `FocusAdapter` — text fields switch border colour on focus
- **Anonymous inner classes** (`new MouseAdapter() { ... }`) and **lambdas**
  (`e -> handleLogin()`) are both used — same interface, two syntaxes (great
  follow-up answer: "what's the difference between anonymous class and lambda?")

### 8.3 Method references & functional interfaces
`CandidateCard` stores a `Consumer<Candidate>`; the dashboard passes
`this::handleCastVote` (a **method reference**). This decouples the card from the
dashboard — the card knows only "accept a candidate", not what voting does.

### 8.4 Collections & generics
- `List<Election>`, `List<Candidate>`, `List<User>` with `ArrayList` implementations
- `Map<String,Integer>` for tallies (label → votes)
- `JComboBox<Election>` — generics prevent putting a `String` where an `Election` is expected
- Java Streams in search: `allElectionsList.stream().filter(x -> ...).toList()`

### 8.5 Exception handling
- `try (Connection...; PreparedStatement...)` — **try-with-resources**: closes
  connections/statements automatically even if an exception occurs (prevents leaks)
- DAOs catch `SQLException` and **return a boolean / null / empty list** — the UI then
  shows a friendly dialog instead of a crash. (Design trade-off discussed in §13.)
- `Database.getConnection()` **throws upward** (`throws SQLException`) — callers must handle it.
- Checked vs unchecked: `SQLException` is *checked* (compiler forces you to deal with it).

### 8.6 JDBC (Java Database Connectivity) — the DB API in 5 steps
1. Load driver: `Class.forName("org.sqlite.JDBC")` (Database.java static block)
2. Connect: `DriverManager.getConnection("jdbc:sqlite:voting_system.db")`
3. Prepare: `conn.prepareStatement("INSERT ... VALUES(?,?)")` — `?` are placeholders
4. Bind & execute: `pstmt.setString(1, name); pstmt.executeUpdate();`
5. Read: `ResultSet rs = pstmt.executeQuery(); while (rs.next()) rs.getInt("id")`
Bonus: `conn.setAutoCommit(false)` + `commit()`/`rollback()` for transactions.

### 8.7 Threads & Swing
- `SwingUtilities.invokeLater` — UI must be built on the EDT (Main.java:11)
- Repainting model: components never "push" pixels; they call `repaint()`, which
  schedules `paintComponent` on the EDT (see `PillButton` hover state).

### 8.8 Other concepts present
- `static` vs instance: `Database`/`PasswordUtil` are static utilities; DAOs are
  instantiated (`new VoteDAO()`) though they hold no state
- `final` fields for injected collaborators (`private final VoteDAO voteDAO = new VoteDAO();`)
- `enum`: `PillButton.Style`, `HeaderIcon.Type`
- **Local class inside a method**: `CandidateMatch` (Admin search), `Match` (Voter search)
- **Composition**: dashboards *have* DAOs and components; `CandidateCard` *has* a `PillButton`
- **String handling**: `text blocks` (`"""..."""`) for SQL readability (Java 15+),
  `trim()`, `equalsIgnoreCase`, `toLowerCase()` for case-insensitive search
- **File handling (implicit)**: SQLite reads/writes `voting_system.db`; `.gitignore` excludes it
- **Custom graphics**: `Graphics2D`, `GradientPaint`, `RoundRectangle2D`, `Ellipse2D`,
  `Path2D`, `FontMetrics` — the whole "modern UI" is drawn by these

---

# 9. Execution Flow (Step-by-Step Traces)

## Trace A — First run
```text
$ mvn exec:java ...            (or run.bat)
 → Maven compiles 25 files → target/classes
 → JVM runs com.votingsystem.Main
 → voting_system.db created (if missing) + 4 tables
 → LoginFrame window appears ("iVOTE - Online Voting System")
```

## Trace B — Admin creates an election
```text
Register as ADMIN → Sign In → AdminDashboard (card DASHBOARD)
 → sidebar "Elections" → type title → "Create Election"
      → ElectionDAO.createElection → INSERT (is_active=1)
      → refreshAllData() → new election card appears; combo boxes updated
 → sidebar "Candidates" → pick election, name, party → "Add Candidate"
      → ElectionDAO.addCandidate → INSERT
      → grid refreshes with the new CandidateCard
 → "Conclude Election" button → toggleElectionStatus(id,false)
      → voters can no longer vote; results become visible to them
```

## Trace C — Voter votes
```text
Sign In as VOTER → VoterDashboard
 → "Vote now" → VOTE view → choose election → candidate cards load
 → click VOTE → confirm dialog → VoteDAO.castVote
      → row inserted; card becomes "Voted ✓" disabled
 → results card shows SEALED (election still active)
```

## Trace D — Results published
```text
Admin concludes the election (Trace B last step)
 → Voter presses < / > on results card
 → updateChartElectionDisplay(): isActive == false
 → voteDAO.getResults() GROUP BY candidate → chart drawn with bars & percentages
```

---

# 10. Setup & Running Instructions

## Prerequisites
| Requirement | Checked on this PC |
|---|---|
| JDK 17+ (project compiles with `--release 25`) | ✅ JDK 26.0.2 installed |
| Maven 3.9+ | ✅ Installed during setup at `C:\Users\samve\Tools\apache-maven-3.9.16` (on user PATH) |
| Internet (first build only, to download 3 jars) | ✅ |
| No database server needed | SQLite file is auto-created |

## Option 1 — Maven (recommended, matches README)
```cmd
cd "C:\Users\samve\Work\Coding\Java\Mini Project\Online-Voting-System"
mvn clean compile exec:java -Dexec.mainClass="com.votingsystem.Main"
```
Expected: build output, then the **iVOTE login window** opens. Close the window (or
press Ctrl+C in the terminal) to stop.

## Option 2 — run.bat (double-click)
```cmd
run.bat
```
The fixed launcher: compiles automatically if `target\classes` is missing, then starts
the app detached with `javaw` (no terminal window).

## Option 3 — Manual (no Maven at all)
```cmd
set M2=%USERPROFILE%\.m2\repository
set CP=%M2%\org\xerial\sqlite-jdbc\3.45.1.0\sqlite-jdbc-3.45.1.0.jar;%M2%\org\slf4j\slf4j-api\1.7.36\slf4j-api-1.7.36.jar;%M2%\org\mindrot\jbcrypt\0.4\jbcrypt-0.4.jar
javac -encoding UTF-8 -cp "%CP%" -d target\classes $(dir /s /b src\main\java\*.java)
java -cp "%CP%;target\classes" com.votingsystem.Main
```
(On PowerShell, replace the `dir /s /b` line with:
`Get-ChildItem -Recurse src\main\java\*.java | ForEach-Object FullName | javac -encoding UTF-8 -cp $CP -d target\classes @args`
— simplest is to let `run.bat`/Maven handle compiling.)

## How to verify it works (5-minute checklist)
1. Window title says **"iVOTE - Online Voting System"**.
2. Register an **ADMIN** (toggle role to Admin) — e.g. `admin` / `admin123`.
3. Register a **VOTER** — e.g. `voter1` / `vote123`.
4. Log in as admin → create an election → add 2 candidates.
5. Log out → log in as voter → vote once → button becomes **"Voted ✓"**.
6. Try voting again in the same election → blocked ("You may have already voted").
7. Results card shows **SEALED**; as admin, Results & Analytics already shows the tally.
8. Admin → Conclude election → voter now sees the bar chart.
9. A file `voting_system.db` appears in the project folder (delete it to reset everything).

---

# 11. Verification: How We Proved It Works

Performed during project analysis (without touching your source code):

1. **Build**: `mvn clean compile` → BUILD SUCCESS, 25 files compiled with `release 25`.
2. **Launch**: `run.bat` → process `javaw` with window title
   *"iVOTE - Online Voting System"* confirmed; `mvn exec:java` confirmed separately
   (window appears ~10 s after start and stays open).
3. **Schema probe** (JDBC `DatabaseMetaData`): all 4 tables, columns, NOT NULL flags,
   PRIMARY KEYs, and the `UNIQUE(election_id, voter_id)` constraint confirmed present.
4. **DAO smoke test — 29 assertions, 29 passed**, run against a fresh temporary database:

```text
[29/29 PASS] includes:
  register admin/voter; duplicate username rejected
  correct password → User; wrong password → null; hash starts with "$2a$12$"
  create election; add 2 candidates; counts correct
  hasVoted false → castVote true → hasVoted true
  DUPLICATE VOTE REJECTED BY DB CONSTRAINT
  second voter votes → tally Alice=1 Bob=1
  conclude → active list empty; delete election → votes+candidates+election all removed
  counts: 2 voters, 3 users total
```
5. **Live data check**: the database created during your own GUI testing contained
   real rows — users `samvesh` (VOTER) and `admin` (ADMIN), election "Stuco"
   (active), candidates Devin (CSE) and Arnav (CE), 1 vote — with BCrypt `$2a$12$…`
   hashes, proving the full register→login→vote path works end-to-end in the real UI.

---

# 12. Troubleshooting Common Errors

| Symptom | Cause | Fix |
|---|---|---|
| `'mvn' is not recognized` | Maven not on PATH (new terminals) | Open a **new** terminal after install; or use full path `C:\Users\samve\Tools\apache-maven-3.9.16\bin\mvn` |
| `run.bat` fails / can't find jars | The **original** script pointed to `C:\Users\Belvia Pereira\.m2` | Already fixed in this repo — pull latest changes |
| `ClassNotFoundException: org.sqlite.JDBC` | Driver jar missing from classpath | Ensure `sqlite-jdbc-3.45.1.0.jar` is on `-cp`; Maven users get it automatically |
| `SQLiteException: ... table users already exists` | Not an error — code uses `IF NOT EXISTS` | Ignore; data is preserved by design |
| `SLF4J: Failed to load class StaticLoggerBinder` | Harmless warning: sqlite-jdbc logs, no binding installed | Ignore (NOP logger) — does not affect function |
| `SQLITE_CONSTRAINT_UNIQUE ... users.username` printed in console | Someone registered an existing username; `UserDAO` prints the stack trace but returns `false` | UI shows "Registration failed. Username may already exist." — expected behaviour |
| Window opens then closes instantly | Exception on EDT | Run from terminal (`mvn exec:java`) to see stack trace |
| `Unsupported class file major version` | Too-old JDK for `release 25` | Install JDK 17+ … ideally 21/25+ |
| Can't delete `voting_system.db` | App still running | Close iVOTE first, then delete the file to reset |
| `Permission denied` / file locked | Another program has the DB open | Close other instances of the app |
| Build works but window never appears | Headless RDP session with no display | Run on the actual desktop session |

---

# 13. Known Bugs, Errors & Missing Features

**Be honest in the viva — examiners respect this section.**

### Found in the original repository (documented, one fixed for running)
1. **`run.bat` was unusable on any other PC** — hard-coded classpath to
   `C:\Users\Belvia Pereira\.m2\repository\...`. *Fixed during setup* to use
   `%USERPROFILE%\.m2` and to auto-compile when `target\classes` is missing. This was
   the only source-file change made.
2. **README inaccuracies** (left unedited, as instructed):
   - Claims **JFreeChart** is a dependency *and* used for visualizations — the jar is in
     `pom.xml`, but **no source file imports `org.jfree`**. All charts are the custom
     `HorizontalBarChart`. (Possible interview trap: *“Why is JFreeChart in your pom but unused?”* —
     honest answer: early plan to use a chart library, later replaced by a hand-drawn
     component matching the custom design.)
   - Mentions a **real-time countdown** to session close — **no `javax.swing.Timer`
     exists** anywhere; closing times are static strings ("6:00 PM").
   - Project structure section lists `models/` (actual: `model/`), `CandidateDAO`,
     and a `Vote` model — **none exist**.
   - Lists `slf4j-api` as a direct dependency — it is only **transitive** via sqlite-jdbc.
3. **`push_to_github.bat`** hard-codes someone else's GitHub repository URL.

### Design limitations / potential bugs in the code itself
4. **Foreign keys are declared but SQLite's FK enforcement is never switched on**
   (`PRAGMA foreign_keys=ON` missing) → `ON DELETE CASCADE` is inert; deletion safety
   relies on the manual transactional deletes (which work — verified).
5. **“Sealed” results are UI-level only** — vote counts sit plainly in the DB; anyone
   with the `voting_system.db` file can read them (no encryption). A real system needs
   end-to-end verifiable mixnets/commitment schemes.
6. **SQLExceptions are swallowed** in DAOs (`return false` + `printStackTrace`) —
   the UI cannot distinguish "duplicate username" from "database disk full".
7. **`getResults()` concatenates name + party into one map key** — two candidates with
   identical name and party would collide in the `HashMap` and overwrite each other
   (extremely unlikely but real).
8. **No server / no networking** — multi-voter “online” voting on separate computers is
   impossible; every user on this PC shares one DB file. Not a distributed system.
9. **No session timeout, no logout on inactivity, no password complexity rules**
   (any length ≥ whatever the user types; only the *username* has a ≥3 rule), no
   account lockout after failed logins.
10. **Anyone can register as ADMIN** — the registration screen exposes the Admin role
    selector to everybody (no invite/admin-approval flow).
11. **Admin can also vote** — roles are not mutually exclusive in the voting logic; a
    user registered as ADMIN can log in and, if also a voter in the DB, cast votes.
    (Ballot eligibility is not role-checked.)
12. **Turnout% uses all registered voters** as denominator even if the election had
    a restricted electorate.
13. **No unit tests in the repo** — our 29-assertion smoke test was run ad-hoc for this
    documentation; it is not part of the build.
14. **`is_active` default is 1** — a freshly created election is immediately "ongoing".

---

# 14. Possible Improvements

| Area | Improvement | Java knowledge it would demonstrate |
|---|---|---|
| Correctness | `PRAGMA foreign_keys=ON` in `getConnection()` | SQLite specifics, JDBC statements |
| Security | Password policy + login attempt limiting; don't expose ADMIN in public signup | Validation, business rules |
| Security | Encrypt DB / store only commitments of ballots | Crypto integration |
| Robustness | Custom `DataAccessException` hierarchy instead of returning `false` | **Custom exceptions**, throw/catch |
| Robustness | Use one shared `Connection` or a connection pool; `DataSource` | JDBC advanced |
| Testing | JUnit 5 tests for DAOs (the smoke test, formalized) | Test-driven development |
| Features | `javax.swing.Timer` live countdown to poll closing | Timers, threads |
| Features | Export results to CSV/PDF; charts via JFreeChart (as pom promises) | I/O, libraries |
| Architecture | Split into interfaces (`UserRepository`) + implementations | **Dependency inversion** |
| UI | Look & Feel themes, `CardLayout` → `UIManager`, internationalisation | Swing advanced |
| Data | Store results as per-candidate rows instead of concatenated map keys | Schema design |

---

