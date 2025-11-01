package net.nicovrc.dev;

import javafx.application.Platform;
import javafx.stage.Stage;

public class Main {

    public static void main(String[] args) {
        if (args.length >= 1){
            System.out.println("test");
        }


        Platform.startup(() -> {
            try {
                GUI gui = new GUI(true);
                gui.start(new Stage());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

}