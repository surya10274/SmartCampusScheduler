# Smart Campus Resource and Event Scheduler

A console-based Java application that helps a college or university manage
its shared physical resources (classrooms, labs, auditoriums, sports
facilities, equipment) and the events/bookings that use them — while
guaranteeing that no resource is ever double-booked.

Built as the **VITyarthi "Build Your Own Project"** submission for the
**Java Programming** course.

---

## Overview

Campuses routinely struggle with double-booked seminar halls, faculty
booking labs that clash with lectures, and no visibility into which rooms
are over- or under-utilised. This project models that problem end-to-end:

- Resources (rooms/labs/equipment) are registered once and reused.
- Users (students, faculty, admins) organise **Events** or make direct
  **Bookings** against those resources.
- A central conflict-detection engine rejects any request that overlaps
  an existing reservation on the same resource — before it is ever saved.
- A reporting module turns the raw event/booking data into utilisation
  and load statistics that a facilities office would actually want.

---

## Features

### Module 1 — Resource Management
- Add, list, update (including status), and delete resources.
- Search available resources by type and minimum capacity.
- Resource statuses: `AVAILABLE`, `UNDER_MAINTENANCE`, `DECOMMISSIONED`.

### Module 2 — Event Scheduling
- Schedule an event against a resource for a fixed time window.
- **Automatic conflict detection**: rejects overlapping time windows on the
  same resource with a clear error message naming the clashing event.
- Capacity check: rejects events whose expected attendance exceeds the
  resource's capacity.
- Reschedule and cancel events; list upcoming events, events by resource,
  or events by organizer.

### Module 3 — Resource Booking & Reporting/Analytics
- Ad-hoc, lighter-weight bookings (e.g. a quick study-room reservation)
  that are checked against **both** existing bookings **and** scheduled
  events on the same resource.
- Reports: utilisation hours per resource, busiest resources (top N),
  event load per organizer, event status breakdown, average expected
  attendance.

### Cross-cutting concerns
- Centralised input validation (`Validator`), with dedicated checked
  exceptions (`ValidationException`, `EntityNotFoundException`,
  `ScheduleConflictException`) instead of generic `RuntimeException`s.
- File-based persistence: every entity is stored as a plain-text,
  pipe-delimited file under `data/` — no external database required.
- Timestamped application logging to `logs/app.log` and the console.
- Demo data is auto-seeded on first run so the menu is immediately usable.

---

## Technologies / Tools Used

| Concern            | Choice                                              |
|---------------------|------------------------------------------------------|
| Language            | Java 17+ (standard library only, no external deps) |
| Persistence          | Flat text files (`data/*.txt`), pipe-delimited      |
| Build                | Plain `javac` / any IDE — no Maven/Gradle required   |
| Version control      | Git / GitHub                                          |
| Testing              | Lightweight self-contained assertion harness (`test/`) |

No third-party libraries are required, so the project compiles with a
stock JDK install and has zero dependency-resolution friction for grading.

---

## Project Structure

```
SmartCampusScheduler/
├── src/main/java/com/campus/scheduler/
│   ├── model/       # Resource, User, Event, Booking
│   ├── service/     # ResourceService, UserService, EventSchedulerService,
│   │                #   BookingService, ReportService
│   ├── util/        # FileStorageUtil, IdGenerator, Validator, Logger
│   ├── exception/   # ValidationException, EntityNotFoundException,
│   │                #   ScheduleConflictException
│   └── main/        # Main.java (console UI / entry point)
├── test/com/campus/scheduler/SchedulerTest.java
├── data/            # created at runtime: resources.txt, users.txt, events.txt, bookings.txt
├── logs/            # created at runtime: app.log
├── docs/            # design diagrams (architecture, UML, ER) — see docs/
├── statement.md
└── README.md
```

## Steps to Install & Run

**Prerequisite:** JDK 17 or newer on your `PATH`.

```bash
# 1. Clone the repository
git clone <your-repo-url>
cd SmartCampusScheduler

# 2. Compile
find src/main/java -name "*.java" > sources.txt
javac -d bin @sources.txt

# 3. Run
java -cp bin com.campus.scheduler.main.Main
```

On first run the app auto-creates `data/` and `logs/` and seeds a few
demo resources and users so you can start scheduling events immediately.

## Instructions for Testing

The project ships a small, dependency-free test harness that exercises
the core validation and conflict-detection logic (no JUnit/Maven required):

```bash
find src/main/java test -name "*.java" > all_sources.txt
javac -d bin @all_sources.txt
java -cp bin com.campus.scheduler.SchedulerTest
```

Each check prints `PASS`/`FAIL`; the process exits non-zero if any check
fails, so it can also be wired into a CI pipeline.

You can also test manually through the console menu: try scheduling two
events on the same resource with overlapping times and confirm the second
one is rejected with a conflict message.

## Screenshots

See `docs/screenshots/` for sample console sessions (menu navigation,
a successful booking, and a rejected double-booking).

---

## Author

Submitted for the Java Programming course — VITyarthi "Build Your Own
Project" flipped-course evaluation.
