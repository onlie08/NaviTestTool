package com.sfmap.map.demo;

import android.graphics.Color;
import android.support.annotation.Nullable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.ForegroundColorSpan;
import android.widget.TextView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.sfmap.api.navi.model.NaviPath;

import java.util.List;

public class PathAdapter extends BaseQuickAdapter<NaviPath, BaseViewHolder> {

    public PathAdapter(@Nullable List<NaviPath> data) {
        super(R.layout.path_item, data);
    }

    @Override
    protected void convert(BaseViewHolder helper, NaviPath item) {
        switch (item.getStrategy()){
            case 0:
                ((TextView) helper.getView(R.id.tv_type)).setText("推荐道路");
                break;
            case 1:
                ((TextView) helper.getView(R.id.tv_type)).setText("避免收费");
                break;
            case 2:
                ((TextView) helper.getView(R.id.tv_type)).setText("距离最短");
                break;
            case 3:
                ((TextView) helper.getView(R.id.tv_type)).setText("高速优先");
                break;
            case 9:
                ((TextView) helper.getView(R.id.tv_type)).setText("推荐道路");
                break;
        }
        ((TextView) helper.getView(R.id.tv_time)).setText(getTimeStr(item.getAllTime()));
        ((TextView) helper.getView(R.id.tv_distance)).setText(a(item.getAllLength(),24));
    }

    public static String getTimeStr(int second) {
        int minute = second / 60;
        String restTime = "";
        if (minute < 60) {
            if (minute == 0) {
                restTime = "<1分钟";
            } else {
                restTime = minute + "分钟";
            }
        } else {
            int hour = minute / 60;
            restTime = hour + "小时";
            minute = minute % 60;
            if (minute > 0) {
                restTime = restTime + minute + "分钟";
            }
        }
        return restTime;
    }

    public static SpannableString a(int paramInt, int textSize) {
        String result = "";
        SpannableString spanString ;
        AbsoluteSizeSpan sizeSpan = new AbsoluteSizeSpan(textSize);
        ForegroundColorSpan colorSpan = new ForegroundColorSpan(Color.BLACK);
        if (paramInt < 1000) {
            result = paramInt+ "米";
            spanString = new SpannableString(result);
            spanString.setSpan(sizeSpan, spanString.length()-1, spanString.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE );
        }else {
            result = (paramInt / 100 * 100 / 1000.0D)+ "公里";
            spanString = new SpannableString(result);
            spanString.setSpan(sizeSpan, spanString.length()-2, spanString.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE );
        }
        spanString.setSpan(colorSpan, 0, spanString.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE );
        return spanString;
    }
}
