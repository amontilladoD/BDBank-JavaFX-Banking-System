package com.bdbank.controller;

import javafx.animation.PauseTransition;
import javafx.fxml.FXML;
import javafx.util.Duration;

public class SplashController {

    @FXML
    private void initialize() {
        PauseTransition pause = new PauseTransition(Duration.seconds(3));
        pause.setOnFinished(e -> LoginController.showLoginScreen(com.bdbank.App.primaryStage));
        pause.play();
    }
}
