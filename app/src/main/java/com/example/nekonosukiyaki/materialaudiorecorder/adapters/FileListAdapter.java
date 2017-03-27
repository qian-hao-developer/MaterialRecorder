package com.example.nekonosukiyaki.materialaudiorecorder.adapters;

import com.example.nekonosukiyaki.materialaudiorecorder.R;
import com.example.nekonosukiyaki.materialaudiorecorder.constants.CommonConstants;
import com.example.nekonosukiyaki.materialaudiorecorder.data.RecordItemData;
import com.example.nekonosukiyaki.materialaudiorecorder.listeners.OnRecyclerViewItemClickListener;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

/**
 * Created by nekonosukiyaki on 3/25/2017 AD.
 */

public class FileListAdapter extends RecyclerView.Adapter<FileListAdapter.FileListViewHolder> {
    private static final String TAG = FileListAdapter.class.getSimpleName();

    private Context mContext;
    private ArrayList<RecordItemData> mList;
    private OnRecyclerViewItemClickListener mOnRecyclerViewItemClickListener;

    public FileListAdapter(Context context, ArrayList<RecordItemData> list, OnRecyclerViewItemClickListener listener) {
        if (context == null) {
            throw new IllegalArgumentException();
        }

        mContext = context;
        mList = list;
        mOnRecyclerViewItemClickListener = listener;
    }

    @Override
    public FileListViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.record_item, parent, false);
        return new FileListViewHolder(v);
    }

    @Override
    public void onBindViewHolder(FileListViewHolder holder, int position) {
        final RecordItemData itemData = mList.get(position);
        long timeDuration = itemData.getLength();
        long minutes = TimeUnit.MILLISECONDS.toMinutes(timeDuration);
        long seconds = TimeUnit.MILLISECONDS.toSeconds(timeDuration) - TimeUnit.MINUTES.toSeconds(minutes);

        holder.vName.setText(itemData.getName());
        holder.vLength.setText(
                String.format(
                        CommonConstants.RECYCLER_VIEW_ITEM_LENGTH_FORMAT,
                        minutes,
                        seconds
                )
        );
        holder.vDataAdded.setText(
                DateUtils.formatDateTime(
                        mContext,
                        itemData.getTime(),
                        DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_NUMERIC_DATE | DateUtils.FORMAT_SHOW_TIME | DateUtils.FORMAT_SHOW_YEAR
                )
        );
        holder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mOnRecyclerViewItemClickListener != null) {
                    mOnRecyclerViewItemClickListener.onClick(v, itemData);
                }
            }
        });
        holder.cardView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (mOnRecyclerViewItemClickListener != null) {
                    return mOnRecyclerViewItemClickListener.onLongClick(v, itemData);
                }
                return false;
            }
        });

    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    public void swapData(ArrayList<RecordItemData> newList) {
        mList = newList;
        notifyDataSetChanged();
    }

    public static class FileListViewHolder extends RecyclerView.ViewHolder {
        protected TextView vName;
        protected TextView vLength;
        protected TextView vDataAdded;
        protected View cardView;

        public FileListViewHolder(View itemView) {
            super(itemView);
            vName = (TextView) itemView.findViewById(R.id.file_name);
            vLength = (TextView) itemView.findViewById(R.id.file_length);
            vDataAdded = (TextView) itemView.findViewById(R.id.file_added_date);
            cardView = itemView.findViewById(R.id.card_view);
        }
    }
}
