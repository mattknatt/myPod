package org.example;

import javafx.application.Application;

public class App {
    public static void main(String[] args) {
        LoggerManager.setup();
        Application.launch(MyPod.class, args);
    }
}
