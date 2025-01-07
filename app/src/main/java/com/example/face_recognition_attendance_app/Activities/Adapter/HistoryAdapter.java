package com.example.face_recognition_attendance_app.Activities.Adapter;

import android.text.Layout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.face_recognition_attendance_app.Activities.Models.AttendanceHistoryModel;
import com.example.face_recognition_attendance_app.R;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.ViewHolder> {
    private List<AttendanceHistoryModel> modelList;

    public HistoryAdapter(List<AttendanceHistoryModel> modelList) {
        this.modelList = modelList;
    }

    @NonNull
    @Override
    public HistoryAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.attendace_recycler_item,parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HistoryAdapter.ViewHolder holder, int position) {
        AttendanceHistoryModel model = modelList.get(position);

        String currentDate = model.getCurrentDateTime();
        String checkIn = model.getCheckInTime();
        String checkOut = model.getCheckOutTime();

        String cleanedTime1 = checkIn.replace(":pm:", "").replace(":am:", "");
        String cleanedTime2 = checkOut.replace(":pm:", "").replace(":am:", "");

        holder.checkInTine.setText(cleanedTime1);
        holder.checkOutTime.setText(cleanedTime2);

        SimpleDateFormat inputFormat = new SimpleDateFormat("hh:mm:ss:aa:dd:MM:yyyy");
        SimpleDateFormat outputFormat = new SimpleDateFormat("dd/MM/yyyy");

        try {
            Date date = inputFormat.parse(currentDate);
            String formattedDate = outputFormat.format(date);
            holder.date.setText(formattedDate);

        } catch (ParseException e) {
            System.out.println("Error parsing date: " + e.getMessage());
        }


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
}
