package net.nicovrc.dev;

import com.amihaiemil.eoyaml.Yaml;
import com.amihaiemil.eoyaml.YamlMapping;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.sun.security.auth.module.NTSystem;
import com.sun.security.auth.module.UnixSystem;
import javafx.application.Platform;
import javafx.stage.Stage;
import net.nicovrc.dev.data.ConfigData;

import java.io.*;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
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
                    //System.out.println("debug : 自動起動オフ");
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

        file = new File("./lang/"+lang+".txt");
        if (!file.exists()){
            file = new File("./lang/ja.txt");
        }

        try (HttpClient client = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_2)
                .followRedirects(HttpClient.Redirect.ALWAYS)
                .connectTimeout(Duration.ofSeconds(5))
                .build()) {

            String langText = null;
            if (file.exists()){
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))){
                    String str;
                    StringBuilder sb = new StringBuilder();
                    while ((str = reader.readLine()) != null) {
                        sb.append(str).append("\n");
                    }
                    langText = sb.toString();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            } else {
                try {
                    HttpRequest request = HttpRequest.newBuilder()
                            .uri(new URI("https://raw.githubusercontent.com/nicovrc-net/VRCVideoLogViewer/refs/heads/release/lang/ja.txt"))
                            .headers("User-Agent", Function.UserAgent)
                            .headers("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
                            .headers("Accept-Language", "ja,en;q=0.7,en-US;q=0.3")
                            .GET()
                            .build();
                    HttpResponse<String> send = client.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
                    langText = send.body();

                    FileWriter file1 = new FileWriter("./lang/ja.txt");
                    PrintWriter pw = new PrintWriter(new BufferedWriter(file1));
                    pw.print(langText);
                    pw.close();
                    file1.close();
                    pw = null;
                    file1 = null;
                } catch (Exception e){
                    throw new RuntimeException(e);
                }
            }

            for (String str : langText.split("\n")) {
                Matcher matcher = Function.matcher_langData.matcher(str);
                //System.out.println("debug : " + str);
                if (matcher.find()){
                    //System.out.println("debug : " + matcher.group(1) + " / " + matcher.group(2));
                    Function.langData.add(matcher.group(1), matcher.group(2));
                }
            }

            System.out.println("[Info] "+Function.langData.get("start-message").replaceAll("#version#", Function.Version));
            final boolean isWindowsBatchStart = Function.ntSystem != null;

            file = new File("./tools/openjdk-21.0.2_windows-x64_bin.zip");
            if (file.exists()){
                file.delete();
            }
            file = new File("./tools/openjdk-21.0.2_linux-x64_bin.tar.gz");
            if (file.exists()){
                file.delete();
            }
            file = new File("./tools/openjfx-21.0.10_windows-x64_bin-sdk.zip");
            if (file.exists()){
                file.delete();
            }
            file = new File("./tools/openjfx-21.0.10_linux-x64_bin-sdk.zip");
            if (file.exists()){
                file.delete();
            }

            // フォント存在チェック
            file = new File("./fonts");

            try {
                if (!file.exists()){
                    file.mkdir();
                }
                if (isWindowsBatchStart){
                    file = new File("./fonts/NotoSansJP-Medium.ttf");
                    if (!file.exists()){
                        DownloadFonts(client,"./fonts/NotoSansJP-Medium.ttf");
                    }
                    file = new File("./fonts/NotoSansKR-Medium.ttf");
                    if (!file.exists()){
                        DownloadFonts(client,"./fonts/NotoSansKR-Medium.ttf");
                    }
                    file = new File("./fonts/NotoSansSC-Medium.ttf");
                    if (!file.exists()){
                        DownloadFonts(client,"./fonts/NotoSansSC-Medium.ttf");
                    }
                    file = new File("./fonts/NotoSansTC-Medium.ttf");
                    if (!file.exists()){
                        DownloadFonts(client,"./fonts/NotoSansTC-Medium.ttf");
                    }
                } else {
                    // https://github.com/notofonts/noto-cjk/raw/main/Sans/Variable/OTC/NotoSansCJK-VF.ttf.ttc
                    DownloadFonts(client,"./fonts/NotoSansCJK-Regular.ttc");
                }
            } catch (Exception e){
                throw new RuntimeException(e);
            }

            if (isWindowsBatchStart){
                file = new File("./tools/ImageMagick-7.1.2-12-portable-Q16-x64.7z");
                file.delete();

                file = new File("./tools/update1.bat");
                file.delete();

                file = new File("./tools/update2.bat");
                file.delete();
            }

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
                        move ./lang\\ja.txt ./lang
                        move ./lang\\en.txt ./lang
                        move ./lang\\zh-Hans.txt ./lang
                        move ./lang\\zh-Hant.txt ./lang
                        move ./lang\\ko.txt ./lang
                        exit
                        """;
                        pw.print(str.replaceAll("#ver#", Function.new_version).replaceAll("\\./", CurrentFolderPass.replaceAll("/", "\\\\\\\\")+"\\\\"));
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

            Platform.startup(() -> {
                try {
                    GUI gui = new GUI(true, client);
                    gui.start(new Stage());
                    gui.stop();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });

        }


    }

    private static void DownloadFonts(HttpClient client, String downloadFilename) throws Exception {

        boolean isJsonDownload = false;
        if (downloadFilename.equals("./fonts/NotoSansJP-Medium.ttf") || downloadFilename.equals("./fonts/NotoSansKR-Medium.ttf") || downloadFilename.equals("./fonts/NotoSansSC-Medium.ttf") || downloadFilename.equals("./fonts/NotoSansTC-Medium.ttf")){
            isJsonDownload = true;
        }

        HttpRequest request = null;
        if (isJsonDownload){
            request = HttpRequest.newBuilder()
                    .uri(new URI("https://fonts.google.com/download/list?family=Noto%20Sans%20JP%2CNoto%20Sans%20KR%2CNoto%20Sans%20SC%2CNoto%20Sans%20TC"))
                    .headers("User-Agent", Function.UserAgent)
                    .headers("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
                    .headers("Accept-Language", "ja,en;q=0.7,en-US;q=0.3")
                    .GET()
                    .build();
            HttpResponse<String> send = client.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            //System.out.println("---");
            //System.out.println(send.body());
            //System.out.println("---");
            JsonElement json = Function.gson.fromJson(send.body(), JsonElement.class);

            JsonArray array = json.getAsJsonObject().get("manifest").getAsJsonObject().get("fileRefs").getAsJsonArray();
            for (int i = 0; i < array.size(); i++){
                if  (array.get(i).getAsJsonObject().get("filename").getAsString().split("/")[2].equals(downloadFilename.replaceAll("\\./fonts/", ""))){
                    request = HttpRequest.newBuilder()
                            .uri(new URI(array.get(i).getAsJsonObject().get("url").getAsString()))
                            .headers("User-Agent", Function.UserAgent)
                            .headers("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
                            .headers("Accept-Language", "ja,en;q=0.7,en-US;q=0.3")
                            .GET()
                            .build();

                    HttpResponse<byte[]> send2 = client.send(request, HttpResponse.BodyHandlers.ofByteArray());
                    FileOutputStream stream = new FileOutputStream(downloadFilename);
                    stream.write(send2.body());
                    stream.close();
                    stream = null;
                }
            }

        } else {
            request = HttpRequest.newBuilder()
                    .uri(new URI("https://github.com/notofonts/noto-cjk/raw/main/Sans/Variable/OTC/NotoSansCJK-VF.ttf.ttc"))
                    .headers("User-Agent", Function.UserAgent)
                    .headers("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
                    .headers("Accept-Language", "ja,en;q=0.7,en-US;q=0.3")
                    .GET()
                    .build();
            HttpResponse<byte[]> send = client.send(request, HttpResponse.BodyHandlers.ofByteArray());
            FileOutputStream stream = new FileOutputStream(downloadFilename);
            stream.write(send.body());
            stream.close();
            stream = null;
        }

    }

}