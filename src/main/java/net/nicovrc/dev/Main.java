package net.nicovrc.dev;

import com.amihaiemil.eoyaml.Yaml;
import com.amihaiemil.eoyaml.YamlMapping;
import javafx.application.Platform;
import javafx.stage.Stage;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Main {
    private static final SimpleDateFormat file_sdf = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");

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
                    List<String> logFileList = new ArrayList<>();
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
                        File file = new File(config.getLogFolderPass());
                        for (File f : file.listFiles()) {
                            if (f.getName().startsWith("output_log_")) {
                                logFileList.add(f.getName());
                            }
                        }

                        if (logFileList.size() > 1) {
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

                            while (isMove) {
                                isMove = false;
                                for (i = 0; i < temp2.length; i++) {
                                    if (i + 1 < temp2.length) {
                                        if (temp2[i] >= temp2[i + 1]) {
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

                            for (i = 0; i < temp1.length; i++) {
                                temp.add(temp1[i]);
                            }
                            logFileList = temp;
                        }

                        String tempLast = logFileList.getLast();

                        while (tempLast.equals(logFileList.getLast())){
                            logFileList.clear();

                            for (File f : file.listFiles()) {
                                if (f.getName().startsWith("output_log_")) {
                                    logFileList.add(f.getName());
                                }
                            }

                            if (logFileList.size() > 1) {
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

                                while (isMove) {
                                    isMove = false;
                                    for (i = 0; i < temp2.length; i++) {
                                        if (i + 1 < temp2.length) {
                                            if (temp2[i] >= temp2[i + 1]) {
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

                                for (i = 0; i < temp1.length; i++) {
                                    temp.add(temp1[i]);
                                }
                                logFileList = temp;
                            }
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