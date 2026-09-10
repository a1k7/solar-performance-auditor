package com.solar.service;

import com.solar.model.Installation;
import com.solar.model.Reading;

import java.time.LocalDate;
import java.time.Period;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

public class Auditor {

    public Report analyse(Installation inst, List<Reading> history) {
        if (history == null || history.isEmpty()) return Report.empty();

        double expectedTotal = 0, actualTotal = 0, ambientSum = 0;
        int clippedDays = 0;
        LocalDate lastRain = null;
        double[] devs = new double[history.size()];
        int i = 0;

        for (Reading r : history) {
            double exp = inst.expectedKWh(r);
            expectedTotal += exp;
            actualTotal   += r.getActualKWh();
            ambientSum    += r.getAmbientC();
            devs[i++] = r.getActualKWh() - exp;

            if (r.isRained() && (lastRain == null || r.getDate().isAfter(lastRain))) {
                lastRain = r.getDate();
            }
            double peakDC = inst.peakDCW(r.getPeakIrradianceWm2(), r.getAmbientC(), r.getDate());
            if (inst.getInverter().clippingLoss(peakDC) > 50.0) clippedDays++;
        }

        double deviationPct = expectedTotal == 0 ? 0
                : ((actualTotal - expectedTotal) / expectedTotal) * 100.0;
        double avgAmbient = ambientSum / history.size();
        LocalDate lastDate = history.get(history.size() - 1).getDate();

        int daysSinceRain = lastRain == null
                ? (int) ChronoUnit.DAYS.between(history.get(0).getDate(), lastDate) + 1
                : (int) ChronoUnit.DAYS.between(lastRain, lastDate);

        double gap = -deviationPct; // positive => underperforming
        List<Diagnosis> diags = new ArrayList<>();

        if (Math.abs(deviationPct) < 3.0) {
            diags.add(new Diagnosis("Healthy",
                    "System is performing within expected range.", 1));
        } else if (deviationPct > 15.0) {
            // Actual far EXCEEDS expected — physically suspicious
            diags.add(new Diagnosis("Reading Anomaly",
                    String.format("Actual generation is %.1f%% ABOVE expected. "
                                    + "Check for incorrect units, wrong tariff, or sensor fault.",
                            deviationPct), 3));
        } else {
            if (gap >= 8 && daysSinceRain >= 10) {
                diags.add(new Diagnosis("Dust / Soiling",
                        String.format("Persistent %.1f%% shortfall with no rain in %d days. Panels likely dusty.",
                                gap, daysSinceRain), 3));
            }
            if (avgAmbient > 33) {
                diags.add(new Diagnosis("Temperature Derating",
                        String.format("Average ambient %.1f°C — noticeable temperature losses expected.",
                                avgAmbient), 2));
            }
            if (clippedDays >= Math.max(3, history.size() / 5)) {
                diags.add(new Diagnosis("Inverter Clipping",
                        String.format("%d of %d days clipped at inverter rated power. "
                                        + "Consider larger inverter or re-check DC/AC ratio.",
                                clippedDays, history.size()), 2));
            }
            int yrs = Period.between(inst.getCommissionedOn(), LocalDate.now()).getYears();
            if (yrs >= 3 && gap >= 3 && gap < 12) {
                diags.add(new Diagnosis("Gradual Degradation",
                        String.format("System is %d years old; a %.1f%% drop is consistent with normal degradation.",
                                yrs, gap), 1));
            }

            // variance check for shading
            double mean = 0;
            for (double d : devs) mean += d;
            mean /= devs.length;
            double var = 0;
            for (double d : devs) var += (d - mean) * (d - mean);
            var /= devs.length;
            double std = Math.sqrt(var);
            if (std > 1.2 && gap >= 5) {
                diags.add(new Diagnosis("Possible Shading",
                        String.format("Day-to-day deviations vary widely (σ=%.2f). Partial shading may be at play.",
                                std), 2));
            }
            if (diags.isEmpty()) {
                diags.add(new Diagnosis("Unclear",
                        "Deviation detected but no specific cause identified with the current readings.", 1));
            }
        }

        double lostKWh = Math.max(0, expectedTotal - actualTotal);
        double moneyLost = lostKWh * inst.getTariffPerKWh();

        return new Report(history.size(), expectedTotal, actualTotal, deviationPct,
                moneyLost, avgAmbient, daysSinceRain, diags);
    }
}