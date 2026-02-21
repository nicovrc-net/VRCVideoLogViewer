package net.nicovrc.dev;

import java.util.Date;

public class LogData {

    private String LogId;
    private Date logDate;
    private String WorldName;
    private String InstanceType;
    private String InstanceId;
    private String URL;
    private String ErrorMessage;
    private String URLType;
    private String ViewText;

    public String getLogId() {
        return LogId;
    }

    public void setLogId(String logId) {
        LogId = logId;
    }

    public Date getLogDate() {
        return logDate;
    }

    public void setLogDate(Date logDate) {
        this.logDate = logDate;
    }

    public String getURL() {
        return URL;
    }

    public void setURL(String URL) {
        this.URL = URL;
    }

    public String getErrorMessage() {
        return ErrorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        ErrorMessage = errorMessage;
    }

    public String getURLType() {
        return URLType;
    }

    public void setURLType(String URLType) {
        this.URLType = URLType;
    }

    public String getWorldName() {
        return WorldName;
    }

    public void setWorldName(String worldName) {
        WorldName = worldName;
    }

    public String getInstanceId() {
        return InstanceId;
    }

    public void setInstanceId(String instanceId) {
        InstanceId = instanceId;
    }

    public String getInstanceType() {
        return InstanceType;
    }

    public void setInstanceType(String instanceType) {
        InstanceType = instanceType;
    }

    public String getViewText() {
        return ViewText;
    }

    public void setViewText(String viewText) {
        ViewText = viewText;
    }
}
