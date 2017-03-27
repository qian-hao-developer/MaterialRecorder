// IRecordService.aidl
package com.example.nekonosukiyaki.materialaudiorecorder.service.aidl;

import com.example.nekonosukiyaki.materialaudiorecorder.data.RecordItemData;

// Declare any non-default types here with import statements

interface IRecordService {
    /**
     * Demonstrates some basic types that you can use as parameters
     * and return values in AIDL.
     */
    void startRecord();

    void stopRecord();

    void getRecordContentList(inout List<RecordItemData> list);

    void syncOrmaWithLocalFile();

    void refleshFileObserver();
}
