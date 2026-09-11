# Solar Performance Auditor

Ever looked at your solar inverter app and wondered if "11 units today" is
actually good? Yeah, me too. That's why I built this.

It's a small command-line tool that figures out how much your rooftop solar
system *should* have generated on a given day, compares that to what it
actually produced, and tries to explain the gap — dust, heat, shading, an
undersized inverter, whatever it might be.

No GUI, no libraries, no nonsense. Just plain Java that runs in your terminal.

---

## Why this exists

My family got rooftop solar installed under the PM Surya Ghar scheme a couple
of years ago. The installer handed us a login to some app that shows a single
big number — units generated this month — and that was it. Nobody told us what
"normal" looked like. If output dropped 15% because the panels were caked in
dust or a new water tank started casting a shadow at 3 PM, we'd have no clue.

Commercial monitoring tools exist, but they're bundled with specific inverters
and don't really *explain* anything. They just show graphs. I wanted something
that says, in plain English: "You're 12% below where you should be. Probably
dust — it hasn't rained in three weeks. You've lost about ₹380 this month."

So I wrote one. It's not fancy. It does the job.

---

## What you'll need

Java 17 or newer. Check with:

    java -version

If nothing shows up, grab Temurin from https://adoptium.net/ and open a fresh
terminal after installing.

That's it. No Maven, no Gradle, no extra JARs to download. Everything is in
the `src/` folder.

---

## Running it

Clone the repo:

    git clone https://github.com/<your-username>/solar-performance-auditor
    cd solar-performance-auditor

Compile:

    mkdir -p out
    javac -d out $(find src -name "*.java")

Run:

    java -cp out com.solar.Main

There's a small `run.sh` helper too, if you'd rather not type those three
lines every time:

    chmod +x run.sh
    ./run.sh

The repo comes with a sample installation already saved — 3.08 kWp of
monocrystalline panels on a south-facing roof, commissioned in May 2022, along
with 31 days of plausible readings. So you can just launch it, hit **3**, and
see a real diagnosis without typing anything in.

---

## What's in the menu

**1 — Set up a new installation.** Asks for panel type, how many panels, what
inverter you have, tilt, orientation, when it was commissioned, and your
electricity tariff. Takes about thirty seconds.

**2 — Log a reading.** You punch in the date, how many units the inverter
reported, how sunny it was (kWh/m²/day), the peak irradiance, the average
ambient temp, and whether it rained. The rain flag matters more than you'd
think — it's how the tool knows whether dust has had a chance to wash off.

**3 — Generate the report.** This is the main event. Prints expected vs
actual, the deviation, average ambient temperature, days since last rain,
a ranked list of likely causes, and the rupee value of whatever's been lost.

**4 — View current setup.** Quick summary of the installation — useful if you
forget what you configured.

**5 — Save and exit.** Everything gets written back to the CSV files in
`data/` so you don't lose it.

All input is validated. Type a blank line, a letter where a number should be,
or an absurd value like 3456 kWh on a 3 kW system and it'll just tell you
that's not possible and ask again. It won't crash.

---


## Technologies & tools used

- **Java 17+** — the language and standard library. No other dependencies.
- **Plain CSV** for storage — inspectable, diffable, and editable by hand.
- **Git** for version control.
- **Zero external libraries** — no Maven, no Gradle, no JARs. Everything
  compiles with `javac` and runs with `java`.

## Running the tests

The repo includes a small unit test suite under `test/`. It covers the physics
of each panel type, inverter clipping behaviour, the auditor's rule engine, and
CSV round-tripping.

Run them with:

    chmod +x test.sh
    ./test.sh

Or manually:

    mkdir -p out test-out
    javac -d out $(find src -name "*.java")
    javac -cp out -d test-out $(find test -name "*.java")
    java -cp out:test-out com.solar.test.TestRunner

Expected output ends with:

    Passed: 15, Failed: 0

The tests don't touch `data/` — they build installations and readings in memory,
so running them never corrupts your saved state.


## How the diagnosis actually works

For each reading, it estimates the DC energy the panels *should* produce:

    E_dc = capacity_kW × insolation × tempLoss × degradation × orientationFactor

Where:

- `tempLoss` accounts for the fact that panels lose efficiency as they get
  hot. The cell temperature is roughly ambient plus some delta depending on
  the panel technology.
- `degradation` is the slow yearly loss every panel has — typically 0.5% to
  1% per year depending on type.
- `orientationFactor` is a fudge factor for anything that isn't south-facing.

Then inverter efficiency is applied, and if it's a string inverter, the code
checks whether the DC output would have been clipped at the inverter's rated
AC power.

Once you've got a bunch of readings, the tool aggregates the deviation and
runs it through a simple ruleset:

