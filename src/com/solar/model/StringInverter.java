package com.solar.model;

public class StringInverter extends Inverter {
    public StringInverter(String model, double ratedKW) {
        super(model, ratedKW, 0.96);
    }

    @Override
    public double deliverEnergy(double dcKWh) {
        return dcKWh * efficiency;
    }

    @Override
    public double clippingLoss(double peakDCW) {
        double ac = peakDCW * efficiency;
        return Math.max(0, ac - ratedKW * 1000.0);
    }
}