package com.solar.test;

import com.solar.model.Reading;
import java.time.LocalDate;

public class ReadingTest {

    public void testCSVRoundTrip() {
        Reading r = new Reading(LocalDate.of(2026, 9, 10), 11.2, 5.0, 880, 29, false, "clear sky");
        Reading back = Reading.fromCSV(r.toCSV());
        Assert.equals(r.getDate(), back.getDate(), "date round-trips");
        Assert.approx(r.getActualKWh(), back.getActualKWh(), 0.0001, "kWh round-trips");
        Assert.approx(r.getInsolationKWhM2(), back.getInsolationKWhM2(), 0.0001, "insolation round-trips");
        Assert.approx(r.getAmbientC(), back.getAmbientC(), 0.0001, "ambient round-trips");
        Assert.equals(r.isRained(), back.isRained(), "rained flag round-trips");
        Assert.equals(r.getNotes(), back.getNotes(), "notes round-trip");
    }

    public void testRainedFlagParsing() {
        Reading r1 = Reading.fromCSV("2026-09-10,11.2,5.0,880,29,1,monsoon");
        Assert.isTrue(r1.isRained(), "rained flag should parse as true");
        Reading r2 = Reading.fromCSV("2026-09-10,11.2,5.0,880,29,0,");
        Assert.isTrue(!r2.isRained(), "rained flag should parse as false");
    }

    public void testCommasInNotesAreSanitised() {
        Reading r = new Reading(LocalDate.of(2026, 9, 10), 11.2, 5.0, 880, 29, false, "a,b,c");
        Reading back = Reading.fromCSV(r.toCSV());
        Assert.equals("a;b;c", back.getNotes(), "commas become semicolons");
    }
}