package com.solar.model;

public class MicroInverter extends Inverter {
    public MicroInverter(String model, double ratedKW) {
        super(model, ratedKW, 0.955);
    }

    @Override
    public double deliverEnergy(double dcKWh) {
        return dcKWh * efficiency;
    }

    @Override
    public double clippingLoss(double peakDCW) {
        return 0.0; // per-panel MPPT, no central clipping
    }
}