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
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import net.nicovrc.dev.data.ConfigData;

import java.io.*;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.*;
import java.util.regex.Matcher;

public class GUI extends Application {

    private static boolean isGUI = true;

    public GUI(boolean isGUI){
        this.isGUI = isGUI;
    }

    @Override
    public void start(Stage stage) throws Exception {

        File file = new File(Function.config.getLogFolderPass());

        boolean isFolderSet = false;
        Stage folder_stage = new Stage();
        if (!file.exists()){
            isFolderSet = true;
            try {
                AnchorPane root = new AnchorPane();

                Label label1 = new Label(Function.langData.get("setting-message2"));
                if (new File("./fonts/NotoSansCJK-Regular.ttc").exists()){
                    label1.setFont(Font.loadFont(new FileInputStream("./fonts/NotoSansCJK-Regular.ttc"), label1.getFont().getSize()));
                } else if (new File("./fonts/NotoSansCJK-Regular.ttf").exists()){
                    label1.setFont(Font.loadFont(new FileInputStream("./fonts/NotoSansCJK-Regular.ttf"), label1.getFont().getSize()));
                }               
                label1.setLayoutX(5);
                label1.setLayoutY(5);
                root.getChildren().add(label1);

                String str = Function.langData.get("setting-vrchat-logfolder").split(" \\(")[0];
                Label label2 = new Label(str);
                if (new File("./fonts/NotoSansCJK-Regular.ttc").exists()){
                    label2.setFont(Font.loadFont(new FileInputStream("./fonts/NotoSansCJK-Regular.ttc"), label2.getFont().getSize()));
                } else if (new File("./fonts/NotoSansCJK-Regular.ttf").exists()){
                    label2.setFont(Font.loadFont(new FileInputStream("./fonts/NotoSansCJK-Regular.ttf"), label2.getFont().getSize()));
                }
                label2.setLayoutX(5);
                label2.setLayoutY(25);
                root.getChildren().add(label2);

                TextField field1 = new TextField();
                field1.setLayoutX(5);
                field1.setLayoutY(45);
                field1.setEditable(true);
                field1.setFocusTraversable(false);
                field1.setText("");
                field1.setPrefWidth(400);
                root.getChildren().add(field1);

                Button button1 = new Button(Function.langData.get("open"));
                if (new File("./fonts/NotoSansCJK-Regular.ttc").exists()){
                    button1.setFont(Font.loadFont(new FileInputStream("./fonts/NotoSansCJK-Regular.ttc"), button1.getFont().getSize()));
                } else if (new File("./fonts/NotoSansCJK-Regular.ttf").exists()){
                    button1.setFont(Font.loadFont(new FileInputStream("./fonts/NotoSansCJK-Regular.ttf"), button1.getFont().getSize()));
                }
                button1.setLayoutX(410);
                button1.setLayoutY(45);
                button1.setOnAction(e->{
                    DirectoryChooser chooser = new DirectoryChooser();
                    chooser.setTitle("Select Folder");
                    chooser.setInitialDirectory(new File("./"));


                    File file2 = chooser.showDialog(folder_stage);
                    if (file2 != null) {
                        try {
                            field1.setText(file2.getCanonicalPath());
                        } catch (Exception ex){
                            //ex.printStackTrace();
                        }
                    }

                });
                root.getChildren().add(button1);

                Button button2 = new Button(Function.langData.get("setting-reflection"));
                if (new File("./fonts/NotoSansCJK-Regular.ttc").exists()){
                    button2.setFont(Font.loadFont(new FileInputStream("./fonts/NotoSansCJK-Regular.ttc"), button2.getFont().getSize()));
                } else if (new File("./fonts/NotoSansCJK-Regular.ttf").exists()){
                    button2.setFont(Font.loadFont(new FileInputStream("./fonts/NotoSansCJK-Regular.ttf"), button2.getFont().getSize()));
                }
                button2.setLayoutX(5);
                button2.setLayoutY(75);
                button2.setOnAction(e->{
                    folder_stage.close();
                });
                root.getChildren().add(button2);


                Scene scene = new Scene(root);
                folder_stage.setResizable(false);
                folder_stage.setMaximized(false);
                folder_stage.setFullScreen(false);
                folder_stage.setTitle(Function.langData.get("setting-message2").split(" \\(")[0]);
                folder_stage.setWidth(600);
                folder_stage.setHeight(200);
                folder_stage.setScene(scene);

                folder_stage.setOnHidden(e->{
                    if (new File(field1.getText()).exists()){
                        Function.config.setLogFolderPass(field1.getText());
                        Function.SettingConfig(Function.config);
                        Platform.runLater(Platform::exit);
                    } else {
                        if (folder_stage.isShowing()){
                            folder_stage.close();
                        }
                        folder_stage.show();
                    }
                });
                folder_stage.show();
            } catch (Exception e){
                e.printStackTrace();
            }
        }

        if (isFolderSet){
            Function.timer1.cancel();
            Function.timer2.cancel();
            return;
        }

        if (Function.config.isDebugOutput()){
            System.out.println("[Info] "+Function.langData.get("logfolder-check"));
        }
        if (Function.config.getLogFolderPass() != null){
            file = new File(Function.config.getLogFolderPass());
            if (!file.exists()){
                System.out.println("[Error] "+Function.langData.get("logfolder-notfound"));
                Function.timer1.cancel();
                Function.timer2.cancel();
                Platform.runLater(Platform::exit);
                return;
            }
        } else {
            System.out.println("[Error] "+Function.langData.get("logfolder-setting-fail"));
            Function.timer1.cancel();
            Function.timer2.cancel();
            Platform.runLater(Platform::exit);
            return;
        }


        try {
            Function.logFileList = Function.getFileList(Function.config.getLogFolderPass());
        } catch (Exception e){
            // e.printStackTrace();
        }
        if (Function.logFileList.isEmpty()){
            System.out.println("[Error] "+Function.langData.get("logfile-notfound"));
            Function.timer1.cancel();
            Function.timer2.cancel();
            Platform.runLater(Platform::exit);
            return;
        }

        if (Function.config.isDebugOutput()){
            System.out.println("[Info] "+Function.langData.get("logfile-sort"));
        }

        Function.timer1.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                try {
                    List<String> logFileList = Function.getFileList(Function.config.getLogFolderPass());
                    if (logFileList.size() > 1){
                        logFileList = Function.ListSort(logFileList);
                    }

                    Function.temp_lastLogFile[0] = Function.config.getLogFolderPass() + File.separator + logFileList.getLast();
                } catch (Exception e){
                    Function.timer1.cancel();
                    Function.timer2.cancel();
                    Function.isTimerRun = false;
                }
            }
        }, 0L, 1000L);

        if (!isGUI){
            Function.timer1.cancel();
            Function.timer2.cancel();
            Platform.runLater(Platform::exit);
            return;
        }

        final ObservableList<String> items = FXCollections.observableArrayList();
        final ListView<String> listView = new ListView<>(items);
        final LogData lastLogData = new LogData();
        lastLogData.setLogDate(new Date());

        if (Function.config.isOldLogCheck()){
            if (Function.config.isDebugOutput()){
                System.out.println("[Info] "+Function.langData.get("log-extraction"));
            }

            for (String s : Function.logFileList) {
                file = new File(Function.config.getLogFolderPass() + File.separator + s);

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
                        System.out.println("[Error] "+Function.langData.get("log-read-fail"));
                        System.out.println("filename : " + file.getName());
                        e.printStackTrace();
                    }
                }
            }
        }

        if (Function.config.isDebugOutput()){
            //System.out.println(Function.log_sdf.format(lastLogData.getLogDate()));
            System.out.println("[Info] "+Function.langData.get("log-realtime-read"));
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
            System.out.println("[Info] "+Function.langData.get("gui-create"));
        }
        AnchorPane root = new AnchorPane();
        Label label = new Label("VRCVideoLogViewer");
        label.setLayoutX(15);
        label.setLayoutY(15);
        if (new File("./fonts/NotoSansCJK-Regular.ttc").exists()){
            label.setFont(Font.loadFont(new FileInputStream("./fonts/NotoSansCJK-Regular.ttc"), 24));
        } else if (new File("./fonts/NotoSansCJK-Regular.ttf").exists()){
            label.setFont(Font.loadFont(new FileInputStream("./fonts/NotoSansCJK-Regular.ttf"), 24));
        } else {
            label.setFont(new Font(24));
        }
        root.getChildren().add(label);

        Button button = new Button(Function.langData.get("setting"));
        if (new File("./fonts/NotoSansCJK-Regular.ttc").exists()){
            button.setFont(Font.loadFont(new FileInputStream("./fonts/NotoSansCJK-Regular.ttc"), button.getFont().getSize()));
        } else if (new File("./fonts/NotoSansCJK-Regular.ttf").exists()){
            button.setFont(Font.loadFont(new FileInputStream("./fonts/NotoSansCJK-Regular.ttf"), button.getFont().getSize()));
        }
        button.setLayoutX(Function.ntSystem != null ? 260 : 300);
        button.setLayoutY(20);
        button.setOnAction(e->{
            final Stage setting_stage = new Stage();
            Thread.ofVirtual().start(()->{
                try {
                    AnchorPane setting_root = new AnchorPane();
                    Scene setting_scene = new Scene(setting_root);
                    Platform.runLater(()->{
                        setting_stage.setResizable(false);
                        setting_stage.setMaximized(false);
                        setting_stage.setFullScreen(false);
                        setting_stage.setTitle("設定");
                        setting_stage.setWidth(800);
                        setting_stage.setHeight(800);
                    });

                    Label setting_label1 = new Label(Function.langData.get("setting-message"));
                    if (new File("./fonts/NotoSansCJK-Regular.ttc").exists()){
                        setting_label1.setFont(Font.loadFont(new FileInputStream("./fonts/NotoSansCJK-Regular.ttc"), setting_label1.getFont().getSize()));
                    } else if (new File("./fonts/NotoSansCJK-Regular.ttf").exists()){
                        setting_label1.setFont(Font.loadFont(new FileInputStream("./fonts/NotoSansCJK-Regular.ttf"), setting_label1.getFont().getSize()));
                    }
                    setting_label1.setLayoutX(5);
                    setting_label1.setLayoutY(5);
                    setting_root.getChildren().add(setting_label1);

                    Button setting_button1 = new Button(Function.langData.get("setting-reflection"));
                    if (new File("./fonts/NotoSansCJK-Regular.ttc").exists()){
                        setting_button1.setFont(Font.loadFont(new FileInputStream("./fonts/NotoSansCJK-Regular.ttc"), setting_button1.getFont().getSize()));
                    } else if (new File("./fonts/NotoSansCJK-Regular.ttf").exists()){
                        setting_button1.setFont(Font.loadFont(new FileInputStream("./fonts/NotoSansCJK-Regular.ttf"), setting_button1.getFont().getSize()));
                    }
                    setting_button1.setLayoutX(700);
                    setting_button1.setLayoutY(5);
                    setting_button1.setOnAction(ev -> setting_stage.close());
                    setting_root.getChildren().add(setting_button1);

                    Label setting_label2 = new Label(Function.langData.get("setting-vrchat-logfolder"));
                    if (new File("./fonts/NotoSansCJK-Regular.ttc").exists()){
                        setting_label2.setFont(Font.loadFont(new FileInputStream("./fonts/NotoSansCJK-Regular.ttc"), setting_label2.getFont().getSize()));
                    } else if (new File("./fonts/NotoSansCJK-Regular.ttf").exists()){
                        setting_label2.setFont(Font.loadFont(new FileInputStream("./fonts/NotoSansCJK-Regular.ttf"), setting_label2.getFont().getSize()));
                    }
                    setting_label2.setLayoutX(5);
                    setting_label2.setLayoutY(25);
                    setting_root.getChildren().add(setting_label2);

                    TextField setting_field1 = new TextField();
                    if (new File("./fonts/NotoSansCJK-Regular.ttc").exists()){
                        setting_field1.setFont(Font.loadFont(new FileInputStream("./fonts/NotoSansCJK-Regular.ttc"), setting_field1.getFont().getSize()));
                    } else if (new File("./fonts/NotoSansCJK-Regular.ttf").exists()){
                        setting_field1.setFont(Font.loadFont(new FileInputStream("./fonts/NotoSansCJK-Regular.ttf"), setting_field1.getFont().getSize()));
                    }
                    setting_field1.setLayoutX(5);
                    setting_field1.setLayoutY(45);
                    setting_field1.setEditable(true);
                    setting_field1.setFocusTraversable(false);
                    setting_field1.setText(Function.config.getLogFolderPass());
                    setting_field1.setPrefWidth(700);
                    setting_root.getChildren().add(setting_field1);

                    CheckBox setting_checkbox1 = new CheckBox();
                    setting_checkbox1.setLayoutX(5);
                    setting_checkbox1.setLayoutY(75);
                    setting_checkbox1.setSelected(Function.config.isDebugOutput());
                    setting_root.getChildren().add(setting_checkbox1);

                    Label setting_label3 = new Label(Function.langData.get("setting-debug-print"));
                    if (new File("./fonts/NotoSansCJK-Regular.ttc").exists()){
                        setting_label3.setFont(Font.loadFont(new FileInputStream("./fonts/NotoSansCJK-Regular.ttc"), setting_label3.getFont().getSize()));
                    } else if (new File("./fonts/NotoSansCJK-Regular.ttf").exists()){
                        setting_label3.setFont(Font.loadFont(new FileInputStream("./fonts/NotoSansCJK-Regular.ttf"), setting_label3.getFont().getSize()));
                    }
                    setting_label3.setLayoutX(25);
                    setting_label3.setLayoutY(75);
                    setting_root.getChildren().add(setting_label3);

                    CheckBox setting_checkbox2 = new CheckBox();
                    setting_checkbox2.setLayoutX(5);
                    setting_checkbox2.setLayoutY(95);
                    setting_checkbox2.setSelected(Function.config.isOldLogCheck());
                    setting_root.getChildren().add(setting_checkbox2);

                    Label setting_label4 = new Label(Function.langData.get("setting-previous-log-get"));
                    if (new File("./fonts/NotoSansCJK-Regular.ttc").exists()){
                        setting_label4.setFont(Font.loadFont(new FileInputStream("./fonts/NotoSansCJK-Regular.ttc"), setting_label4.getFont().getSize()));
                    } else if (new File("./fonts/NotoSansCJK-Regular.ttf").exists()){
                        setting_label4.setFont(Font.loadFont(new FileInputStream("./fonts/NotoSansCJK-Regular.ttf"), setting_label4.getFont().getSize()));
                    }
                    setting_label4.setLayoutX(25);
                    setting_label4.setLayoutY(95);
                    setting_root.getChildren().add(setting_label4);

                    CheckBox setting_checkbox3 = new CheckBox();
                    setting_checkbox3.setLayoutX(5);
                    setting_checkbox3.setLayoutY(115);
                    setting_checkbox3.setSelected(Function.config.isVideoPlayer());
                    setting_root.getChildren().add(setting_checkbox3);

                    Label setting_label5 = new Label(Function.langData.get("setting-videoplayer"));
                    if (new File("./fonts/NotoSansCJK-Regular.ttc").exists()){
                        setting_label5.setFont(Font.loadFont(new FileInputStream("./fonts/NotoSansCJK-Regular.ttc"), setting_label5.getFont().getSize()));
                    } else if (new File("./fonts/NotoSansCJK-Regular.ttf").exists()){
                        setting_label5.setFont(Font.loadFont(new FileInputStream("./fonts/NotoSansCJK-Regular.ttf"), setting_label5.getFont().getSize()));
                    }
                    setting_label5.setLayoutX(25);
                    setting_label5.setLayoutY(115);
                    setting_root.getChildren().add(setting_label5);

                    CheckBox setting_checkbox4 = new CheckBox();
                    setting_checkbox4.setLayoutX(5);
                    setting_checkbox4.setLayoutY(135);
                    setting_checkbox4.setSelected(Function.config.isImageDownloader());
                    setting_root.getChildren().add(setting_checkbox4);

                    Label setting_label6 = new Label(Function.langData.get("setting-image"));
                    if (new File("./fonts/NotoSansCJK-Regular.ttc").exists()){
                        setting_label6.setFont(Font.loadFont(new FileInputStream("./fonts/NotoSansCJK-Regular.ttc"), setting_label6.getFont().getSize()));
                    } else if (new File("./fonts/NotoSansCJK-Regular.ttf").exists()){
                        setting_label6.setFont(Font.loadFont(new FileInputStream("./fonts/NotoSansCJK-Regular.ttf"), setting_label6.getFont().getSize()));
                    }
                    setting_label6.setLayoutX(25);
                    setting_label6.setLayoutY(135);
                    setting_root.getChildren().add(setting_label6);

                    CheckBox setting_checkbox5 = new CheckBox();
                    setting_checkbox5.setLayoutX(5);
                    setting_checkbox5.setLayoutY(155);
                    setting_checkbox5.setSelected(Function.config.isStringDownloader());
                    setting_root.getChildren().add(setting_checkbox5);

                    Label setting_label7 = new Label(Function.langData.get("setting-string"));
                    if (new File("./fonts/NotoSansCJK-Regular.ttc").exists()){
                        setting_label7.setFont(Font.loadFont(new FileInputStream("./fonts/NotoSansCJK-Regular.ttc"), setting_label7.getFont().getSize()));
                    } else if (new File("./fonts/NotoSansCJK-Regular.ttf").exists()){
                        setting_label7.setFont(Font.loadFont(new FileInputStream("./fonts/NotoSansCJK-Regular.ttf"), setting_label7.getFont().getSize()));
                    }
                    setting_label7.setLayoutX(25);
                    setting_label7.setLayoutY(155);
                    setting_root.getChildren().add(setting_label7);

                    CheckBox setting_checkbox6 = new CheckBox();
                    CheckBox setting_checkbox7 = new CheckBox();
                    CheckBox setting_checkbox8 = new CheckBox();
                    if (Function.ntSystem != null){
                        Label setting_label8 = new Label(Function.langData.get("setting-autostart"));
                        setting_label8.setLayoutX(5);
                        setting_label8.setLayoutY(175);
                        setting_root.getChildren().add(setting_label8);

                        setting_checkbox6.setLayoutX(5);
                        setting_checkbox6.setLayoutY(195);
                        setting_checkbox6.setSelected(!Function.config.isAutoStaring());
                        setting_checkbox6.setOnAction(ev->{
                            if (setting_checkbox6.isSelected()){
                                setting_checkbox7.setSelected(false);
                                setting_checkbox8.setSelected(false);
                                setting_checkbox7.setDisable(true);
                                setting_checkbox8.setDisable(true);
                            } else {
                                setting_checkbox7.setDisable(false);
                                setting_checkbox8.setDisable(false);
                            }
                        });
                        setting_root.getChildren().add(setting_checkbox6);

                        Label setting_label9 = new Label(Function.langData.get("setting-autostart-no"));
                        if (new File("./fonts/NotoSansCJK-Regular.ttc").exists()){
                            setting_label9.setFont(Font.loadFont(new FileInputStream("./fonts/NotoSansCJK-Regular.ttc"), setting_label9.getFont().getSize()));
                        } else if (new File("./fonts/NotoSansCJK-Regular.ttf").exists()){
                            setting_label9.setFont(Font.loadFont(new FileInputStream("./fonts/NotoSansCJK-Regular.ttf"), setting_label9.getFont().getSize()));
                        }
                        setting_label9.setLayoutX(25);
                        setting_label9.setLayoutY(195);
                        setting_root.getChildren().add(setting_label9);

                        setting_checkbox7.setLayoutX(5);
                        setting_checkbox7.setLayoutY(215);
                        setting_checkbox7.setSelected(Function.config.getAutoStaringMode().equals("Windows"));
                        setting_checkbox7.setOnAction(ev->{
                            setting_checkbox8.setSelected(false);
                        });
                        setting_checkbox7.setDisable(!Function.config.isAutoStaring());
                        setting_root.getChildren().add(setting_checkbox7);

                        Label setting_label10 = new Label(Function.langData.get("setting-autostart-windows"));
                        if (new File("./fonts/NotoSansCJK-Regular.ttc").exists()){
                            setting_label10.setFont(Font.loadFont(new FileInputStream("./fonts/NotoSansCJK-Regular.ttc"), setting_label10.getFont().getSize()));
                        } else if (new File("./fonts/NotoSansCJK-Regular.ttf").exists()){
                            setting_label10.setFont(Font.loadFont(new FileInputStream("./fonts/NotoSansCJK-Regular.ttf"), setting_label10.getFont().getSize()));
                        }
                        setting_label10.setLayoutX(25);
                        setting_label10.setLayoutY(215);
                        setting_root.getChildren().add(setting_label10);

                        setting_checkbox8.setLayoutX(5);
                        setting_checkbox8.setLayoutY(235);
                        setting_checkbox8.setSelected(Function.config.getAutoStaringMode().equals("VRChat"));
                        setting_checkbox8.setOnAction(ev->{
                            setting_checkbox7.setSelected(false);
                        });
                        setting_checkbox8.setDisable(!Function.config.isAutoStaring());
                        setting_root.getChildren().add(setting_checkbox8);

                        Label setting_label11 = new Label(Function.langData.get("setting-autostart-vrchat"));
                        if (new File("./fonts/NotoSansCJK-Regular.ttc").exists()){
                            setting_label11.setFont(Font.loadFont(new FileInputStream("./fonts/NotoSansCJK-Regular.ttc"), setting_label11.getFont().getSize()));
                        } else if (new File("./fonts/NotoSansCJK-Regular.ttf").exists()){
                            setting_label11.setFont(Font.loadFont(new FileInputStream("./fonts/NotoSansCJK-Regular.ttf"), setting_label11.getFont().getSize()));
                        }
                        setting_label11.setLayoutX(25);
                        setting_label11.setLayoutY(235);
                        setting_root.getChildren().add(setting_label11);

                    }

                    final ObservableList<String> lang_items = FXCollections.observableArrayList();
                    final HashMap<String, String> langList = new HashMap<>();

                    for (String s : Function.iso639_1) {
                        if (new File("./lang/"+s+".txt").exists()){
                            String langText = null;
                            try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(new File("./lang/"+s+".txt")), StandardCharsets.UTF_8))){
                                String str;
                                StringBuilder sb = new StringBuilder();
                                while ((str = reader.readLine()) != null) {
                                    sb.append(str).append("\n");
                                }
                                langText = sb.toString();
                            } catch (IOException ex) {
                                ex.printStackTrace();
                            }

                            for (String str : langText.split("\n")) {
                                Matcher matcher = Function.matcher_langData.matcher(str);
                                //System.out.println("debug : " + str);
                                if (matcher.find()){
                                    //System.out.println("debug : " + matcher.group(1) + " / " + matcher.group(2));
                                    if (matcher.group(1).equals("lang_name")){
                                        lang_items.add(matcher.group(2));
                                        langList.put(matcher.group(2), s);
                                        break;
                                    }
                                }
                            }

                        }
                    }

                    Label setting_label12 = new Label(Function.langData.get("setting-lang"));
                    if (new File("./fonts/NotoSansCJK-Regular.ttc").exists()){
                        setting_label12.setFont(Font.loadFont(new FileInputStream("./fonts/NotoSansCJK-Regular.ttc"), setting_label12.getFont().getSize()));
                    } else if (new File("./fonts/NotoSansCJK-Regular.ttf").exists()){
                        setting_label12.setFont(Font.loadFont(new FileInputStream("./fonts/NotoSansCJK-Regular.ttf"), setting_label12.getFont().getSize()));
                    }
                    setting_label12.setLayoutX(5);
                    setting_label12.setLayoutY(255);
                    setting_root.getChildren().add(setting_label12);

                    ListView<String> setting_listView = new ListView<>(lang_items);
                    setting_listView.setEditable(false);
                    setting_listView.setPrefSize(400, 400);
                    setting_listView.setLayoutX(5);
                    setting_listView.setLayoutY(275);
                    final String[] setting_lang = {langList.get(Function.langData.get("lang_name"))};
                    setting_listView.setOnMouseClicked(event -> {
                        setting_lang[0] = langList.get(setting_listView.getSelectionModel().getSelectedItem());
                        //System.out.println(setting_lang[0]);
                    });
                    setting_root.getChildren().add(setting_listView);
                    setting_listView.getSelectionModel().select(setting_lang[0]);

                    Platform.runLater(()->{
                        setting_stage.setScene(setting_scene);
                        setting_stage.showAndWait();

                        ConfigData data = new ConfigData();
                        data.setLang(setting_lang[0]);
                        data.setLogFolderPass(setting_field1.getText());
                        data.setDebugOutput(setting_checkbox1.isSelected());
                        data.setOldLogCheck(setting_checkbox2.isSelected());
                        data.setVideoPlayer(setting_checkbox3.isSelected());
                        data.setImageDownloader(setting_checkbox4.isSelected());
                        data.setStringDownloader(setting_checkbox5.isSelected());
                        data.setAutoStaring(!setting_checkbox6.isSelected());
                        data.setAutoStaringMode(setting_checkbox7.isSelected() ? "Windows" : setting_checkbox8.isSelected() ? "VRChat" : "");

                        Function.SettingConfig(data);
                    });
                } catch (Exception ex){
                    ex.printStackTrace();
                }
            });

        });
        root.getChildren().add(button);

        listView.setEditable(false);
        listView.setPrefSize(1200, 600);
        listView.setLayoutX(15);
        listView.setLayoutY(55);
        listView.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                final Stage detail_stage = new Stage();
                Thread.ofVirtual().start(()->{
                    //System.out.println(listView.getItems().size() + " / " + value);
                    String selectedItem = listView.getSelectionModel().getSelectedItem();
                    if (selectedItem != null) {
                        LogData data = Function.logDataList.get(selectedItem);
                        if (data == null){
                            return;
                        }

                        Platform.runLater(()->{
                            detail_stage.setResizable(false);
                            detail_stage.setMaximized(false);
                            detail_stage.setFullScreen(false);
                            detail_stage.setTitle("詳細");
                            detail_stage.setWidth(800);
                            detail_stage.setHeight(800);
                        });

                        AnchorPane detail_root = new AnchorPane();
                        Scene detail_scene = new Scene(detail_root);

                        Label detail_label1 = new Label(Function.langData.get("detail"));
                        try {
                            if (new File("./fonts/NotoSansCJK-Regular.ttc").exists()){
                                detail_label1.setFont(Font.loadFont(new FileInputStream("./fonts/NotoSansCJK-Regular.ttc"), detail_label1.getFont().getSize()));
                            } else if (new File("./fonts/NotoSansCJK-Regular.ttf").exists()){
                                detail_label1.setFont(Font.loadFont(new FileInputStream("./fonts/NotoSansCJK-Regular.ttf"), detail_label1.getFont().getSize()));
                            }
                        } catch (Exception e){
                            //e.printStackTrace();
                        }
                        detail_label1.setLayoutX(5);
                        detail_label1.setLayoutY(5);
                        detail_label1.setFont(new Font(16));
                        detail_root.getChildren().add(detail_label1);

                        Label detail_label1_2 = new Label(Function.langData.get("date"));
                        try {
                            if (new File("./fonts/NotoSansCJK-Regular.ttc").exists()){
                                detail_label1_2.setFont(Font.loadFont(new FileInputStream("./fonts/NotoSansCJK-Regular.ttc"), detail_label1_2.getFont().getSize()));
                            } else if (new File("./fonts/NotoSansCJK-Regular.ttf").exists()){
                                detail_label1_2.setFont(Font.loadFont(new FileInputStream("./fonts/NotoSansCJK-Regular.ttf"), detail_label1_2.getFont().getSize()));
                            }
                        } catch (Exception e){
                            //e.printStackTrace();
                        }
                        detail_label1_2.setLayoutX(10);
                        detail_label1_2.setLayoutY(40);
                        detail_root.getChildren().add(detail_label1_2);

                        Label detail_label1_2_1 = new Label(Function.log_sdf.format(data.getLogDate()));
                        try {
                            if (new File("./fonts/NotoSansCJK-Regular.ttc").exists()){
                                detail_label1_2_1.setFont(Font.loadFont(new FileInputStream("./fonts/NotoSansCJK-Regular.ttc"), detail_label1_2_1.getFont().getSize()));
                            } else if (new File("./fonts/NotoSansCJK-Regular.ttf").exists()){
                                detail_label1_2_1.setFont(Font.loadFont(new FileInputStream("./fonts/NotoSansCJK-Regular.ttf"), detail_label1_2_1.getFont().getSize()));
                            }
                        } catch (Exception e){
                            //e.printStackTrace();
                        }
                        detail_label1_2_1.setLayoutX(10);
                        detail_label1_2_1.setLayoutY(60);
                        detail_root.getChildren().add(detail_label1_2_1);

                        Label detail_label1_3 = new Label(Function.langData.get("url"));
                        try {
                            if (new File("./fonts/NotoSansCJK-Regular.ttc").exists()){
                                detail_label1_3.setFont(Font.loadFont(new FileInputStream("./fonts/NotoSansCJK-Regular.ttc"), detail_label1_3.getFont().getSize()));
                            } else if (new File("./fonts/NotoSansCJK-Regular.ttf").exists()){
                                detail_label1_3.setFont(Font.loadFont(new FileInputStream("./fonts/NotoSansCJK-Regular.ttf"), detail_label1_3.getFont().getSize()));
                            }
                        } catch (Exception e){
                            //e.printStackTrace();
                        }
                        detail_label1_3.setLayoutX(10);
                        detail_label1_3.setLayoutY(80);
                        detail_root.getChildren().add(detail_label1_3);

                        Button detail_button1 = new Button(Function.langData.get("open"));
                        try {
                            if (new File("./fonts/NotoSansCJK-Regular.ttc").exists()){
                                detail_button1.setFont(Font.loadFont(new FileInputStream("./fonts/NotoSansCJK-Regular.ttc"), detail_button1.getFont().getSize()));
                            } else if (new File("./fonts/NotoSansCJK-Regular.ttf").exists()){
                                detail_button1.setFont(Font.loadFont(new FileInputStream("./fonts/NotoSansCJK-Regular.ttf"), detail_button1.getFont().getSize()));
                            }
                        } catch (Exception e){
                            //e.printStackTrace();
                        }
                        detail_button1.setLayoutX(150);
                        detail_button1.setLayoutY(75);
                        detail_button1.setOnAction(e -> {
                            Thread.ofVirtual().start(()->{
                                try {
                                    final Process exec0;
                                    String batText = "start "+data.getURL();
                                    if (Function.ntSystem != null){
                                        FileWriter file1 = new FileWriter("./temp.bat");
                                        PrintWriter pw = new PrintWriter(new BufferedWriter(file1));
                                        pw.print(batText);
                                        pw.close();
                                        file1.close();
                                        pw = null;
                                        file1 = null;
                                        exec0 = Function.runtime.exec(new String[]{"./temp.bat"});
                                        Thread.ofVirtual().start(()->{
                                            try {
                                                Thread.sleep(5000L);
                                                exec0.destroy();
                                            } catch (Exception ex){
                                                // ex.printStackTrace();
                                            }
                                        });
                                        exec0.waitFor();
                                        new File("./temp.bat").delete();
                                    } else if (Function.unixSystem != null) {
                                        ProcessBuilder pb = new ProcessBuilder("/bin/bash", "-c", "xdg-open "+data.getURL());
                                        Process process = pb.start();
                                        process.waitFor();
                                        //System.out.println(process.exitValue());
                                        //System.out.println(new String(process.getErrorStream().readAllBytes(), StandardCharsets.UTF_8));
                                        //System.out.println(new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8));
                                    }
                                } catch (Exception ex){
                                    ex.printStackTrace();
                                }
                            });
                        });
                        detail_root.getChildren().add(detail_button1);

                        TextField detail_field = new TextField();
                        try {
                            if (new File("./fonts/NotoSansCJK-Regular.ttc").exists()){
                                detail_field.setFont(Font.loadFont(new FileInputStream("./fonts/NotoSansCJK-Regular.ttc"), detail_field.getFont().getSize()));
                            } else if (new File("./fonts/NotoSansCJK-Regular.ttf").exists()){
                                detail_field.setFont(Font.loadFont(new FileInputStream("./fonts/NotoSansCJK-Regular.ttf"), detail_field.getFont().getSize()));
                            }
                        } catch (Exception e){
                            //e.printStackTrace();
                        }
                        detail_field.setLayoutX(10);
                        detail_field.setLayoutY(100);
                        detail_field.setEditable(false);
                        detail_field.setFocusTraversable(false);
                        detail_field.setText(data.getURL());
                        detail_field.setPrefWidth(700);
                        detail_root.getChildren().add(detail_field);
                        //Platform.runLater(()-> );

                        Label detail_label1_4 = new Label(Function.langData.get("kind"));
                        try {
                            if (new File("./fonts/NotoSansCJK-Regular.ttc").exists()){
                                detail_label1_4.setFont(Font.loadFont(new FileInputStream("./fonts/NotoSansCJK-Regular.ttc"), detail_label1_4.getFont().getSize()));
                            } else if (new File("./fonts/NotoSansCJK-Regular.ttf").exists()){
                                detail_label1_4.setFont(Font.loadFont(new FileInputStream("./fonts/NotoSansCJK-Regular.ttf"), detail_label1_4.getFont().getSize()));
                            }
                        } catch (Exception e){
                            //e.printStackTrace();
                        }
                        detail_label1_4.setLayoutX(10);
                        detail_label1_4.setLayoutY(130);
                        detail_root.getChildren().add(detail_label1_4);

                        Label detail_label1_4_1 = new Label(data.getURLType().equals("Video") ? Function.langData.get("video") : data.getURLType().equals("String") ? Function.langData.get("string") : Function.langData.get("image"));
                        try {
                            if (new File("./fonts/NotoSansCJK-Regular.ttc").exists()){
                                detail_label1_4_1.setFont(Font.loadFont(new FileInputStream("./fonts/NotoSansCJK-Regular.ttc"), detail_label1_4_1.getFont().getSize()));
                            } else if (new File("./fonts/NotoSansCJK-Regular.ttf").exists()){
                                detail_label1_4_1.setFont(Font.loadFont(new FileInputStream("./fonts/NotoSansCJK-Regular.ttf"), detail_label1_4_1.getFont().getSize()));
                            }
                        } catch (Exception e){
                            //e.printStackTrace();
                        }
                        detail_label1_4_1.setLayoutX(10);
                        detail_label1_4_1.setLayoutY(150);
                        detail_root.getChildren().add(detail_label1_4_1);

                        Label detail_label1_5 = new Label(Function.langData.get("error-message"));
                        try {
                            if (new File("./fonts/NotoSansCJK-Regular.ttc").exists()){
                                detail_label1_5.setFont(Font.loadFont(new FileInputStream("./fonts/NotoSansCJK-Regular.ttc"), detail_label1_5.getFont().getSize()));
                            } else if (new File("./fonts/NotoSansCJK-Regular.ttf").exists()){
                                detail_label1_5.setFont(Font.loadFont(new FileInputStream("./fonts/NotoSansCJK-Regular.ttf"), detail_label1_5.getFont().getSize()));
                            }
                        } catch (Exception e){
                            //e.printStackTrace();
                        }
                        detail_label1_5.setLayoutX(10);
                        detail_label1_5.setLayoutY(170);
                        detail_root.getChildren().add(detail_label1_5);

                        TextArea detail_textArea = new TextArea();
                        try {
                            if (new File("./fonts/NotoSansCJK-Regular.ttc").exists()){
                                detail_textArea.setFont(Font.loadFont(new FileInputStream("./fonts/NotoSansCJK-Regular.ttc"), detail_textArea.getFont().getSize()));
                            } else if (new File("./fonts/NotoSansCJK-Regular.ttf").exists()){
                                detail_textArea.setFont(Font.loadFont(new FileInputStream("./fonts/NotoSansCJK-Regular.ttf"), detail_textArea.getFont().getSize()));
                            }
                        } catch (Exception e){
                            //e.printStackTrace();
                        }
                        detail_textArea.setLayoutX(10);
                        detail_textArea.setLayoutY(190);
                        detail_textArea.setText(data.getErrorMessage());
                        detail_textArea.setPrefSize(700, 150);
                        detail_textArea.setEditable(false);
                        detail_textArea.setWrapText(false);
                        //Platform.runLater(()-> );
                        detail_root.getChildren().add(detail_textArea);

                        Button detail_button2 = new Button(Function.langData.get("exit"));
                        try {
                            if (new File("./fonts/NotoSansCJK-Regular.ttc").exists()){
                                detail_button2.setFont(Font.loadFont(new FileInputStream("./fonts/NotoSansCJK-Regular.ttc"), detail_button2.getFont().getSize()));
                            } else if (new File("./fonts/NotoSansCJK-Regular.ttf").exists()){
                                detail_button2.setFont(Font.loadFont(new FileInputStream("./fonts/NotoSansCJK-Regular.ttf"), detail_button2.getFont().getSize()));
                            }
                        } catch (Exception e){
                            //e.printStackTrace();
                        }
                        detail_button2.setLayoutX(650);
                        detail_button2.setLayoutY(10);
                        detail_button2.setOnAction(e -> detail_stage.close());
                        detail_root.getChildren().add(detail_button2);

                        Label detail_label1_6 = new Label(Function.langData.get("now-loading"));
                        try {
                            if (new File("./fonts/NotoSansCJK-Regular.ttc").exists()){
                                detail_label1_6.setFont(Font.loadFont(new FileInputStream("./fonts/NotoSansCJK-Regular.ttc"), detail_label1_6.getFont().getSize()));
                            } else if (new File("./fonts/NotoSansCJK-Regular.ttf").exists()){
                                detail_label1_6.setFont(Font.loadFont(new FileInputStream("./fonts/NotoSansCJK-Regular.ttf"), detail_label1_6.getFont().getSize()));
                            }
                        } catch (Exception e){
                            //e.printStackTrace();
                        }
                        detail_label1_6.setLayoutX(10);
                        detail_label1_6.setLayoutY(360);
                        detail_root.getChildren().add(detail_label1_6);


                        Platform.runLater(()->{
                            detail_stage.setScene(detail_scene);
                            detail_stage.show();
                        });
                        switch (data.getURLType()) {
                            case "Video" -> {
                                //System.out.println("debug 0");
                                VideoData videoData = Function.getVideoData(data.getURL());
                                //System.out.println("debug 1");
                                Image fxImage = videoData.getThumbnail() != null ? new Image(new ByteArrayInputStream(videoData.getThumbnail())) : null;
                                //System.out.println("debug 2");

                                Platform.runLater(()->detail_label1_6.setText(Function.langData.get("video-title")));

                                TextField detail_field2 = new TextField();
                                try {
                                    if (new File("./fonts/NotoSansCJK-Regular.ttc").exists()){
                                        detail_field2.setFont(Font.loadFont(new FileInputStream("./fonts/NotoSansCJK-Regular.ttc"), detail_field2.getFont().getSize()));
                                    } else if (new File("./fonts/NotoSansCJK-Regular.ttf").exists()){
                                        detail_field2.setFont(Font.loadFont(new FileInputStream("./fonts/NotoSansCJK-Regular.ttf"), detail_field2.getFont().getSize()));
                                    }
                                } catch (Exception e){
                                    //e.printStackTrace();
                                }
                                detail_field2.setLayoutX(10);
                                detail_field2.setLayoutY(380);
                                detail_field2.setEditable(false);
                                detail_field2.setFocusTraversable(false);
                                detail_field2.setText(videoData.getVideoTitle());
                                detail_field2.setPrefWidth(700);
                                Platform.runLater(()->detail_root.getChildren().add(detail_field2));

                                if (fxImage != null) {
                                    //System.out.println("debug 3");

                                    ImageView detail_imageView = new ImageView(fxImage);
                                    detail_imageView.setLayoutX(10);
                                    detail_imageView.setLayoutY(420);
                                    detail_imageView.setFitHeight(300);
                                    detail_imageView.setPreserveRatio(true);
                                    Platform.runLater(()->detail_root.getChildren().add(detail_imageView));
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

                                Platform.runLater(()->detail_root.getChildren().remove(detail_label1_6));
                                if (fxImage != null) {

                                    ImageView detail_imageView = new ImageView(fxImage);
                                    detail_imageView.setLayoutX(10);
                                    detail_imageView.setLayoutY(360);
                                    detail_imageView.setFitHeight(350);
                                    detail_imageView.setPreserveRatio(true);
                                    Platform.runLater(()->detail_root.getChildren().add(detail_imageView));
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

                                Platform.runLater(()->detail_root.getChildren().remove(detail_label1_6));
                                if (str != null) {

                                    TextArea detail_textArea2 = new TextArea();
                                    try {
                                        if (new File("./fonts/NotoSansCJK-Regular.ttc").exists()){
                                            detail_textArea2.setFont(Font.loadFont(new FileInputStream("./fonts/NotoSansCJK-Regular.ttc"), detail_textArea2.getFont().getSize()));
                                        } else if (new File("./fonts/NotoSansCJK-Regular.ttf").exists()){
                                            detail_textArea2.setFont(Font.loadFont(new FileInputStream("./fonts/NotoSansCJK-Regular.ttf"), detail_textArea2.getFont().getSize()));
                                        }
                                    } catch (Exception e){
                                        //e.printStackTrace();
                                    }
                                    detail_textArea2.setLayoutX(10);
                                    detail_textArea2.setLayoutY(360);
                                    detail_textArea2.setText(str);
                                    detail_textArea2.setPrefSize(700, 300);
                                    detail_textArea2.setEditable(false);
                                    detail_textArea2.setWrapText(false);
                                    Platform.runLater(()-> detail_root.getChildren().add(detail_textArea2));
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

        Stage update_stage = new Stage();
        if (Function.isUpdate){
            // アップデート通知
            update_stage.setResizable(false);
            update_stage.setMaximized(false);
            update_stage.setFullScreen(false);
            update_stage.setTitle(Function.langData.get("update-notify"));
            update_stage.setWidth(400);
            update_stage.setHeight(200);

            AnchorPane update_root = new AnchorPane();
            Scene update_scene = new Scene(update_root);

            Button update_button1 = new Button(Function.langData.get("exit"));
            try {
                if (new File("./fonts/NotoSansCJK-Regular.ttc").exists()){
                    update_button1.setFont(Font.loadFont(new FileInputStream("./fonts/NotoSansCJK-Regular.ttc"), update_button1.getFont().getSize()));
                } else if (new File("./fonts/NotoSansCJK-Regular.ttf").exists()){
                    update_button1.setFont(Font.loadFont(new FileInputStream("./fonts/NotoSansCJK-Regular.ttf"), update_button1.getFont().getSize()));
                }
            } catch (Exception e){
                //e.printStackTrace();
            }
            update_button1.setLayoutX(300);
            update_button1.setLayoutY(10);
            update_button1.setOnAction(e -> update_stage.close());
            update_root.getChildren().add(update_button1);

            Label update_label1 = new Label(Function.langData.get("update-notify"));
            try {
                if (new File("./fonts/NotoSansCJK-Regular.ttc").exists()){
                    update_label1.setFont(Font.loadFont(new FileInputStream("./fonts/NotoSansCJK-Regular.ttc"), 16));
                } else if (new File("./fonts/NotoSansCJK-Regular.ttf").exists()){
                    update_label1.setFont(Font.loadFont(new FileInputStream("./fonts/NotoSansCJK-Regular.ttf"), 16));
                }
            } catch (Exception e){
                //e.printStackTrace();
            }
            update_label1.setLayoutX(5);
            update_label1.setLayoutY(5);
            update_root.getChildren().add(update_label1);

            Label update_label2 = new Label(Function.langData.get("update-found"));
            try {
                if (new File("./fonts/NotoSansCJK-Regular.ttc").exists()){
                    update_label2.setFont(Font.loadFont(new FileInputStream("./fonts/NotoSansCJK-Regular.ttc"), update_label2.getFont().getSize()));
                } else if (new File("./fonts/NotoSansCJK-Regular.ttf").exists()){
                    update_label2.setFont(Font.loadFont(new FileInputStream("./fonts/NotoSansCJK-Regular.ttf"), update_label2.getFont().getSize()));
                }
            } catch (Exception e){
                //e.printStackTrace();
            }
            update_label2.setLayoutX(10);
            update_label2.setLayoutY(40);
            update_root.getChildren().add(update_label2);

            Label update_label3 = new Label(Function.langData.get("update-now-version").replaceAll("#nowver#", Function.Version));
            try {
                if (new File("./fonts/NotoSansCJK-Regular.ttc").exists()){
                    update_label3.setFont(Font.loadFont(new FileInputStream("./fonts/NotoSansCJK-Regular.ttc"), update_label3.getFont().getSize()));
                } else if (new File("./fonts/NotoSansCJK-Regular.ttf").exists()){
                    update_label3.setFont(Font.loadFont(new FileInputStream("./fonts/NotoSansCJK-Regular.ttf"), update_label3.getFont().getSize()));
                }
            } catch (Exception e){
                //e.printStackTrace();
            }
            update_label3.setLayoutX(10);
            update_label3.setLayoutY(80);
            update_root.getChildren().add(update_label3);

            Label update_label4 = new Label(Function.langData.get("update-new-version").replaceAll("#newver#", Function.new_version));
            try {
                if (new File("./fonts/NotoSansCJK-Regular.ttc").exists()){
                    update_label4.setFont(Font.loadFont(new FileInputStream("./fonts/NotoSansCJK-Regular.ttc"), update_label4.getFont().getSize()));
                } else if (new File("./fonts/NotoSansCJK-Regular.ttf").exists()){
                    update_label4.setFont(Font.loadFont(new FileInputStream("./fonts/NotoSansCJK-Regular.ttf"), update_label4.getFont().getSize()));
                }
            } catch (Exception e){
                //e.printStackTrace();
            }
            update_label4.setLayoutX(10);
            update_label4.setLayoutY(100);
            update_root.getChildren().add(update_label4);

            if (Function.ntSystem != null){
                // Windowsの場合のアップデートバッチ用のボタン
                Button update_button2 = new Button(Function.langData.get("update"));
                try {
                    if (new File("./fonts/NotoSansCJK-Regular.ttc").exists()){
                        update_button2.setFont(Font.loadFont(new FileInputStream("./fonts/NotoSansCJK-Regular.ttc"), update_button2.getFont().getSize()));
                    } else if (new File("./fonts/NotoSansCJK-Regular.ttf").exists()){
                        update_button2.setFont(Font.loadFont(new FileInputStream("./fonts/NotoSansCJK-Regular.ttf"), update_button2.getFont().getSize()));
                    }
                } catch (Exception e){
                    //e.printStackTrace();
                }
                update_button2.setLayoutX(10);
                update_button2.setLayoutY(120);
                update_button2.setOnAction(e -> {
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
                    update_stage.close();
                    stage.close();
                });
                update_root.getChildren().add(update_button2);
            }

            update_stage.setScene(update_scene);
        }

        if (Function.config.isDebugOutput()){
            System.out.println("[Info] "+Function.langData.get("gui-create-success"));
        }
        if (Function.config.isDebugOutput()){
            System.out.println("[Info] "+Function.langData.get("gui-print"));
        }
        stage.show();
        if (Function.isUpdate){
            update_stage.show();
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
