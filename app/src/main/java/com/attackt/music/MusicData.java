package com.attackt.music;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by mh on 2017/5/14.
 */
public class MusicData implements Parcelable {

    private String m4a;              //http:ws.stream.qqmusic.qq.com/4833285.m4a?fromtag=46	流媒体地址
    private long songid;              //歌曲id
    private String songname;          //海阔天空	歌曲名称
    private long duration;

    public MusicData() {

    }

    public String getM4a() {
        return m4a;
    }

    public void setM4a(String m4a) {
        this.m4a = m4a;
    }

    public long getSongid() {
        return songid;
    }

    public void setSongid(long songid) {
        this.songid = songid;
    }

    public String getSongname() {
        return songname;
    }

    public void setSongname(String songname) {
        this.songname = songname;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    protected MusicData(Parcel in) {
        m4a = in.readString();
        songid = in.readLong();
        songname = in.readString();
        duration = in.readLong();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(m4a);
        dest.writeLong(songid);
        dest.writeString(songname);
        dest.writeLong(duration);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<MusicData> CREATOR = new Creator<MusicData>() {
        @Override
        public MusicData createFromParcel(Parcel in) {
            return new MusicData(in);
        }

        @Override
        public MusicData[] newArray(int size) {
            return new MusicData[size];
        }
    };
}
