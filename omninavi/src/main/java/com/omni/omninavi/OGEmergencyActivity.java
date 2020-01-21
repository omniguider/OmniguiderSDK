package com.omni.omninavi;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Spinner;
import android.widget.TextView;

/**
 * Created by wiliiamwang on 14/12/2017.
 */

public class OGEmergencyActivity extends Activity {

    private Spinner typeSpinner;

    private String[] typeArray = new String[]{"Entrance/Exit", "Stair", "Hydrant", "AED"};
    private String selectedType = null;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_emergency);

        findViewById(R.id.activity_emergency_btn_start).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                OGMapsActivity.emergencyNaviTo(OGEmergencyActivity.this, "1", selectedType);
            }
        });

        typeSpinner = (Spinner) findViewById(R.id.activity_emergency_spinner_type);
        typeSpinner.setAdapter(new TypeSpinnerAdapter(OGEmergencyActivity.this, typeArray));
        typeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedType = typeArray[position];
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    class TypeSpinnerAdapter<T> extends BaseAdapter {

        private String[] mList;
        private Context mContext;

        public TypeSpinnerAdapter(Context context, String[] dataList) {
            mList = dataList;
            mContext = context;
        }

        public void updateData(String[] dataList) {
            mList = dataList;
            notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            return mList.length;
        }

        @Override
        public String getItem(int position) {
            return mList[position];
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder;

            if (convertView == null) {
                convertView = LayoutInflater.from(mContext).inflate(R.layout.options_spinner_item_view, null, false);
                viewHolder = new ViewHolder();
                viewHolder.titleTextView = (TextView) convertView.findViewById(R.id.options_spinner_item_view_tv_title);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }

            viewHolder.titleTextView.setText(getItem(position));

            return convertView;
        }

        class ViewHolder {
            TextView titleTextView;
        }
    }
}
