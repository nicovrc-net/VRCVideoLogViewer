package net.nicovrc.dev;

public class VideoData {

    private String VideoTitle = null;
    private byte[] Thumbnail = null;

    public String getVideoTitle() {
        return VideoTitle;
    }

    public void setVideoTitle(String videoTitle) {
        VideoTitle = videoTitle;
    }

    public byte[] getThumbnail() {
        return Thumbnail;
    }

    public void setThumbnail(byte[] thumbnail) {
        Thumbnail = thumbnail;
    }
}
