package com.reelvideos.app;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import com.reelvideos.app.api.CommonClassForAPI;
import com.reelvideos.app.base.BaseActivity;
import com.reelvideos.app.callbacks.ApiResponseCallback;
import com.reelvideos.app.config.GlobalVariables;
import com.reelvideos.app.databinding.ActivityLoginBinding;
import com.stfalcon.smsverifycatcher.OnSmsCatchListener;
import com.stfalcon.smsverifycatcher.SmsVerifyCatcher;

import org.json.JSONObject;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.reelvideos.app.utils.AppExtensions.showToast;

public class LoginActivity extends BaseActivity {

    ActivityLoginBinding binding;
    boolean isNewUser = true;
    SmsVerifyCatcher smsVerifyCatcher;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT == 26) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
        } else {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_FULL_USER);
        }

        getWindow().setBackgroundDrawable(
                new ColorDrawable(android.graphics.Color.TRANSPARENT));

        this.getWindow()
                .setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                        WindowManager.LayoutParams.FLAG_FULLSCREEN);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        initComponents();

//        smsVerifyCatcher = new SmsVerifyCatcher ( this, new OnSmsCatchListener<String> () {
//            @Override
//            public void onSmsCatch(String message) {
//                String code = parseCode ( message );//Parse verification code
//                binding.otpView.setText ( code );//set code in edit text
//                Toast.makeText ( LoginActivity.this, code, Toast.LENGTH_SHORT ).show ();
//                //then you can send verification code to server
//            }
//        } );
//        //set phone number filter if needed
//        smsVerifyCatcher.setPhoneNumberFilter ( "777" );
//        //smsVerifyCatcher.setFilter("regexp");

    }

    @Override
    public void initComponents() {

        //Set listener
        binding.goBack.setOnClickListener(view -> onBackPressed());
        binding.loginBtn.setOnClickListener(view -> requestOTP());
        //binding.mobileInputET.setOnClickListener ( view -> shoeAvailableMobileNumber () );
        binding.startExplore.setOnClickListener(view -> {
            sendBroadcast(new Intent("newVideo"));
            onBackPressed();
        });

        binding.resendOTP.setOnClickListener(view -> {
            binding.otpLayout.setVisibility(View.GONE);
            binding.progressText.setText("Resending OTP");
            showToast(LoginActivity.this, getString(R.string.resent_otp));
            requestOTP();
        });

        //OTP Layout
        binding.otpBtn.setOnClickListener(view -> verifyOTP());


    }

