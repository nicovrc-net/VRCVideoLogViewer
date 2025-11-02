package net.nicovrc.dev;

import com.amihaiemil.eoyaml.Yaml;
import com.amihaiemil.eoyaml.YamlMapping;
import javafx.application.Platform;
import javafx.stage.Stage;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Date;
import java.util.List;
import java.util.TimerTask;
import java.util.regex.Matcher;

public class Main {

    public static void main(String[] args) {
        if (args.length == 1){
            if (args[0].equals("--start-Windows")){
                boolean isAutoStaring = false;
                String AutoStaringMode = "";
                if (new File("./config-windows.yml").exists()){
                    try {
                        final YamlMapping yamlMapping = Yaml.createYamlInput(new File("./config-windows.yml")).readYamlMapping();
                        isAutoStaring = yamlMapping.bool("isAutoStaring");
                        AutoStaringMode = yamlMapping.string("AutoStaringMode");
                    } catch (Exception e){
                        // e.printStackTrace();
                        isAutoStaring = false;
                        AutoStaringMode = "";
                    }
                } else {
                    return;
                }

                if (!isAutoStaring){
                    return;
                }

                if (AutoStaringMode.equals("Start-Windows")){
                    try {
                        final Runtime runtime = Runtime.getRuntime();
                        final Process exec0 = runtime.exec(new String[]{"./auto-start.bat"});
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
                    } catch (Exception e){
                        // e.printStackTrace();
                    }
                    return;
                }

                if (AutoStaringMode.equals("Start-VRChat")){
                    List<String> logFileList = null;
                    try {

                        ConfigData config = new ConfigData();
                        if (new File("./config.yml").exists()){
                            final YamlMapping yamlMapping = Yaml.createYamlInput(new File("./config.yml")).readYamlMapping();
                            config.setLogFolderPass(yamlMapping.string("logfolder"));
                            config.setDebugOutput(yamlMapping.bool("debugOutput"));
                            config.setOldLogCheck(yamlMapping.bool("oldLogCheck"));
                            config.setVideoPlayer(yamlMapping.bool("VideoPlayer"));
                            config.setImageDownloader(yamlMapping.bool("ImageDownloader"));
                            config.setStringDownloader(yamlMapping.bool("StringDownloader"));
                        }
                        logFileList = Function.getFileList(config.getLogFolderPass());

                        if (logFileList.size() > 1) {
                            logFileList = Function.ListSort(logFileList);
                        }

                        String tempLast = logFileList.getLast();

                        while (tempLast.equals(logFileList.getLast())){
                            logFileList.clear();
                            logFileList = Function.getFileList(config.getLogFolderPass());
                        }

                        try {
                            final Runtime runtime = Runtime.getRuntime();
                            final Process exec0 = runtime.exec(new String[]{"./auto-start.bat"});
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
                        } catch (Exception e){
                            // e.printStackTrace();
                        }

                        return;

                    } catch (Exception e){
                        //e.printStackTrace();
                    }
                }

            }
            return;
        }



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
            Matcher matcher = Function.matcher_version.matcher(send.body());
            if (matcher.find()){
                Function.new_version = matcher.group(2);
            }
        } catch (Exception e){
            e.printStackTrace();
            Function.timer1.cancel();
            Function.timer2.cancel();
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

            isUpdate = !Function.Version.equals(Function.new_version);

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
                    pw.print(str.replaceAll("#ver#", Function.new_version).replaceAll("\\./", CurrentFolderPass.replaceAll("/", "\\\\\\\\")+"\\\\"));
                    pw.close();
                    file1.close();
                    pw = null;
                    file1 = null;
                }
            } else {
                System.out.println("[Info] アップデートはありませんでした。 (現在: "+Function.Version+" 最新:"+Function.new_version+")");
            }
        } catch (Exception e){
            e.printStackTrace();
            Function.timer1.cancel();
            Function.timer2.cancel();
            return;
        }

        System.out.println("[Info] config.yml 存在チェック");
        file = new File("./config.yml");

        if (!file.exists()){
            System.out.println("[Info] config.yml 自動生成します");

            try {
                FileWriter file1 = new FileWriter("./config.yml");
                PrintWriter pw = new PrintWriter(new BufferedWriter(file1));
                pw.print(Function.configText);
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
            Function.config.setLogFolderPass(yamlMapping.string("logfolder"));
            Function.config.setDebugOutput(yamlMapping.bool("debugOutput"));
            Function.config.setOldLogCheck(yamlMapping.bool("oldLogCheck"));
            Function.config.setVideoPlayer(yamlMapping.bool("VideoPlayer"));
            Function.config.setImageDownloader(yamlMapping.bool("ImageDownloader"));
            Function.config.setStringDownloader(yamlMapping.bool("StringDownloader"));
        } catch (Exception e){
            // e.printStackTrace();

        }

        if (Function.config.getLogFolderPass().isEmpty()){

            if (Function.ntSystem.getName().isEmpty()){
                //UnixSystem unixSystem = new UnixSystem();

            } else {
                try {
                    if (Function.config.isDebugOutput()){
                        System.out.println("[Info] ログフォルダの自動取得");
                    }
                    String LocalAppData = System.getenv().get("LOCALAPPDATA");
                    file = new File("C:\\Users\\"+Function.ntSystem.getName()+"\\AppData\\LocalLow\\VRChat\\VRChat");
                    //file = new File(LocalAppData+"Low\\VRChat\\VRChat");
                    if (file.exists()) {
                        Function.config.setLogFolderPass(file.getCanonicalPath());

                        if (Function.config.isDebugOutput()) {
                            System.out.println("[Info] 自動取得成功 : " + file.getCanonicalPath());
                        }
                    } else if (new File(LocalAppData+"Low\\VRChat\\VRChat").exists()){
                        file = new File(LocalAppData+"Low\\VRChat\\VRChat");

                        Function.config.setLogFolderPass(file.getCanonicalPath());
                        System.out.println("[Info] 自動取得成功 : " + file.getCanonicalPath());
                    } else {
                        System.out.println("[Info] 自動取得失敗");
                    }
                } catch (Exception e) {
                    //e.printStackTrace();
                }

            }
        }

        if (Function.config.isDebugOutput()){
            System.out.println("[Info] フォルダチェック");
        }
        if (Function.config.getLogFolderPass() != null){
            file = new File(Function.config.getLogFolderPass());
            if (!file.exists()){
                System.out.println("フォルダが見つかりませんでした。\nFolder not found.");
                Function.timer1.cancel();
                Function.timer2.cancel();
                return;
            }
        } else {
            System.out.println("設定ファイルが正しく設定されていません。\nThe configuration file is not set up correctly.");
            Function.timer1.cancel();
            Function.timer2.cancel();
            return;
        }


        try {
            Function.logFileList = Function.getFileList(Function.config.getLogFolderPass());
        } catch (Exception e){
            // e.printStackTrace();
        }
        if (Function.logFileList.isEmpty()){
            System.out.println("ログファイルが見つかりませんでした。\nLog file not found.");
            Function.timer1.cancel();
            Function.timer2.cancel();
            return;
        }

        if (Function.config.isDebugOutput()){
            System.out.println("[Info] ログファイルの並び替え");
        }

        Function.timer1.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                try {
                    List<String> logFileList = Function.getFileList(Function.config.getLogFolderPass());
                    if (logFileList.size() > 1){
                        logFileList = Function.ListSort(logFileList);
                    }

                    Function.temp_lastLogFile[0] = Function.config.getLogFolderPass() + "\\" + logFileList.getLast();
                } catch (Exception e){
                    Function.timer1.cancel();
                    Function.timer2.cancel();
                    Function.isTimerRun = false;
                }
            }
        }, 0L, 1000L);

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