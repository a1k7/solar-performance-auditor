package com.solar.model;

import java.time.LocalDate;

public class Reading {
    private final LocalDate date;
    private final double actualKWh;
    private final double insolationKWhM2;
    private final double peakIrradianceWm2;
    private final double ambientC;
    private final boolean rained;
    private final String notes;

    public Reading(LocalDate date, double actualKWh, double insolationKWhM2,
                   double peakIrradianceWm2, double ambientC,
                   boolean rained, String notes) {
        this.date = date;
        this.actualKWh = actualKWh;
        this.insolationKWhM2 = insolationKWhM2;
        this.peakIrradianceWm2 = peakIrradianceWm2;
        this.ambientC = ambientC;
        this.rained = rained;
        this.notes = notes == null ? "" : notes;
    }

    public LocalDate getDate()            { return date; }
    public double getActualKWh()          { return actualKWh; }
    public double getInsolationKWhM2()    { return insolationKWhM2; }
    public double getPeakIrradianceWm2()  { return peakIrradianceWm2; }
    public double getAmbientC()           { return ambientC; }
    public boolean isRained()             { return rained; }
    public String getNotes()              { return notes; }

    public String toCSV() {
        return String.join(",",
            date.toString(),
            String.valueOf(actualKWh),
            String.valueOf(insolationKWhM2),
            String.valueOf(peakIrradianceWm2),
            String.valueOf(ambientC),
            rained ? "1" : "0",
            notes.replace(",", ";")
        );
    }

    public static Reading fromCSV(String line) {
        String[] p = line.split(",", -1);
        return new Reading(
            LocalDate.parse(p[0]),
            Double.parseDouble(p[1]),
            Double.parseDouble(p[2]),
            Double.parseDouble(p[3]),
            Double.parseDouble(p[4]),
            "1".equals(p[5]),
            p.length > 6 ? p[6] : ""
        );
    }
}