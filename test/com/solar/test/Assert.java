package com.solar.test;

public class Assert {
    public static void isTrue(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
    }

    public static void approx(double expected, double actual, double tol, String msg) {
        if (Math.abs(expected - actual) > tol)
            throw new AssertionError(msg + " — expected " + expected + " ± " + tol + ", got " + actual);
    }

    public static void equals(Object expected, Object actual, String msg) {
        if (expected == null ? actual != null : !expected.equals(actual))
            throw new AssertionError(msg + " — expected " + expected + ", got " + actual);
    }
}