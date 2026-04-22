package net.nicovrc.dev;

import com.amihaiemil.eoyaml.Yaml;
import com.amihaiemil.eoyaml.YamlMapping;
import com.sun.security.auth.module.NTSystem;
import com.sun.security.auth.module.UnixSystem;
import javafx.application.Platform;
import javafx.stage.Stage;

import java.io.*;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Main {

    private static final Pattern matcher_ls = Pattern.compile("/home/(.+)/\\.steampath -> (.+)");

    public static void main(String[] args) {

        try {
            //System.out.println(System.getProperty("os.name").toLowerCase(Locale.ROOT));
            if (System.getProperty("os.name").toLowerCase(Locale.ROOT).equals("linux")){
                Function.unixSystem = new UnixSystem();
            } else if (System.getProperty("os.name").toLowerCase(Locale.ROOT).startsWith("windows")){
                Function.ntSystem = new NTSystem();
            }
        } catch (Exception e){
            e.printStackTrace();
            return;
        }

        String lang = "ja";

        if (args.length == 1){
            if (args[0].equals("--start-Windows")){
                try {
                    Function.StartUp();
                } catch (Exception e){
                    e.printStackTrace();
                }
                return;
            }
            if (args[0].startsWith("--lang:")){
                lang = args[0].replaceFirst("--lang:", "");
            }
        }

        // 言語ファイル
        File file = new File("./lang");
        if (!file.exists()){
            file.mkdir();
        }

        if (args.length == 0){
            file = new File("./config.yml");
            if (file.exists()){
                try {
                    final YamlMapping yamlMapping = Yaml.createYamlInput(new File("./config.yml")).readYamlMapping();
                    lang = yamlMapping.string("lang");
                } catch (Exception e){
                    lang = "ja";
                }
            }
        }

        try (HttpClient client = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_2)
                .followRedirects(HttpClient.Redirect.ALWAYS)
                .connectTimeout(Duration.ofSeconds(5))
                .build()) {

            Function.Main_Init(client, lang);

            System.out.println("[Info] "+Function.langData.get("start-message").replaceAll("#version#", Function.Version));
            System.out.println("[Info] "+Function.langData.get("update-check"));

            try {

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

            try {

                file = new File("./");
                final String CurrentFolderPass = file.getCanonicalPath();

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

                Function.isUpdate = !Function.Version.equals(Function.new_version);

                if (Function.isUpdate){
                    System.out.println("[Info] " + Function.langData.get("update-found"));
                    if (Function.ntSystem != null){
                        File update_file = new File("./tools/update1.bat");
                        if (update_file.exists()){
                            update_file.delete();
                        }
                        update_file = new File("./tools/update2.bat");
                        if (update_file.exists()){
                            update_file.delete();
                        }

                        FileWriter file1 = new FileWriter("./tools/update.bat");
                        PrintWriter pw = new PrintWriter(new BufferedWriter(file1));
                        pw.print("@echo off\npowershell -NoProfile -ExecutionPolicy Unrestricted .\\tools\\update.ps1\nexit");
                        pw.close();
                        file1.close();
                        pw = null;
                        file1 = null;

                        file1 = new FileWriter("./tools/update.ps1");
                        pw = new PrintWriter(new BufferedWriter(file1));
                        String str = """
                        Invoke-WebRequest -Uri https://github.com/nicovrc-net/VRCVideoLogViewer/releases/download/#ver#/VRCVideoLogViewer.zip -OutFile ./tools/VRCVideoLogViewer.zip
                        Expand-Archive -Path ./tools/VRCVideoLogViewer.zip -DestinationPath ./tools/
                        
                        Remove-Item ./tools/VRCVideoLogViewer.zip
                        
                        Remove-Item ./VRCVideoLogViewer-1.0-SNAPSHOT-all.jar
                        Remove-Item ./start.bat
                        if ((Test-Path './start.ps1')) {
                          Remove-Item ./start.ps1
                        }
                        Remove-Item ./lang -Recurse -Force
                        
                        Move-Item -Path ./tools/VRCVideoLogViewer-1.0-SNAPSHOT-all.jar -Destination ./
                        Move-Item -Path ./tools/start.bat -Destination ./
                        Move-Item -Path ./tools/start.ps1 -Destination ./
                        
                        New-Item -ItemType Directory -Path ./lang
                        Move-Item -Path ./tools/lang/* -Destination ./lang
                        
                        Remove-Item ./tools -Recurse -Force
                        exit
                        """;
                        pw.print(str.replaceAll("#ver#", Function.new_version));
                        pw.close();
                        file1.close();
                        pw = null;
                        file1 = null;
                    }
                } else {
                    System.out.println("[Info] "+Function.langData.get("update-notfound").replaceAll("#nowver#", Function.Version).replaceAll("#newver#", Function.new_version));
                }
            } catch (Exception e){
                e.printStackTrace();
                Function.timer1.cancel();
                Function.timer2.cancel();
                return;
            }

            System.out.println("[Info] "+Function.langData.get("config-check"));
            file = new File("./config.yml");

            if (!file.exists()){
                System.out.println("[Info] "+Function.langData.get("config-autocreate"));

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
                        System.out.println("[Info] "+Function.langData.get("logfolder-autoget"));
                    }
                    String LocalAppData = System.getenv().get("LOCALAPPDATA");
                    String LinuxUserHome = System.getenv().get("HOME");
                    if (Function.ntSystem != null){
                        file = new File("C:\\Users\\"+Function.ntSystem.getName()+"\\AppData\\LocalLow\\VRChat\\VRChat");
                    } else if (Function.unixSystem != null) {
                        file = new File("/home/"+Function.unixSystem.getUsername()+"/.steam/steam/steamapps/compatdata/438100/pfx/drive_c/users/steamuser/AppData/LocalLow/VRChat/VRChat");
                    }

                    String path = file.getCanonicalPath();
                    if (file.exists()) {
                        Function.config.setLogFolderPass(path);

                        if (Function.config.isDebugOutput()) {
                            if (Function.ntSystem != null){
                                System.out.println("[Info] " + Function.langData.get("logfolder-autoget-success").replaceAll("#folder_pass#", path.replaceAll(Pattern.quote("\\"), "/").replaceAll("/", "\\\\\\\\")));
                            } else {
                                System.out.println("[Info] " + Function.langData.get("logfolder-autoget-success").replaceAll("#folder_pass#", path));
                            }
                        }
                    } else if (Function.ntSystem != null && new File(LocalAppData+"Low\\VRChat\\VRChat").exists()) {
                        file = new File(LocalAppData + "Low\\VRChat\\VRChat");
                        path = file.getCanonicalPath();

                        Function.config.setLogFolderPass(path);
                        System.out.println("[Info] " + Function.langData.get("logfolder-autoget-success").replaceAll("#folder_pass#", path.replaceAll(Pattern.quote("\\"), "/").replaceAll("/", "\\\\\\\\")));
                    } else if (new File(LinuxUserHome+"/.steam/steam/steamapps/compatdata/438100/pfx/drive_c/users/steamuser/AppData/LocalLow/VRChat/VRChat").exists()){
                        file = new File(LinuxUserHome+"/.steam/steam/steamapps/compatdata/438100/pfx/drive_c/users/steamuser/AppData/LocalLow/VRChat/VRChat");
                        path = file.getCanonicalPath();

                        Function.config.setLogFolderPass(path);
                        System.out.println("[Info] " + Function.langData.get("logfolder-autoget-success").replaceAll("#folder_pass#", path));
                    } else if (!LinuxUserHome.isEmpty()) {
                        file = new File(LinuxUserHome+"/.steampath");

                        ProcessBuilder pb = new ProcessBuilder("/bin/bash", "-c", "ls -al "+file.getCanonicalPath());
                        Process process = pb.start();
                        process.waitFor();

                        String s = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
                        //System.out.println(s);

                        Matcher matcher = matcher_ls.matcher(s);
                        if (matcher.find()){
                            path = matcher.group(2).replaceAll("/sdk32/steam", "/steam/steamapps/compatdata/438100/pfx/drive_c/users/steamuser/AppData/LocalLow/VRChat/VRChat");
                            file = new File(path);
                        }

                        if (file.exists()){
                            Function.config.setLogFolderPass(path);
                            System.out.println("[Info] " + Function.langData.get("logfolder-autoget-success").replaceAll("#folder_pass#", path));
                        } else {
                            System.out.println("[Info] " + Function.langData.get("logfolder-autoget-fail"));
                        }

                    } else {
                        System.out.println("[Info] " + Function.langData.get("logfolder-autoget-fail"));
                    }
                } catch (Exception e) {
                    //e.printStackTrace();
                }


            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        Platform.startup(() -> {
            try {
                GUI gui = new GUI(true);
                gui.start(new Stage());
                gui.stop();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

    }

}