//    private void shoeAvailableMobileNumber() {
//
//        TelephonyManager tm = null;
//        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
//            tm = (TelephonyManager) getSystemService ( Context.TELEPHONY_SERVICE );
//        }
//        if (ActivityCompat.checkSelfPermission ( this, Manifest.permission.READ_SMS ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission ( this, Manifest.permission.READ_PHONE_NUMBERS ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission ( this, Manifest.permission.READ_PHONE_STATE ) != PackageManager.PERMISSION_GRANTED) {
//            // TODO: Consider calling
//            //    ActivityCompat#requestPermissions
//            // here to request the missing permissions, and then overriding
//            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
//            //                                          int[] grantResults)
//            // to handle the case where the user grants the permission. See the documentation
//            // for ActivityCompat#requestPermissions for more details.
//            return;
//        }
//        String n = tm.getLine1Number ();
//        binding.mobileInputET.setText ( n );
//
//    }


    private void requestOTP() {
        //otp Layout
        String hint = "+91 " + binding.mobileInputET.getText().toString();
        binding.tv3.setText(hint);


        //Validate and Fire
//        if (!isValidMobileNumber(binding.mobileInputET.getText().toString().trim())) {
//            showToast(LoginActivity.this, getString(R.string.mob_error));
//            cancelLoader();
//            return;
//        }

        if(isValidMobileNumber(binding.mobileInputET.getText().toString().trim())){
            binding.mobileLayout.setVisibility(View.GONE);
            binding.progressText.setText(R.string.sending_otp);
            showLoader();
            //Fire for OTP
            CommonClassForAPI.callAuthAPI(binding.mobileInputET.getText().toString(), new ApiResponseCallback() {
                @Override
                public void onApiSuccessResult(JSONObject jsonObject) {

                    //Checks for new user registration
                    if (!jsonObject.optBoolean("signup"))
                        isNewUser = false;

                    showOtpLayout();
                    cancelLoader();
                    countDownToResendOTP();
                }

                @Override
                public void onApiFailureResult(Exception e) {
                    showToast(LoginActivity.this, getString(R.string.error_send_otp));
                    cancelLoader();
                    binding.mobileLayout.setVisibility(View.VISIBLE);
                }
            });
        }else {
            showToast(LoginActivity.this, getString(R.string.mob_error));
            cancelLoader();
        }

    }


    private void verifyOTP() {
        //Check length and fire
        if (Objects.requireNonNull(binding.otpView.getText()).toString().trim().length() == 6) {
            binding.otpLayout.setVisibility(View.GONE);
            binding.progressText.setText(R.string.validate_otp);
            showLoader();

            //Send for verification
            CommonClassForAPI.callOtpVerifyAPI(binding.mobileInputET.getText().toString(), binding.otpView.getText().toString(), new ApiResponseCallback() {
                @Override
                public void onApiSuccessResult(JSONObject jsonObject) {

                    GlobalVariables.saveUserData(jsonObject);
                    binding.successLayout.setVisibility(View.GONE);
                    sendBroadcast(new Intent("newVideo"));
                    sendBroadcast(new Intent("uploadVideo"));
                    cancelLoader();
                    if (!isNewUser) {
//                        binding.otpLayout.setVisibility ( View.GONE );
//                        binding.successLayout.setVisibility ( View.GONE );
//                        binding.logo.setVisibility ( View.GONE );
                        sendBroadcast(new Intent("newVideo"));
                        onBackPressed();

                    }
                }

                @Override
                public void onApiFailureResult(Exception e) {
                    cancelLoader();
                    showToast(LoginActivity.this, getString(R.string.opt_err));
                    binding.otpLayout.setVisibility(View.VISIBLE);
                    binding.otpView.setText("");
                    binding.otpView.setLineColor(getResources().getColor(R.color.redTransparent));
                }
            });

        } else showToast(LoginActivity.this, getString(R.string.otp_len_error));

    }


    void cancelLoader() {
        binding.progressLayout.setVisibility(View.GONE);
        binding.logoIV.clearAnimation();
//        binding.logo.setVisibility ( View.VISIBLE );
    }

    void showLoader() {

        binding.progressLayout.setVisibility(View.VISIBLE);
        Animation rotateAnim = AnimationUtils.loadAnimation(this, R.anim.clockwise_rotation_fast);
        binding.logoIV.startAnimation(rotateAnim);
        binding.logo.setVisibility(View.GONE);
    }

    @Override
    public void onBackPressed() {
        if (binding.otpLayout.getVisibility() == View.VISIBLE) {
            binding.otpLayout.setVisibility(View.GONE);
            binding.mobileLayout.setVisibility(View.VISIBLE);
            overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
        } else {
            finish();
        }
        overridePendingTransition(R.anim.slide_from_top, R.anim.out_from_bottom);

    }

    private void showOtpLayout() {

        binding.mobileLayout.setVisibility(View.GONE);
        binding.otpLayout.setVisibility(View.VISIBLE);

    }

    @Override
    public void onEnterAnimationComplete() {
        super.onEnterAnimationComplete();
        AlphaAnimation anim = new AlphaAnimation(0.0f, 1.0f);
        anim.setDuration(200);


    }


    public void countDownToResendOTP() {
        new CountDownTimer(60000, 100) {

            public void onTick(long millisUntilFinished) {
                binding.resendOTP.setClickable(false);
                binding.resendOTP.setBackground(getDrawable(R.color.transparentColor));
                binding.resendOTP.setText("0:" + ((millisUntilFinished / 1000)));

                //here you can have your logic to set text to edittext
            }

            public void onFinish() {
                binding.resendOTP.setText("Resend");
                binding.resendOTP.setClickable(true);
            }

        }.start();
    }


    public static boolean isValidMobileNumber(String s) {
        Pattern p = Pattern.compile("(0/91)?[7-9][0-9]{9}");


        Matcher m = p.matcher(s);
        return (m.find() && m.group().equals(s));
    }


    // otp reader
//    /**
//     * Parse verification code
//     *
//     * @param message sms message
//     * @return only four numbers from massage string
//     */
//    private String parseCode(String message) {
//        Pattern p = Pattern.compile("\\b\\d{4}\\b");
//        Matcher m = p.matcher(message);
//        String code = "";
//        while (m.find()) {
//            code = m.group(0);
//        }
//        return code;
//    }
//
//    @Override
//    protected void onStart() {
//        super.onStart();
//        smsVerifyCatcher.onStart();
//    }
//
//    @Override
//    protected void onStop() {
//        super.onStop();
//        smsVerifyCatcher.onStop();
//    }
//
//    /**
//     * need for Android 6 real time permissions
//     */
//    @Override
//    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
//        smsVerifyCatcher.onRequestPermissionsResult(requestCode, permissions, grantResults);
//    }
//

}

/*
*
* version -
* model -
*
* */