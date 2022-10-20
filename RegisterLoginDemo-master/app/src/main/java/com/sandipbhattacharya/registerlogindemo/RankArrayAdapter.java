package com.sandipbhattacharya.registerlogindemo;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;

public class RankArrayAdapter extends ArrayAdapter<RankItem> {
    private Context context;
    private List<RankItem> rankItems;

    public RankArrayAdapter(@NonNull Context context, int resource, List<RankItem> rankItems) {
        super(context, resource, rankItems);
        this.context = context;
        this.rankItems = rankItems;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        LayoutInflater inflater = LayoutInflater.from(this.context);
        LinearLayout itemLayout = null;

        if (convertView == null) {
            itemLayout = (LinearLayout) inflater.inflate(R.layout.rank_list, null);
        } else {
            itemLayout = (LinearLayout) convertView;
        }

        RankItem item = rankItems.get(position);

        TextView tvRankname = itemLayout.findViewById(R.id.tv_rank_name);
        tvRankname.setText(item.getRank_name());

        TextView tvRankscore = itemLayout.findViewById(R.id.tv_rank_score);
        tvRankscore.setText(item.getRank_score());

        TextView tvRankupdatetime = itemLayout.findViewById(R.id.tv_rank_updatetime);
        tvRankupdatetime.setText(item.getRank_updatetime());

        return itemLayout;
    }
}
