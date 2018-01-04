package com.herokuapp.chatio_me.realchat_ios;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

public class RegisterActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private DatabaseReference storeUserDefaultDataReference;

    private Toolbar mToolbar;
    private ProgressDialog loadingBar;

    private EditText RegisterUserName;
    private EditText RegisterEmail;
    private EditText RegisterPassword;
    private Button CreateAccountButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        mToolbar = (Toolbar) findViewById(R.id.register_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Đăng Ký");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        loadingBar = new ProgressDialog(this);
        mAuth = FirebaseAuth.getInstance();

        //Lay du lieu tu form
        RegisterUserName = (EditText) findViewById(R.id.register_name);
        RegisterEmail = (EditText) findViewById(R.id.register_email);
        RegisterPassword = (EditText) findViewById(R.id.register_password);
        CreateAccountButton = (Button) findViewById(R.id.create_account_button);
        CreateAccountButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String name = RegisterUserName.getText().toString();
                String email = RegisterEmail.getText().toString();
                String password  = RegisterPassword.getText().toString();

                //chuyen du lieu len servereceiv
                RegisterAccount(name, email, password);
            }
        });
    }

    private void RegisterAccount(final String name, String email, String password) {
        //kiem tra form
        if(TextUtils.isEmpty(name)){
            Toast.makeText(RegisterActivity.this, "Nhập vào tên hiển thị !", Toast.LENGTH_SHORT).show();
        }else  if(TextUtils.isEmpty(email)){
            Toast.makeText(RegisterActivity.this, "Email không được để trống !", Toast.LENGTH_SHORT).show();
        }
        else  if(TextUtils.isEmpty(password)){
            Toast.makeText(RegisterActivity.this, "Password không được để trống !", Toast.LENGTH_SHORT).show();
        }else{
            //
            loadingBar.setTitle("Đang tạo tài khoản!");
            loadingBar.setMessage("Vui lòng chờ trong giây lát!");
            loadingBar.show();
            mAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if(task.isSuccessful()){
                        String deviceToken = FirebaseInstanceId.getInstance().getToken();


                        String current_user_Id = mAuth.getCurrentUser().getUid();

                        //config child branch datsbase
                        storeUserDefaultDataReference = FirebaseDatabase.getInstance().getReference().child("Users").child(current_user_Id);
                        storeUserDefaultDataReference.child("user_name").setValue(name);
                        storeUserDefaultDataReference.child("device_token").setValue(deviceToken);
                        storeUserDefaultDataReference.child("user_status").setValue("Heyyy, Mình đang sử dụng REAL-CHAT, kết bạn với mình để trò chuyện nha!");
                        storeUserDefaultDataReference.child("user_image").setValue("dafault_profile");
                        storeUserDefaultDataReference.child("user_thumb_image").setValue("default_image").addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if(task.isSuccessful()){

                                    Intent mainIntent = new Intent(RegisterActivity.this, MainActivity.class);
                                    mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                    Toast.makeText(RegisterActivity.this, "Đăng ký thành công, bạn đã trở thành thành viên của REAL-CHAT", Toast.LENGTH_LONG).show();
                                    startActivity(mainIntent);
                                    finish();
                                }
                            }
                        });


                    }else{
                        // loi
                        Toast.makeText(RegisterActivity.this, "Email đã được sử dụng, vui lònng chọn email khác !", Toast.LENGTH_SHORT).show();
                    }
                    //update loadingbar
                    loadingBar.dismiss();
                }
            });
        }
    }
}
