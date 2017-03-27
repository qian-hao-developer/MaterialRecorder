package com.example.nekonosukiyaki.materialaudiorecorder.data.orma;

import com.github.gfx.android.orma.annotation.Database;

/**
 * define database name
 */

@Database(
        databaseClassName = "RecordDatabase",
        includes = {Record.class}
)
public class DatabaseRecordConfiguration {
}
