package com.example.nekonosukiyaki.materialaudiorecorder.fragments;

import com.example.nekonosukiyaki.materialaudiorecorder.R;
import com.example.nekonosukiyaki.materialaudiorecorder.adapters.FileListAdapter;
import com.example.nekonosukiyaki.materialaudiorecorder.data.RecordItemData;
import com.example.nekonosukiyaki.materialaudiorecorder.event.EventBusDbChangedEvent;
import com.example.nekonosukiyaki.materialaudiorecorder.listeners.OnInitializeListener;
import com.example.nekonosukiyaki.materialaudiorecorder.listeners.OnRecyclerViewItemClickListener;
import com.example.nekonosukiyaki.materialaudiorecorder.model.RecordServiceModel;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringDef;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.io.File;
import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;

/**
 * Created by nekonosukiyaki on 3/25/2017 AD.
 */

public class FileListFragment extends Fragment {
    private static final String TAG = FileListFragment.class.getSimpleName();
    private static final int REQUEST_PERMISSION_CODE_STORAGE_WRITE = 0x02;

    private RecordServiceModel mRecordServiceModel;
    private RecyclerView mRecyclerView;
    private FileListAdapter mFileListAdapter;
    private OnRecyclerViewItemClickListener mOnRecyclerViewItemClickListener;
    private ArrayList<RecordItemData> mRecyclerViewList = new ArrayList<>();

    public FileListFragment() {

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setOnRecyclerViewItemClickListener();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_file_list, container, false);
        mRecyclerView = (RecyclerView) v.findViewById(R.id.recyclerView);
        mRecyclerView.setHasFixedSize(true);
        LinearLayoutManager llm = new LinearLayoutManager(getActivity());
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        llm.setReverseLayout(true);
        llm.setStackFromEnd(true);
        mRecyclerView.setLayoutManager(llm);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mFileListAdapter = new FileListAdapter(getContext(), mRecyclerViewList, mOnRecyclerViewItemClickListener);
        mRecyclerView.setAdapter(mFileListAdapter);
        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
        // permission check
        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                Log.d(TAG, "shouldShowRequest");
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_PERMISSION_CODE_STORAGE_WRITE);
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
            return;
        }
        mRecordServiceModel = new RecordServiceModel(getContext(), new OnInitializeListener() {
            @Override
            public void onInitialize() {
                mRecordServiceModel.refleshFileObserver();
                mRecordServiceModel.syncOrmaWithLocalFile();
                setRecyclerViewList();
                mFileListAdapter.swapData(mRecyclerViewList);
            }
        });
        mRecordServiceModel.publish();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        EventBus.getDefault().unregister(this);
        if (mRecordServiceModel != null) {
            mRecordServiceModel.terminal();
            mRecordServiceModel = null;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_PERMISSION_CODE_STORAGE_WRITE:
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

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onDbChangedEvent(EventBusDbChangedEvent event) {
        switch (event.getEventType()) {
            case CREATE:
            case DELETE:
                setRecyclerViewList();
                mFileListAdapter.swapData(mRecyclerViewList);
                break;
            default:
                break;
        }
    }

    private void setOnRecyclerViewItemClickListener() {
        mOnRecyclerViewItemClickListener = new OnRecyclerViewItemClickListener() {
            @Override
            public void onClick(View v, RecordItemData item) {
                PlaybackFragment playbackFragment = PlaybackFragment.newInstance(item);
                playbackFragment.show(((FragmentActivity) getContext()).getSupportFragmentManager(), PlaybackFragment.class.getSimpleName());
            }

            @Override
            public boolean onLongClick(View v, final RecordItemData itemData) {
                ArrayList<String> actions = new ArrayList<String>() {
                    {
                        add("Share File");
                        add("Delete File");
                    }
                };

                AlertDialog.Builder builder = new AlertDialog.Builder(getContext())
                        .setTitle("Options")
                        .setItems(actions.toArray(new CharSequence[actions.size()]), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                switch (which) {
                                    case 0:
                                        showShareFileDialog(itemData);
                                        dialog.dismiss();
                                        break;
                                    case 1:
                                        showDeleteFileDialog(itemData);
                                        dialog.dismiss();
                                        break;
                                    default:
                                        break;
                                }
                            }
                        })
                        .setCancelable(true)
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                builder.create().show();

                return false;
            }
        };
    }

    private void setRecyclerViewList() {
        if (mRecordServiceModel == null || !mRecordServiceModel.isInitialized()) {
            Log.e(TAG, "service model illegal state");
            return;
        }
        mRecyclerViewList = new ArrayList<>();
        mRecordServiceModel.getRecordContentList(mRecyclerViewList);
    }

    private void showShareFileDialog(RecordItemData itemData) {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_SEND);
        intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(new File(itemData.getFilePath())));
        intent.setType("audio/mp4");
        getContext().startActivity(Intent.createChooser(intent, "Send to"));
    }

    private void showDeleteFileDialog(final RecordItemData itemData) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext())
                .setTitle("Confirm Delete")
                .setMessage("Are you sure to delete this file?")
                .setCancelable(true)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        File file = new File(itemData.getFilePath());
                        if (file.exists() && file.isFile()) {
                            file.delete();
                        }
                        dialog.dismiss();
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        builder.create().show();
    }
}
