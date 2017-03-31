package com.byteshaft.patient.doctors;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.byteshaft.patient.R;
import com.byteshaft.patient.utils.AppGlobals;
import com.byteshaft.patient.utils.Helpers;
import com.byteshaft.requests.HttpRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Field;
import java.net.HttpURLConnection;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * Created by s9iper1 on 3/23/17.
 */

public class MySchedule extends Fragment implements HttpRequest.OnReadyStateChangeListener, HttpRequest.OnErrorListener {

    private View mBaseView;
    private ListView mListView;
    private ArrayList<JSONObject> scheduleList;
    private LinearLayout searchContainer;
    private String currentDate;
    private ArrayList<String> initialTimeSLots;
    private HttpRequest request;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mBaseView = inflater.inflate(R.layout.my_schedule, container, false);
        setHasOptionsMenu(true);
        searchContainer = new LinearLayout(getActivity());
        initialTimeSLots = new ArrayList<>();
        Toolbar toolbar = (Toolbar) getActivity().findViewById(R.id.toolbar);
        Toolbar.LayoutParams containerParams = new Toolbar.LayoutParams
                (ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        containerParams.gravity = Gravity.CENTER_VERTICAL;
        containerParams.setMargins(20, 20, 10, 20);
        getSchedule(Helpers.getDate());
        searchContainer.setLayoutParams(containerParams);
        // Setup search view
        final EditText toolbarSearchView = new EditText(getActivity());
        toolbarSearchView.setBackgroundColor(getResources().getColor(R.color.search_background));
        // Set width / height / gravity
        int[] textSizeAttr = new int[]{android.R.attr.actionBarSize};
        int indexOfAttrTextSize = 0;
        TypedArray a = getActivity().obtainStyledAttributes(new TypedValue().data, textSizeAttr);
        int actionBarHeight = a.getDimensionPixelSize(indexOfAttrTextSize, -1);
        a.recycle();
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, actionBarHeight);
        params.gravity = Gravity.CENTER_VERTICAL;
        params.setMargins(5, 5, 5, 5);
        params.weight = 1;
        toolbarSearchView.setLayoutParams(params);
        // Setup display
        toolbarSearchView.setPadding(2, 0, 0, 0);
        toolbarSearchView.setTextColor(Color.WHITE);
        toolbarSearchView.setGravity(Gravity.CENTER_VERTICAL);
        toolbarSearchView.setSingleLine(true);
        toolbarSearchView.setImeActionLabel("Search", EditorInfo.IME_ACTION_UNSPECIFIED);
        try {
            Field f = TextView.class.getDeclaredField("mCursorDrawableRes");
            f.setAccessible(true);
            f.set(toolbarSearchView, R.drawable.cursor_color);
        } catch (Exception ignored) {

        }
        // Search text changed listener
        toolbarSearchView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        toolbarSearchView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if (!b) {

                } else {

                }
            }
        });
        toolbarSearchView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                toolbarSearchView.setFocusable(true);
                toolbarSearchView.setFocusableInTouchMode(true);
                return false;
            }
        });
        toolbarSearchView.setFocusableInTouchMode(false);
        toolbarSearchView.setFocusable(false);
        (searchContainer).addView(toolbarSearchView);

        // Setup the clear button
        Resources r = getResources();
        int px = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 16, r.getDisplayMetrics());
        LinearLayout.LayoutParams clearParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        clearParams.gravity = Gravity.CENTER;
        // Add search view to toolbar and hide it
        toolbar.addView(searchContainer);
        scheduleList = new ArrayList<>();

        currentDate = Helpers.getDate();
        getTimeSlotsForDate(currentDate, TimeUnit.MINUTES.toMillis(45));
        return mBaseView;
    }

    private void getTimeSlotsForDate(String targetDate, long duration) {
        String time1 = "08:00";
        String time2 = "22:31";

        String format = "dd/MM/yyyy hh:mm";

        Log.i("TAG", "" + targetDate);
        Log.i("TAG", "" + targetDate);

        SimpleDateFormat sdf = new SimpleDateFormat(format);

        Date dateObj1 = null;
        Date dateObj2 = null;
        try {
            dateObj1 = sdf.parse(targetDate + " " + time1);
            dateObj2 = sdf.parse(targetDate + " " + time2);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        System.out.println("Date Start: " + dateObj1);
        System.out.println("Date End: " + dateObj2);

        long dif = dateObj1.getTime();
        while (dif < dateObj2.getTime()) {
            Date slot = new Date(dif);
//            System.out.println("Hour Slot --->" + slot);
            DateFormat df = new SimpleDateFormat("HH:mm");
            String date = df.format(slot.getTime());
//            System.out.println("Time --->" + date);
            initialTimeSLots.add(date);
            dif += duration;
        }

        for (int i = 0; i < initialTimeSLots.size(); i++) {
            StringBuilder time = new StringBuilder();
            if (i + 1 < initialTimeSLots.size()) {
                time.append(initialTimeSLots.get(i));
            }
            time.append(" , ");
            if (i + 1 < initialTimeSLots.size()) {
                time.append(initialTimeSLots.get(i + 1));
            }

            if (!time.toString().trim().isEmpty()) {
                String[] bothTimes = time.toString().split(",");
                JSONObject jsonObject = new JSONObject();
                try {
                    jsonObject.put("start_time", bothTimes[0]);
                    jsonObject.put("end_time", bothTimes[1]);
                    jsonObject.put("state", 0);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                if (!bothTimes[0].trim().isEmpty()) {
                    scheduleList.add(jsonObject);
                }
            }
        }
        mListView = (ListView) mBaseView.findViewById(R.id.schedule_list);
        mListView.setAdapter(new ScheduleAdapter(getActivity().getApplicationContext(), scheduleList));
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.search_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_search:

                return true;
            default:
                return false;
        }
    }

    private class ScheduleAdapter extends ArrayAdapter<String> {

        private ArrayList<JSONObject> scheduleList;
        private ViewHolder viewHolder;

        public ScheduleAdapter(@NonNull Context context, ArrayList<JSONObject> scheduleList) {
            super(context, R.layout.delegate_doctor_schedule);
            this.scheduleList = scheduleList;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = getActivity().getLayoutInflater()
                        .inflate(R.layout.delegate_doctor_schedule, parent, false);
                viewHolder = new ViewHolder();
                viewHolder.startTime = (TextView) convertView.findViewById(R.id.start_time);
                viewHolder.endTime = (TextView) convertView.findViewById(R.id.end_time);
                viewHolder.state = (CheckBox) convertView.findViewById(R.id.check_box_appointment);
                convertView.setTag(viewHolder);
                viewHolder.state.setTag(position);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }
            viewHolder.state.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    JSONObject jsonObject = scheduleList.get(position);
                    int pos = (int) viewHolder.state.getTag();
                    View checkBoxView = mListView.getChildAt(pos);
                    CheckBox cbx = (CheckBox) checkBoxView.findViewById(R.id.check_box_appointment);
                    Log.i("TAG", " checked" + cbx.isChecked());
                    Log.i("TAG", " id" + String.valueOf(cbx.getId() == R.id.check_box_appointment));
//                    if (!viewHolder.state.isChecked()) {
//                        try {
//                            jsonObject.put("state", 1);
//                        } catch (JSONException e) {
//                            e.printStackTrace();
//                        }
//                        viewHolder.state.setChecked(true);
//                        scheduleList.remove(position);
//                        scheduleList.add(position, jsonObject);
//                    } else {
//                        try {
//                            jsonObject.put("state", 0);
//                        } catch (JSONException e) {
//                            e.printStackTrace();
//                        }
//                        viewHolder.state.setChecked(false);
//                        scheduleList.remove(position);
//                        scheduleList.add(position, jsonObject);
//                    }
//                    notifyDataSetChanged();
//                    scheduleList.remove(position);
//                    scheduleList.add(position, jsonObject);
//                    Log.i("TAG", "List " + scheduleList);
                }
            });
