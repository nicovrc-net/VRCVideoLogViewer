package net.nicovrc.dev;

import com.amihaiemil.eoyaml.Yaml;
import com.amihaiemil.eoyaml.YamlMapping;
import com.sun.security.auth.module.NTSystem;
import com.sun.security.auth.module.UnixSystem;
import javafx.application.Platform;
import javafx.stage.Stage;
import net.nicovrc.dev.data.ConfigData;

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
import java.util.List;
import java.util.Locale;
import java.util.TimerTask;
import java.util.regex.Matcher;

public class Main {

    public static void main(String[] args) {

        try {
            if (System.getProperty("os.name").toLowerCase(Locale.ROOT).equals("linux")){
                Function.unixSystem = new UnixSystem();
            } else if (System.getProperty("os.name").toLowerCase(Locale.ROOT).equals("windows")){
                Function.ntSystem = new NTSystem();
            }
        } catch (Exception e){
            e.printStackTrace();
            return;
        }

        if (args.length == 1){
            if (args[0].equals("--start-Windows")){
                boolean isAutoStaring = false;
                String AutoStaringMode = "";
                if (new File("./config.yml").exists()){
                    try {
                        final YamlMapping yamlMapping = Yaml.createYamlInput(new File("./config.yml")).readYamlMapping();
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
                    System.out.println("debug : 自動起動オフ");
                    return;
                }

                if (AutoStaringMode.equals("Windows")){
                    //System.out.println("debug : 自動起動 : Windows");
                    try {
                        final Runtime runtime = Runtime.getRuntime();
                        if (new File("./auto-start.bat").exists()){
                            new File("./auto-start.bat").delete();
                        }

                        FileWriter file1 = new FileWriter("./auto-start.bat");
                        PrintWriter pw = new PrintWriter(new BufferedWriter(file1));
                        pw.print("start .\\start.bat");
                        pw.close();
                        file1.close();
                        pw = null;
                        file1 = null;

                        final Process exec0 = runtime.exec(new String[]{".\\auto-start.bat"});
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
                        new File("./auto-start.bat").delete();
                    } catch (Exception e){
                        // e.printStackTrace();
                    }
                    return;
                }

                if (AutoStaringMode.equals("VRChat")){
                    try {
                        //System.out.println("debug : 自動起動 : VRChat");
                        ConfigData config = new ConfigData();
                        if (new File("./config.yml").exists()){
                            final YamlMapping yamlMapping = Yaml.createYamlInput(new File("./config.yml")).readYamlMapping();
                            config.setLogFolderPass(yamlMapping.string("logfolder"));
                        }
                        List<String> list = Function.ListSort(Function.getFileList(config.getLogFolderPass()));
                        final String last = list.getLast();

                        while (last.equals(Function.ListSort(Function.getFileList(config.getLogFolderPass())).getLast())){
                            try {
                                //System.out.println(last);
                                //System.out.println(Function.ListSort(Function.getFileList(config.getLogFolderPass())).getLast());
                                Thread.sleep(1000L);
                            } catch (Exception ex) {
                                //ex.printStackTrace();
                                return;
                            }
                        }
                        config = null;

                        //System.out.println("debug");
                        File file = new File("C:\\Users\\" + Function.ntSystem.getName() + "\\AppData\\Roaming\\Microsoft\\Windows\\Start Menu\\Programs\\Startup");
                        if (!file.exists()){
                            String AppData = System.getenv().get("APPDATA");
                            file = new File(AppData+"\\Microsoft\\Windows\\Start Menu\\Programs\\Startup");
                        }
                        if (!file.exists()){
                            return;
                        }
                        //System.out.println("debug2");

                        if (new File("./auto-start.bat").exists()){
                            new File("./auto-start.bat").delete();
                        }

                        FileWriter file1 = new FileWriter("./auto-start.bat");
                        PrintWriter pw = new PrintWriter(new BufferedWriter(file1));
                        pw.print("start .\\start.bat\r\ncd /d \""+file.getCanonicalPath().replaceAll("\\\\", "/").replaceAll("/", "\\\\")+"\"\r\nstart .\\vrcvideologviewer.bat");
                        pw.close();
                        file1.close();
                        pw = null;
                        file1 = null;

                        final Runtime runtime = Runtime.getRuntime();
                        final Process exec0 = runtime.exec(new String[]{".\\auto-start.bat"});
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
                        runtime.exit(0);

                        new File("./auto-start.bat").delete();

                    } catch (Exception e){
                        //e.printStackTrace();
                    }
                }
                return;
            }
            return;
        }



        System.out.println("[Info] VRCVideoLogViewer Ver " + Function.Version + "起動");
        final boolean isWindowsBatchStart = Function.ntSystem != null;

        File file = new File("./tools/openjdk-21.0.2_windows-x64_bin.zip");
        if (file.exists()){
            file.delete();
        }
        file = new File("./tools/openjdk-21.0.2_linux-x64_bin.tar.gz");
        if (file.exists()){
            file.delete();
        }
        file = new File("./tools/openjfx-21.0.9_windows-x64_bin-sdk.zip");
        if (file.exists()){
            file.delete();
        }
        file = new File(".//tools/openjfx-21.0.9_linux-x64_bin-sdk.zip");
        if (file.exists()){
            file.delete();
        }

        if (isWindowsBatchStart){
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
            final String CurrentFolderPass = file.getCanonicalPath().replaceAll("\\\\", "/").replaceAll("/", File.separator);

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
                if (Function.ntSystem != null){
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
            try {
                Function.config.setLang(yamlMapping.string("lang"));
            } catch (Exception e){
                Function.config.setLang("ja");
            }
            Function.config.setLogFolderPass(yamlMapping.string("logfolder"));
            Function.config.setDebugOutput(yamlMapping.bool("debugOutput"));
            Function.config.setOldLogCheck(yamlMapping.bool("oldLogCheck"));
            Function.config.setVideoPlayer(yamlMapping.bool("VideoPlayer"));
            Function.config.setImageDownloader(yamlMapping.bool("ImageDownloader"));
            Function.config.setStringDownloader(yamlMapping.bool("StringDownloader"));
            Function.config.setAutoStaring(yamlMapping.bool("isAutoStaring"));
            Function.config.setAutoStaringMode(yamlMapping.string("AutoStaringMode"));
        } catch (Exception e){
            // e.printStackTrace();

        }

        if (Function.config.getLogFolderPass().isEmpty()){

            try {
                if (Function.config.isDebugOutput()){
                    System.out.println("[Info] ログフォルダの自動取得");
                }
                String LocalAppData = System.getenv().get("LOCALAPPDATA");
                String LinuxUserHome = System.getenv().get("HOME");
                if (Function.ntSystem != null){
                    file = new File("C:\\Users\\"+Function.ntSystem.getName()+"\\AppData\\LocalLow\\VRChat\\VRChat");
                } else if (Function.unixSystem != null) {
                    file = new File("/home/"+Function.unixSystem.getUsername()+"/.steam/steam/steamapps/compatdata/438100/pfx/drive_c/users/steamuser/AppData/LocalLow/VRChat/VRChat");
                }

                if (file.exists()) {
                    Function.config.setLogFolderPass(file.getCanonicalPath());

                    if (Function.config.isDebugOutput()) {
                        System.out.println("[Info] 自動取得成功 : " + file.getCanonicalPath());
                    }
                } else if (Function.ntSystem != null && new File(LocalAppData+"Low\\VRChat\\VRChat").exists()) {
                    file = new File(LocalAppData + "Low\\VRChat\\VRChat");

                    Function.config.setLogFolderPass(file.getCanonicalPath());
                    System.out.println("[Info] 自動取得成功 : " + file.getCanonicalPath());
                } else if (Function.unixSystem != null && new File(LinuxUserHome+"/.steam/steam/steamapps/compatdata/438100/pfx/drive_c/users/steamuser/AppData/LocalLow/VRChat/VRChat").exists()){
                    file = new File(LinuxUserHome+"/.steam/steam/steamapps/compatdata/438100/pfx/drive_c/users/steamuser/AppData/LocalLow/VRChat/VRChat");
                    Function.config.setLogFolderPass(file.getCanonicalPath());
                    System.out.println("[Info] 自動取得成功 : " + file.getCanonicalPath());
                } else {
                    System.out.println("[Info] 自動取得失敗");
                }
            } catch (Exception e) {
                //e.printStackTrace();
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

                    Function.temp_lastLogFile[0] = Function.config.getLogFolderPass() + File.separator + logFileList.getLast();
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