- Within ±3%? Everything's fine. **Healthy.**
- Actual is way above expected (over 15%)? Something's wrong with your data —
  wrong units, wrong tariff, bad sensor. **Reading Anomaly.**
- Short by 8% or more, and it hasn't rained in 10+ days? **Dust / Soiling.**
- Average ambient above 33°C? **Temperature Derating.** Welcome to Indian summers.
- Clipping on 20% or more of days? **Inverter Clipping.**
- System is 3+ years old with a 3–12% shortfall? **Gradual Degradation.** Normal aging.
- Day-to-day deviations vary a lot (high standard deviation) with a 5%+ shortfall? **Possible Shading.**

Results are sorted by severity — 3 is serious, 1 is "keep an eye on it."

It's not machine learning. It's just rules I wrote after thinking about what
actually goes wrong with rooftop solar in practice. If you find a case it
gets wrong, the logic is all in `Auditor.java` and easy to tweak.

---

## About the code

The OOP structure is the part I'm happiest with. Everything hinges on two
abstract classes.

`SolarPanel` is abstract. `Monocrystalline`, `Polycrystalline`, and `ThinFilm`
extend it. Each one carries its own temperature coefficient, NOCT delta, and
annual degradation rate. So the physics is different for each type, but they
all speak the same interface.

`Inverter` is also abstract. `StringInverter` clamps its output at rated AC
power — that's how clipping happens. `MicroInverter` doesn't, because each
panel has its own little inverter and there's no central bottleneck.

The interesting bit is `Installation.expectedKWh()`. It loops over the list of
panels, calls `dailyOutputKWh` on each, adds everything up, then passes the
total to `inverter.deliverEnergy(...)`. Because everything's polymorphic, if I
ever wanted to add a `Bifacial` panel type, I'd write one new class and change
literally nothing else. `Installation` and `Auditor` don't need to know it
exists.

`Reading` is immutable — you create one and it never changes. `Installation`
keeps its internals private and exposes them through getters. State gets
persisted by a small `Storage` class that reads and writes two plain CSV files
under `data/`.

---
## Project Layout

    solar-performance-auditor/
    ├── README.md
    ├── statement.md
    ├── .gitignore
    ├── run.sh
    ├── test.sh
    ├── data/
    │   ├── installation.csv
    │   └── readings.csv
    ├── src/
    │   └── com/
    │       └── solar/
    │           ├── Main.java
    │           ├── model/
    │           │   ├── SolarPanel.java
    │           │   ├── Monocrystalline.java
    │           │   ├── Polycrystalline.java
    │           │   ├── ThinFilm.java
    │           │   ├── Inverter.java
    │           │   ├── StringInverter.java
    │           │   ├── MicroInverter.java
    │           │   ├── Installation.java
    │           │   └── Reading.java
    │           ├── service/
    │           │   ├── Auditor.java
    │           │   ├── Report.java
    │           │   └── Diagnosis.java
    │           └── io/
    │               └── Storage.java
    └── test/
        └── com/
            └── solar/
                └── test/
                    ├── TestRunner.java
                    ├── Assert.java
                    ├── PanelTest.java
                    ├── InverterTest.java
                    ├── AuditorTest.java
                    └── ReadingTest.java

## Data files

Two CSVs live in `data/`. Both are plain text and you can edit them in any
editor if you want to poke around.

`installation.csv` holds your system config — one header row, one data row:

    owner,panelType,panelModel,panelCount,panelW,inverterType,inverterModel,inverterKW,tilt,orientation,commissioned,tariff
    Demo User,Monocrystalline,Mono-440,7,440,StringInverter,StringInv-3K,3,15,South,2022-05-15,8.0

`readings.csv` holds every reading you've logged:

    date,actualKWh,insolationKWhM2,peakIrradianceWm2,ambientC,rained,notes
    2026-09-10,11.2,5.0,880,29,0,

Delete either file and the tool starts fresh. Nothing fancy going on under
the hood.

---

## If something breaks

**`javac: command not found`** — Java isn't installed or isn't on your PATH.
Install Temurin JDK 17+ from https://adoptium.net/ and open a new terminal
window.

**`./run.sh: Permission denied`** — you need to make it executable once:
`chmod +x run.sh`.

**`./run.sh: bad interpreter`** — the file picked up Windows line endings
somehow. Fix it with `sed -i '' 's/\r$//' run.sh` on macOS, or without the
empty quotes on Linux.

**Report says "Reading Anomaly"** — one of your readings is physically
impossible. Open `data/readings.csv`, find the row with the huge `actualKWh`
value, delete it.

**Report says "No readings yet"** — `data/readings.csv` is missing or empty.
Either restore it from the sample above, or log a few readings first.

---

## License

MIT. Use it, fork it, change it, whatever.