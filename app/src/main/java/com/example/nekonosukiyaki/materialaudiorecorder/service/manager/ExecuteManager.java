package com.example.nekonosukiyaki.materialaudiorecorder.service.manager;


import com.example.nekonosukiyaki.materialaudiorecorder.constants.CommonConstants;
import com.example.nekonosukiyaki.materialaudiorecorder.data.RecordItemData;
import com.example.nekonosukiyaki.materialaudiorecorder.data.orma.Record;
import com.example.nekonosukiyaki.materialaudiorecorder.data.orma.RecordDatabase;
import com.example.nekonosukiyaki.materialaudiorecorder.data.orma.RecordRefactor;
import com.example.nekonosukiyaki.materialaudiorecorder.data.orma.Record_Selector;
import com.example.nekonosukiyaki.materialaudiorecorder.event.EventBusDbChangedEvent;

import org.greenrobot.eventbus.EventBus;

import android.content.ContentResolver;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Environment;
import android.os.FileObserver;
import android.os.Looper;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by nekonosukiyaki on 3/25/2017 AD.
 */

public class ExecuteManager {
    private static final String TAG = ExecuteManager.class.getSimpleName();

    private RecordDatabase mRecordDatabase = null;
    private Lock ormaLock = new ReentrantLock();
    private MediaRecorder mRecorder = null;
    private final ExecutorService mExecutor = Executors.newFixedThreadPool(CommonConstants.SERVICE_EXECUTOR_MAX_THREAD);
    private FileObserver mFileObserviObserver = new FileObserver(Environment.getExternalStorageDirectory().toString() + CommonConstants.RECORD_FILE_PATH_TOP) {
        @Override
        public void onEvent(int event, String path) {
            switch (event) {
                case CREATE:
                    Log.d(TAG, "file ovserver: " + path + " create");
                    EventBus.getDefault().post(new EventBusDbChangedEvent(EventBusDbChangedEvent.EventType.CREATE));
                    break;
                case DELETE:
                    Log.d(TAG, "file ovserver: " + path + " delete");
                    updateDatabaseDelete(path);
                    EventBus.getDefault().post(new EventBusDbChangedEvent(EventBusDbChangedEvent.EventType.DELETE));
                    break;
                default:
                    break;
            }
        }
    };

    private String mFileName = null;
    private String mFilePath = null;
    private long mStartTimeMillis = 0;
    private long mElapsedMillis = 0;

    public ExecuteManager(RecordDatabase database) {
        if (database == null) {
            throw new IllegalArgumentException();
        }

        mRecordDatabase = database;
        mFileObserviObserver.startWatching();
    }

    public void terminal() {
        mFileObserviObserver.stopWatching();
        stopRecording();
        mRecordDatabase = null;
    }

    public void refleshFileObserver() {
        mFileObserviObserver.stopWatching();
        mFileObserviObserver.startWatching();
    }

    private void updateDatabaseDelete(String path) {
        if (mRecordDatabase == null) {
            return;
        }

        ormaLock.lock();
        try {
            Record_Selector selector = mRecordDatabase.selectFromRecord()
                    .fileNameEq(path);
            if (!selector.isEmpty()) {
                mRecordDatabase.deleteFromRecord()
                        .idEq(selector.get(0).id)
                        .execute();
            }
        } finally {
            ormaLock.unlock();
        }
    }

