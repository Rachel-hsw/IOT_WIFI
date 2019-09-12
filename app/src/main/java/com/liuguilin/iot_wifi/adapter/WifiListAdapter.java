package com.liuguilin.iot_wifi.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.liuguilin.iot_wifi.R;
import com.liuguilin.iot_wifi.model.WifiListModel;

import org.w3c.dom.Text;

import java.util.List;

/**
 * FileName: WifiListAdapter
 * Founder: LiuGuiLin
 * Create Date: 2019/2/13 10:52
 * Email: lgl@szokl.com.cn
 * Profile:
 */
public class WifiListAdapter extends RecyclerView.Adapter<WifiListAdapter.ViewHolder> {

    private Context mContext;
    private LayoutInflater inflater;
    private List<WifiListModel> mList;

    private OnItemClickListener mOnItemClickListener;

    public void setOnItemClickListener(OnItemClickListener mOnItemClickListener) {
        this.mOnItemClickListener = mOnItemClickListener;
    }

    public WifiListAdapter(Context mContext, List<WifiListModel> mList) {
        this.mContext = mContext;
        this.mList = mList;
        inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        return new ViewHolder(inflater.inflate(R.layout.layout_wifi_list_item, null));
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int i) {
        WifiListModel model = mList.get(i);
        viewHolder.tvName.setText(model.getName());
        viewHolder.tvState.setText(model.getState() ? "有锁" : "");
        viewHolder.tvLevel.setText(model.getLevel() + "");

        viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mOnItemClickListener.OnClick(i);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private TextView tvName;
        private TextView tvState;
        private TextView tvLevel;

        public ViewHolder(View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_name);
            tvState = itemView.findViewById(R.id.tv_status);
            tvLevel = itemView.findViewById(R.id.tv_level);
        }
    }

    public interface  OnItemClickListener{
        void OnClick(int i);
    }
}
