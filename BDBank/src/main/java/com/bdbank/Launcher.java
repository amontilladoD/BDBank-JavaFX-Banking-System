package com.bdbank;

/**
 * Why this class exists:
 * When the JVM is started with a main class that DIRECTLY extends javafx.application.Application
 * (like App), and JavaFX is on the plain classpath (not the module path), Java refuses to start
 * with: "Error: JavaFX runtime components are missing, and are required to run this application".
 *
 * The standard fix is to launch through a separate class that does NOT extend Application.
 * This Launcher class is what you should set as the "Main class" in your Run Configuration
 * (and in Project Structure -> Artifacts, if you build a runnable JAR).
 */
public class Launcher {
    public static void main(String[] args) {
        App.main(args);
    }
}
