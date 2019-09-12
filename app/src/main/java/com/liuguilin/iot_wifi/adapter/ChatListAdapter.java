package com.liuguilin.iot_wifi.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.liuguilin.iot_wifi.R;
import com.liuguilin.iot_wifi.model.ChatListModel;

import java.util.List;

/**
 * FileName: ChatListAdapter
 * Founder: LiuGuiLin
 * Create Date: 2019/2/18 17:03
 * Email: lgl@szokl.com.cn
 * Profile:
 */
public class ChatListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public static final int LEFT_TEXT = 0;
    public static final int RIGHT_TEXT = 1;

    private Context mContext;
    private LayoutInflater inflater;
    private List<ChatListModel> mList;

    public ChatListAdapter(Context mContext, List<ChatListModel> mList) {
        this.mContext = mContext;
        this.mList = mList;
        inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        if (LEFT_TEXT == i) {
            return new LeftViewHolder(inflater.inflate(R.layout.layout_left_text_item, null));
        } else {
            return new RightViewHolder(inflater.inflate(R.layout.layout_right_text_item, null));
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int i) {
        int type = getItemViewType(i);
        ChatListModel model = mList.get(i);
        if (LEFT_TEXT == type) {
            ((LeftViewHolder) viewHolder).tvLeft.setText(model.getLeftText());
        } else if (RIGHT_TEXT == type) {
            ((RightViewHolder) viewHolder).tvRight.setText(model.getRightText());
        }
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    @Override
    public int getItemViewType(int position) {
        int type = mList.get(position).getType();
        return type;
    }

    class LeftViewHolder extends RecyclerView.ViewHolder {

        private TextView tvLeft;

        public LeftViewHolder(View itemView) {
            super(itemView);
            tvLeft = itemView.findViewById(R.id.tv_left);
        }
    }

    class RightViewHolder extends RecyclerView.ViewHolder {

        private TextView tvRight;

        public RightViewHolder(View itemView) {
            super(itemView);
            tvRight = itemView.findViewById(R.id.tv_right);
        }
    }
}

