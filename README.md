# Online Voting System

A secure, modern Online Voting System built with **Java Swing** and **SQLite**, featuring a modern UI/UX inspired by the [iVOTE Behance design system](https://www.behance.net/gallery/171144057/iVOTE-Online-Voting-System).

## Key Features

- **Modern iVOTE UI/UX Design**:
  - Indigo & vibrant violet color palette with pill-shaped rounded components.
  - Custom Java Swing vector illustrations, smooth antialiased rendering, and segmented controls.
  - Interactive voter dashboard featuring real-time candidate search, collapsible sidebar, notifications, and profile details.
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
