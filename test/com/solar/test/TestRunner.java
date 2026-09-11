package com.solar.test;

import java.lang.reflect.Method;
import java.util.List;

public class TestRunner {
    public static void main(String[] args) {
        List<Class<?>> suites = List.of(
                PanelTest.class,
                InverterTest.class,
                AuditorTest.class,
                ReadingTest.class
        );

        int passed = 0, failed = 0;
        for (Class<?> c : suites) {
            System.out.println("\n>>> " + c.getSimpleName());
            for (Method m : c.getDeclaredMethods()) {
                if (!m.getName().startsWith("test")) continue;
                try {
                    Object inst = c.getDeclaredConstructor().newInstance();
                    m.setAccessible(true);
                    m.invoke(inst);
                    System.out.println("  [PASS] " + m.getName());
                    passed++;
                } catch (Exception e) {
                    Throwable cause = e.getCause() == null ? e : e.getCause();
                    System.out.println("  [FAIL] " + m.getName() + " — " + cause.getMessage());
                    failed++;
                }
            }
        }

        System.out.println();
        System.out.printf("Passed: %d, Failed: %d%n", passed, failed);
        if (failed > 0) System.exit(1);
    }
}