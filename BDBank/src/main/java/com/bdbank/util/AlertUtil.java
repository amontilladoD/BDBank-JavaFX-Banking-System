package com.bdbank.util;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Alert.AlertType;

import java.util.Optional;

public class AlertUtil {

    public static void info(String title, String msg) {
        runOnFx(() -> build(AlertType.INFORMATION, title, msg).showAndWait());
    }

    public static void error(String title, String msg) {
        runOnFx(() -> build(AlertType.ERROR, title, msg).showAndWait());
    }

    public static boolean confirm(String title, String msg) {
        Alert a = build(AlertType.CONFIRMATION, title, msg);
        Optional<ButtonType> res = a.showAndWait();
        return res.isPresent() && res.get() == ButtonType.OK;
    }

    private static Alert build(AlertType type, String title, String msg) {
        Alert a = new Alert(type);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(msg);
        return a;
    }

    private static void runOnFx(Runnable r) {
        if (Platform.isFxApplicationThread()) r.run();
        else Platform.runLater(r);
    }
}
