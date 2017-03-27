package com.example.nekonosukiyaki.materialaudiorecorder.listeners;

import com.example.nekonosukiyaki.materialaudiorecorder.data.RecordItemData;

import android.view.View;

/**
 * Created by nekonosukiyaki on 3/26/2017 AD.
 */

public interface OnRecyclerViewItemClickListener {
    void onClick(View v, RecordItemData item);
    boolean onLongClick(View v, RecordItemData itemData);
}
