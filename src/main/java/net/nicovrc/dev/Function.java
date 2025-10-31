package net.nicovrc.dev;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.sun.security.auth.module.NTSystem;

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

public class Function {

    public static final String Version = "0.6.0-beta.1";

    public static final String UserAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:144.0) Gecko/20100101 Firefox/144.0 VRCVideoLogViewer/"+Version;
    public static final String Unity_UserAgent = "UnityPlayer/2022.3.22f1-DWR (UnityWebRequest/1.0, libcurl/8.5.0-DEV)";
    public static final String HTTP_x_unity_version = "2022.3.22f1-DWR";

    public static final NTSystem ntSystem = new NTSystem();
    public static final Runtime runtime = Runtime.getRuntime();

    private static final Pattern matcher_VideoLog = Pattern.compile("(\\d+)\\.(\\d+)\\.(\\d+) (\\d+):(\\d+):(\\d+) Debug      -  \\[Video Playback\\] Attempting to resolve URL '(.+)'");
    private static final Pattern matcher_VideoLog2 = Pattern.compile("(\\d+)\\.(\\d+)\\.(\\d+) (\\d+):(\\d+):(\\d+) Debug      -  \\[Video Playback\\] Resolving URL '(.+)'");
    private static final Pattern matcher_ImageLog = Pattern.compile("(\\d+)\\.(\\d+)\\.(\\d+) (\\d+):(\\d+):(\\d+) Debug      -  \\[Image Download\\] Attempting to load image from URL '(.+)'");
    private static final Pattern matcher_StringLog = Pattern.compile("(\\d+)\\.(\\d+)\\.(\\d+) (\\d+):(\\d+):(\\d+) Debug      -  \\[String Download\\] Attempting to load String from URL '(.+)'");

    private static final Pattern matcher_VideoErrorLog = Pattern.compile("(\\d+)\\.(\\d+)\\.(\\d+) (\\d+):(\\d+):(\\d+) Warning    -  \\[Video Playback\\] (.+)");
    private static final Pattern matcher_VideoErrorLog2 = Pattern.compile("(\\d+)\\.(\\d+)\\.(\\d+) (\\d+):(\\d+):(\\d+) Error      -  \\[AVProVideo\\] (.+)");
    private static final Pattern matcher_ImageErrorLog = Pattern.compile("(\\d+)\\.(\\d+)\\.(\\d+) (\\d+):(\\d+):(\\d+) Debug      -  \\[Image Download\\] A web request exception occurred while loading image from URL '(.+)'\\. Exception: (.+)");
    private static final Pattern matcher_StringErrorLog = Pattern.compile("(\\d+)\\.(\\d+)\\.(\\d+) (\\d+):(\\d+):(\\d+) Debug      -  \\[String Download\\] A web request exception occurred while loading string from URL '(.+)'\\. Exception: (.+)");

    private static final SimpleDateFormat logDate = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss");


    public static String getTextForFile(File file){
        String logText = null;
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))){
            String str;
            StringBuilder sb = new StringBuilder();
            while ((str = reader.readLine()) != null) {
                sb.append(str).append("\n");
            }
            logText = sb.toString();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return logText;
    }

    public static List<LogData> getLogForURL(String logText) throws Exception{
        ArrayList<LogData> logData = new ArrayList<>();
        HashMap<String, String> errorList = new HashMap<>();

        for (String s : logText.split("\n")) {
            Matcher video = matcher_VideoLog.matcher(s);
            Matcher video2 = matcher_VideoLog2.matcher(s);
            Matcher image = matcher_ImageLog.matcher(s);
            Matcher string = matcher_StringLog.matcher(s);

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
                    logData.getLast().setErrorMessage(group);
                }
            }

            if (image_error.find()){
                //System.out.println(image_error.group(7) + " : " + image_error.group(8));
                errorList.put(image_error.group(7), image_error.group(8));
            }

            if (string_error.find()){
                //System.out.println(string_error.group(7) + " : " + string_error.group(8));
                errorList.put(string_error.group(7), string_error.group(8));
            }

            if (video.find()){
                String tempDate = video.group(1)+"."+video.group(2)+"."+video.group(3)+" "+video.group(4)+":"+video.group(5)+":"+video.group(6);
                data.setLogDate(logDate.parse(tempDate));
                data.setURL(video.group(7));
                data.setErrorMessage(null);
                data.setURLType("Video");
                logData.add(data);
            }

            if (video2.find()){
                String tempDate = video2.group(1)+"."+video2.group(2)+"."+video2.group(3)+" "+video2.group(4)+":"+video2.group(5)+":"+video2.group(6);
                data.setLogDate(logDate.parse(tempDate));
                data.setURL(video2.group(7));
                data.setErrorMessage(null);
                data.setURLType("Video");
                logData.add(data);
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
                logData.add(data);
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
                logData.add(data);
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
                JsonElement json = new Gson().fromJson(send.body(), JsonElement.class);

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

        File file = new File("./temp");
        if (!file.exists()){
            file.mkdir();
        }

        String filename = new Date().getTime()+"_"+ UUID.randomUUID().toString().split("-")[0];
        FileOutputStream stream = new FileOutputStream("./temp/"+filename+".webp");
        stream.write(input);
        stream.close();
        stream = null;

        final Process exec0;
        if (!ntSystem.getName().isEmpty()){
            exec0 = runtime.exec(new String[]{".\\tools\\ImageMagick-7.1.2-8-portable-Q16-x64\\magick.exe", "./temp/"+filename+".webp", "./temp/"+filename+".png"});
        } else {
            exec0 = runtime.exec(new String[]{""});
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

        FileInputStream inputStream = new FileInputStream("");
        byte[] output = inputStream.readAllBytes();
        inputStream.close();

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

}
