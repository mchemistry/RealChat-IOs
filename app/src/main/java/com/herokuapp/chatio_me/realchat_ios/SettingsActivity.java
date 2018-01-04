package com.herokuapp.chatio_me.realchat_ios;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Gallery;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.data.BitmapTeleporter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import org.w3c.dom.Text;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import id.zelory.compressor.Compressor;

public class SettingsActivity extends AppCompatActivity {
    private CircleImageView settingsDisplayImage;
    private TextView settingsDisplayName;
    private TextView settingsDisplayStatus;
    private Button settingsChangeProfileImage;
    private Button settingsChangeStatus;

    private final static int Gallery_Pick = 1;

    private StorageReference storeProfileImagestorageRef;
    private DatabaseReference getUserDatabaseReference;
    private FirebaseAuth mAuth;
    private StorageReference thumbImageRef;
    private ProgressDialog loadingBar;

    Bitmap thumbBitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        mAuth = FirebaseAuth.getInstance();
        String online_user_id = mAuth.getCurrentUser().getUid();
        getUserDatabaseReference = FirebaseDatabase.getInstance().getReference().child("Users").child(online_user_id);
        //lay thong tin  user offline
        getUserDatabaseReference.keepSynced(true);
        storeProfileImagestorageRef  = FirebaseStorage.getInstance().getReference().child("Profile_Images");

        thumbImageRef = FirebaseStorage.getInstance().getReference().child("Thumb_Images");

        settingsDisplayImage = (CircleImageView) findViewById(R.id.settings_profile_image);
        settingsDisplayName = (TextView) findViewById(R.id.settings_username);
        settingsDisplayStatus = (TextView) findViewById(R.id.settings_user_status);
        settingsChangeStatus = (Button) findViewById(R.id.settings_change_profile_status);
        settingsChangeProfileImage = (Button) findViewById(R.id.settings_change_profile_image);
        loadingBar = new ProgressDialog(this);

        getUserDatabaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                //Lay du lieu tu firebase va tra ve man hinh settings
                String name = dataSnapshot.child("user_name").getValue().toString();
                String status = dataSnapshot.child("user_status").getValue().toString();
                final String image = dataSnapshot.child("user_image").getValue().toString();
                String thumb_image = dataSnapshot.child("user_thumb_image").getValue().toString();

                settingsDisplayName.setText(name);
                settingsDisplayStatus.setText(status);
                if(image.equals("default_profile")){
                    //download anh mac dinh tu databse
                    Picasso.with(SettingsActivity.this).load(image).networkPolicy(NetworkPolicy.OFFLINE).placeholder(R.drawable.user)
                            .into(settingsDisplayImage, new Callback() {
                        @Override
                        public void onSuccess() {

                        }

                        @Override
                        public void onError() {
                            Picasso.with(SettingsActivity.this).load(image).placeholder(R.drawable.user).into(settingsDisplayImage);
                        }
                    });
                }
                else{
                    Picasso.with(SettingsActivity.this).load(image).into(settingsDisplayImage);
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        //chinh anh khi an vao "Doi Avatar"
        settingsChangeProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent galleryIntent =  new Intent();
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                //chuyen den folder default de lay anh
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent,Gallery_Pick);
            }
        });


        //Thay doi trang thai Status va upload len server
        settingsChangeStatus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent statusIntent = new Intent(SettingsActivity.this,StatusActivity.class);
                startActivity(statusIntent);
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent i = new Intent(SettingsActivity.this, MainActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(i);
        finish();
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == Gallery_Pick && resultCode ==RESULT_OK && data != null){
            //lay du lieu anh ca nhan tu server
            Uri ImageUri = data.getData();
            CropImage.activity()
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1,1)
                    .start(this);
        }
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {

                loadingBar.setTitle("Upload Avatar to Server");
                loadingBar.setMessage("Chờ xíu, avatar của bạn đang được lưu lên hệ thống!");
                loadingBar.show();


                Uri resultUri = result.getUri();
                File thumb_filePathUri = new File(resultUri.getPath());
                try{
                    //Format anh upload thanh anh nho hon

                    thumbBitmap = new Compressor(this)
                            .setMaxWidth(200)
                            .setMaxHeight(200)
                            .setQuality(50)
                            .compressToBitmap(thumb_filePathUri);
                }catch (IOException e){
                    e.printStackTrace();
                }

                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                thumbBitmap.compress(Bitmap.CompressFormat.JPEG,50,byteArrayOutputStream);
                final byte[] thumb_byte = byteArrayOutputStream.toByteArray();

                String user_id = mAuth.getCurrentUser().getUid();
                //upload avatar len server
                StorageReference filePath = storeProfileImagestorageRef.child(user_id + ".jpg");

                final StorageReference thumb_filePath = thumbImageRef.child(user_id + ".jpg");




                filePath.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        //upload avatar len Firebase Storage
                        if(task.isSuccessful()){
                            Toast.makeText(SettingsActivity.this, "Đang upload avatar của bạn lên server...", Toast.LENGTH_LONG).show();
                            final String downloadUrl = task.getResult().getDownloadUrl().toString();

                            //upload anh dinh dang len server
                            UploadTask uploadTask = thumb_filePath.putBytes(thumb_byte);
                            uploadTask.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> thumb_task) {
                                    String thumb_downloadUrl = thumb_task.getResult().getDownloadUrl().toString();

                                    if(thumb_task.isSuccessful()){
                                        Map update_user_data = new HashMap();
                                        update_user_data.put("user_image", downloadUrl);
                                        update_user_data.put("user_thumb_image", thumb_downloadUrl);

                                        getUserDatabaseReference.updateChildren(update_user_data)
                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        Toast.makeText(SettingsActivity.this, "Update Avatar thành công!", Toast.LENGTH_LONG).show();
                                                        loadingBar.dismiss();
                                                    }
                                                });
                                    }
                                }
                            });


                        }else{
                            Toast.makeText(SettingsActivity.this, "Upload không thành công, vui lòng kiểm tra lại kết nối!", Toast.LENGTH_LONG).show();
                            loadingBar.dismiss();
                        }
                    }
                });
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }

    }
}
