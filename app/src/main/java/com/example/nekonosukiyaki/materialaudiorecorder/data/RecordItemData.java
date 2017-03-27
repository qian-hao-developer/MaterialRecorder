package com.example.nekonosukiyaki.materialaudiorecorder.data;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by nekonosukiyaki on 3/25/2017 AD.
 */

public class RecordItemData implements Parcelable {
    private static final int NONE = 0;

    private long mId; //id in database
    private String mName; // file name
    private String mFilePath; //file path
    private long mLength; // length of recording in seconds
    private long mTime; // date/time of the recording


    /**
     * Constructor
     *
     * @param id
     * @param name
     * @param path
     */
    public RecordItemData(long id, String name, String path) {
        mId = id;
        mName = name;
        mFilePath = path;
        mLength = NONE;
        mTime = NONE;
    }

    /**
     * Constructor
     *
     * @param id
     * @param name
     * @param path
     * @param length
     * @param time
     */
    public RecordItemData(long id, String name, String path, long length, long time) {
        mId = id;
        mName = name;
        mFilePath = path;
        mLength = length;
        mTime = time;
    }


    /** setter */
    public void setName(String name) {
        mName = name;
    }

    public void setFilePath(String path) {
        mFilePath = path;
    }

    public void setLength(int length) {
        mLength = length;
    }

    public void setTime(long time) {
        mTime = time;
    }


    /** getter */
    public long getId() {
        return mId;
    }

    public String getName() {
        return mName;
    }

    public String getFilePath() {
        return mFilePath;
    }

    public long getLength() {
        return mLength;
    }

    public long getTime() {
        return mTime;
    }

    protected RecordItemData(Parcel in) {
        mId = in.readLong();
        mName = in.readString();
        mFilePath = in.readString();
        mLength = in.readLong();
        mTime = in.readLong();
    }

    public static final Creator<RecordItemData> CREATOR = new Creator<RecordItemData>() {
        @Override
        public RecordItemData createFromParcel(Parcel in) {
            return new RecordItemData(in);
        }

        @Override
        public RecordItemData[] newArray(int size) {
            return new RecordItemData[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(mId);
        dest.writeString(mName);
        dest.writeString(mFilePath);
        dest.writeLong(mLength);
        dest.writeLong(mTime);
    }
}
