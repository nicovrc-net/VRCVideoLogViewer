package net.nicovrc.dev;

import com.amihaiemil.eoyaml.Yaml;
import com.amihaiemil.eoyaml.YamlMapping;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.sun.security.auth.module.NTSystem;
import com.sun.security.auth.module.UnixSystem;
import net.nicovrc.dev.data.ConfigData;
import net.nicovrc.dev.data.LangData;

import java.io.*;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class Function {

    public static final String Version = "1.3.0";

    public static final Gson gson = new Gson();
    public static final String UserAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:149.0) Gecko/20100101 Firefox/149.0 VRCVideoLogViewer/"+Version;
    public static final String Unity_UserAgent = "UnityPlayer/2022.3.22f1-DWR (UnityWebRequest/1.0, libcurl/8.5.0-DEV)";
    public static final String HTTP_x_unity_version = "2022.3.22f1-DWR";

    public static final String configText = """
                # 言語設定 (Language Settings)
                lang: 'ja'
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
                # 自動起動するか (Windowsのみ) (Enable auto-start (Windows only))
                isAutoStaring: false
                # 自動起動タイミング (Windowsのみ) (Auto-start timing (Windows only))
                AutoStaringMode: ''
                """;

    public static final ConfigData config = new ConfigData();

    public static LangData langData = new LangData();
    public static final Pattern matcher_langData = Pattern.compile("(.+)=\"(.+)\"");

    public static final Timer timer1 = new Timer();
    public static final Timer timer2 = new Timer();

    public static boolean isUpdate = false;

    public static NTSystem ntSystem = null;
    public static UnixSystem unixSystem = null;
    public static final Runtime runtime = Runtime.getRuntime();

    public static final SimpleDateFormat file_sdf = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
    public static final SimpleDateFormat log_sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public static final Pattern matcher_version = Pattern.compile("<id>tag:github\\.com,2008:Repository/(\\d+)/(.+)</id>");

    public static List<String> logFileList = null;
    public static final String[] temp_lastLogFile = {null};

    public static final HashMap<String, LogData> logDataList = new HashMap<>();

    public static String new_version = Function.Version;

    public static boolean isTimerRun = true;

    private static final Pattern matcher_VideoLog = Pattern.compile("(\\d+)\\.(\\d+)\\.(\\d+) (\\d+):(\\d+):(\\d+) Debug      -  \\[Video Playback\\] Attempting to resolve URL '(.+)'");
    private static final Pattern matcher_VideoLog2 = Pattern.compile("(\\d+)\\.(\\d+)\\.(\\d+) (\\d+):(\\d+):(\\d+) Debug      -  \\[Video Playback\\] Resolving URL '(.+)'");
    private static final Pattern matcher_ImageLog = Pattern.compile("(\\d+)\\.(\\d+)\\.(\\d+) (\\d+):(\\d+):(\\d+) Debug      -  \\[Image Download\\] Attempting to load image from URL '(.+)'");
    private static final Pattern matcher_StringLog = Pattern.compile("(\\d+)\\.(\\d+)\\.(\\d+) (\\d+):(\\d+):(\\d+) Debug      -  \\[String Download\\] Attempting to load String from URL '(.+)'");
    private static final Pattern matcher_worldid = Pattern.compile("(\\d+)\\.(\\d+)\\.(\\d+) (\\d+):(\\d+):(\\d+) Debug      -  \\[Behaviour\\] Destination requested: (.+):(\\d+)~region\\((.+)\\)");
    private static final Pattern matcher_worldid2 = Pattern.compile("(\\d+)\\.(\\d+)\\.(\\d+) (\\d+):(\\d+):(\\d+) Debug      -  \\[Behaviour\\] Destination requested: (.+):(\\d+)~group\\((.+)~groupAccessType((.+))~region\\((.+)\\)");
    private static final Pattern matcher_worldid3 = Pattern.compile("2026.02.20 12:09:41 Debug      -  [Behaviour] Destination fetching: (.+):(\\d+)~hidden\\((.+)\\)~region\\((.+)\\)");

    private static final Pattern matcher_VideoErrorLog = Pattern.compile("(\\d+)\\.(\\d+)\\.(\\d+) (\\d+):(\\d+):(\\d+) Warning    -  \\[Video Playback\\] (.+)");
    private static final Pattern matcher_VideoErrorLog2 = Pattern.compile("(\\d+)\\.(\\d+)\\.(\\d+) (\\d+):(\\d+):(\\d+) Error      -  \\[AVProVideo\\] (.+)");
    private static final Pattern matcher_ImageErrorLog = Pattern.compile("(\\d+)\\.(\\d+)\\.(\\d+) (\\d+):(\\d+):(\\d+) Debug      -  \\[Image Download\\] A web request exception occurred while loading image from URL '(.+)'\\. Exception: (.+)");
    private static final Pattern matcher_StringErrorLog = Pattern.compile("(\\d+)\\.(\\d+)\\.(\\d+) (\\d+):(\\d+):(\\d+) Debug      -  \\[String Download\\] A web request exception occurred while loading string from URL '(.+)'\\. Exception: (.+)");

    private static final Pattern matcher_ImageMagickLast = Pattern.compile("https://imagemagick\\.org/archive/binaries/ImageMagick-(\\d+)\\.(\\d+)\\.(\\d+)-(\\d+)-portable-Q16-x64\\.7z");

    private static final SimpleDateFormat logDate = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss");

    public static final String[] iso639_1 = {"aa", "ab", "af", "ak", "sq", "am", "ar", "an", "hy", "as", "av", "ae", "ay", "az", "ba", "bm", "eu", "be", "bn", "bi", "bo", "bs", "br", "bg", "my", "ca", "cs", "ch", "ce", "zh-Hans", "zh-Hant", "cu", "cv", "kw", "co", "cr", "cy", "cs", "da", "de", "dv", "nl", "dz", "el", "en", "eo", "et", "eu", "ee", "fo", "fa", "fj", "fi", "fr", "fr", "fy", "ff", "ka", "de", "gd", "ga", "gl", "gv", "el", "gn", "gu", "ht", "ha", "he", "hz", "hi", "ho", "hr", "hu", "hy", "ig", "is", "io", "ii", "iu", "ie", "ia", "id", "ik", "is", "it", "jv", "ja", "kl", "kn", "ks", "ka", "kr", "kk", "km", "ki", "rw", "ky", "kv", "kg", "ko", "kj", "ku", "lo", "la", "lv", "li", "ln", "lt", "lb", "lu", "lg", "mk", "mh", "ml", "mi", "mr", "ms", "mk", "mg", "mt", "mn", "mi", "ms", "my", "na", "nv", "nr", "nd", "ng", "ne", "nl", "nn", "nb", "no", "ny", "oc", "oj", "or", "om", "os", "pa", "fa", "pi", "pl", "pt", "ps", "qu", "rm", "ro", "ro", "rn", "ru", "sg", "sa", "si", "sk", "sk", "sl", "se", "sm", "sn", "sd", "so", "st", "es", "sq", "sc", "sr", "ss", "su", "sw", "sv", "ty", "ta", "tt", "te", "tg", "tl", "th", "bo", "ti", "to", "tn", "ts", "tk", "tr", "tw", "ug", "uk", "ur", "uz", "ve", "vi", "vo", "cy", "wa", "wo", "xh", "yi", "yo", "za", "zh", "zu"};

    public static boolean isFoundFile(String filePass) {
        Path path = Paths.get(filePass);
        return Files.exists(path);
    }

    public static boolean isFoundFolder(String folderPass) {
        Path path = Paths.get(folderPass);
        return Files.exists(path);
    }

    public static boolean createFolder(String filePass) {
        if (!isFoundFile(filePass)) {
            try {
                Files.createDirectory(Path.of(filePass));
                return true;
            } catch (IOException e) {
                return false;
            }
        }
        return false;
    }

    public static String getFileByText(String filePass, Charset charset) {

        if (charset == null) {
            charset = StandardCharsets.UTF_8;
        }

        byte[] binary = getFileByBinary(filePass);
        if (binary == null) {
            return null;
        }

        return new String(binary, charset);
    }

    public static byte[] getFileByBinary(String filePass){
        final Path path = Path.of(filePass);
        try {
            return Files.readAllBytes(path);
        } catch (Exception e) {
            return null;
        }
    }

    public static boolean writeFile(String filePass, String content, Charset charset) {
        return writeFile(filePass, content.getBytes(charset));
    }

    public static boolean writeFile(String filePass, byte[] content) {
        Path path = Paths.get(filePass);

        try (OutputStream out = new BufferedOutputStream(Files.newOutputStream(path))) {
            out.write(content);
            out.flush();
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    public static boolean deleteFile(String filePass) {
        Path path = Paths.get(filePass);
        try {
            Files.delete(path);
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    public static boolean deleteFolder(String folderPass) {
        Path targetDir = Paths.get(folderPass);

        try {
            if (Files.exists(targetDir)) {
                try (var paths = Files.walk(targetDir)) {
                    paths.sorted(Comparator.reverseOrder())
                            .forEach(path -> {
                                try {
                                    Files.delete(path);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            });
                }
                return true;
            }
        } catch (IOException e) {
            //e.printStackTrace();
            return false;
        }

        return false;
    }

    public static String getFullPath(String filePass) {
        Path path = Paths.get(filePass);
        return path.toAbsolutePath().toString();
    }

    public static String getTextForFile(String filePass){
        String logText = getFileByText(filePass, StandardCharsets.UTF_8);
        return logText;
    }

    public static List<String> getFileList(String FolderPass) throws Exception {
        List<String> logFileList = new ArrayList<>();

        try (Stream<Path> stream = Files.list(Paths.get(FolderPass))) {
            stream.forEach(p -> {
                if (p.getFileName().toString().startsWith("output_log_")){
                    logFileList.add(p.getFileName().toString());
                }
            });
        } catch(IOException e) {
            System.out.println(e);
        }

        return logFileList;
    }

    public static List<String> ListSort(List<String> list) throws Exception {
        List<String> temp = new ArrayList<>();

        String[] temp1 = new String[list.size()];
        long[] temp2 = new long[list.size()];
        int i = 0;
        for (String s : list) {
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
        return temp;
    }

    public static int getFileListCount(String FolderPass) throws Exception {
        return getFileList(FolderPass).size();
    }

    public static List<LogData> getLogForURL(String logText) throws Exception{
        ArrayList<LogData> logData = new ArrayList<>();
        HashMap<String, String> errorList = new HashMap<>();
        try (HttpClient client = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_2)
                .followRedirects(HttpClient.Redirect.ALWAYS)
                .connectTimeout(Duration.ofSeconds(5))
                .build()) {

            for (String s : logText.split("\n")) {

                boolean isWorld = false;
                boolean isAdd = false;

                Matcher video = matcher_VideoLog.matcher(s);
                Matcher video2 = matcher_VideoLog2.matcher(s);
                Matcher image = matcher_ImageLog.matcher(s);
                Matcher string = matcher_StringLog.matcher(s);
                Matcher worldId1 = matcher_worldid.matcher(s);
                Matcher worldId2 = matcher_worldid2.matcher(s);
                Matcher worldId3 = matcher_worldid3.matcher(s);

                Matcher video_error = matcher_VideoErrorLog.matcher(s);
                Matcher video_error2 = matcher_VideoErrorLog2.matcher(s);
                Matcher image_error = matcher_ImageErrorLog.matcher(s);
                Matcher string_error = matcher_StringErrorLog.matcher(s);

                LogData data = new LogData();
                if (video_error.find()){
                    logData.getLast().setErrorMessage(video_error.group(7));
                }
                if (video_error2.find()){
                    String group = video_error2.group(7);
                    if (group.startsWith("Error: ")){
                        if (logData.getLast().getErrorMessage() == null || logData.getLast().getErrorMessage().isEmpty()){
                            logData.getLast().setErrorMessage(group);
                        } else {
                            logData.getLast().setErrorMessage(logData.getLast().getErrorMessage() + "\n" + group);
                        }
                    }
                }

                if (image_error.find()){
                    //System.out.println(image_error.group(7) + " : " + image_error.group(8));
                    if (errorList.get(image_error.group(7)) == null || errorList.get(image_error.group(7)).isEmpty()) {
                        errorList.put(image_error.group(7), image_error.group(8));
                    } else {
                        errorList.put(image_error.group(7), errorList.get(image_error.group(7)) + "\n" + image_error.group(8));
                    }
                }

                if (string_error.find()){
                    //System.out.println(string_error.group(7) + " : " + string_error.group(8));
                    if (errorList.get(string_error.group(7)) == null || errorList.get(string_error.group(7)).isEmpty()) {
                        errorList.put(string_error.group(7), string_error.group(8));
                    } else {
                        errorList.put(string_error.group(7), errorList.get(image_error.group(7)) + "\n" + string_error.group(8));
                    }
                }

                if (video.find()){
                    String tempDate = video.group(1)+"."+video.group(2)+"."+video.group(3)+" "+video.group(4)+":"+video.group(5)+":"+video.group(6);
                    data.setLogDate(logDate.parse(tempDate));
                    data.setURL(video.group(7));
                    data.setErrorMessage(null);
                    data.setURLType("Video");
                    isAdd = true;
                }

                if (video2.find()){
                    String tempDate = video2.group(1)+"."+video2.group(2)+"."+video2.group(3)+" "+video2.group(4)+":"+video2.group(5)+":"+video2.group(6);
                    data.setLogDate(logDate.parse(tempDate));
                    data.setURL(video2.group(7));
                    data.setErrorMessage(null);
                    data.setURLType("Video");
                    isAdd = true;
                }

                if (image.find()){
                    String tempDate = image.group(1)+"."+image.group(2)+"."+image.group(3)+" "+image.group(4)+":"+image.group(5)+":"+image.group(6);
                    data.setLogDate(logDate.parse(tempDate));
                    data.setURL(image.group(7));
                    //System.out.println(image.group(7) + " : " + errorList.get(image.group(7)));
                    if (errorList.get(image.group(7)) != null){
                        data.setErrorMessage(errorList.get(image.group(7)));
                        errorList.remove(image.group(7));
                    } else {
                        data.setErrorMessage(null);
                    }
                    data.setURLType("Image");
                    isAdd = true;
                }
                if (string.find()){
                    String tempDate = string.group(1)+"."+string.group(2)+"."+string.group(3)+" "+string.group(4)+":"+string.group(5)+":"+string.group(6);
                    data.setLogDate(logDate.parse(tempDate));
                    data.setURL(string.group(7));
                    //System.out.println(string.group(7) + " : " + errorList.get(string.group(7)));
                    if (errorList.get(string.group(7)) != null){
                        data.setErrorMessage(errorList.get(string.group(7)));
                        errorList.remove(string.group(7));
                    } else {
                        data.setErrorMessage(null);
                    }
                    data.setURLType("String");
                    isAdd = true;
                }
                if (worldId1.find()){
                    isWorld = true;
                    HttpRequest request = HttpRequest.newBuilder()
                            .uri(new URI("https://api.vrchat.cloud/api/1/worlds/"+worldId1.group(7)))
                            .headers("User-Agent", Function.UserAgent)
                            .headers("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
                            .headers("Accept-Language", "ja,en;q=0.7,en-US;q=0.3")
                            .GET()
                            .build();

                    HttpResponse<String> send = client.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
                    //System.out.println(send.body());
                    JsonElement json = Function.gson.fromJson(send.body(), JsonElement.class);
                    if (json.getAsJsonObject().has("name")){
                        data.setWorldName(json.getAsJsonObject().get("name").getAsString().replaceAll("\u2024", "."));
                    }

                    String tempDate = worldId1.group(1)+"."+worldId1.group(2)+"."+worldId1.group(3)+" "+worldId1.group(4)+":"+worldId1.group(5)+":"+worldId1.group(6);
                    data.setLogDate(logDate.parse(tempDate));
                    data.setInstanceType("public");
                    data.setInstanceId(worldId1.group(8));
                    data.setURL("https://vrchat.com/home/world/"+worldId1.group(7));
                    data.setWorldId(worldId1.group(7));
                    data.setURLType("World");
                    isAdd = true;
                }
                if (worldId2.find()){
                    isWorld = true;
                    HttpRequest request = HttpRequest.newBuilder()
                            .uri(new URI("https://api.vrchat.cloud/api/1/worlds/"+worldId2.group(7)))
                            .headers("User-Agent", Function.UserAgent)
                            .headers("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
                            .headers("Accept-Language", "ja,en;q=0.7,en-US;q=0.3")
                            .GET()
                            .build();

                    HttpResponse<String> send = client.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
                    //System.out.println(send.body());
                    JsonElement json = Function.gson.fromJson(send.body(), JsonElement.class);
                    if (json.getAsJsonObject().has("name")){
                        data.setWorldName(json.getAsJsonObject().get("name").getAsString().replaceAll("\u2024", "."));
                    }

                    String tempDate = worldId2.group(1)+"."+worldId2.group(2)+"."+worldId2.group(3)+" "+worldId2.group(4)+":"+worldId2.group(5)+":"+worldId2.group(6);
                    data.setLogDate(logDate.parse(tempDate));
                    data.setURL("https://vrchat.com/home/world/"+worldId2.group(7));
                    data.setWorldId(worldId2.group(7));
                    data.setURLType("World");
                    data.setInstanceType("group");
                    data.setInstanceId(worldId2.group(8));
                    isAdd = true;
                }
                if (worldId3.find()){
                    isWorld = true;
                    HttpRequest request = HttpRequest.newBuilder()
                            .uri(new URI("https://api.vrchat.cloud/api/1/worlds/"+worldId3.group(7)))
                            .headers("User-Agent", Function.UserAgent)
                            .headers("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
                            .headers("Accept-Language", "ja,en;q=0.7,en-US;q=0.3")
                            .GET()
                            .build();

                    HttpResponse<String> send = client.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
                    //System.out.println(send.body());
                    JsonElement json = Function.gson.fromJson(send.body(), JsonElement.class);
                    if (json.getAsJsonObject().has("name")){
                        data.setWorldName(json.getAsJsonObject().get("name").getAsString().replaceAll("\u2024", "."));
                    }

                    String tempDate = worldId3.group(1)+"."+worldId3.group(2)+"."+worldId3.group(3)+" "+worldId3.group(4)+":"+worldId3.group(5)+":"+worldId3.group(6);
                    data.setLogDate(logDate.parse(tempDate));
                    data.setURL("https://vrchat.com/home/world/"+worldId3.group(7));
                    data.setWorldId(worldId3.group(7));
                    data.setURLType("World");
                    data.setInstanceType("group");
                    data.setInstanceId(worldId3.group(8));
                    isAdd = true;
                }

                if (isAdd){
                    //System.out.println("debug : " + data.getLogDate());
                    if (!isWorld){
                        data.setViewText("[" + log_sdf.format(data.getLogDate()) + "] " + data.getURL() + " (" + data.getURLType() + ")");
                    } else {
                        data.setViewText("[" + log_sdf.format(data.getLogDate()) + "] " + langData.get("world").replaceAll("#worldid#", data.getWorldId()).replaceAll("#worldname#", data.getWorldName()));
                    }

                    data.setLogId(UUID.randomUUID().toString()+"-"+new Date().getTime());
                    logData.add(data);
                }
            }
        }
        return logData;
    }

    public static VideoData getVideoData(String url){
        final VideoData data = new VideoData();

        try (HttpClient client = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_2)
                .followRedirects(HttpClient.Redirect.ALWAYS)
                .connectTimeout(Duration.ofSeconds(5))
                .build()) {

            String tempUrl = replaceURL(url);

            if (tempUrl.startsWith("http://youtu.be") || tempUrl.startsWith("https://youtu.be") || tempUrl.startsWith("https://nico.ms")){
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(new URI(tempUrl))
                        .headers("User-Agent", Function.UserAgent)
                        .headers("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
                        .headers("Accept-Language", "ja,en;q=0.7,en-US;q=0.3")
                        .GET()
                        .build();
                HttpResponse<String> send = client.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
                tempUrl = send.uri().toURL().toString();

                if (tempUrl.matches(".*si=.*")){
                    tempUrl = tempUrl.replaceAll("si=(.+)&v=", "v=");
                }
            }

            //System.out.println("https://nicovrc.net/api/v1/videoinfo?apikey=vrcvideologviewer&url="+tempUrl);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(new URI("https://nicovrc.net/api/v1/videoinfo?apikey=vrcvideologviewer&url="+tempUrl))
                    .headers("User-Agent", Function.UserAgent)
                    .headers("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
                    .headers("Accept-Language", "ja,en;q=0.7,en-US;q=0.3")
                    .GET()
                    .build();

            HttpResponse<String> send = client.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            try {
                JsonElement json = gson.fromJson(send.body(), JsonElement.class);

                if (!json.isJsonObject() || json.getAsJsonObject().has("ErrorMessage")){
                    data.setVideoTitle("取得失敗");
                    data.setThumbnail(null);
                    //System.out.println("[Debug] 取得失敗");
                    return data;
                }

                if (json.getAsJsonObject().has("Title")){
                    data.setVideoTitle(json.getAsJsonObject().get("Title").getAsString());
                }

                if (tempUrl.startsWith("https://www.youtube.com")){

                    request = HttpRequest.newBuilder()
                            .uri(new URI("https://i2i.nicovrc.net/?url="+tempUrl))
                            .headers("User-Agent", Function.UserAgent)
                            .headers("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
                            .headers("Accept-Language", "ja,en;q=0.7,en-US;q=0.3")
                            .GET()
                            .build();

                    HttpResponse<byte[]> send2 = client.send(request, HttpResponse.BodyHandlers.ofByteArray());
                    data.setThumbnail(send2.body());

                } else if (json.getAsJsonObject().has("Thumbnail") || json.getAsJsonObject().has("thumbnail")) {

                    request = HttpRequest.newBuilder()
                            .uri(new URI(json.getAsJsonObject().has("Thumbnail") ? json.getAsJsonObject().get("Thumbnail").getAsString() : json.getAsJsonObject().get("thumbnail").getAsString()))
                            .headers("User-Agent", Function.UserAgent)
                            .headers("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
                            .headers("Accept-Language", "ja,en;q=0.7,en-US;q=0.3")
                            .GET()
                            .build();

                    HttpResponse<byte[]> send2 = client.send(request, HttpResponse.BodyHandlers.ofByteArray());

                    if (send2.headers().firstValue("content-type").isPresent() && send2.headers().firstValue("content-type").get().endsWith("webp")){
                        data.setThumbnail(Webp2PngConverter(send2.body()));
                    } else {
                        data.setThumbnail(send2.body());
                    }

                }
            } catch (Exception e){
                data.setVideoTitle("取得失敗");
                data.setThumbnail(null);
                //System.out.println("[Debug] 取得失敗");
                return data;
            }

        } catch (Exception e){
            e.printStackTrace();
            data.setVideoTitle(null);
            data.setThumbnail(null);
        }

        //System.out.println("[Debug] 取得成功");
        return data;
    }

    public static byte[] Webp2PngConverter(byte[] input) throws Exception {
        byte[] output = null;

        if (!isFoundFolder("./temp")){
            createFolder("./temp");
        }

        String filename = new Date().getTime()+"_"+ UUID.randomUUID().toString().split("-")[0];
        if (isFoundFile("./temp/"+filename+".webp")){
            deleteFile("./temp/"+filename+".webp");
        }
        writeFile("./temp/"+filename+".webp", input);

        final Process exec0;
        if (ntSystem != null){
            exec0 = runtime.exec(new String[]{".\\tools\\ImageMagick\\magick.exe", "./temp/"+filename+".webp", "./temp/"+filename+".png"});
        } else {
            exec0 = runtime.exec(new String[]{"./tools/ImageMagick/magick", "./temp/"+filename+".webp", "./temp/"+filename+".png"});
        }
        Thread.ofVirtual().start(()->{
            try {
                Thread.sleep(5000L);
                exec0.destroy();
            } catch (Exception e){
                // e.printStackTrace();
            }
        });
        exec0.waitFor();

        output = getFileByBinary("./temp/" + filename + ".png");
        deleteFile("./temp/" + filename + ".webp");
        deleteFile("./temp/" + filename + ".png");

        return output;
    }

    private static String replaceURL(String url){
        String tmp = url;

        tmp = tmp.replaceAll("http://nicovrc\\.net/\\?url=", "");
        tmp = tmp.replaceAll("http://nicovrc\\.net/proxy/\\?", "");
        tmp = tmp.replaceAll("http://nicovrc\\.net/\\?vi=", "");
        tmp = tmp.replaceAll("https://nicovrc\\.net/\\?url=", "");
        tmp = tmp.replaceAll("https://nicovrc\\.net/proxy/\\?", "");
        tmp = tmp.replaceAll("https://nicovrc\\.net/\\?vi=", "");
        tmp = tmp.replaceAll("http://nicovrc\\.net/proxy/dummy\\.m3u8\\?(.+)", "");
        tmp = tmp.replaceAll("https://nicovrc\\.net/proxy/dummy\\.m3u8\\?(.+)", "");

        tmp = tmp.replaceAll("http://yt\\.8uro\\.net/r\\?v=", "");
        tmp = tmp.replaceAll("https://yt\\.8uro\\.net/r\\?v=", "");
        tmp = tmp.replaceAll("http://vrc\\.kuroneko6423\\.com/proxy\\?url=", "");
        tmp = tmp.replaceAll("https://vrc\\.kuroneko6423\\.com/proxy\\?url=", "");
        tmp = tmp.replaceAll("http://kvvs\\.net/proxy\\?url=", "");
        tmp = tmp.replaceAll("https://kvvs\\.net/proxy\\?url=", "");
        tmp = tmp.replaceAll("http://questify\\.dev/\\?url=", "");
        tmp = tmp.replaceAll("https://questify\\.dev/\\?url=", "");
        tmp = tmp.replaceAll("http://questing\\.thetechnolus\\.com/v\\?url=", "");
        tmp = tmp.replaceAll("https://questing\\.thetechnolus\\.com/v\\?url=", "");
        tmp = tmp.replaceAll("http://questing\\.thetechnolus\\.com/", "");
        tmp = tmp.replaceAll("https://questing\\.thetechnolus\\.com/", "");
        tmp = tmp.replaceAll("http://vq\\.vrcprofile\\.com/\\?url=", "");
        tmp = tmp.replaceAll("https://vq\\.vrcprofile\\.com/\\?url=", "");
        tmp = tmp.replaceAll("http://api\\.yamachan\\.moe/proxy\\?url=", "");
        tmp = tmp.replaceAll("https://api\\.yamachan\\.moe/proxy\\?url=", "");
        tmp = tmp.replaceAll("http://nico\\.7mi\\.site/proxy/\\?", "");
        tmp = tmp.replaceAll("https://nico\\.7mi\\.site/proxy/\\?", "");
        tmp = tmp.replaceAll("http://nico\\.7mi\\.site/proxy/dummy\\.m3u8\\?", "");
        tmp = tmp.replaceAll("https://nico\\.7mi\\.site/proxy/dummy\\.m3u8\\?", "");
        tmp = tmp.replaceAll("http://qst\\.akakitune87\\.net/q\\?url=", "");
        tmp = tmp.replaceAll("https://qst\\.akakitune87\\.net/q\\?url=", "");
        tmp = tmp.replaceAll("http://u2b\\.cx/", "");
        tmp = tmp.replaceAll("https://u2b\\.cx/", "");
        tmp = tmp.replaceAll("https://k\\.0cm\\.org/\\?url=", "");

        tmp = tmp.replaceAll("http://shay\\.loan/", "https://youtu.be/");
        tmp = tmp.replaceAll("https://shay\\.loan/", "https://youtu.be/");
        tmp = tmp.replaceAll("http://questing\\.thetechnolus\\.com/watch\\?v=", "https://youtu.be/");
        tmp = tmp.replaceAll("https://questing\\.thetechnolus\\.com/watch\\?v=", "https://youtu.be/");
        tmp = tmp.replaceAll("http://questing\\.thetechnolus\\.com/v/", "https://youtu.be/");
        tmp = tmp.replaceAll("https://questing\\.thetechnolus\\.com/v/", "https://youtu.be/");
        tmp = tmp.replaceAll("http://youtube\\.irunu\\.co/watch\\?v=", "https://youtu.be/");
        tmp = tmp.replaceAll("https://youtube\\.irunu\\.co/watch\\?v=", "https://youtu.be/");

        tmp = tmp.replaceAll("http://www\\.nicovideo\\.life/watch\\?v=", "https://nico.ms/");
        tmp = tmp.replaceAll("https://www\\.nicovideo\\.life/watch\\?v=", "https://nico.ms/");
        tmp = tmp.replaceAll("http://live\\.nicovideo\\.life/watch\\?v=", "https://nico.ms/");
        tmp = tmp.replaceAll("https://live\\.nicovideo\\.life/watch\\?v=", "https://nico.ms/");
        tmp = tmp.replaceAll("https://shinchan\\.biz/player\\.html\\?video_id=", "https://nico.ms/");
        tmp = tmp.replaceAll("https://k\\.0cm\\.org/\\?u=nico\\.ms%2F", "https://nico.ms/");
        tmp = tmp.replaceAll("https://www\\.nicozon\\.net/player\\.html\\?video_id=", "https://nico.ms/");
        tmp = tmp.replaceAll("http://suzumebachi\\.xyz:1323/go/", "https://nico.ms/");
        tmp = tmp.replaceAll("http://suzumebachi\\.xyz:1323/tmsk/", "https://nico.ms/");

        return tmp;

    }

    public static void SettingConfig(ConfigData config){

        String configText = """
                # 言語設定 (Language Settings)
                lang: '#lang#'
                # VRChat ログフォルダパス (VRChat log folder path)
                logfolder: '#logfolder#'
                # デバッグログを表示するか (Enable debug log display?)
                debugOutput: #debug#
                # 過去のログから取得して表示するか (Display data from previous logs?)
                oldLogCheck: #oldcheck#
                # 動画プレーヤーのログを表示するか (Enable video player log display?)
                VideoPlayer: #videoplayer#
                # ImageDownloaderのログを表示するか (Enable ImageDownloader log display?)
                ImageDownloader: #image#
                # StringDownloaderのログを表示するか (Enable StringDownloader log display?)
                StringDownloader: #string#
                # 自動起動するか (Windowsのみ) (Enable auto-start (Windows only))
                isAutoStaring: #autoflag#
                # 自動起動タイミング (Windowsのみ) (Auto-start timing (Windows only))
                AutoStaringMode: '#autotiming#'
                """;

        configText = configText.replaceAll("#lang#", config.getLang());
        if (ntSystem != null){
            configText = configText.replaceAll("#logfolder#", config.getLogFolderPass().replaceAll(Pattern.quote("\\"), "/").replaceAll("/", "\\\\\\\\"));
        } else {
            configText = configText.replaceAll("#logfolder#", config.getLogFolderPass());
        }

        configText = configText.replaceAll("#debug#", (config.isDebugOutput()+"").toLowerCase(Locale.ROOT));
        configText = configText.replaceAll("#oldcheck#", (config.isOldLogCheck()+"").toLowerCase(Locale.ROOT));
        configText = configText.replaceAll("#videoplayer#", (config.isVideoPlayer()+"").toLowerCase(Locale.ROOT));
        configText = configText.replaceAll("#image#", (config.isImageDownloader()+"").toLowerCase(Locale.ROOT));
        configText = configText.replaceAll("#string#", (config.isStringDownloader()+"").toLowerCase(Locale.ROOT));
        configText = configText.replaceAll("#autoflag#", (config.isAutoStaring()+"").toLowerCase(Locale.ROOT));
        configText = configText.replaceAll("#autotiming#", config.getAutoStaringMode());

        Function.config.setLang(config.getLang());
        Function.config.setLogFolderPass(config.getLogFolderPass());
        Function.config.setDebugOutput(config.isDebugOutput());
        Function.config.setOldLogCheck(config.isOldLogCheck());
        Function.config.setVideoPlayer(config.isVideoPlayer());
        Function.config.setImageDownloader(config.isImageDownloader());
        Function.config.setStringDownloader(config.isStringDownloader());
        Function.config.setAutoStaring(config.isAutoStaring());
        Function.config.setAutoStaringMode(config.getAutoStaringMode());

        if (isFoundFile("./config.yml")){
            deleteFile("./config.yml");
        }
        writeFile("./config.yml", configText, StandardCharsets.UTF_8);

        // TODO Linux環境での自動起動を調べてそのうち実装する
        if (ntSystem == null){
            return;
        }

        if (config.isAutoStaring()){
            String path = "";
            path = getFullPath("./").replaceAll(Pattern.quote("\\"), "/").replaceAll("/", "\\\\\\\\");

            String batText = """
                    cd /d #path#
                    start #path#/tools/jdk-21/bin/javaw.exe -jar ./VRCVideoLogViewer-1.0-SNAPSHOT-all.jar --start-Windows
                    """;

            // C:\Users\xxx\AppData\Roaming
            // C:\Users\xxx\AppData\Roaming\Microsoft\Windows\Start Menu\Programs\Startup

            if (!isFoundFolder("C:\\Users\\" + ntSystem.getName() + "\\AppData\\Roaming\\Microsoft\\Windows\\Start Menu\\Programs\\Startup")){
                String AppData = System.getenv().get("APPDATA");
                path = AppData+"\\Microsoft\\Windows\\Start Menu\\Programs\\Startup";
            }

            if (!isFoundFolder(path)){
                return;
            }

            if (isFoundFile(getFullPath(path)+"\\vrcvideologviewer.bat")){
                deleteFile(getFullPath(path)+"\\vrcvideologviewer.bat");
            }

            writeFile(getFullPath(path)+"\\vrcvideologviewer.bat", batText.replaceAll("#path#", path), StandardCharsets.UTF_8);

        } else {
            String path = "C:\\Users\\" + ntSystem.getName() + "\\AppData\\Roaming\\Microsoft\\Windows\\Start Menu\\Programs\\Startup";
            if (!isFoundFolder(path)){
                String AppData = System.getenv().get("APPDATA");
                path = AppData+"\\Microsoft\\Windows\\Start Menu\\Programs\\Startup";
            }
            if (!isFoundFile(path)){
                return;
            }

            path = getFullPath(path).replaceAll(Pattern.quote("\\"), "/").replaceAll("/", "\\\\\\\\")+"\\vrcvideologviewer.bat";

            if (isFoundFile(getFullPath(path))){
                deleteFile(getFullPath(path));
            }
        }

    }

    public static void StartUp() throws Exception {

        boolean isAutoStaring = false;
        String AutoStaringMode = "";
        if (isFoundFile("./config.yml")){
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
            final Runtime runtime = Runtime.getRuntime();
            if (isFoundFile("./auto-start.bat")){
                deleteFile("./auto-start.bat");
            }

            writeFile("./auto-start.bat", "start .\\start.bat", StandardCharsets.UTF_8);

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
            deleteFile("./auto-start.bat");
            return;
        }

        if (AutoStaringMode.equals("VRChat")){
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
            String path = "C:\\Users\\" + Function.ntSystem.getName() + "\\AppData\\Roaming\\Microsoft\\Windows\\Start Menu\\Programs\\Startup";
            if (!isFoundFolder("C:\\Users\\" + Function.ntSystem.getName() + "\\AppData\\Roaming\\Microsoft\\Windows\\Start Menu\\Programs\\Startup")){
                String AppData = System.getenv().get("APPDATA");
                path = AppData+"\\Microsoft\\Windows\\Start Menu\\Programs\\Startup";
            }
            if (!isFoundFolder(path)){
                return;
            }
            //System.out.println("debug2");

            if (isFoundFile("./auto-start.bat")){
                deleteFile("./auto-start.bat");
            }

            writeFile("./auto-start.bat", "start .\\start.bat\r\ncd /d \""+getFullPath(path).replaceAll("\\\\", "/").replaceAll("/", "\\\\")+"\"\r\nstart .\\vrcvideologviewer.bat", StandardCharsets.UTF_8);

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

            deleteFile("./auto-start.bat");

        }
    }

    public static void DownloadFonts(HttpClient client, String downloadFilename) throws Exception {

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

            for (JsonElement element : array.asList()) {
                String filename = element.getAsJsonObject().get("filename").getAsString();

                if  (filename.split("/")[filename.split("/").length - 1].equals(downloadFilename.replaceAll("\\./fonts/", ""))){
                    request = HttpRequest.newBuilder()
                            .uri(new URI(element.getAsJsonObject().get("url").getAsString()))
                            .headers("User-Agent", Function.UserAgent)
                            .headers("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
                            .headers("Accept-Language", "ja,en;q=0.7,en-US;q=0.3")
                            .GET()
                            .build();

                    HttpResponse<byte[]> send2 = client.send(request, HttpResponse.BodyHandlers.ofByteArray());
                    writeFile(downloadFilename, send2.body());
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
            writeFile(downloadFilename, send.body());
        }

    }

    public static void Main_Init(HttpClient client, String lang) throws Exception {
        String langText = null;

        String path = "./lang/"+lang+".txt";
        if (!isFoundFile(path)) {
            path = "./lang/ja.txt";
        }

        if (isFoundFile(path)) {
            langText = getFileByText(path, StandardCharsets.UTF_8);
        } else {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(new URI("https://raw.githubusercontent.com/nicovrc-net/VRCVideoLogViewer/refs/heads/release/lang/ja.txt"))
                    .headers("User-Agent", Function.UserAgent)
                    .headers("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
                    .headers("Accept-Language", "ja,en;q=0.7,en-US;q=0.3")
                    .GET()
                    .build();
            HttpResponse<String> send = client.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            langText = send.body();

            writeFile("./lang/ja.txt", langText, StandardCharsets.UTF_8);
        }

        for (String str : langText.split("\n")) {
            Matcher matcher = Function.matcher_langData.matcher(str);
            //System.out.println("debug : " + str);
            if (matcher.find()){
                //System.out.println("debug : " + matcher.group(1) + " / " + matcher.group(2));
                Function.langData.add(matcher.group(1), matcher.group(2));
            }
        }

        final boolean isWindowsBatchStart = Function.ntSystem != null;

        if (isFoundFile("./tools/openjdk-21.0.2_windows-x64_bin.zip")) {
            deleteFile("./tools/openjdk-21.0.2_windows-x64_bin");
        }
        if (isFoundFile("./tools/openjdk-21.0.2_linux-x64_bin.tar.gz")) {
            deleteFile("./tools/openjdk-21.0.2_linux-x64_bin.tar.gz");
        }
        if (isFoundFile("./tools/openjfx-21.0.10_windows-x64_bin-sdk.zip")) {
            deleteFile("./tools/openjfx-21.0.10_windows-x64_bin-sdk.zip");
        }
        if (isFoundFile("./tools/openjfx-21.0.10_linux-x64_bin-sdk.zip")) {
            deleteFile("./tools/openjfx-21.0.10_linux-x64_bin-sdk.zip");
        }

        // フォント存在チェック
        if (!isFoundFolder("./fonts")) {
            createFolder("./fonts");
        }
        if (isWindowsBatchStart){
            if (!isFoundFile("./fonts/NotoSansJP-Medium.ttf")) {
                DownloadFonts(client,"./fonts/NotoSansJP-Medium.ttf");
            }
            if (!isFoundFile("./fonts/NotoSansKR-Medium.ttf")) {
                DownloadFonts(client,"./fonts/NotoSansKR-Medium.ttf");
            }
            if (!isFoundFile("./fonts/NotoSansSC-Medium.ttf")) {
                DownloadFonts(client,"./fonts/NotoSansSC-Medium.ttf");
            }
            if (!isFoundFile("./fonts/NotoSansTC-Medium.ttf")) {
                DownloadFonts(client,"./fonts/NotoSansTC-Medium.ttf");
            }
        } else {
            if (!isFoundFile("./fonts/NotoSansCJK-Regular.ttc")) {
                DownloadFonts(client,"./fonts/NotoSansCJK-Regular.ttc");
            }
        }

        // ImageMagic存在チェック
        // https://imagemagick.org/script/download.php
        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI("https://imagemagick.org/script/download.php"))
                .headers("User-Agent", Function.UserAgent)
                .headers("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
                .headers("Accept-Language", "ja,en;q=0.7,en-US;q=0.3")
                .GET()
                .build();
        HttpResponse<String> send = client.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        //System.out.println(send.body());
        if (isWindowsBatchStart) {
            if (!isFoundFile(".\\tools\\ImageMagick\\magick.exe")) {
                Matcher matcher = matcher_ImageMagickLast.matcher(send.body());
                if (matcher.find()){
                    //System.out.println(matcher.group(0));
                    final String str = matcher.group(0);
                    request = HttpRequest.newBuilder()
                            .uri(new URI(str))
                            .headers("User-Agent", Function.UserAgent)
                            .headers("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
                            .headers("Accept-Language", "ja,en;q=0.7,en-US;q=0.3")
                            .GET()
                            .build();
                    HttpResponse<byte[]> send2 = client.send(request, HttpResponse.BodyHandlers.ofByteArray());
                    writeFile("./tools/"+str.split("/")[str.split("/").length - 1], send2.body());

                    //System.out.println("debug1");
                    String str2 = """
                        .\\tools\\7z2501\\7za.exe x -o./tools/ImageMagick ./tools/#file#
                        exit
                        """;

                    writeFile("./tools/im1.bat", str2.replaceAll("#file#", str.split("/")[str.split("/").length - 1]), StandardCharsets.UTF_8);

                    //System.out.println("debug3");

                    final Runtime runtime = Runtime.getRuntime();
                    Process exec0 = runtime.exec(new String[]{"./tools/im1.bat"});
                    Thread.ofVirtual().start(() -> {
                        try {
                            Thread.sleep(15000L);
                        } catch (Exception ex) {
                            //ex.printStackTrace();
                        }

                        if (exec0.isAlive()) {
                            exec0.destroy();
                        }
                    });
                    exec0.waitFor();

                    deleteFile("./tools/im1.bat");
                    deleteFile("./tools/"+ str.split("/")[str.split("/").length - 1]);

                }
            }
        } else {
            if (!isFoundFile("./tools/ImageMagick/magick")) {
                request = HttpRequest.newBuilder()
                        .uri(new URI("https://imagemagick.org/archive/binaries/magick"))
                        .headers("User-Agent", Function.UserAgent)
                        .headers("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
                        .headers("Accept-Language", "ja,en;q=0.7,en-US;q=0.3")
                        .GET()
                        .build();

                HttpResponse<byte[]> send2 = client.send(request, HttpResponse.BodyHandlers.ofByteArray());
                writeFile("./tools/ImageMagick/magick", send2.body());
            }
        }

        if (isWindowsBatchStart){
            deleteFile("./tools/update1.bat");
            deleteFile("./tools/update2.bat");
        }
    }

}
