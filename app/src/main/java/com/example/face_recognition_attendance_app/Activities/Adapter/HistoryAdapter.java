package com.example.face_recognition_attendance_app.Activities.Adapter;

import android.text.Layout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.face_recognition_attendance_app.Activities.Models.AttendanceDBModel;
import com.example.face_recognition_attendance_app.Activities.Models.AttendanceHistoryModel;
import com.example.face_recognition_attendance_app.R;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.ViewHolder> {
    private List<AttendanceDBModel> modelList;

    public HistoryAdapter(List<AttendanceDBModel> modelList) {
        this.modelList = modelList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.attendace_recycler_item,parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        AttendanceDBModel model = modelList.get(position);

        String currentDate = model.getCheckInDate();
        String checkIn = model.getCheckInTime();
        String checkOut = model.getCheckOutTime();

        String cleanedTime1 = checkIn.replace(":pm:", "").replace(":am:", "");
        String cleanedTime2 = checkOut.replace(":pm:", "").replace(":am:", "");

        holder.checkInTine.setText(cleanedTime1);
        holder.checkOutTime.setText(cleanedTime2);

        holder.date.setText(currentDate);

        String formattedTime = calculateTime(checkIn,checkOut);
        holder.totalTime.setText(formattedTime);
    }

    @Override
    public int getItemCount() {
        return modelList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private TextView date,checkInTine,checkOutTime,totalTime;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            date = itemView.findViewById(R.id.dateTxt);
            checkInTine = itemView.findViewById(R.id.checkInTime);
            checkOutTime = itemView.findViewById(R.id.checkOutTime);
            totalTime = itemView.findViewById(R.id.totalTime);

        }
    }
    private String  calculateTime(String checkInTime,String checkOutTime){

        SimpleDateFormat format = new SimpleDateFormat("hh:mm:ss:aa");
        String formattedTime = "";
        try {
            // Parse the time strings into Date objects
            Date checkInDate = format.parse(checkInTime);
            Date checkOutDate = format.parse(checkOutTime);

            // Calculate the difference in milliseconds
            long differenceInMillis = checkOutDate.getTime() - checkInDate.getTime();

            // Convert the difference to seconds (or other units)
            long differenceInSeconds = TimeUnit.MILLISECONDS.toSeconds(differenceInMillis);
            long hours = differenceInSeconds / 3600;
            long minutes = (differenceInSeconds % 3600) / 60;
            formattedTime = String.format("%02dH %02dM", hours, minutes);

            System.out.println("Time Difference: " + formattedTime + " seconds");
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return formattedTime;
    }
}
