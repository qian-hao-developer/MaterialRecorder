package com.example.nekonosukiyaki.materialaudiorecorder.constants;

import java.text.SimpleDateFormat;
import java.util.Locale;

/**
 * Common Constants Definition
 */

public class CommonConstants {
    public static final String RECORD_DATABASE_NAME = "record.db";
    public static final String DEFAULT_RECORD_FILE_NAME = "MyRecording";
    public static final String RECORD_FILE_PATH_TOP = "/SoundRecorder";
    public static final int SERVICE_EXECUTOR_MAX_THREAD = 4;
    public static final String RECYCLER_VIEW_ITEM_LENGTH_FORMAT = "%02d:%02d";
    private static final SimpleDateFormat TIME_FORMAT = new SimpleDateFormat("mm:ss", Locale.getDefault());
}
