package com.example.face_recognition_attendance_app.Activities.Util;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Handler;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.face_recognition_attendance_app.R;

public class UiHelper {
    public static Runnable runnableDeclined = null;
    public static Handler handlerDeclined = new Handler();
    public static void showFlawDialog(Context context, String title, String content, int type) {
        Dialog errorDialog;
        Button btnContinue;
        TextView contentTextView, titleTextView;
        ImageView imgErrorType;
        errorDialog = new Dialog(context);
        errorDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        errorDialog.setContentView(R.layout.error_type_dialog);
        errorDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        imgErrorType = errorDialog.findViewById(R.id.imageView11);
        btnContinue = errorDialog.findViewById(R.id.button7);
        contentTextView = errorDialog.findViewById(R.id.pass);
        titleTextView = errorDialog.findViewById(R.id.textView32);

//        setText:
        titleTextView.setText("" + title);
        contentTextView.setText("" + content);

//        showImageType:
        if (type == 1) {
            Glide.with(context).load(R.drawable.icon_error).into(imgErrorType);
        } else if (type == 2) {
            Glide.with(context).load(R.drawable.icon_success).into(imgErrorType);
        } else if (type == 3) {
            Glide.with(context).load(R.drawable.icon_warning).into(imgErrorType);
        }else if (type ==4){
            Glide.with(context).load(R.drawable.icon_warning).into(imgErrorType);
            btnContinue.setText("Ok");
        }
        else {
            Glide.with(context).load(R.drawable.icon_error).into(imgErrorType);
        }

        btnContinue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                handlerDeclined.removeCallbacks(runnableDeclined);
                errorDialog.dismiss();
            }
        });


        errorDialog.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
        errorDialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
        errorDialog.setCancelable(false);
        errorDialog.show();

        runnableDeclined = new Runnable() {
            @Override
            public void run() {
                handlerDeclined.removeCallbacks(runnableDeclined);
                errorDialog.dismiss();
            }
        };
        handlerDeclined.postDelayed(runnableDeclined, 7000);
    }

}
