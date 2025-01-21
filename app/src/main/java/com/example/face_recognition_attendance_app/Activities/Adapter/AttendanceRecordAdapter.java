package com.example.face_recognition_attendance_app.Activities.Adapter;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.example.face_recognition_attendance_app.R;
import com.google.android.datatransport.backend.cct.BuildConfig;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class AttendanceRecordAdapter extends RecyclerView.Adapter<AttendanceRecordAdapter.ViewHolder> {
    private Context context;
    private List<File> fileList;

    public AttendanceRecordAdapter(Context context, List<File> fileList) {
        this.context = context;
        this.fileList = fileList;
    }

    @NonNull
    @Override
    public AttendanceRecordAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.attendance_record_rv_item,parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AttendanceRecordAdapter.ViewHolder holder, int position) {
        File file = fileList.get(position);
        holder.nameTv.setText(file.getName());

        holder.createdAt.setText(getFileCreationTime(file));

        holder.cardView.setOnClickListener(view -> {
            try {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                Uri fileUri = FileProvider.getUriForFile(context, context.getPackageName() + ".provider", file);
                intent.setDataAndType(fileUri, getMimeType(file));
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

                context.startActivity(intent);
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(context, "Unable to open file: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
        holder.cardView.setOnLongClickListener(view -> {
            Uri fileUri = FileProvider.getUriForFile(context, context.getPackageName() + ".provider", file);

            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType(getMimeType(file));
            shareIntent.putExtra(Intent.EXTRA_STREAM, fileUri);
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            try {
                context.startActivity(Intent.createChooser(shareIntent, "Share File Using"));
            } catch (ActivityNotFoundException e) {
                Toast.makeText(context, "No application found to share this file", Toast.LENGTH_SHORT).show();
            }

            return true;
        });
    }

    @Override
    public int getItemCount() {
        return fileList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView createdAt,nameTv;
        CardView cardView;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            nameTv = itemView.findViewById(R.id.fileNameTv);
            createdAt = itemView.findViewById(R.id.createdTv);
            cardView = itemView.findViewById(R.id.itemCard);
        }
    }
    public String  getFileCreationTime(File file) {
        String mothStr = "";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try {

                BasicFileAttributes attributes = Files.readAttributes(file.toPath(), BasicFileAttributes.class);

                long creationTimeInMillis = attributes.creationTime().toMillis();

                SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yyyy ");
                String formattedTime = sdf.format(new Date(creationTimeInMillis));
                mothStr = formattedTime;
                System.out.println("File: " + file.getName() + " created on: " + formattedTime);
            } catch (IOException e) {
                e.printStackTrace();
                System.out.println("Unable to retrieve file attributes: " + e.getMessage());
            }
        } else {
            System.out.println("File creation time retrieval requires Android O (API 26) or higher.");
        }
        return mothStr;
    }
    private String getMimeType(File file) {
        String extension = android.webkit.MimeTypeMap.getFileExtensionFromUrl(file.getName());
        return android.webkit.MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
    }

}