//            viewHolder.state.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
//                @Override
//                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
//                    Log.i("TAG", " boolean " + b);
//                }
//            });
            try {
                viewHolder.startTime.setText(scheduleList.get(position).getString("start_time"));
                viewHolder.endTime.setText(scheduleList.get(position).getString("end_time"));
                if (scheduleList.get(position).getInt("state") == 0) {
                    viewHolder.state.setChecked(false);
                } else {
                    viewHolder.state.setChecked(true);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return convertView;
        }

        @Override
        public int getCount() {
            return scheduleList.size();
        }
    }

    private class ViewHolder {
        TextView startTime;
        TextView endTime;
        CheckBox state;
    }

    // Internet Connectivity Functions

    private void getSchedule(String date) {
        request = new HttpRequest(getActivity());
        request.setOnReadyStateChangeListener(this);
        request.setOnErrorListener(this);
        request.open("GET", String.format("%sdoctor/schedule?date=%s", AppGlobals.BASE_URL, date));
        request.setRequestHeader("Authorization", "Token " +
                AppGlobals.getStringFromSharedPreferences(AppGlobals.KEY_TOKEN));
        request.send();
    }

    @Override
    public void onReadyStateChange(HttpRequest request, int readyState) {

    }

    @Override
    public void onError(HttpRequest request, int readyState, short error, Exception exception) {
        switch (readyState) {
            case HttpRequest.STATE_DONE:
                switch (request.getStatus()) {
                    case HttpURLConnection.HTTP_OK:
                        Log.i("TAG", this.request.getResponseText());
                        break;

                }
        }

    }
}
