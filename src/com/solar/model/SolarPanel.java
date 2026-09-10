package com.solar.model;

public abstract class SolarPanel {
    protected final String model;
    protected final double capacityW;          // rated watts at STC
    protected final double tempCoeff;          // fractional power loss per °C above 25°C
    protected final double degradationPerYear; // fractional loss per year
    protected final double dayHeatDelta;       // avg cell-to-ambient delta over a full day

    protected SolarPanel(String model, double capacityW, double tempCoeff,
                         double degradationPerYear, double dayHeatDelta) {
        this.model = model;
        this.capacityW = capacityW;
        this.tempCoeff = tempCoeff;
        this.degradationPerYear = degradationPerYear;
        this.dayHeatDelta = dayHeatDelta;
    }

    /** Daily DC energy in kWh for given daily insolation (kWh/m²/day). */
    public abstract double dailyOutputKWh(double insolationKWhM2, double ambientC, int yearsInstalled);

    /** Peak instantaneous DC power in W at given peak irradiance. */
    public double peakDCW(double peakIrradianceWm2, double ambientC, int yearsInstalled) {
        double cellTemp = ambientC + (peakIrradianceWm2 / 800.0) * 30.0;
        double tempLoss = Math.max(0.5, 1.0 - tempCoeff * (cellTemp - 25.0));
        double degrade  = Math.pow(1 - degradationPerYear, yearsInstalled);
        return capacityW * (peakIrradianceWm2 / 1000.0) * tempLoss * degrade;
    }

    public double capacityW() { return capacityW; }
    public String getModel()  { return model; }
    public double getTempCoeff() { return tempCoeff; }
    public double getDegradationPerYear() { return degradationPerYear; }

    @Override public String toString() {
        return String.format("%s (%.0f W)", model, capacityW);
    }
}