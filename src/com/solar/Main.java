package com.solar;

import com.solar.io.Storage;
import com.solar.model.*;
import com.solar.service.Auditor;
import com.solar.service.Diagnosis;
import com.solar.service.Report;

import java.io.IOException;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Scanner;

public class Main {
    private static final Scanner sc = new Scanner(System.in);
    private static final Storage storage = new Storage(Paths.get("data"));
    private static Installation installation;
    private static List<Reading> readings = new ArrayList<>();

    public static void main(String[] args) {
        try {
            installation = storage.loadInstallation();
            readings = storage.loadReadings();
        } catch (IOException e) {
            System.out.println("Note: could not load previous data (" + e.getMessage() + ")");
        }

        while (true) {
            System.out.println();
            System.out.println("==== Solar Performance Auditor ====");
            System.out.println("1. Setup new installation");
            System.out.println("2. Log a reading");
            System.out.println("3. Generate performance report");
            System.out.println("4. View installation");
            System.out.println("5. Exit");
            System.out.print("Choose: ");

            String choice = sc.nextLine().trim();
            try {
                switch (choice) {
                    case "1": setupInstallation(); break;
                    case "2": logReading(); break;
                    case "3": generateReport(); break;
                    case "4": viewInstallation(); break;
                    case "5":
                        saveAll();
                        System.out.println("Goodbye.");
                        return;
                    default: System.out.println("Invalid choice. Enter 1–5.");
                }
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
            }
        }
    }

    // ---------------- input helpers (never throw, always re-prompt) ----------------

    private static String promptLine(String label) {
        while (true) {
            System.out.print(label);
            String s = sc.nextLine().trim();
            if (!s.isEmpty()) return s;
            System.out.println("  ! Cannot be empty. Try again.");
        }
    }

    private static double promptDouble(String label, double min, double max) {
        while (true) {
            System.out.print(label);
            String s = sc.nextLine().trim();
            if (s.isEmpty()) { System.out.println("  ! Cannot be empty."); continue; }
            try {
                double v = Double.parseDouble(s);
                if (v < min || v > max) {
                    System.out.printf("  ! Must be between %.2f and %.2f.%n", min, max);
                    continue;
                }
                return v;
            } catch (NumberFormatException e) {
                System.out.println("  ! Not a valid number.");
            }
        }
    }

    private static int promptInt(String label, int min, int max) {
        while (true) {
            System.out.print(label);
            String s = sc.nextLine().trim();
            if (s.isEmpty()) { System.out.println("  ! Cannot be empty."); continue; }
            try {
                int v = Integer.parseInt(s);
                if (v < min || v > max) {
                    System.out.printf("  ! Must be between %d and %d.%n", min, max);
                    continue;
                }
                return v;
            } catch (NumberFormatException e) {
                System.out.println("  ! Not a valid integer.");
            }
        }
    }

    private static LocalDate promptDate(String label) {
        while (true) {
            System.out.print(label);
            String s = sc.nextLine().trim();
            if (s.isEmpty()) { System.out.println("  ! Cannot be empty."); continue; }
            try {
                LocalDate d = LocalDate.parse(s);
                if (d.isAfter(LocalDate.now())) {
                    System.out.println("  ! Date cannot be in the future.");
                    continue;
                }
                return d;
            } catch (DateTimeParseException e) {
                System.out.println("  ! Use yyyy-MM-dd format (e.g. 2026-09-10).");
            }
        }
    }

    private static boolean promptYesNo(String label) {
        while (true) {
            System.out.print(label);
            String s = sc.nextLine().trim().toLowerCase();
            if (s.equals("y") || s.equals("yes")) return true;
            if (s.equals("n") || s.equals("no"))  return false;
            System.out.println("  ! Enter y or n.");
        }
    }

    // ---------------- menu actions ----------------

    private static void setupInstallation() throws IOException {
        if (installation != null) {
            System.out.print("An installation already exists. Overwrite? (y/n): ");
            String s = sc.nextLine().trim().toLowerCase();
            if (!s.equals("y") && !s.equals("yes")) {
                System.out.println("Cancelled.");
                return;
            }
        }

        String owner = promptLine("Owner name: ");

        int pt = promptInt("Panel type (1=Mono, 2=Poly, 3=ThinFilm): ", 1, 3);
        String panelType, panelModel;
        double panelW;
        switch (pt) {
            case 2: panelType = "Polycrystalline"; panelModel = "Poly-330"; panelW = 330; break;
            case 3: panelType = "ThinFilm";        panelModel = "TF-300";   panelW = 300; break;
            default: panelType = "Monocrystalline"; panelModel = "Mono-440"; panelW = 440;
        }

        int count = promptInt("Number of panels (1–200): ", 1, 200);

        int invChoice = promptInt("Inverter type (1=String, 2=Micro): ", 1, 2);
        String invType = (invChoice == 2) ? "MicroInverter" : "StringInverter";
        double invKW = promptDouble("Inverter rated kW (0.3–100): ", 0.3, 100.0);
        String invModel = invType.equals("StringInverter")
                ? "StringInv-" + (int) invKW + "K" : "MicroInv-0.4K";

        int tilt = promptInt("Tilt in degrees (0–60): ", 0, 60);
        String orientation = promptLine("Orientation (South/South-West/West/East/North): ");
        LocalDate commissioned = promptDate("Commissioned on (yyyy-MM-dd): ");
        double tariff = promptDouble("Tariff per kWh (₹) (0.5–100): ", 0.5, 100.0);

        List<SolarPanel> panels = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            switch (panelType) {
                case "Polycrystalline": panels.add(new Polycrystalline(panelModel, panelW)); break;
                case "ThinFilm":        panels.add(new ThinFilm(panelModel, panelW));        break;
                default:                panels.add(new Monocrystalline(panelModel, panelW));
            }
        }
        Inverter inv = invType.equals("StringInverter")
                ? new StringInverter(invModel, invKW)
                : new MicroInverter(invModel, invKW);

