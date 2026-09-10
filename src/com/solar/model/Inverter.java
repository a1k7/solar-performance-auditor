package com.solar.model;

public abstract class Inverter {
    protected final String model;
    protected final double ratedKW;
    protected final double efficiency;

    protected Inverter(String model, double ratedKW, double efficiency) {
        this.model = model;
        this.ratedKW = ratedKW;
        this.efficiency = efficiency;
    }

    /** Daily AC energy after conversion losses. */
    public abstract double deliverEnergy(double dcKWh);

    /** Estimated peak-power clipping loss in W (>0 if clipping). */
    public abstract double clippingLoss(double peakDCW);

    public String getModel() { return model; }
    public double getRatedKW() { return ratedKW; }
    public double getEfficiency() { return efficiency; }
}