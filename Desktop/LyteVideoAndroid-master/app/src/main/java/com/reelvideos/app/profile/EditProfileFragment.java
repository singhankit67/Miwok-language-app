package com.reelvideos.app.profile;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.exifinterface.media.ExifInterface;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.toolbox.HttpHeaderParser;
import com.google.android.exoplayer2.source.SampleStream;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.reelvideos.app.R;
import com.reelvideos.app.base.BaseFragment;
import com.reelvideos.app.base.CoreApp;
import com.reelvideos.app.config.Constants;
import com.reelvideos.app.config.GlobalVariables;
import com.reelvideos.app.databinding.FragmentEditProfileBinding;
import com.reelvideos.app.utils.AppExtensions;
import com.reelvideos.app.utils.DialogCreator;
import com.reelvideos.app.utils.Utils;
import com.reelvideos.app.utils.VolleyMultipartRequest;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;

import org.apache.commons.lang3.StringEscapeUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import static android.app.Activity.RESULT_OK;
import static com.reelvideos.app.config.Constants.INDIAN_STATES;
import static com.reelvideos.app.config.Constants.PREF_FULL_NAME;
import static com.reelvideos.app.config.Constants.PREF_INSTA_ID;
import static com.reelvideos.app.config.Constants.PREF_USER_BIO;
import static com.reelvideos.app.config.Constants.PREF_USER_COVER;
import static com.reelvideos.app.config.Constants.PREF_USER_GENDER;
import static com.reelvideos.app.config.Constants.PREF_USER_PIC;
import static com.reelvideos.app.config.Constants.PREF_USER_REGION;
import static com.reelvideos.app.utils.AppExtensions.showToast;


public class EditProfileFragment extends BaseFragment {

