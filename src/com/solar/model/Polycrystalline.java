package com.solar.model;

public class Polycrystalline extends SolarPanel {
    public Polycrystalline(String model, double capacityW) {
        super(model, capacityW, 0.0040, 0.007, 27.0);
    }

    @Override
    public double dailyOutputKWh(double insolationKWhM2, double ambientC, int yearsInstalled) {
        double cellTemp = ambientC + dayHeatDelta;
        double tempLoss = Math.max(0.5, 1.0 - tempCoeff * (cellTemp - 25.0));
        double degrade  = Math.pow(1 - degradationPerYear, yearsInstalled);
        return (capacityW / 1000.0) * insolationKWhM2 * tempLoss * degrade;
    }
}