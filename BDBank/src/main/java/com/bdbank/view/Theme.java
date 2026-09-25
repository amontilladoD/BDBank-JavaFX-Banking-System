package com.bdbank.view;

import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

/** Central place for the blue & white visual identity so every screen looks consistent. */
public class Theme {
    public static final String NAVY = "#0d47a1";
    public static final String BLUE = "#1565c0";
    public static final String LIGHT_BLUE = "#e3f2fd";
    public static final String ACCENT_BLUE = "#1976d2";
    public static final String WHITE = "#ffffff";
    public static final String TEXT_DARK = "#0d1b2a";
    public static final String DANGER = "#c62828";
    public static final String SUCCESS = "#2e7d32";

    public static Label h1(String text) {
        Label l = new Label(text);
        l.setFont(Font.font("Segoe UI", FontWeight.BOLD, 24));
        l.setTextFill(Color.web(NAVY));
        return l;
    }

    public static Label h2(String text) {
        Label l = new Label(text);
        l.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));
        l.setTextFill(Color.web(NAVY));
        return l;
    }

    public static Label muted(String text) {
        Label l = new Label(text);
        l.setFont(Font.font("Segoe UI", 12));
        l.setTextFill(Color.web("#546e7a"));
        return l;
    }

    public static Button primaryButton(String text) {
        Button b = new Button(text);
        b.setStyle("-fx-background-color: " + ACCENT_BLUE + "; -fx-text-fill: white; -fx-font-weight: bold; " +
                "-fx-background-radius: 6; -fx-padding: 8 20 8 20; -fx-cursor: hand;");
        return b;
    }

    public static Button secondaryButton(String text) {
        Button b = new Button(text);
        b.setStyle("-fx-background-color: white; -fx-text-fill: " + ACCENT_BLUE + "; -fx-font-weight: bold; " +
                "-fx-border-color: " + ACCENT_BLUE + "; -fx-border-radius: 6; -fx-background-radius: 6; " +
                "-fx-padding: 8 20 8 20; -fx-cursor: hand;");
        return b;
    }

    public static Button dangerButton(String text) {
        Button b = new Button(text);
        b.setStyle("-fx-background-color: " + DANGER + "; -fx-text-fill: white; -fx-font-weight: bold; " +
                "-fx-background-radius: 6; -fx-padding: 6 16 6 16; -fx-cursor: hand;");
        return b;
    }

    public static Button successButton(String text) {
        Button b = new Button(text);
        b.setStyle("-fx-background-color: " + SUCCESS + "; -fx-text-fill: white; -fx-font-weight: bold; " +
                "-fx-background-radius: 6; -fx-padding: 6 16 6 16; -fx-cursor: hand;");
        return b;
    }

    public static void card(Region r) {
        r.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-effect: dropshadow(gaussian, rgba(13,71,161,0.15), 10, 0, 0, 2);");
        r.setPadding(new Insets(20));
    }

    public static void sidebarButtonStyle(Button b, boolean active) {
        String bg = active ? ACCENT_BLUE : "transparent";
        String fg = active ? "white" : LIGHT_BLUE;
        b.setStyle("-fx-background-color: " + bg + "; -fx-text-fill: " + fg + "; -fx-font-size: 13px; " +
                "-fx-alignment: CENTER_LEFT; -fx-padding: 10 16 10 16; -fx-background-radius: 6; -fx-cursor: hand; -fx-max-width: infinity;");
    }
}