    FragmentEditProfileBinding binding;
    final Calendar myCalendar = Calendar.getInstance();
    String imageFilePath;
    int lastRequestCode = 0;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentEditProfileBinding.inflate(inflater, container, false);
        initComponents();
        return binding.getRoot();
    }
    @SuppressLint("RestrictedApi")
    @Override
    public void initComponents() {

        binding.closeBtn.setOnClickListener(view -> activity.onBackPressed());
        binding.saveBtn.setOnClickListener(view -> {
            String username = binding.usernameET.getText().toString();
            String lastChar = null;
            if (!username.equals(""))
                lastChar = String.valueOf(username.charAt(username.length()-1));
            if (lastChar != null) {
                if (!(lastChar.equals("."))) {
                    saveData(coverImage != null, userImage != null);
                } else {
                    binding.usernameET.setError("Username can't use underscores and periods at the end.");
                }
            }  else {
                binding.usernameET.setError("Username can not be blank.");
            }

        });
        binding.usernameET.setText(GlobalVariables.getUserName());
        binding.nameInputEt.setText(StringEscapeUtils.unescapeJava(GlobalVariables.getPrefsString(PREF_FULL_NAME)));
        binding.bioET.setText(StringEscapeUtils.unescapeJava(GlobalVariables.getPrefsString(PREF_USER_BIO)));
        binding.instaET.setText (  GlobalVariables.getPrefsString ( PREF_INSTA_ID ) );


        Picasso.get().load(GlobalVariables.getPrefsString(PREF_USER_COVER)).placeholder(R.drawable.cover).into(binding.coverImage);
        Picasso.get().
                load(GlobalVariables.getPrefsString(PREF_USER_PIC)).centerCrop()
                .placeholder(ContextCompat.getDrawable(activity, R.drawable.default_pic))
                .resize(200,200).into(binding.userImage);


        /*binding.dobET.setOnClickListener(view -> new MaterialStyledDatePickerDialog(activity, date, myCalendar
                .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                myCalendar.get(Calendar.DAY_OF_MONTH)).show());*/

        binding.userImage.setOnClickListener(view -> {
            selectImage(100);
        });
        binding.uploadPicBtn.setOnClickListener(view ->  {
            selectImage(100);
        });
        binding.changeCover.setOnClickListener(view ->  {
            selectImage(200);
        });

        // 200 -> Camera, 201 -> From Gallery

        //INDIAN_STATES
        //shubham_keshri
        if(GlobalVariables.getPrefsString ( PREF_USER_REGION )!=null)
            binding.filledExposedDropdown.setText ( GlobalVariables.getPrefsString ( PREF_USER_REGION).trim () );
        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(
                        activity,
                        R.layout.dropdown_menu_popup_item,
                        INDIAN_STATES);
        binding.filledExposedDropdown.setAdapter(adapter);



        //GENDER
        //shubham_keshri
        if(GlobalVariables.getPrefsString ( PREF_USER_GENDER )!=null)
            binding.genderDrop.setText ( GlobalVariables.getPrefsString ( PREF_USER_GENDER).trim () );
        adapter = new ArrayAdapter<>(activity, R.layout.dropdown_menu_popup_item, new String[]  {"Male", "Female"});
        binding.genderDrop.setAdapter(adapter);

        
    }

    private void selectImage(int requestCode) {

        final CharSequence[] options = { getString(R.string.take_pic), getString(R.string.choose_gal), getString(R.string.cancel) };

        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(activity);

        builder.setTitle(R.string.add_photo);

        builder.setItems(options, (dialog, item) -> {

            if (options[item].equals(getString(R.string.take_pic)))

            {
                if(checkPermission())
                    openCameraIntent(requestCode);

            }

            else if (options[item].equals(getString(R.string.choose_gal)))
            {

                if(checkPermission()) {
                    Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(intent, requestCode + 1);
                }
            }

            else if (options[item].equals(getString(R.string.cancel))) {

                dialog.dismiss();

            }

        });

        builder.show();

    }


    private void openCameraIntent(int requestCode) {
        Intent pictureIntent = new Intent(
                MediaStore.ACTION_IMAGE_CAPTURE);
        if(pictureIntent.resolveActivity(activity.getPackageManager()) != null){
            //Create a file to store the image
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File

            }
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(activity.getApplicationContext(), getActivity().getPackageName()+".fileprovider", photoFile);
                pictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(pictureIntent, requestCode);
            }
        }
    }

    public boolean checkPermission() {

        String[] PERMISSIONS = {
                Manifest.permission.READ_EXTERNAL_STORAGE,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.CAMERA
        };

        if (!AppExtensions.hasPermissions(activity, PERMISSIONS)) {
            requestPermissions(PERMISSIONS, 4);
        }else {

            return true;
        }

        return false;
    }


    private File createImageFile() throws IOException {
        String timeStamp =
                new SimpleDateFormat("yyyyMMdd_HHmmss",
                        Locale.getDefault()).format(new Date());
        String imageFileName = "IMG_" + timeStamp + "_";
        File storageDir =
                getActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,
                ".jpg",
                storageDir
        );

        imageFilePath = image.getAbsolutePath();
        return image;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {

            if (requestCode != CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE)
                lastRequestCode = requestCode;

            if (requestCode == 100) {
                //User pic from camera
                Matrix matrix = new Matrix();
                try {
                    ExifInterface exif = new ExifInterface(imageFilePath);
                    int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 1);
                    switch (orientation) {
                        case ExifInterface.ORIENTATION_ROTATE_90:
                            matrix.postRotate(90);
                            break;
                        case ExifInterface.ORIENTATION_ROTATE_180:
                            matrix.postRotate(180);
                            break;
                        case ExifInterface.ORIENTATION_ROTATE_270:
                            matrix.postRotate(270);
                            break;
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }
                Uri selectedImage =(Uri.fromFile(new File(imageFilePath)));

                beginCropForImage(selectedImage);


            }

            else if (requestCode == 101) {
                //User pic from gallery
                Uri selectedImage = data.getData();
                beginCropForImage(selectedImage);

            }
            if (requestCode == 200) {
                //User Cover from camera
                Matrix matrix = new Matrix();
                try {
                    ExifInterface exif = new ExifInterface(imageFilePath);
                    int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 1);
                    switch (orientation) {
                        case ExifInterface.ORIENTATION_ROTATE_90:
                            matrix.postRotate(90);
                            break;
                        case ExifInterface.ORIENTATION_ROTATE_180:
                            matrix.postRotate(180);
                            break;
                        case ExifInterface.ORIENTATION_ROTATE_270:
                            matrix.postRotate(270);
                            break;
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }
                Uri selectedImage =(Uri.fromFile(new File(imageFilePath)));

                beginCropForCover(selectedImage);


            }

            else if (requestCode == 201) {
                //User Cover from gallery
                Uri selectedImage = data.getData();
                beginCropForCover(selectedImage);

            }
            else if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {

                CropImage.ActivityResult result = CropImage.getActivityResult(data);
                handleCrop(result.getUri(), lastRequestCode == 100 || lastRequestCode == 101);
            }

        }

    }
    private void beginCropForImage(Uri source) {

     CropImage.activity(source).setAspectRatio(1,1).start(getContext(), this);
    }
    private void beginCropForCover(Uri source){


        CropImage.activity(source).setAspectRatio(5,2).start(getContext(), this);

    }

    Bitmap userImage = null;
    Bitmap coverImage = null;

    private void handleCrop( Uri finalImageURI, boolean isPicRequest) {

        InputStream imageStream = null;
        try {
            imageStream = activity.getContentResolver().openInputStream(finalImageURI);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        final Bitmap imagebitmap = BitmapFactory.decodeStream(imageStream);

        String path = finalImageURI.getPath();
        Matrix matrix = new Matrix();
        android.media.ExifInterface exif = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            try {
                exif = new android.media.ExifInterface(path);
                int orientation = exif.getAttributeInt(android.media.ExifInterface.TAG_ORIENTATION, 1);
                switch (orientation) {
                    case android.media.ExifInterface.ORIENTATION_ROTATE_90:
                        matrix.postRotate(90);
                        break;
                    case android.media.ExifInterface.ORIENTATION_ROTATE_180:
                        matrix.postRotate(180);
                        break;
                    case android.media.ExifInterface.ORIENTATION_ROTATE_270:
                        matrix.postRotate(270);
                        break;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        Bitmap rotatedBitmap = Bitmap.createBitmap(imagebitmap, 0, 0, imagebitmap.getWidth(), imagebitmap.getHeight(), matrix, true);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        rotatedBitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);


        if (isPicRequest){
            binding.userImage.setImageBitmap(rotatedBitmap);
            userImage = rotatedBitmap;
        }
        else  {
            binding.coverImage.setImageBitmap(rotatedBitmap);
            coverImage = rotatedBitmap;
        }


    }


    private void saveData(boolean isCoverRequested, boolean isPicRequested) {
        DialogCreator.showProgressDialog(activity);
        DialogCreator.updateStatus(getString(R.string.updating_profile));

        VolleyMultipartRequest volleyMultipartRequest = new VolleyMultipartRequest(Request.Method.POST, Constants.API_UPDATE_PROFILE,
                (NetworkResponse response) -> {
                    DialogCreator.cancelProgressDialog();
                    if (response.statusCode == 200) {
                        try {
                            String jsonString = new String(response.data, HttpHeaderParser.parseCharset(response.headers));
                            GlobalVariables.saveUserData(new JSONObject(jsonString));
                            showToast(activity, getString(R.string.update_success));
                            activity.sendBroadcast(new Intent("newVideo"));
                        } catch (UnsupportedEncodingException | JSONException e) {
                            e.printStackTrace();
                        }
                    }else {
                        showToast(activity, getString(R.string.update_fail) + response.data);
                    }
                    DialogCreator.cancelProgressDialog();
                },
                error -> {
                    DialogCreator.cancelProgressDialog();
                    showToast(activity, getString(R.string.update_fail));
                })


        {



            @Override
            protected Map<String, DataPart> getByteData() {
                Map<String, DataPart> params = new HashMap<>();
                long imageName = System.currentTimeMillis();
                if (isPicRequested)
                params.put("pic", new DataPart(imageName + "_.jpg", Utils.getFileDataFromDrawable(userImage)));
                if (isCoverRequested)
                params.put("cover", new DataPart(imageName + ".jpg", Utils.getFileDataFromDrawable(coverImage)));
                return params;
            }

            @Override
            public Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();



                params.put("username", binding.usernameET.getText().toString());


                params.put("fullname", StringEscapeUtils.escapeJava( binding.nameInputEt.getText().toString()));


                params.put("bio", StringEscapeUtils.escapeJava( binding.bioET.getText().toString()));


                params.put("instagram_username", binding.instaET.getText().toString());



                if (binding.filledExposedDropdown.getText().length() > 3)
                    params.put("region", binding.filledExposedDropdown.getText().toString());

                if (binding.genderDrop.getText().length() > 3)
                    params.put("gender", binding.genderDrop.getText().toString());

                /*if (binding.dobET.getText().length() > 3)
                    params.put("dob", binding.dobET.getText().toString());*/
                return params;
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("Authorization", "Bearer " + GlobalVariables.getAuthKey());



                return params;
            }


        };


        CoreApp.getInstance().queue.add(volleyMultipartRequest);



    }

    private boolean isValidUserName(String username){
        String usernamePattern = "^[a-zA-Z0-9]+([_ -]?[a-zA-Z0-9])*$";
        if (username.matches(usernamePattern)) {
            return true;
        } else {
            return false;
        }
    }







}