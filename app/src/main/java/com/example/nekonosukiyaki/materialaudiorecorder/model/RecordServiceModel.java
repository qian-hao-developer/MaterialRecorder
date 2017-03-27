package com.example.nekonosukiyaki.materialaudiorecorder.model;

import com.example.nekonosukiyaki.materialaudiorecorder.data.RecordItemData;
import com.example.nekonosukiyaki.materialaudiorecorder.listeners.OnInitializeListener;
import com.example.nekonosukiyaki.materialaudiorecorder.service.RecordService;
import com.example.nekonosukiyaki.materialaudiorecorder.service.aidl.IRecordService;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import java.util.ArrayList;

/**
 * Created by nekonosukiyaki on 3/26/2017 AD.
 */

public class RecordServiceModel {
    private static final String TAG = RecordServiceModel.class.getSimpleName();

    private Context mContext;
    private OnInitializeListener mOnInitializeListener;
    private IRecordService mIRecordService;

    private boolean mIsBind = false;
    private boolean mBounded = false;
    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mIRecordService = IRecordService.Stub.asInterface(service);
            mBounded = true;
            if (mOnInitializeListener != null) {
                mOnInitializeListener.onInitialize();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mIRecordService = null;
            mBounded = false;
        }
    };

    public RecordServiceModel(Context context, OnInitializeListener listener) {
        mContext = context;
        mOnInitializeListener = listener;
    }

    public void publish() {
        Intent intent = new Intent();
        intent.setClass(mContext, RecordService.class);
        mContext.startService(intent);
        mIsBind = mContext.bindService(intent, mServiceConnection, 0);
    }

    public void terminal() {
        if (mIsBind) {
            try {
                mContext.unbindService(mServiceConnection);
                mIsBind = false;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public boolean isInitialized() {
        return mBounded;
    }


    /** service api */
    public void startRecord() {
        if (mIRecordService == null) {
            Log.e(TAG, "service state illegal");
            return;
        }

        try {
            mIRecordService.startRecord();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public void stopRecord() {
        if (mIRecordService == null) {
            Log.e(TAG, "service state illegal");
            return;
        }

        try {
            mIRecordService.stopRecord();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public void getRecordContentList(ArrayList<RecordItemData> list) {
        if (mIRecordService == null) {
            Log.e(TAG, "service state illegal");
            return;
        }

        try {
            mIRecordService.getRecordContentList(list);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public void syncOrmaWithLocalFile() {
        if (mIRecordService == null) {
            Log.e(TAG, "service state illegal");
            return;
        }

        try {
            mIRecordService.syncOrmaWithLocalFile();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public void refleshFileObserver() {
        if (mIRecordService == null) {
            Log.e(TAG, "service state illegal");
            return;
        }

        try {
            mIRecordService.refleshFileObserver();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }
}
