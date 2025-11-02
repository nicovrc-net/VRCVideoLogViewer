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