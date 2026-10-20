# Online Voting System

A secure, modern Online Voting System built with **Java Swing** and **SQLite**, featuring a modern UI/UX inspired by the [iVOTE Behance design system](https://www.behance.net/gallery/171144057/iVOTE-Online-Voting-System).

## Key Features

- **Fully Custom Java Swing UI — No External UI Library**:
  - Every visual element — rounded cards, pill buttons, capsule inputs, avatars, and bar charts — is hand-drawn using `Graphics2D` and `paintComponent` overrides; zero dependence on a third-party look-and-feel.
  - Indigo & violet brand palette defined in a central `Theme.java` design system (colors, fonts, shadows) consumed consistently across all 12 custom components.
  - Smooth antialiased rendering, hover/press button states, focus-highlighted inputs, and a decorative ballot-box illustration — all rendered with pure Java2D.
- **Election Integrity & Secret Ballot Protocol**:
  - Live rolling vote tallies are hidden from voters during active ongoing election sessions to eliminate the bandwagon effect, tactical voting, and voter apathy.
  - Real-time countdown to session close, with final verified results published once the election concludes.
- **Role-Based Authentication**:
  - Secure registration and login for Voters and Administrators.
  - Passwords hashed using `BCrypt`.
- **Database Architecture**:
  - SQLite backend (`voting_system.db`) with automatic table initialization and foreign key constraints.
  - Multi-candidate election support with vote duplicate prevention.

## Requirements

- **JDK 17+** (Recommended: Java 21 LTS or newer; tested with Java 25/26)
- **Maven 3.9+** (or compile using the included standalone launcher)

## Dependencies

- `org.xerial:sqlite-jdbc:3.45.1.0` - SQLite JDBC driver
- `org.mindrot:jbcrypt:0.4` - BCrypt password hashing
- `org.jfree:jfreechart:1.5.4` - Data visualization & metrics
- `org.slf4j:slf4j-api:1.7.36` - Logging facade

## Running the Application

### Option 1: Quick Launcher (Windows)
Double-click `run.bat` or run:
```cmd
run.bat
```

### Option 2: Maven
```cmd
mvn clean compile exec:java -Dexec.mainClass="com.votingsystem.Main"
```

## Project Structure

```text
Online Voting System/
├── src/main/java/com/votingsystem/
│   ├── Main.java                        # Application entry point
│   ├── config/Database.java             # SQLite connection & schema initialization
│   ├── dao/                             # Data Access Objects (UserDAO, ElectionDAO, VoteDAO, CandidateDAO)
│   ├── models/                          # Data models (User, Election, Candidate, Vote)
│   └── ui/                              # Java Swing UI & Components
│       ├── LoginFrame.java              # Auth screen with tabbed sign-in/registration
│       ├── VoterDashboard.java          # Modern voter portal
│       ├── theme/Theme.java             # Color palette, fonts, spacing, shadows
│       └── components/                  # Custom components (PillButton, RoundedPanel, etc.)
├── pom.xml                              # Maven configuration
├── run.bat                              # Launcher script
└── README.md
```

## License
Open-source under the MIT License.
