package com.solar.model;

import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.List;

public class Installation {
    private final String owner;
    private final List<SolarPanel> panels;
    private final Inverter inverter;
    private final int tiltDeg;
    private final String orientation;
    private final LocalDate commissionedOn;
    private final double tariffPerKWh;

    public Installation(String owner, List<SolarPanel> panels, Inverter inverter,
                        int tiltDeg, String orientation, LocalDate commissionedOn,
                        double tariffPerKWh) {
        this.owner = owner;
        this.panels = new ArrayList<>(panels);
        this.inverter = inverter;
        this.tiltDeg = tiltDeg;
        this.orientation = orientation;
        this.commissionedOn = commissionedOn;
        this.tariffPerKWh = tariffPerKWh;
    }

    public double expectedKWh(Reading r) {
        int yrs = Math.max(0, Period.between(commissionedOn, r.getDate()).getYears());
        double dcKWh = 0;
        for (SolarPanel p : panels) {                 // <-- polymorphism
            dcKWh += p.dailyOutputKWh(r.getInsolationKWhM2(), r.getAmbientC(), yrs);
        }
        dcKWh *= orientationFactor();
        return inverter.deliverEnergy(dcKWh);         // <-- polymorphism
    }

    public double peakDCW(double peakIrr, double ambient, LocalDate onDate) {
        int yrs = Math.max(0, Period.between(commissionedOn, onDate).getYears());
        double dc = 0;
        for (SolarPanel p : panels) {
            dc += p.peakDCW(peakIrr, ambient, yrs);
        }
        return dc * orientationFactor();
    }

    private double orientationFactor() {
        switch (orientation.toLowerCase()) {
            case "south":       return 1.00;
            case "south-west":  return 0.97;
            case "south-east":  return 0.97;
            case "west":        return 0.90;
            case "east":        return 0.90;
            case "north":       return 0.75;
            default:            return 0.95;
        }
    }

    public String getOwner()              { return owner; }
    public List<SolarPanel> getPanels()   { return panels; }
    public Inverter getInverter()         { return inverter; }
    public int getTiltDeg()               { return tiltDeg; }
    public String getOrientation()        { return orientation; }
    public LocalDate getCommissionedOn()  { return commissionedOn; }
    public double getTariffPerKWh()       { return tariffPerKWh; }

    public double totalCapacityKW() {
        double sum = 0;
        for (SolarPanel p : panels) sum += p.capacityW();
        return sum / 1000.0;
    }

    public String summary() {
        return String.format("%.2f kWp (%d panels) + %s (%.1f kW) | %s, tilt %d°, commissioned %s",
            totalCapacityKW(), panels.size(), inverter.getModel(), inverter.getRatedKW(),
            orientation, tiltDeg, commissionedOn);
    }
}