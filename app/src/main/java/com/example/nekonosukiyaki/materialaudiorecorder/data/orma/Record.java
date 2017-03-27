package com.example.nekonosukiyaki.materialaudiorecorder.data.orma;

import com.github.gfx.android.orma.annotation.Column;
import com.github.gfx.android.orma.annotation.PrimaryKey;
import com.github.gfx.android.orma.annotation.Table;

import android.support.annotation.Nullable;

/**
 * OrmaRecord content db synopsis
 */

@Table
public class Record {

    @PrimaryKey(autoincrement = true)
    public long id;

    @Column(indexed = true)
    public String fileName;

    @Column(indexed = true)
    public String filePath;

    @Column(indexed = true)
    @Nullable
    public long fileLength;

    @Column(indexed = true)
    @Nullable
    public long addedDate;

}
