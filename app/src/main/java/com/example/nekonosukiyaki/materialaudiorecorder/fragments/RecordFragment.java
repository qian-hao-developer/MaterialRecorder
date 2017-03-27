package com.example.nekonosukiyaki.materialaudiorecorder.fragments;

import com.example.nekonosukiyaki.materialaudiorecorder.R;
import com.example.nekonosukiyaki.materialaudiorecorder.constants.CommonConstants;
import com.example.nekonosukiyaki.materialaudiorecorder.listeners.OnInitializeListener;
import com.example.nekonosukiyaki.materialaudiorecorder.model.RecordServiceModel;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.SystemClock;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Chronometer;
import android.widget.Toast;

import java.io.File;


public class RecordFragment extends Fragment {
    private static final String TAG = RecordFragment.class.getSimpleName();
    private static final int REQUEST_PERMISSION_CODE_RECORD_AUDIO = 0x01;

    /** components */
    private FloatingActionButton mRecordButton = null;
    private Chronometer mChronometer = null;

    /** instants */
    private boolean mStartRecording = true;
    private RecordServiceModel mRecordServiceModel = null;

    /**
     * Constructor
     */
    public RecordFragment() {

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View recordView = inflater.inflate(R.layout.fragment_record, container, false);

        mChronometer = (Chronometer) recordView.findViewById(R.id.chronometer);
        mRecordButton = (FloatingActionButton) recordView.findViewById(R.id.btnRecord);
        mRecordButton.setBackgroundTintList(new ColorStateList(
                new int[][] {
                        new int[]{android.R.attr.state_pressed},
                        new int[]{-android.R.attr.state_pressed}
                },
                new int[]{
                        //FIXME: no change
                        getResources().getColor(R.color.accent, null),
                        getResources().getColor(R.color.primary, null)
                }
        ));
        mRecordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                executeRecord(mStartRecording);
                mStartRecording = !mStartRecording;
            }
        });
        mRecordButton.setClickable(false);

        mRecordServiceModel = new RecordServiceModel(getContext(), new OnInitializeListener() {
            @Override
            public void onInitialize() {
                mRecordButton.setClickable(true);
            }
        });
        mRecordServiceModel.publish();

        return recordView;
    }

    @Override
    public void onResume() {
        super.onResume();
        // permission check
        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), Manifest.permission.RECORD_AUDIO)) {
                Log.d(TAG, "shouldShowRequest");
                requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO}, REQUEST_PERMISSION_CODE_RECORD_AUDIO);
            } else {
                Log.d(TAG, "shouldn't show request");
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext())
                        .setTitle("Attention")
                        .setMessage("Permission Check Denied\nPlease allow mic permission @ Setting\nPush OK to Settings or NO to finish")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + getActivity().getPackageName()));
                                intent.addCategory(Intent.CATEGORY_DEFAULT);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                            }
                        })
                        .setNegativeButton("NO", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                getActivity().finish();
                            }
                        });
                builder.create().show();
            }
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        // stop record
        executeRecord(false);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mRecordServiceModel.terminal();
        mRecordServiceModel = null;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_PERMISSION_CODE_RECORD_AUDIO:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d(TAG, "permission granted");
                } else {
                    Log.d(TAG, "permission denied");
                }
                break;
            default:
                break;
        }
    }

    private void executeRecord(boolean start) {
        if (mRecordServiceModel == null || !mRecordServiceModel.isInitialized()) {
            Toast.makeText(getContext(), "service not ready", Toast.LENGTH_SHORT).show();
            return;
        }

        if (start) {
            mRecordButton.setImageResource(R.drawable.ic_media_stop);
            File folder = new File(Environment.getExternalStorageDirectory() + CommonConstants.RECORD_FILE_PATH_TOP);
            if (!folder.exists()) {
                folder.mkdir();
            }

            mChronometer.setBase(SystemClock.elapsedRealtime());
            mChronometer.start();

            mRecordServiceModel.startRecord();

            // keep screen on while recording
            getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        } else {
            mRecordButton.setImageResource(R.drawable.ic_mic_white_36dp);
            mChronometer.stop();
            mChronometer.setBase(SystemClock.elapsedRealtime());

            mRecordServiceModel.stopRecord();

            getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }
    }
}
