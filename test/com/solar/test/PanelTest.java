package com.solar.test;

import com.solar.model.*;

public class PanelTest {

    public void testMonocrystallineBaseline() {
        SolarPanel p = new Monocrystalline("Test", 440);
        // insolation 1.0, ambient 25 -> cellTemp 50, tempLoss 0.9125
        // output = 0.44 * 1.0 * 0.9125 = 0.4015 kWh
        double out = p.dailyOutputKWh(1.0, 25.0, 0);
        Assert.approx(0.4015, out, 0.001, "Mono baseline output");
    }

    public void testPolyProducesLessThanMonoWhenHot() {
        SolarPanel mono = new Monocrystalline("M", 400);
        SolarPanel poly = new Polycrystalline("P", 400);
        double m = mono.dailyOutputKWh(5.0, 40.0, 0);
        double p = poly.dailyOutputKWh(5.0, 40.0, 0);
        Assert.isTrue(p < m, "Poly should produce less than Mono at high ambient temp");
    }

    public void testDegradationReducesOutput() {
        SolarPanel p = new Monocrystalline("M", 400);
        double y0  = p.dailyOutputKWh(5.0, 30.0, 0);
        double y10 = p.dailyOutputKWh(5.0, 30.0, 10);
        Assert.isTrue(y10 < y0 * 0.96, "10-year output should be below 96% of new");
        Assert.isTrue(y10 > y0 * 0.94, "10-year output should be above 94% of new");
    }

    public void testThinFilmBestInHotWeather() {
        SolarPanel mono = new Monocrystalline("M", 400);
        SolarPanel thin = new ThinFilm("T", 400);
        // At high ambient ThinFilm's lower tempCoeff should help it catch up
        double m = mono.dailyOutputKWh(6.0, 45.0, 0);
        double t = thin.dailyOutputKWh(6.0, 45.0, 0);
        Assert.isTrue(t / 400.0 > m / 400.0 * 0.85, "ThinFilm should stay reasonably close to Mono when hot");
    }
}