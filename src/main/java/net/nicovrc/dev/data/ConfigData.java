package net.nicovrc.dev.data;

public class ConfigData {
    private String lang;
    private String logFolderPass;
    private boolean debugOutput;
    private boolean oldLogCheck;
    private boolean VideoPlayer;
    private boolean ImageDownloader;
    private boolean StringDownloader;
    private boolean isAutoStaring;
    private String AutoStaringMode;

    public String getLang() {
        return lang;
    }

    public void setLang(String lang) {
        this.lang = lang;
    }

    public String getLogFolderPass() {
        return logFolderPass;
    }

    public void setLogFolderPass(String logFolderPass) {
        this.logFolderPass = logFolderPass;
    }

    public boolean isDebugOutput() {
        return debugOutput;
    }

    public void setDebugOutput(boolean debugOutput) {
        this.debugOutput = debugOutput;
    }

    public boolean isOldLogCheck() {
        return oldLogCheck;
    }

    public void setOldLogCheck(boolean oldLogCheck) {
        this.oldLogCheck = oldLogCheck;
    }

    public boolean isVideoPlayer() {
        return VideoPlayer;
    }

    public void setVideoPlayer(boolean videoPlayer) {
        VideoPlayer = videoPlayer;
    }

    public boolean isImageDownloader() {
        return ImageDownloader;
    }

    public void setImageDownloader(boolean imageDownloader) {
        ImageDownloader = imageDownloader;
    }

    public boolean isStringDownloader() {
        return StringDownloader;
    }

    public void setStringDownloader(boolean stringDownloader) {
        StringDownloader = stringDownloader;
    }

    public boolean isAutoStaring() {
        return isAutoStaring;
    }

    public void setAutoStaring(boolean autoStaring) {
        isAutoStaring = autoStaring;
    }

    public String getAutoStaringMode() {
        return AutoStaringMode;
    }

    public void setAutoStaringMode(String autoStaringMode) {
        AutoStaringMode = autoStaringMode;
    }
}
