package com.solar.io;

import com.solar.model.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class Storage {
    private final Path dataDir;

    public Storage(Path dataDir) {
        this.dataDir = dataDir;
    }

    public Path installationFile() { return dataDir.resolve("installation.csv"); }
    public Path readingsFile()     { return dataDir.resolve("readings.csv"); }

    public void saveInstallation(Installation inst) throws IOException {
        Files.createDirectories(dataDir);
        List<String> lines = new ArrayList<>();
        lines.add("owner,panelType,panelModel,panelCount,panelW,inverterType,inverterModel,inverterKW,tilt,orientation,commissioned,tariff");
        SolarPanel first = inst.getPanels().get(0);
        String line = String.join(",",
            inst.getOwner(),
            first.getClass().getSimpleName(),
            first.getModel(),
            String.valueOf(inst.getPanels().size()),
            String.valueOf(first.capacityW()),
            inst.getInverter().getClass().getSimpleName(),
            inst.getInverter().getModel(),
            String.valueOf(inst.getInverter().getRatedKW()),
            String.valueOf(inst.getTiltDeg()),
            inst.getOrientation(),
            inst.getCommissionedOn().toString(),
            String.valueOf(inst.getTariffPerKWh())
        );
        lines.add(line);
        Files.write(installationFile(), lines);
    }

    public Installation loadInstallation() throws IOException {
        if (!Files.exists(installationFile())) return null;
        List<String> lines = Files.readAllLines(installationFile());
        if (lines.size() < 2) return null;
        String[] p = lines.get(1).split(",", -1);
        String owner = p[0];
        String panelType = p[1];
        String panelModel = p[2];
        int panelCount = Integer.parseInt(p[3]);
        double panelW = Double.parseDouble(p[4]);
        String invType = p[5];
        String invModel = p[6];
        double invKW = Double.parseDouble(p[7]);
        int tilt = Integer.parseInt(p[8]);
        String orientation = p[9];
        LocalDate commissioned = LocalDate.parse(p[10]);
        double tariff = Double.parseDouble(p[11]);

        List<SolarPanel> panels = new ArrayList<>();
        for (int i = 0; i < panelCount; i++) {
            panels.add(createPanel(panelType, panelModel, panelW));
        }
        Inverter inv = createInverter(invType, invModel, invKW);
        return new Installation(owner, panels, inv, tilt, orientation, commissioned, tariff);
    }

    private SolarPanel createPanel(String type, String model, double w) {
        switch (type) {
            case "Polycrystalline": return new Polycrystalline(model, w);
            case "ThinFilm":        return new ThinFilm(model, w);
            default:                return new Monocrystalline(model, w);
        }
    }

    private Inverter createInverter(String type, String model, double kw) {
        switch (type) {
            case "MicroInverter": return new MicroInverter(model, kw);
            default:              return new StringInverter(model, kw);
        }
    }

    public void saveReadings(List<Reading> readings) throws IOException {
        Files.createDirectories(dataDir);
        List<String> lines = new ArrayList<>();
        lines.add("date,actualKWh,insolationKWhM2,peakIrradianceWm2,ambientC,rained,notes");
        for (Reading r : readings) lines.add(r.toCSV());
        Files.write(readingsFile(), lines);
    }

    public List<Reading> loadReadings() throws IOException {
        List<Reading> out = new ArrayList<>();
        if (!Files.exists(readingsFile())) return out;
        List<String> lines = Files.readAllLines(readingsFile());
        for (int i = 1; i < lines.size(); i++) {
            if (lines.get(i).isBlank()) continue;
            out.add(Reading.fromCSV(lines.get(i)));
        }
        return out;
    }
}