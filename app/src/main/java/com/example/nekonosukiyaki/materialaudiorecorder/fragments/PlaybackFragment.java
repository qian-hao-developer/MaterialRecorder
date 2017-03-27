package com.example.nekonosukiyaki.materialaudiorecorder.fragments;

import com.example.nekonosukiyaki.materialaudiorecorder.R;
import com.example.nekonosukiyaki.materialaudiorecorder.constants.CommonConstants;
import com.example.nekonosukiyaki.materialaudiorecorder.data.RecordItemData;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.res.ColorStateList;
import android.graphics.ColorFilter;
import android.graphics.LightingColorFilter;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabItem;
import android.support.v4.app.DialogFragment;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.SeekBar;
import android.widget.TextView;

import java.io.IOException;
import java.sql.Time;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

/**
 * Created by nekonosukiyaki on 3/26/2017 AD.
 */

public class PlaybackFragment extends DialogFragment {
    private static final String TAG = PlaybackFragment.class.getSimpleName();
    private static final String BUNDLE_KEY_ITEM = "bundle_key_item";
    private static final long SEEK_BAR_UPDATE_DURATION = 10;
    private static final long CURRENT_PROGRESS_UPDATE_DURATION = 1000;

    private RecordItemData mRecordItemData;
    private MediaPlayer mMediaPlayer;
    private Handler mHandler = new Handler();

    private SeekBar mSeekBar = null;
    private FloatingActionButton mFloatingActionButton = null;
    private TextView mCurrentProgressTextView = null;
    private TextView mFileNameTextView = null;
    private TextView mFileLengthTextView = null;

    private boolean mIsPlaying = true;
    private Runnable mUpdateSeekBarRunnable = new Runnable() {
        @Override
        public void run() {
            if (mMediaPlayer != null) {
                int currentPosition = mMediaPlayer.getCurrentPosition();
                mSeekBar.setProgress(currentPosition);
                updateSeekBarDelay();
            }
        }
    };
    private Runnable mUpdateCurrentProgressRunnable = new Runnable() {
        @Override
        public void run() {
            if (mMediaPlayer != null) {
                int currentPosition = mMediaPlayer.getCurrentPosition();
                long minutes = TimeUnit.MILLISECONDS.toMinutes(currentPosition);
                long seconds = TimeUnit.MILLISECONDS.toSeconds(currentPosition) - TimeUnit.MINUTES.toSeconds(minutes);
                mCurrentProgressTextView.setText(String.format(Locale.getDefault(), CommonConstants.RECYCLER_VIEW_ITEM_LENGTH_FORMAT, minutes, seconds));
                updateCurrentProgressDelay();
            }
        }
    };

    public static PlaybackFragment newInstance(RecordItemData itemData) {
        PlaybackFragment fragment = new PlaybackFragment();
        Bundle bundle = new Bundle();
        bundle.putParcelable(BUNDLE_KEY_ITEM, itemData);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mRecordItemData = getArguments().getParcelable(BUNDLE_KEY_ITEM);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        View v = getActivity().getLayoutInflater().inflate(R.layout.fragment_playback, null);

        mFileNameTextView = (TextView) v.findViewById(R.id.file_name_text_view);
        mFileLengthTextView = (TextView) v.findViewById(R.id.file_length_text_view);
        mCurrentProgressTextView = (TextView) v.findViewById(R.id.current_progress_text_view);
        mSeekBar = (SeekBar) v.findViewById(R.id.seekbar);
        mFloatingActionButton = (FloatingActionButton) v.findViewById(R.id.fab_play);

        ColorFilter colorFilter = new LightingColorFilter(
                getResources().getColor(R.color.primary, null),
                getResources().getColor(R.color.primary, null)
        );
        mSeekBar.getProgressDrawable().setColorFilter(colorFilter);
        mSeekBar.getThumb().setColorFilter(colorFilter);
        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (mMediaPlayer != null && fromUser) {
                    mMediaPlayer.seekTo(progress);
                    long minutes = TimeUnit.MILLISECONDS.toMinutes(mMediaPlayer.getCurrentPosition());
                    long seconds = TimeUnit.MILLISECONDS.toSeconds(mMediaPlayer.getCurrentPosition()) - TimeUnit.MINUTES.toSeconds(minutes);
                    mCurrentProgressTextView.setText(String.format(Locale.getDefault(), CommonConstants.RECYCLER_VIEW_ITEM_LENGTH_FORMAT, minutes, seconds));
                    updateSeekBarImmediately();
                    updateCurrentProgressImmediately();
                } else if (mMediaPlayer == null && fromUser) {
                    preparePlayingWithoutClickPlayBtn(progress);
                    updateSeekBarImmediately();
                    updateCurrentProgressImmediately();
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                stopUpdatingSeekBar();
                stopUpdatingCurrentProgress();
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        mFloatingActionButton.setBackgroundTintList(new ColorStateList(
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
        mFloatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                executePlay(mIsPlaying);
                mIsPlaying = !mIsPlaying;
            }
        });

        mFileNameTextView.setText(mRecordItemData.getName());

        long duration = mRecordItemData.getLength();
        long fileMinutes = TimeUnit.MILLISECONDS.toMinutes(duration);
        long fileSeconds = TimeUnit.MILLISECONDS.toSeconds(duration) - TimeUnit.MINUTES.toSeconds(fileMinutes);
        mFileLengthTextView.setText(String.format(Locale.getDefault(), CommonConstants.RECYCLER_VIEW_ITEM_LENGTH_FORMAT, fileMinutes, fileSeconds));

        builder.setView(v);
        dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        return builder.create();
    }

