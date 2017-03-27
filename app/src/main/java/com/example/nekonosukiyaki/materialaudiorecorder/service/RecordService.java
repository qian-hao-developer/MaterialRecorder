package com.example.nekonosukiyaki.materialaudiorecorder.service;


import com.example.nekonosukiyaki.materialaudiorecorder.constants.CommonConstants;
import com.example.nekonosukiyaki.materialaudiorecorder.data.RecordItemData;
import com.example.nekonosukiyaki.materialaudiorecorder.data.orma.RecordDatabase;
import com.example.nekonosukiyaki.materialaudiorecorder.data.orma.RecordDatabaseVersion;
import com.example.nekonosukiyaki.materialaudiorecorder.service.aidl.IRecordService;
import com.example.nekonosukiyaki.materialaudiorecorder.service.manager.ExecuteManager;
import com.github.gfx.android.orma.AccessThreadConstraint;

import android.app.Service;
import android.content.Intent;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.util.Log;

import java.util.List;

/**
 * Created by nekonosukiyaki on 3/25/2017 AD.
 */

public class RecordService extends Service {
    private static final String TAG = RecordService.class.getSimpleName();

    private HandlerThread mHandlerThread;
    private ExecuteManager mExecuteManager;


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    private final IRecordService.Stub mBinder = new IRecordService.Stub() {
        @Override
        public void startRecord() throws RemoteException {
            Log.d(TAG, "startRecord");
            if (mExecuteManager == null) {
                Log.e(TAG, "executeManager illegal state");
                return;
            }
            mExecuteManager.startRecording();
        }

        @Override
        public void stopRecord() throws RemoteException {
            Log.d(TAG, "stopRecord");
            if (mExecuteManager == null) {
                Log.e(TAG, "executeManager illegal state");
                return;
            }
            mExecuteManager.stopRecording();
        }

        @Override
        public void getRecordContentList(List<RecordItemData> list) throws RemoteException {
            if (mExecuteManager == null) {
                Log.e(TAG, "executeManager illegal state");
                return;
            }
            mExecuteManager.getRecordContentList(list);
        }

        @Override
        public void syncOrmaWithLocalFile() throws RemoteException {
            if (mExecuteManager == null) {
                Log.e(TAG, "executeManager illegal state");
                return;
            }
            mExecuteManager.syncOrmaWithLocalFile();
        }

        @Override
        public void refleshFileObserver() throws RemoteException {
            if (mExecuteManager == null) {
                Log.e(TAG, "executeManager illegal state");
                return;
            }
            mExecuteManager.refleshFileObserver();
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();

        RecordDatabase recordDatabase = RecordDatabase
                .builder(this)
                .readOnMainThread(AccessThreadConstraint.WARNING)
                .writeOnMainThread(AccessThreadConstraint.WARNING)
                .name(CommonConstants.RECORD_DATABASE_NAME)
                .versionForManualStepMigration(RecordDatabaseVersion.CURRENT_DB_VERSION)
                .build();

        mHandlerThread = new HandlerThread(TAG);
        mHandlerThread.start();

        mExecuteManager = new ExecuteManager(recordDatabase);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mExecuteManager.terminal();
        mHandlerThread.quit();
    }
}
