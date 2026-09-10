package com.solar.service;

public class Diagnosis {
    private final String label;
    private final String explanation;
    private final int severity; // 1 = low, 2 = medium, 3 = high

    public Diagnosis(String label, String explanation, int severity) {
        this.label = label;
        this.explanation = explanation;
        this.severity = severity;
    }

    public String getLabel()       { return label; }
    public String getExplanation() { return explanation; }
    public int getSeverity()       { return severity; }
}