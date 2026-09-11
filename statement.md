# Project Statement

## Problem Statement

Under India's PM Surya Ghar scheme, lakhs of homes installed rooftop solar in the
last two years. Once installed, owners are handed a login to an inverter app
that shows a single number — units generated — with no reference point to judge
whether that number is good, average, or disastrous.

Panels lose output silently from dust accumulation, partial shading, high
ambient temperatures, inverter clipping, and normal yearly degradation. By the
time an owner notices a problem, months of generation — and money — have already
been lost. No free, independent tool exists for a homeowner to audit their own
system against physics-based expectations.

## Scope

**In scope:**

- Model an installation from a small set of parameters (panel technology,
  count, inverter type, tilt, orientation, commissioning date, tariff).
- Accept daily readings (generation, insolation, peak irradiance, ambient
  temperature, rain flag).
- Compute expected generation from first principles.
- Compare against actual and classify the root cause of any shortfall.
- Persist state to plain CSV files.
- Operate entirely from the command line, with no GUI.

**Out of scope:**

- Real-time inverter integration (Modbus, APIs).
- Weather forecast fetching.
- Financial ROI or payback calculations over the lifetime of the system.
- Cloud sync or multi-user accounts.

## Target Users

- **Homeowners with rooftop solar** who want to know if their system is
  underperforming and why.
- **Installers and service technicians** who need to triage complaints remotely
  before dispatching a crew.
- **Housing societies and RWAs** monitoring common-area solar installations.
- **Students and researchers** studying real-world solar performance.

## High-Level Features

1. **Installation Setup** — configure a solar installation with panel type,
   count, inverter, tilt, orientation, and tariff.
2. **Reading Logging** — record a day's generation along with the weather
   conditions that produced it.
3. **Performance Auditing** — compute expected vs actual generation, deviation
   percentage, and rupee value of any shortfall.
4. **Diagnosis** — classify the cause of underperformance using a rule-based
   engine: dust, temperature derating, shading, degradation, or inverter
   clipping.
5. **Persistence** — save and load all state from CSV files under `data/`.

## Non-Functional Requirements

**Usability.** The tool must be fully operable from a terminal without any GUI,
help files, or prior knowledge of the domain. Every prompt is self-explanatory
and every error is actionable. A first-time user should be able to generate a
diagnosis within 60 seconds of cloning the repository.

**Reliability.** The program must never crash on user input. Empty fields,
non-numeric input, out-of-range values, malformed dates, and physically
impossible readings are all rejected with a clear message and re-prompted.
Running the same input twice produces identical output.

**Maintainability.** The code is split into three layers — `model`, `service`,
and `io` — with each class handling a single responsibility. Physics constants
are stored per panel subclass, not scattered through the codebase. Adding a new
panel type requires writing one new class and changing nothing else.

**Performance.** The auditor processes 30 days of readings in under 50 ms on
any modern laptop. All operations run in O(n) time and O(n) space relative to
the number of readings stored. No background threads, no external calls.

**Portability.** The project depends only on the Java standard library (JDK 17+)
and runs identically on macOS, Linux, and Windows. No build tool, package
manager, or external dependency is required.

**Data integrity.** All state written to `data/` uses human-readable CSV.
Partial writes are avoided by writing to a temp file and renaming, so a crash
mid-save never corrupts existing data.