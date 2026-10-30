package edu.campus.findit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class TestRunner {

    public static void main(String[] args) {
        System.out.println("=================================================");
        System.out.println("  FindIt v2.0 Automated Test Suite Execution");
        System.out.println("=================================================");

        List<Class<?>> testClasses = List.of(
                edu.campus.findit.service.MatchingServiceTest.class,
                edu.campus.findit.service.ClaimServiceTest.class
        );

        int totalTests = 0;
        int passedTests = 0;
        int failedTests = 0;
        List<String> failures = new ArrayList<>();

        for (Class<?> testClass : testClasses) {
            System.out.println("\nRunning Test Class: " + testClass.getSimpleName());
            System.out.println("-------------------------------------------------");

            List<Method> beforeMethods = new ArrayList<>();
            List<Method> testMethods = new ArrayList<>();

            for (Method m : testClass.getDeclaredMethods()) {
                if (m.isAnnotationPresent(BeforeEach.class)) {
                    beforeMethods.add(m);
                }
                if (m.isAnnotationPresent(Test.class)) {
                    testMethods.add(m);
                }
            }

            for (Method testMethod : testMethods) {
                totalTests++;
                String displayName = testMethod.getName();
                if (testMethod.isAnnotationPresent(DisplayName.class)) {
                    displayName = testMethod.getAnnotation(DisplayName.class).value();
                }

                try {
                    Object testInstance = testClass.getDeclaredConstructor().newInstance();

                    // If mockito extension is simulated, initialize mocks
                    for (java.lang.reflect.Field field : testClass.getDeclaredFields()) {
                        if (field.isAnnotationPresent(org.mockito.Mock.class)) {
                            field.setAccessible(true);
                            field.set(testInstance, org.mockito.Mockito.mock(field.getType()));
                        }
                    }

                    // Execute @BeforeEach
                    for (Method b : beforeMethods) {
                        b.setAccessible(true);
                        b.invoke(testInstance);
                    }

                    // Execute @Test
                    testMethod.setAccessible(true);
                    testMethod.invoke(testInstance);

                    passedTests++;
                    System.out.println("  [PASS] " + displayName);
                } catch (Exception e) {
                    failedTests++;
                    Throwable cause = e.getCause() != null ? e.getCause() : e;
                    String failMsg = testClass.getSimpleName() + "." + testMethod.getName() + " -> " + cause.getMessage();
                    failures.add(failMsg);
                    System.err.println("  [FAIL] " + displayName);
                    cause.printStackTrace(System.err);
                }
            }
        }

        System.out.println("\n=================================================");
        System.out.println("  TEST SUMMARY:");
        System.out.println("  Total Executed : " + totalTests);
        System.out.println("  Passed         : " + passedTests);
        System.out.println("  Failed         : " + failedTests);
        System.out.println("=================================================");

        if (failedTests > 0) {
            System.err.println("\nFailed tests details:");
            failures.forEach(f -> System.err.println("  - " + f));
            System.exit(1);
        } else {
            System.out.println("\nALL TESTS PASSED SUCCESSFULLY!");
            System.exit(0);
        }
    }
}
