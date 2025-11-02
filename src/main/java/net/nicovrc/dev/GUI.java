package net.nicovrc.dev;

import com.amihaiemil.eoyaml.Yaml;
import com.amihaiemil.eoyaml.YamlMapping;
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
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GUI extends Application {

    private static final SimpleDateFormat file_sdf = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
    private static final SimpleDateFormat log_sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    private static final String configText = """
                # VRChat ログフォルダパス (VRChat log folder path)
                logfolder: ''
                # デバッグログを表示するか (Enable debug log display?)
                debugOutput: true
                # 過去のログから取得して表示するか (Display data from previous logs?)
                oldLogCheck: true
                # 動画プレーヤーのログを表示するか (Enable video player log display?)
                VideoPlayer: true
                # ImageDownloaderのログを表示するか (Enable ImageDownloader log display?)
                ImageDownloader: true
                # StringDownloaderのログを表示するか (Enable StringDownloader log display?)
                StringDownloader: true
                """;
    private static final ConfigData config = new ConfigData();

    private static final Timer timer1 = new Timer();
    private static final Timer timer2 = new Timer();

    private static final HashMap<String, LogData> logDataList = new HashMap<>();
    private static final ObservableList<String> items = FXCollections.observableArrayList();
    private static final ListView<String> listView = new ListView<>(items);

    private static final Pattern matcher_version = Pattern.compile("<id>tag:github\\.com,2008:Repository/(\\d+)/(.+)</id>");

    private static String new_version = Function.Version;

    private static boolean isGUI = true;

    public GUI(boolean isGUI){
        this.isGUI = isGUI;
    }

    @Override
    public void start(Stage stage) throws Exception {

        System.out.println("[Info] VRCVideoLogViewer Ver " + Function.Version + "起動");
        final boolean isWindowsBatchStart = new File("./tools").exists() && new File("./tools/jdk-21.0.2").exists();

        File file = new File("./tools/openjdk-21.0.2_windows-x64_bin.zip");
        if (file.exists()){
            file.delete();
        }
        file = new File("./tools/openjfx-21.0.9_windows-x64_bin-sdk.zip");
        if (file.exists()){
            file.delete();
        }

        if (!Function.ntSystem.getName().isEmpty()){
            file = new File("./tools/ImageMagick-7.1.2-8-portable-Q16-x64.7z");
            file.delete();
        }

        System.out.println("[Info] アップデート確認");

        try (HttpClient client = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_2)
                .followRedirects(HttpClient.Redirect.ALWAYS)
                .connectTimeout(Duration.ofSeconds(5))
                .build()) {

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(new URI("https://github.com/nicovrc-net/VRCVideoLogViewer/releases.atom"))
                    .headers("User-Agent", Function.UserAgent)
                    .headers("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
                    .headers("Accept-Language", "ja,en;q=0.7,en-US;q=0.3")
                    .GET()
                    .build();
            HttpResponse<String> send = client.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            //System.out.println(send.body());
            Matcher matcher = matcher_version.matcher(send.body());
            if (matcher.find()){
                new_version = matcher.group(2);
            }
        } catch (Exception e){
            e.printStackTrace();
            timer1.cancel();
            timer2.cancel();
            stage.close();
            stop();
            return;
        }

        boolean isUpdate;
        try {

            file = new File("./");
            final String CurrentFolderPass = file.getCanonicalPath().replaceAll("\\\\", "/");

            file = new File("./tools/VRCVideoLogViewer.zip");
            if (file.exists()){
                file.delete();
            }
            file = new File("./tools/VRCVideoLogViewer");
            if (file.exists()){
                for (File listFile : file.listFiles()) {
                    listFile.delete();
                }
                file.delete();
            }

            isUpdate = !Function.Version.equals(new_version);

            if (isUpdate){
                System.out.println("[Info] アップデートが見つかりました。");
                if (isWindowsBatchStart || !Function.ntSystem.getName().isEmpty()){
                    File update_file = new File("./tools/update1.bat");
                    if (update_file.exists()){
                        update_file.delete();
                    }
                    update_file = new File("./tools/update2.bat");
                    if (update_file.exists()){
                        update_file.delete();
                    }

                    FileWriter file1 = new FileWriter("./tools/update1.bat");
                    PrintWriter pw = new PrintWriter(new BufferedWriter(file1));
                    pw.print("start ./tools/update2.bat".replaceAll("\\./", CurrentFolderPass+"/"));
                    pw.close();
                    file1.close();
                    pw = null;
                    file1 = null;

                    file1 = new FileWriter("./tools/update2.bat");
                    pw = new PrintWriter(new BufferedWriter(file1));
                    String str = """
                        curl https://github.com/nicovrc-net/VRCVideoLogViewer/releases/download/#ver#/VRCVideoLogViewer.zip -L --output ./tools/VRCVideoLogViewer.zip
                        tar -xf ./tools/VRCVideoLogViewer.zip -C ./tools\\
                        del ./VRCVideoLogViewer-1.0-SNAPSHOT-all.jar
                        del ./start.bat
                        move ./tools\\VRCVideoLogViewer-1.0-SNAPSHOT-all.jar ./
                        move ./tools\\start.bat ./
                        exit
                        """;
                    pw.print(str.replaceAll("#ver#", new_version).replaceAll("\\./", CurrentFolderPass.replaceAll("/", "\\\\\\\\")+"\\\\"));
                    pw.close();
                    file1.close();
                    pw = null;
                    file1 = null;
                }
            } else {
                System.out.println("[Info] アップデートはありませんでした。 (現在: "+Function.Version+" 最新:"+new_version+")");
            }
        } catch (Exception e){
            e.printStackTrace();
            timer1.cancel();
            timer2.cancel();
            stage.close();
            stop();
            return;
        }

        System.out.println("[Info] config.yml 存在チェック");
        file = new File("./config.yml");

        if (!file.exists()){
            System.out.println("[Info] config.yml 自動生成します");

            try {
                FileWriter file1 = new FileWriter("./config.yml");
                PrintWriter pw = new PrintWriter(new BufferedWriter(file1));
                pw.print(configText);
                pw.close();
                file1.close();
                pw = null;
                file1 = null;
            } catch (Exception e){
                e.printStackTrace();
            }
        }

        try {
            final YamlMapping yamlMapping = Yaml.createYamlInput(new File("./config.yml")).readYamlMapping();
            config.setLogFolderPass(yamlMapping.string("logfolder"));
            config.setDebugOutput(yamlMapping.bool("debugOutput"));
            config.setOldLogCheck(yamlMapping.bool("oldLogCheck"));
            config.setVideoPlayer(yamlMapping.bool("VideoPlayer"));
            config.setImageDownloader(yamlMapping.bool("ImageDownloader"));
            config.setStringDownloader(yamlMapping.bool("StringDownloader"));
        } catch (Exception e){
            // e.printStackTrace();

        }

        if (config.getLogFolderPass().isEmpty()){

            if (Function.ntSystem.getName().isEmpty()){
                //UnixSystem unixSystem = new UnixSystem();

            } else {
                if (config.isDebugOutput()){
                    System.out.println("[Info] ログフォルダの自動取得");
                }

                file = new File("C:\\Users\\"+Function.ntSystem.getName()+"\\AppData\\LocalLow\\VRChat\\VRChat");
                if (file.exists()){
                    config.setLogFolderPass("C:\\Users\\"+Function.ntSystem.getName()+"\\AppData\\LocalLow\\VRChat\\VRChat");

                    if (config.isDebugOutput()){
                        System.out.println("[Info] 自動取得成功 : " + "C:\\Users\\"+Function.ntSystem.getName()+"\\AppData\\LocalLow\\VRChat\\VRChat");
                    }
                } else {
                    System.out.println("[Info] 自動取得失敗");
                }
            }
        }

        if (config.isDebugOutput()){
            System.out.println("[Info] フォルダチェック");
        }
        if (config.getLogFolderPass() != null){
            file = new File(config.getLogFolderPass());
            if (!file.exists()){
                System.out.println("フォルダが見つかりませんでした。\nFolder not found.");
                timer1.cancel();
                timer2.cancel();
                stage.close();
                stop();
                return;
            }
        } else {
            System.out.println("設定ファイルが正しく設定されていません。\nThe configuration file is not set up correctly.");
            timer1.cancel();
            timer2.cancel();
            stage.close();
            stop();
            return;
        }

        List<String> logFileList = new ArrayList<>();
        try {
            for (File f : file.listFiles()){
                if (f.getName().startsWith("output_log_")){
                    logFileList.add(f.getName());
                }
            }
        } catch (Exception e){
            // e.printStackTrace();
        }
        if (logFileList.isEmpty()){
            System.out.println("ログファイルが見つかりませんでした。\nLog file not found.");
            timer1.cancel();
            timer2.cancel();
            stage.close();
            stop();
            return;
        }

        if (config.isDebugOutput()){
            System.out.println("[Info] ログファイルの並び替え");
        }
        if (logFileList.size() > 1){
            try {

                List<String> temp = new ArrayList<>();

                String[] temp1 = new String[logFileList.size()];
                long[] temp2 = new long[logFileList.size()];
                int i = 0;
                for (String s : logFileList) {
                    Date date = file_sdf.parse(s.replaceAll("output_log_", "").replaceAll("\\.txt", ""));
                    temp1[i] = s;
                    temp2[i] = date.getTime();
                    i++;
                }

                boolean isMove = true;
                String te1;
                long te2;

                while (isMove){
                    isMove = false;
                    for (i = 0; i < temp2.length; i++){
                        if (i + 1 < temp2.length){
                            if (temp2[i] >= temp2[i + 1]){
                                isMove = true;
                                te1 = temp1[i];
                                te2 = temp2[i];

                                temp1[i] = temp1[i + 1];
                                temp2[i] = temp2[i + 1];
                                temp1[i + 1] = te1;
                                temp2[i + 1] = te2;
                            }
                        }
                    }
                }

                for (i = 0; i < temp1.length; i++){
                    temp.add(temp1[i]);
                }
                logFileList = temp;

            } catch (Exception e){
                //e.printStackTrace();
                if (config.isDebugOutput()){
                    System.out.println("[Error] 並び替えに失敗");
                }
            }
        }

        final LogData lastLogData = new LogData();
        lastLogData.setLogDate(new Date());

        if (config.isOldLogCheck()){
            if (config.isDebugOutput()){
                System.out.println("[Info] 抽出開始");
            }

            for (String s : logFileList) {
                file = new File(config.getLogFolderPass() + "\\" + s);

                String text = Function.getTextForFile(file);
                try {
                    List<LogData> log = Function.getLogForURL(text);
                    for (LogData logData : log) {
                        lastLogData.setLogDate(logData.getLogDate());
                        lastLogData.setURL(logData.getURL());
                        lastLogData.setErrorMessage(logData.getErrorMessage());
                        lastLogData.setURLType(logData.getURLType());

                        //LogData.add(logData);
                        logDataList.put(("["+log_sdf.format(logData.getLogDate())+"] " + logData.getURL() + " ("+logData.getURLType()+")"), logData);
                        Platform.runLater(() -> {
                            items.add(("["+log_sdf.format(logData.getLogDate())+"] " + logData.getURL() + " ("+logData.getURLType()+")"));
                            listView.refresh();
                            listView.scrollTo(items.size());
                        });

                    }

                } catch (Exception e){
                    //e.printStackTrace();
                    if (config.isDebugOutput()){
                        System.out.println("[Error] ログファイル読み込みに失敗");
                        System.out.println("filename : " + file.getName());
                        e.printStackTrace();
                    }
                }
            }
        }


        if (config.isDebugOutput()){
            System.out.println(log_sdf.format(lastLogData.getLogDate()));
            System.out.println("[Info] リアルタイム取得開始します...");
        }

        final String[] temp_lastLogFile = {config.getLogFolderPass() + "\\" + logFileList.getLast()};

        timer1.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                List<String> logFileList = new ArrayList<>();
                try {
                    File file = new File(config.getLogFolderPass());
                    for (File f : file.listFiles()){
                        if (f.getName().startsWith("output_log_")){
                            logFileList.add(f.getName());
                        }
                    }

                    if (logFileList.size() > 1){
                        List<String> temp = new ArrayList<>();

                        String[] temp1 = new String[logFileList.size()];
                        long[] temp2 = new long[logFileList.size()];
                        int i = 0;
                        for (String s : logFileList) {
                            Date date = file_sdf.parse(s.replaceAll("output_log_", "").replaceAll("\\.txt", ""));
                            temp1[i] = s;
                            temp2[i] = date.getTime();
                            i++;
                        }

                        boolean isMove = true;
                        String te1;
                        long te2;

                        while (isMove){
                            isMove = false;
                            for (i = 0; i < temp2.length; i++){
                                if (i + 1 < temp2.length){
                                    if (temp2[i] >= temp2[i + 1]){
                                        isMove = true;
                                        te1 = temp1[i];
                                        te2 = temp2[i];

                                        temp1[i] = temp1[i + 1];
                                        temp2[i] = temp2[i + 1];
                                        temp1[i + 1] = te1;
                                        temp2[i + 1] = te2;
                                    }
                                }
                            }
                        }

                        for (i = 0; i < temp1.length; i++){
                            temp.add(temp1[i]);
                        }
                        logFileList = temp;
                    }

                    temp_lastLogFile[0] = config.getLogFolderPass() + "\\" + logFileList.getLast();
                } catch (Exception e){
                    timer1.cancel();
                    timer2.cancel();
                    try {
                        stop();
                        stage.close();
                    } catch (Exception ex) {
                        // ex.printStackTrace();
                    }
                }
            }
        }, 0L, 1000L);

        timer2.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                final String lastLogFile = temp_lastLogFile[0];

                try {
                    File f = new File(lastLogFile);
                    for (LogData logData : Function.getLogForURL(Function.getTextForFile(f))){
                        String s = "[" + log_sdf.format(logData.getLogDate()) + "] " + logData.getURL() + " (" + logData.getURLType() + ")";
                        if (logData.getLogDate().getTime() >= lastLogData.getLogDate().getTime() && logDataList.get(s) == null){

                            lastLogData.setLogDate(logData.getLogDate());
                            lastLogData.setURL(logData.getURL());
                            lastLogData.setErrorMessage(logData.getErrorMessage());
                            lastLogData.setURLType(logData.getURLType());

                            logDataList.put(s, logData);
                            Platform.runLater(() -> {
                                items.add(s);
                                listView.refresh();
                                listView.scrollTo(items.size());
                            });

                        }
                    }
                } catch (Exception e){
                    timer1.cancel();
                    timer2.cancel();
                    try {
                        stop();
                        stage.close();
                    } catch (Exception ex) {
                        // ex.printStackTrace();
                    }
                }
            }
        }, 0L, 1000L);

        if (!isGUI){
            return;
        }

        if (config.isDebugOutput()){
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
                        LogData data = logDataList.get(selectedItem);
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

                        Label label1_2_1 = new Label(log_sdf.format(data.getLogDate()));
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
        if (isUpdate){
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

            Label update_label4 = new Label("最新のバージョン : " + new_version);
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

        if (config.isDebugOutput()){
            System.out.println("[Info] GUI組み立て完了！");
        }
        if (config.isDebugOutput()){
            System.out.println("[Info] GUI表示！");
        }
        stage.show();
        if (isUpdate){
            stage1.show();
        }
        Thread.ofVirtual().start(()->{
            while (stage.isShowing()){
                try {
                    Thread.sleep(1000L);
                } catch (Exception e){
                    //e.printStackTrace();
                }
            }
            timer1.cancel();
            timer2.cancel();
            try {
                stop();
            } catch (Exception e) {
                //e.printStackTrace();
            }
        });
    }
}
