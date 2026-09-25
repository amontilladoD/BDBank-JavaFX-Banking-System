package com.bdbank;

import com.bdbank.service.AdminBankingService;
import com.bdbank.service.ConfigService;
import com.bdbank.util.ExecutorServiceManager;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class App extends Application {

    public static Stage primaryStage;

    @Override
    public void start(Stage stage) throws Exception {
        primaryStage = stage;
        stage.setTitle("BD Bank - Banking Management System");

        // Global handler so an unexpected exception never silently kills a background thread.
        Thread.setDefaultUncaughtExceptionHandler((t, e) -> {
            System.err.println("[Uncaught] on " + t.getName() + ": " + e.getMessage());
            e.printStackTrace();
        });

        Parent splash = FXMLLoader.load(getClass().getResource("/com/bdbank/fxml/Splash.fxml"));
        Scene scene = new Scene(splash, 900, 600);
        scene.getStylesheets().add(getClass().getResource("/com/bdbank/css/style.css").toExternalForm());
        stage.setScene(scene);
        stage.show();

        // Kick off background jobs that should run for the whole application lifetime.
        ConfigService.get().startDollarRateTicker();
        AdminBankingService.get().startInterestAccrualJob();
    }

    @Override
    public void stop() {
        ExecutorServiceManager.get().shutdown();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
