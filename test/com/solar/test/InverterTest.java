package com.solar.test;

import com.solar.model.*;

public class InverterTest {

    public void testStringInverterClipsAboveRated() {
        Inverter inv = new StringInverter("Test", 3.0);
        // 4000 W DC * 0.96 = 3840 W AC, rated 3000 W -> 840 W clip
        Assert.approx(840.0, inv.clippingLoss(4000.0), 1.0, "String clipping loss");
    }

    public void testStringInverterNoClipBelow() {
        Inverter inv = new StringInverter("Test", 5.0);
        Assert.approx(0.0, inv.clippingLoss(2000.0), 0.001, "No clip under rated");
    }

    public void testMicroInverterNeverClips() {
        Inverter inv = new MicroInverter("Test", 0.4);
        Assert.approx(0.0, inv.clippingLoss(10000.0), 0.001, "Micro never clips");
    }

    public void testStringDeliversWithEfficiency() {
        Inverter inv = new StringInverter("Test", 5.0);
        Assert.approx(1.92, inv.deliverEnergy(2.0), 0.001, "String efficiency 96%");
    }
}