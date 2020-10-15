package com.reelvideos.app.utils;


import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import com.airbnb.lottie.LottieAnimationView;
import com.reelvideos.app.R;

public class DialogCreator {

    static Dialog progressDialog;


    public static void showProgressDialog(Context context){
        progressDialog = new Dialog(context);
        progressDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setCancelable(false);
        progressDialog.setContentView(R.layout.dialog_progress);
        LottieAnimationView logoIV = progressDialog.findViewById(R.id.logoIV);
        TextView progressText = progressDialog.findViewById(R.id.progressText);
        logoIV.setVisibility(View.VISIBLE);
        logoIV.playAnimation();
        progressText.setVisibility(View.VISIBLE);
        //Animation rotateAnim = AnimationUtils.loadAnimation(context, R.anim.clockwise_rotation_fast);
        //logoIV.startAnimation(rotateAnim);
        progressDialog.show();
    }

    public static void updateStatus(String text){
        ((TextView)progressDialog.findViewById(R.id.progressText)).setText(text);
    }

    public static void cancelProgressDialog(){
        if (progressDialog != null)
            progressDialog.cancel();
    }


}
