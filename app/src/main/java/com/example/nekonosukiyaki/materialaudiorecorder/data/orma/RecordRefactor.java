package com.example.nekonosukiyaki.materialaudiorecorder.data.orma;

import java.io.File;

/**
 * Created by nekonosukiyaki on 3/26/2017 AD.
 */

public class RecordRefactor {
    public static void insertDefault(RecordDatabase database,
                                     String fileName,
                                     String filePath,
                                     long length) {
        insertOrmaRecord(database, fileName, filePath, length);
    }

    private static void insertOrmaRecord(RecordDatabase database,
                                         String fileName,
                                         String filePath,
                                         long length) {
        File file = new File(filePath);
        long time = file.lastModified();

        Record record = new Record();
        record.fileName = fileName;
        record.filePath = filePath;
        record.fileLength = length;
        record.addedDate = time;
        database.insertIntoRecord(record);
    }
}
