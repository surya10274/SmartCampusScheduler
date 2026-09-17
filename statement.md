# Problem Statement

## Title
Smart Campus Resource and Event Scheduler

## Problem Statement
Educational institutions operate a limited pool of shared physical
resources — classrooms, laboratories, auditoriums, sports facilities, and
equipment — that must be shared across departments, clubs, faculty, and
students. Without a centralised system, resource allocation is typically
managed through informal channels (spreadsheets, notice boards, verbal
agreements), which frequently leads to:

- Double-booked rooms and labs, discovered only when two groups arrive at
  the same venue at the same time.
- No single source of truth for what is scheduled where and when.
- No visibility into which resources are over-utilised (chronically busy)
  or under-utilised (rarely booked), making facilities planning difficult.
- No easy way for a student, faculty member, or club coordinator to check
  resource availability before requesting a slot.

The **Smart Campus Resource and Event Scheduler** solves this by providing
a single Java application that models resources, users, events, and
bookings, and enforces conflict-free scheduling automatically at the
moment a reservation is requested — rather than after the fact.

## Scope of the Project
In scope:
- Registering and maintaining campus resources (rooms, labs, auditoriums,
  sports facilities, equipment) with capacity, location, and status.
- Registering users (students, faculty, admins).
- Scheduling formal **Events** against a resource for a fixed time window,
  with automatic overlap and capacity checking.
- Creating lightweight, ad-hoc **Bookings** of a resource that are checked
  against both other bookings and scheduled events.
- Rescheduling and cancelling events; cancelling bookings.
- Generating utilisation and load reports for facilities planning.
- Persisting all data to local flat files so state survives between runs.

Out of scope (possible future work, see report §14):
- A graphical or web-based front end (this is a console application).
- Multi-user concurrent access over a network (single-process, single-user
  console session).
- Authentication/authorization beyond simple role tagging.
- Recurring/repeating events (each event is currently a single occurrence).

## Target Users
- **Students** — booking a study room or checking event schedules.
- **Faculty** — scheduling extra lab sessions, seminars, or guest lectures.
- **Club coordinators / event organisers** — scheduling fests, workshops,
  and competitions that need an auditorium or sports facility.
- **Campus administrators / facilities office** — managing the resource
  inventory and reviewing utilisation reports to plan renovations,
  purchases, or reassignments.

## High-Level Features
1. **Resource Management** — add, update, deactivate, search, and list
   campus resources.
2. **Event Scheduling** — schedule, reschedule, and cancel events with
   built-in double-booking prevention and capacity validation.
3. **Resource Booking** — quick, ad-hoc reservations that share the same
   conflict-free guarantee as full events.
4. **Reporting & Analytics** — utilisation hours per resource, busiest
   resources, event load per organizer, status breakdowns, and average
   expected attendance — to support data-driven facilities decisions.
