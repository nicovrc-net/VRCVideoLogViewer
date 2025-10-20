package net.nicovrc.dev;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Function {

    public static String Version = "0.3.2-beta.1";

    public static String UserAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:144.0) Gecko/20100101 Firefox/144.0 VRCVideoLogViewer/"+Version;
    public static String Unity_UserAgent = "UnityPlayer/2022.3.22f1-DWR (UnityWebRequest/1.0, libcurl/8.5.0-DEV)";
    public static String HTTP_x_unity_version = "2022.3.22f1-DWR";

    private static Pattern matcher_VideoLog = Pattern.compile("(\\d+)\\.(\\d+)\\.(\\d+) (\\d+):(\\d+):(\\d+) Debug      -  \\[Video Playback\\] Attempting to resolve URL '(.+)'");
    private static Pattern matcher_VideoLog2 = Pattern.compile("(\\d+)\\.(\\d+)\\.(\\d+) (\\d+):(\\d+):(\\d+) Debug      -  \\[Video Playback\\] Resolving URL '(.+)'");
    private static Pattern matcher_ImageLog = Pattern.compile("(\\d+)\\.(\\d+)\\.(\\d+) (\\d+):(\\d+):(\\d+) Debug      -  \\[Image Download\\] Attempting to load image from URL '(.+)'");
    private static Pattern matcher_StringLog = Pattern.compile("(\\d+)\\.(\\d+)\\.(\\d+) (\\d+):(\\d+):(\\d+) Debug      -  \\[String Download\\] Attempting to load String from URL '(.+)'");

    private static Pattern matcher_VideoErrorLog = Pattern.compile("(\\d+)\\.(\\d+)\\.(\\d+) (\\d+):(\\d+):(\\d+) Warning    -  \\[Video Playback\\] (.+)");
    private static Pattern matcher_VideoErrorLog2 = Pattern.compile("(\\d+)\\.(\\d+)\\.(\\d+) (\\d+):(\\d+):(\\d+) Error      -  \\[AVProVideo\\] (.+)");
    private static Pattern matcher_ImageErrorLog = Pattern.compile("(\\d+)\\.(\\d+)\\.(\\d+) (\\d+):(\\d+):(\\d+) Debug      -  \\[Image Download\\] A web request exception occurred while loading image from URL '(.+)'\\. Exception: (.+)");
    private static Pattern matcher_StringErrorLog = Pattern.compile("(\\d+)\\.(\\d+)\\.(\\d+) (\\d+):(\\d+):(\\d+) Debug      -  \\[String Download\\] A web request exception occurred while loading string from URL '(.+)'\\. Exception: (.+)");

    private static SimpleDateFormat logDate = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss");


    public static String getTextForFile(File file){
        String logText = null;
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8));){
            String str;
            StringBuffer sb = new StringBuffer();
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
                logData.getLast().setErrorMessage(video_error2.group(7));
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

}