    private void updateDatabaseCreate(String path) {
        if (mRecordDatabase == null) {
            return;
        }

        ormaLock.lock();
        try {
            Record_Selector selector = mRecordDatabase.selectFromRecord()
                    .fileNameEq(path);
            if (selector.isEmpty()) {
                File addedFile = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + CommonConstants.RECORD_FILE_PATH_TOP + path);
                long fileLength = addedFile.length();
                RecordRefactor.insertDefault(mRecordDatabase,
                        path,
                        Environment.getExternalStorageDirectory().getAbsolutePath() + CommonConstants.RECORD_FILE_PATH_TOP + path,
                        fileLength
                        );
            }
        } finally {
            ormaLock.unlock();
        }
    }

    public void startRecording() {
        setFileNameAndPath();
        setMediaRecorder();

        mExecutor.submit(new Runnable() {
            @Override
            public void run() {
                try {
                    mRecorder.prepare();
                    mRecorder.start();
                    mStartTimeMillis = System.currentTimeMillis();
                } catch (Exception e) {
                    Log.e(TAG, "MediaRecorder prepare failed");
                    e.printStackTrace();
                }
            }
        });
    }

    private void setFileNameAndPath() {
        int count = 0;
        int dbSize = 0;
        File f;

        ormaLock.lock();
        try {
            Record_Selector selector = mRecordDatabase.selectFromRecord();
            dbSize = selector.count();
        } finally {
            ormaLock.unlock();
        }

        do {
            count++;
            mFileName = CommonConstants.DEFAULT_RECORD_FILE_NAME
                    + "#"
                    + String.valueOf(dbSize + count)
                    + ".mp4";
            mFilePath = Environment.getExternalStorageDirectory().getAbsolutePath();
            mFilePath += CommonConstants.RECORD_FILE_PATH_TOP
                    + "/"
                    + mFileName;
            f = new File(mFilePath);
        } while (f.exists() && !f.isDirectory());
    }

    private void setMediaRecorder() {
        if (mFilePath.isEmpty() || mFilePath == null) {
            Log.w(TAG, "setMediaRecorder filePath none");
            return;
        }

        mRecorder = new MediaRecorder();
        mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        mRecorder.setOutputFile(mFilePath);
        mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
        mRecorder.setAudioChannels(1); //Mono
    }

    public void stopRecording() {
        if (mRecorder == null) {
            Log.w(TAG, "stopRecording recorder none");
            return;
        }

        mExecutor.submit(new Runnable() {
            @Override
            public void run() {
                mRecorder.stop();
                mElapsedMillis = System.currentTimeMillis() - mStartTimeMillis;
                mRecorder.release();
                mRecorder = null;

                ormaLock.lock();
                try {
                    Record_Selector selector = mRecordDatabase.selectFromRecord()
                            .fileNameEq(mFileName);
                    if (selector.isEmpty()) {
                        RecordRefactor.insertDefault(mRecordDatabase,
                                mFileName,
                                mFilePath,
                                mElapsedMillis);
                    } else {
                        mRecordDatabase.updateRecord()
                                .idEq(selector.get(0).id)
                                .fileLength(mElapsedMillis)
                                .addedDate(System.currentTimeMillis())
                                .execute();
                    }
                } finally {
                    ormaLock.unlock();
                }

                EventBus.getDefault().post(new EventBusDbChangedEvent(EventBusDbChangedEvent.EventType.CREATE));
            }
        });
    }

    public void getRecordContentList(List<RecordItemData> list) {
        ormaLock.lock();
        try {
            Record_Selector selector = mRecordDatabase.selectFromRecord();
            if (!selector.isEmpty()) {
                for (Record item : selector) {
                    list.add(new RecordItemData(
                            item.id,
                            item.fileName,
                            item.filePath,
                            item.fileLength,
                            item.addedDate
                    ));
                }
            }
        } finally {
            ormaLock.unlock();
        }
    }

    public void syncOrmaWithLocalFile() {
        if (mRecordDatabase == null) {
            Log.w(TAG, "sync orma database np");
            return;
        }

        ormaLock.lock();
        try {
            mRecordDatabase.deleteAll();
            MediaPlayer mediaPlayer = new MediaPlayer();
            File[] files = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + CommonConstants.RECORD_FILE_PATH_TOP).listFiles();
            for (File file : files) {
                long length = 0;
                try {
                    mediaPlayer.setDataSource(file.getAbsolutePath());
                    mediaPlayer.prepare();
                    length = mediaPlayer.getDuration();
                    mediaPlayer.reset();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                RecordRefactor.insertDefault(
                        mRecordDatabase,
                        file.getName(),
                        file.getAbsolutePath(),
                        length
                );
            }
            mediaPlayer.release();
        } finally {
            ormaLock.unlock();
        }
    }
}
