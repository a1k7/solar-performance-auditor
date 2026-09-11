package com.solar.test;

import com.solar.model.*;
import com.solar.service.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class AuditorTest {

    private Installation demoInstall() {
        List<SolarPanel> panels = new ArrayList<>();
        for (int i = 0; i < 7; i++) panels.add(new Monocrystalline("Mono-440", 440));
        Inverter inv = new StringInverter("StringInv-3K", 3.0);
        return new Installation("Test", panels, inv, 15, "South",
                LocalDate.of(2022, 5, 15), 8.0);
    }

    public void testHealthyWhenCloseToExpected() {
        Installation inst = demoInstall();
        List<Reading> hist = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            LocalDate d = LocalDate.of(2026, 9, 1).plusDays(i);
            Reading baseline = new Reading(d, 0, 5.0, 880, 28, false, "");
            double expected = inst.expectedKWh(baseline);
            hist.add(new Reading(d, expected, 5.0, 880, 28, false, ""));
        }
        Report rep = new Auditor().analyse(inst, hist);
        Assert.isTrue(Math.abs(rep.getDeviationPct()) < 1.0, "Deviation should be near zero");
        Assert.equals("Healthy", rep.getDiagnoses().get(0).getLabel(), "Top diagnosis");
    }

    public void testReadingAnomalyWhenWayOver() {
        Installation inst = demoInstall();
        List<Reading> hist = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            LocalDate d = LocalDate.of(2026, 9, 1).plusDays(i);
            hist.add(new Reading(d, 100.0, 5.0, 880, 28, false, ""));
        }
        Report rep = new Auditor().analyse(inst, hist);
        Assert.isTrue(rep.getDeviationPct() > 100, "Deviation should be huge");
        Assert.equals("Reading Anomaly", rep.getDiagnoses().get(0).getLabel(), "Top diagnosis");
    }

    public void testDustDiagnosisWhenShortAndDry() {
        Installation inst = demoInstall();
        List<Reading> hist = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            LocalDate d = LocalDate.of(2026, 8, 15).plusDays(i);
            Reading baseline = new Reading(d, 0, 5.0, 880, 28, false, "");
            double expected = inst.expectedKWh(baseline);
            hist.add(new Reading(d, expected * 0.80, 5.0, 880, 28, false, ""));
        }
        Report rep = new Auditor().analyse(inst, hist);
        boolean found = rep.getDiagnoses().stream()
                .anyMatch(x -> x.getLabel().equals("Dust / Soiling"));
        Assert.isTrue(found, "Should flag Dust / Soiling");
    }

    public void testEmptyHistoryReturnsEmptyReport() {
        Report rep = new Auditor().analyse(demoInstall(), new ArrayList<>());
        Assert.equals(0, rep.getDaysAnalysed(), "Empty history");
    }
}