    @Override
    public void onStart() {
        super.onStart();

        //set transparent background
        Window window = getDialog().getWindow();
        window.setBackgroundDrawableResource(android.R.color.transparent);

        //disable buttons from dialog
        AlertDialog alertDialog = (AlertDialog) getDialog();
        alertDialog.getButton(Dialog.BUTTON_POSITIVE).setEnabled(false);
        alertDialog.getButton(Dialog.BUTTON_NEGATIVE).setEnabled(false);
        alertDialog.getButton(Dialog.BUTTON_NEUTRAL).setEnabled(false);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mMediaPlayer != null) {
            stopPlaying();
        }
    }

    private void updateSeekBarImmediately() {
        mHandler.post(mUpdateSeekBarRunnable);
    }

    private void updateSeekBarDelay() {
        mHandler.postDelayed(mUpdateSeekBarRunnable, SEEK_BAR_UPDATE_DURATION);
    }

    private void updateCurrentProgressImmediately() {
        mHandler.post(mUpdateCurrentProgressRunnable);
    }

    private void updateCurrentProgressDelay() {
        mHandler.postDelayed(mUpdateCurrentProgressRunnable, CURRENT_PROGRESS_UPDATE_DURATION);
    }

    private void stopUpdatingSeekBar() {
        mHandler.removeCallbacks(mUpdateSeekBarRunnable);
    }

    private void stopUpdatingCurrentProgress() {
        mHandler.removeCallbacks(mUpdateCurrentProgressRunnable);
    }

    private void executePlay(boolean start) {
        if (start) {
            if (mMediaPlayer == null) {
                startPlaying();
            } else {
                resumePlaying();
            }
        } else {
            pausePlaying();
        }
    }

    private void startPlaying() {
        mFloatingActionButton.setImageResource(R.drawable.ic_media_pause);
        mMediaPlayer = new MediaPlayer();

        try {
            mMediaPlayer.setDataSource(mRecordItemData.getFilePath());
            mMediaPlayer.prepare();
            mSeekBar.setMax(mMediaPlayer.getDuration());
            mSeekBar.setProgress(0);
            mMediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    mMediaPlayer.start();
                }
            });
            mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    stopPlaying();
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }

        updateSeekBarImmediately();
        updateCurrentProgressImmediately();

        getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    private void pausePlaying() {
        mFloatingActionButton.setImageResource(R.drawable.ic_media_play);
        stopUpdatingSeekBar();
        stopUpdatingCurrentProgress();
        mMediaPlayer.pause();
    }

    private void resumePlaying() {
        mFloatingActionButton.setImageResource(R.drawable.ic_media_pause);
        mMediaPlayer.start();
        updateSeekBarImmediately();
        updateCurrentProgressImmediately();
        getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    private void stopPlaying() {
        mFloatingActionButton.setImageResource(R.drawable.ic_media_play);
        stopUpdatingSeekBar();
        stopUpdatingCurrentProgress();
        mMediaPlayer.stop();
        mMediaPlayer.reset();
        mMediaPlayer.release();
        mMediaPlayer = null;

        mSeekBar.setProgress(mSeekBar.getMax());
        mIsPlaying = true;

        mCurrentProgressTextView.setText(mFileLengthTextView.getText());

        getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    private void preparePlayingWithoutClickPlayBtn(int progress) {
        mMediaPlayer = new MediaPlayer();

        try {
            mMediaPlayer.setDataSource(mRecordItemData.getFilePath());
            mMediaPlayer.prepare();
            mSeekBar.setMax(mMediaPlayer.getDuration());
            mMediaPlayer.seekTo(progress);
            mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    stopPlaying();
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
