package com.solar.service;

import java.util.Collections;
import java.util.List;

public class Report {
    private final int daysAnalysed;
    private final double expectedKWhTotal;
    private final double actualKWhTotal;
    private final double deviationPct;
    private final double moneyLostINR;
    private final double avgAmbientC;
    private final int daysSinceRain;
    private final List<Diagnosis> diagnoses;

    public Report(int daysAnalysed, double expectedKWhTotal, double actualKWhTotal,
                  double deviationPct, double moneyLostINR, double avgAmbientC,
                  int daysSinceRain, List<Diagnosis> diagnoses) {
        this.daysAnalysed = daysAnalysed;
        this.expectedKWhTotal = expectedKWhTotal;
        this.actualKWhTotal = actualKWhTotal;
        this.deviationPct = deviationPct;
        this.moneyLostINR = moneyLostINR;
        this.avgAmbientC = avgAmbientC;
        this.daysSinceRain = daysSinceRain;
        this.diagnoses = diagnoses;
    }

    public static Report empty() {
        return new Report(0, 0, 0, 0, 0, 0, 0, Collections.emptyList());
    }

    public int getDaysAnalysed()          { return daysAnalysed; }
    public double getExpectedKWhTotal()   { return expectedKWhTotal; }
    public double getActualKWhTotal()     { return actualKWhTotal; }
    public double getDeviationPct()       { return deviationPct; }
    public double getMoneyLostINR()       { return moneyLostINR; }
    public double getAvgAmbientC()        { return avgAmbientC; }
    public int getDaysSinceRain()         { return daysSinceRain; }
    public List<Diagnosis> getDiagnoses() { return diagnoses; }
}