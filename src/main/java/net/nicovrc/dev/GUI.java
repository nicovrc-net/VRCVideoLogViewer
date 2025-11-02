package net.nicovrc.dev;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import java.io.*;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.*;

public class GUI extends Application {

    private static boolean isGUI = true;

    public GUI(boolean isGUI){
        this.isGUI = isGUI;
    }

    @Override
    public void start(Stage stage) throws Exception {

        if (!isGUI){
            return;
        }

        final ObservableList<String> items = FXCollections.observableArrayList();
        final ListView<String> listView = new ListView<>(items);
        final LogData lastLogData = new LogData();
        lastLogData.setLogDate(new Date());

        if (Function.config.isOldLogCheck()){
            if (Function.config.isDebugOutput()){
                System.out.println("[Info] 抽出開始");
            }

            for (String s : Function.logFileList) {
                File file = new File(Function.config.getLogFolderPass() + "\\" + s);

                String text = Function.getTextForFile(file);
                try {
                    List<LogData> log = Function.getLogForURL(text);
                    for (LogData logData : log) {
                        lastLogData.setLogDate(logData.getLogDate());
                        lastLogData.setURL(logData.getURL());
                        lastLogData.setErrorMessage(logData.getErrorMessage());
                        lastLogData.setURLType(logData.getURLType());

                        //LogData.add(logData);
                        final String str = "["+Function.log_sdf.format(logData.getLogDate())+"] " + logData.getURL() + " ("+logData.getURLType()+")";
                        Function.logDataList.put(str, logData);
                        items.add(str);
                        listView.refresh();
                        listView.scrollTo(items.size());
                    }

                } catch (Exception e){
                    //e.printStackTrace();
                    if (Function.config.isDebugOutput()){
                        System.out.println("[Error] ログファイル読み込みに失敗");
                        System.out.println("filename : " + file.getName());
                        e.printStackTrace();
                    }
                }
            }
        }

        if (Function.config.isDebugOutput()){
            //System.out.println(Function.log_sdf.format(lastLogData.getLogDate()));
            System.out.println("[Info] リアルタイム取得開始します...");
        }

        Function.timer2.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                final String lastLogFile = Function.temp_lastLogFile[0];

                try {
                    File f = new File(lastLogFile);
                    for (LogData logData : Function.getLogForURL(Function.getTextForFile(f))){
                        String s = "[" + Function.log_sdf.format(logData.getLogDate()) + "] " + logData.getURL() + " (" + logData.getURLType() + ")";
                        if (logData.getLogDate().getTime() >= lastLogData.getLogDate().getTime() && Function.logDataList.get(s) == null){

                            lastLogData.setLogDate(logData.getLogDate());
                            lastLogData.setURL(logData.getURL());
                            lastLogData.setErrorMessage(logData.getErrorMessage());
                            lastLogData.setURLType(logData.getURLType());

                            Function.logDataList.put(s, logData);
                            Platform.runLater(() -> {
                                items.add(s);
                                listView.refresh();
                                listView.scrollTo(items.size());
                            });

                        }
                    }
                } catch (Exception e){
                    Function.timer1.cancel();
                    Function.timer2.cancel();
                    Function.isTimerRun = false;
                }
            }
        }, 0L, 1000L);


        if (Function.config.isDebugOutput()){
            System.out.println("[Info] GUI組み立て中...");
        }
        AnchorPane root = new AnchorPane();
        Label label = new Label("VRCVideoLogViewer");
        label.setLayoutX(15);
        label.setLayoutY(15);
        label.setFont(new Font(24));
        root.getChildren().add(label);

        listView.setEditable(false);
        listView.setPrefSize(1200, 600);
        listView.setLayoutX(15);
        listView.setLayoutY(55);
        listView.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                final Stage stage1 = new Stage();
                Thread.ofVirtual().start(()->{
                    //System.out.println(listView.getItems().size() + " / " + value);
                    String selectedItem = listView.getSelectionModel().getSelectedItem();
                    if (selectedItem != null) {
                        LogData data = Function.logDataList.get(selectedItem);
                        if (data == null){
                            return;
                        }

                        Platform.runLater(()->{
                            stage1.setResizable(false);
                            stage1.setMaximized(false);
                            stage1.setFullScreen(false);
                            stage1.setTitle("詳細");
                            stage1.setWidth(800);
                            stage1.setHeight(800);
                        });

                        AnchorPane root1 = new AnchorPane();
                        Scene scene1 = new Scene(root1);

                        Label label1 = new Label("詳細");
                        label1.setLayoutX(5);
                        label1.setLayoutY(5);
                        label1.setFont(new Font(16));
                        root1.getChildren().add(label1);

                        Label label1_2 = new Label("Date");
                        label1_2.setLayoutX(10);
                        label1_2.setLayoutY(40);
                        root1.getChildren().add(label1_2);

                        Label label1_2_1 = new Label(Function.log_sdf.format(data.getLogDate()));
                        label1_2_1.setLayoutX(10);
                        label1_2_1.setLayoutY(60);
                        root1.getChildren().add(label1_2_1);

                        Label label1_3 = new Label("URL");
                        label1_3.setLayoutX(10);
                        label1_3.setLayoutY(80);
                        root1.getChildren().add(label1_3);

                        TextField field2 = new TextField();
                        field2.setLayoutX(10);
                        field2.setLayoutY(100);
                        field2.setEditable(false);
                        field2.setFocusTraversable(false);
                        field2.setText(data.getURL());
                        field2.setPrefWidth(700);
                        root1.getChildren().add(field2);
                        //Platform.runLater(()-> );

                        Label label1_4 = new Label("種類");
                        label1_4.setLayoutX(10);
                        label1_4.setLayoutY(130);
                        root1.getChildren().add(label1_4);

                        Label label1_4_1 = new Label(data.getURLType().equals("Video") ? "動画(Video)" : data.getURLType().equals("String") ? "テキスト(String)" : "画像(Image)");
                        label1_4_1.setLayoutX(10);
                        label1_4_1.setLayoutY(150);
                        root1.getChildren().add(label1_4_1);

                        Label label1_5 = new Label("エラーメッセージ");
                        label1_5.setLayoutX(10);
                        label1_5.setLayoutY(170);
                        root1.getChildren().add(label1_5);

                        TextArea textArea = new TextArea();
                        textArea.setLayoutX(10);
                        textArea.setLayoutY(190);
                        textArea.setText(data.getErrorMessage());
                        textArea.setPrefSize(700, 150);
                        textArea.setEditable(false);
                        textArea.setWrapText(false);
                        //Platform.runLater(()-> );
                        root1.getChildren().add(textArea);

                        Button button = new Button("閉じる");
                        button.setLayoutX(650);
                        button.setLayoutY(10);
                        button.setOnAction(e -> stage1.close());
                        root1.getChildren().add(button);

                        Label label1_6 = new Label("Now Loading...");
                        label1_6.setLayoutX(10);
                        label1_6.setLayoutY(360);
                        root1.getChildren().add(label1_6);

                        Platform.runLater(()->{
                            stage1.setScene(scene1);
                            stage1.show();
                        });
                        switch (data.getURLType()) {
                            case "Video" -> {
                                //System.out.println("debug 0");
                                VideoData videoData = Function.getVideoData(data.getURL());
                                //System.out.println("debug 1");
                                Image fxImage = videoData.getThumbnail() != null ? new Image(new ByteArrayInputStream(videoData.getThumbnail())) : null;
                                //System.out.println("debug 2");

                                label1_6.setText("タイトル");

                                TextField field3 = new TextField();
                                field3.setLayoutX(10);
                                field3.setLayoutY(380);
                                field3.setEditable(false);
                                field3.setFocusTraversable(false);
                                field3.setText(videoData.getVideoTitle());
                                field3.setPrefWidth(700);
                                Platform.runLater(()->root1.getChildren().add(field3));

                                if (fxImage != null) {
                                    //System.out.println("debug 3");

                                    ImageView imageView = new ImageView(fxImage);
                                    imageView.setLayoutX(10);
                                    imageView.setLayoutY(420);
                                    imageView.setFitHeight(300);
                                    imageView.setPreserveRatio(true);
                                    Platform.runLater(()->root1.getChildren().add(imageView));
                                }

                            }
                            case "Image" -> {
                                Image fxImage = null;
                                try (HttpClient client = HttpClient.newBuilder()
                                        .version(HttpClient.Version.HTTP_2)
                                        .followRedirects(HttpClient.Redirect.NORMAL)
                                        .connectTimeout(Duration.ofSeconds(5))
                                        .build()) {

                                    HttpRequest request = HttpRequest.newBuilder()
                                            .uri(new URI(data.getURL()))
                                            //.uri(new URI("https://i2i.nicovrc.net/?url=https://nicovrc.net/VRChat_2024-08-16_03-59-02.141_3840x2160.png"))
                                            .headers("User-Agent", Function.UserAgent)
                                            .headers("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
                                            .headers("Accept-Language", "ja,en;q=0.7,en-US;q=0.3")
                                            .GET()
                                            .build();

                                    HttpResponse<byte[]> send = client.send(request, HttpResponse.BodyHandlers.ofByteArray());

                                    byte[] input = send.body();
                                    if (send.headers().firstValue("content-type").isPresent() && send.headers().firstValue("content-type").get().endsWith("webp")){
                                        input = Function.Webp2PngConverter(input);
                                    }

                                    fxImage = new Image(new ByteArrayInputStream(input));
                                } catch (Exception e) {
                                    // e.printStackTrace();
                                }

                                Platform.runLater(()->root1.getChildren().remove(label1_6));
                                if (fxImage != null) {

                                    ImageView imageView = new ImageView(fxImage);
                                    imageView.setLayoutX(10);
                                    imageView.setLayoutY(360);
                                    imageView.setFitHeight(350);
                                    imageView.setPreserveRatio(true);
                                    Platform.runLater(()->root1.getChildren().add(imageView));
                                }

                            }
                            case "String" -> {

                                String str = null;

                                try (HttpClient client = HttpClient.newBuilder()
                                        .version(HttpClient.Version.HTTP_2)
                                        .followRedirects(HttpClient.Redirect.NORMAL)
                                        .connectTimeout(Duration.ofSeconds(5))
                                        .build()) {

                                    HttpRequest request = HttpRequest.newBuilder()
                                            .uri(new URI(data.getURL()))
                                            //.uri(new URI("https://i2i.nicovrc.net/?url=https://nicovrc.net/VRChat_2024-08-16_03-59-02.141_3840x2160.png"))
                                            .headers("User-Agent", Function.Unity_UserAgent)
                                            .headers("Accept", "*/*")
                                            .headers("x-unity-version", Function.HTTP_x_unity_version)
                                            .GET()
                                            .build();

                                    HttpResponse<String> send = client.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
                                    str = send.body();

                                } catch (Exception e) {
                                    // e.printStackTrace();
                                }

                                Platform.runLater(()->root1.getChildren().remove(label1_6));
                                if (str != null) {

                                    TextArea textArea2 = new TextArea();
                                    textArea2.setLayoutX(10);
                                    textArea2.setLayoutY(360);
                                    textArea2.setText(str);
                                    textArea2.setPrefSize(700, 300);
                                    textArea2.setEditable(false);
                                    textArea2.setWrapText(false);
                                    Platform.runLater(()-> root1.getChildren().add(textArea2));
                                }
                            }
                        }
                    }
                });
            }
        });
        //listView.scrollTo(items.size() - 1);
        root.getChildren().add(listView);

        Scene scene = new Scene(root);
        stage.setTitle("VRCVideoLogViewer Ver " + Function.Version);
        stage.setWidth(1280);
        stage.setHeight(720);
        stage.setFullScreen(false);
        stage.setMaximized(false);
        stage.setResizable(false);


        stage.setScene(scene);

        Stage stage1 = new Stage();
        if (Function.isUpdate){
            // アップデート通知
            stage1.setResizable(false);
            stage1.setMaximized(false);
            stage1.setFullScreen(false);
            stage1.setTitle("アップデートのお知らせ");
            stage1.setWidth(400);
            stage1.setHeight(200);

            AnchorPane root1 = new AnchorPane();
            Scene scene1 = new Scene(root1);

            Button button = new Button("閉じる");
            button.setLayoutX(300);
            button.setLayoutY(10);
            button.setOnAction(e -> stage1.close());
            root1.getChildren().add(button);

            Label update_label1 = new Label("アップデートのお知らせ");
            update_label1.setLayoutX(5);
            update_label1.setLayoutY(5);
            update_label1.setFont(new Font(16));
            root1.getChildren().add(update_label1);

            Label update_label2 = new Label("アップデートがあります。");
            update_label2.setLayoutX(10);
            update_label2.setLayoutY(40);
            root1.getChildren().add(update_label2);

            Label update_label3 = new Label("現在のバージョン : " + Function.Version);
            update_label3.setLayoutX(10);
            update_label3.setLayoutY(80);
            root1.getChildren().add(update_label3);

            Label update_label4 = new Label("最新のバージョン : " + Function.new_version);
            update_label4.setLayoutX(10);
            update_label4.setLayoutY(100);
            root1.getChildren().add(update_label4);

            if (!Function.ntSystem.getName().isEmpty()){
                // Windowsの場合のアップデートバッチ用のボタン
                Button update_button = new Button("アップデート");
                update_button.setLayoutX(10);
                update_button.setLayoutY(120);
                update_button.setOnAction(e -> {
                    try {
                        final Runtime runtime = Runtime.getRuntime();
                        final Process exec0 = runtime.exec(new String[]{"./tools/update1.bat"});
                        Thread.ofVirtual().start(() -> {
                            try {
                                Thread.sleep(5000L);
                            } catch (Exception ex) {
                                //ex.printStackTrace();
                            }

                            if (exec0.isAlive()) {
                                exec0.destroy();
                            }
                        });
                        exec0.waitFor();
                    } catch (Exception ex){
                        // ex.printStackTrace();
                    }
                    stage1.close();
                    stage.close();
                });
                root1.getChildren().add(update_button);
            }

            stage1.setScene(scene1);
        }

        if (Function.config.isDebugOutput()){
            System.out.println("[Info] GUI組み立て完了！");
        }
        if (Function.config.isDebugOutput()){
            System.out.println("[Info] GUI表示！");
        }
        stage.show();
        if (Function.isUpdate){
            stage1.show();
        }
        Thread.ofVirtual().start(()->{
            while (stage.isShowing() && Function.isTimerRun){
                try {
                    Thread.sleep(1000L);
                } catch (Exception e){
                    //e.printStackTrace();
                }
            }
            Function.timer1.cancel();
            Function.timer2.cancel();
            Function.isTimerRun = false;
            try {
                stop();
            } catch (Exception e) {
                //e.printStackTrace();
            }
        });
    }
}
