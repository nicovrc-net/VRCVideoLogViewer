package net.nicovrc.dev;

import com.amihaiemil.eoyaml.Yaml;
import com.amihaiemil.eoyaml.YamlMapping;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import java.awt.*;
import java.io.*;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.*;
import java.util.List;

public class Main extends Application {

    private static SimpleDateFormat file_sdf = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
    private static SimpleDateFormat log_sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    private static final ConfigData config = new ConfigData();

    private static final Timer timer1 = new Timer();
    private static final Timer timer2 = new Timer();

    private static HashMap<String, LogData> logDataList = new HashMap<>();
    private static ObservableList<String> items = FXCollections.observableArrayList();
    private static ListView<String> listView = new ListView<>(items);

    public static void main(String[] args) {

        System.out.println("[Info] VRCVideoLogViewer Ver " + Function.Version + "起動");

        File file = new File("./config.yml");

        final String configText = """
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

        System.out.println("[Info] config.yml 存在チェック");
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

        if (config.isDebugOutput()){
            System.out.println("[Info] フォルダチェック");
        }
        if (config.getLogFolderPass() != null){
            file = new File(config.getLogFolderPass());
            if (!file.exists()){
                System.out.println("フォルダが見つかりませんでした。\nFolder not found.");
                return;
            }
        } else {
            System.out.println("設定ファイルが正しく設定されていません。\nThe configuration file is not set up correctly.");
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
                String te1 = "";
                long te2 = -1;

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
                        String te1 = "";
                        long te2 = -1;

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
                }
            }
        }, 0L, 1000L);

        try {
            launch();
        } catch (Exception e){
            e.printStackTrace();
            timer1.cancel();
            timer2.cancel();
        }

        //timer.cancel();

    }

    @Override
    public void start(Stage stage) throws Exception {
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
                //System.out.println(listView.getItems().size() + " / " + value);
                String selectedItem = listView.getSelectionModel().getSelectedItem();
                if (selectedItem != null) {
                    LogData data = logDataList.get(selectedItem);
                    if (data == null){
                        return;
                    }
                    Stage stage1 = new Stage();
                    stage1.setResizable(false);
                    stage1.setMaximized(false);
                    stage1.setFullScreen(false);
                    stage1.setTitle("詳細");
                    stage1.setWidth(800);
                    stage1.setHeight(800);

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
                    root1.getChildren().add(textArea);

                    if (data.getURLType().equals("Image")){
                        Image fxImage = null;
                        try (HttpClient client = HttpClient.newBuilder()
                                .version(HttpClient.Version.HTTP_2)
                                .followRedirects(HttpClient.Redirect.NORMAL)
                                .connectTimeout(Duration.ofSeconds(5))
                                .build()){

                            HttpRequest request = HttpRequest.newBuilder()
                                    .uri(new URI("https://i2i.nicovrc.net/?url="+data.getURL().replaceAll("https://i2i\\.nicovrc\\.net/\\?url=", "")))
                                    //.uri(new URI("https://i2i.nicovrc.net/?url=https://nicovrc.net/VRChat_2024-08-16_03-59-02.141_3840x2160.png"))
                                    .headers("User-Agent", Function.UserAgent)
                                    .headers("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
                                    .headers("Accept-Language", "ja,en;q=0.7,en-US;q=0.3")
                                    .GET()
                                    .build();

                            HttpResponse<byte[]> send = client.send(request, HttpResponse.BodyHandlers.ofByteArray());
                            fxImage = new Image(new ByteArrayInputStream(send.body()));
                        } catch (Exception e){
                            // e.printStackTrace();
                        }

                        if (fxImage == null){
                            return;
                        }

                        ImageView imageView = new ImageView(fxImage);
                        imageView.setLayoutX(10);
                        imageView.setLayoutY(360);
                        imageView.setFitHeight(350);
                        imageView.setPreserveRatio(true);
                        root1.getChildren().add(imageView);
                    }

                    if (data.getURLType().equals("String")){

                        String str = null;

                        try (HttpClient client = HttpClient.newBuilder()
                                .version(HttpClient.Version.HTTP_2)
                                .followRedirects(HttpClient.Redirect.NORMAL)
                                .connectTimeout(Duration.ofSeconds(5))
                                .build()){

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

                        } catch (Exception e){
                            // e.printStackTrace();
                        }

                        if (str != null){

                            TextArea textArea2 = new TextArea();
                            textArea2.setLayoutX(10);
                            textArea2.setLayoutY(360);
                            textArea2.setText(str);
                            textArea2.setPrefSize(700, 300);
                            textArea2.setEditable(false);
                            textArea2.setWrapText(false);
                            root1.getChildren().add(textArea2);

                        }
                    }

                    javafx.scene.control.Button button = new javafx.scene.control.Button("閉じる");
                    button.setLayoutX(650);
                    button.setLayoutY(10);
                    button.setOnAction(e -> {
                        stage1.close();
                    });
                    root1.getChildren().add(button);


                    stage1.setScene(scene1);
                    stage1.show();
                }
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
        if (config.isDebugOutput()){
            System.out.println("[Info] GUI組み立て完了！");
        }
        if (config.isDebugOutput()){
            System.out.println("[Info] GUI表示！");
        }
        stage.show();
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
        });
    }
}