        installation = new Installation(owner, panels, inv, tilt, orientation, commissioned, tariff);
        storage.saveInstallation(installation);
        System.out.println("✔ Installation saved.");
    }

    private static void logReading() throws IOException {
        if (installation == null) {
            System.out.println("Please setup an installation first (option 1).");
            return;
        }
        LocalDate date = promptDate("Date (yyyy-MM-dd): ");

        // Physically plausible upper bound: capacity (kW) × 8 sun-hours/day
        double maxPlausible = installation.totalCapacityKW() * 8.0;
        double kwh = promptDouble(
                String.format("Actual generation (kWh) (0–%.1f): ", maxPlausible),
                0.0, maxPlausible);

        double ins = promptDouble("Daily insolation (kWh/m²/day) (0–12): ", 0.0, 12.0);
        double irr = promptDouble("Peak irradiance (W/m²) (0–1500): ", 0.0, 1500.0);
        double amb = promptDouble("Average ambient temp (°C) (-10–60): ", -10.0, 60.0);
        boolean rained = promptYesNo("Did it rain? (y/n): ");
        System.out.print("Notes (optional): ");
        String notes = sc.nextLine().trim();

        readings.add(new Reading(date, kwh, ins, irr, amb, rained, notes));
        storage.saveReadings(readings);
        System.out.println("✔ Reading logged. Total readings: " + readings.size());
    }

    private static void generateReport() {
        if (installation == null) { System.out.println("Please setup an installation first (option 1)."); return; }
        if (readings.isEmpty())   { System.out.println("No readings yet. Log some first (option 2)."); return; }

        Report r = new Auditor().analyse(installation, readings);

        System.out.println();
        System.out.println("--- Performance Report (" + r.getDaysAnalysed() + " days) ---");
        System.out.println("System: " + installation.summary());
        System.out.printf("Expected total : %.1f kWh (%.2f kWh/day avg)%n",
                r.getExpectedKWhTotal(), r.getExpectedKWhTotal() / r.getDaysAnalysed());
        System.out.printf("Actual total   : %.1f kWh (%.2f kWh/day avg)%n",
                r.getActualKWhTotal(), r.getActualKWhTotal() / r.getDaysAnalysed());
        System.out.printf("Deviation      : %.1f%%%n", r.getDeviationPct());
        System.out.printf("Avg ambient    : %.1f °C%n", r.getAvgAmbientC());
        System.out.println("Days since rain: " + r.getDaysSinceRain());
        System.out.println();
        System.out.println("Diagnosis (ranked):");
        r.getDiagnoses().stream()
                .sorted(Comparator.comparingInt(Diagnosis::getSeverity).reversed())
                .forEach(d -> System.out.printf("  • [%s] %s%n", d.getLabel(), d.getExplanation()));
        System.out.println();

        double lostKWh = r.getExpectedKWhTotal() - r.getActualKWhTotal();
        if (lostKWh > 0) {
            System.out.printf("Estimated loss over period: %.1f kWh  ≈ ₹%.0f at ₹%.2f/unit%n",
                    lostKWh, r.getMoneyLostINR(), installation.getTariffPerKWh());
        } else {
            System.out.printf("No shortfall detected (surplus of %.1f kWh).%n", -lostKWh);
        }
    }

    private static void viewInstallation() {
        if (installation == null) { System.out.println("No installation configured."); return; }
        System.out.println("Owner    : " + installation.getOwner());
        System.out.println("System   : " + installation.summary());
        System.out.println("Tariff   : ₹" + installation.getTariffPerKWh() + "/kWh");
        System.out.println("Readings : " + readings.size());
    }

    private static void saveAll() {
        try {
            if (installation != null) storage.saveInstallation(installation);
            storage.saveReadings(readings);
        } catch (IOException e) {
            System.out.println("Save warning: " + e.getMessage());
        }
    }
}