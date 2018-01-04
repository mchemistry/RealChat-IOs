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
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

public class LoginActivity extends AppCompatActivity {
    private Toolbar mToolbar;
    private Button LoginButton;
    private EditText LoginEmail;
    private EditText LoginPassword;
    private ProgressDialog loadingBar;

    private FirebaseAuth mAuth;
    private DatabaseReference usersReferentce;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        mToolbar = (Toolbar) findViewById(R.id.login_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Đăng Nhập");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mAuth = FirebaseAuth.getInstance();
        usersReferentce = FirebaseDatabase.getInstance().getReference().child("Users");

        //lay du lieu tu o nhap
        LoginEmail = (EditText) findViewById(R.id.login_email);
        LoginPassword = (EditText) findViewById(R.id.login_password);
        LoginButton = (Button) findViewById(R.id.login_button);
        loadingBar = new ProgressDialog(this);

        //xu ly su kien dang nhap
        LoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = LoginEmail.getText().toString();
                String password = LoginPassword.getText().toString();
                LoginUserAccount(email, password);

            }
        });
    }
        private void LoginUserAccount(String email, String password){
            //kiem tra email va pass word
            if(TextUtils.isEmpty(email)){
                Toast.makeText(LoginActivity.this, "Email không được bỏ trống!", Toast.LENGTH_SHORT).show();
            } else if(TextUtils.isEmpty(password)){
                Toast.makeText(LoginActivity.this, "Password không được bỏ trống!", Toast.LENGTH_SHORT).show();
            }else{
                loadingBar.setTitle("Đăng Nhập....");
                loadingBar.setMessage("Đang đăng nhập vào REAL-CHAT, bạn vui lòng chờ !");
                loadingBar.show();
                mAuth.signInWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()) {

                            String online_user_id = mAuth.getCurrentUser().getUid();
                            String deviceToken = FirebaseInstanceId.getInstance().getToken();

                            usersReferentce.child(online_user_id).child("device_token").setValue(deviceToken)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            Intent mainIntent = new Intent(LoginActivity.this,MainActivity.class);
                                            mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                            startActivity(mainIntent);
                                            finish();
                                        }
                                    });



                        }else{
                            Toast.makeText(LoginActivity.this, "Thông tin email hoặc mật khẩu không đúng!", Toast.LENGTH_SHORT).show();
                        }
                        loadingBar.dismiss();
                    }
                });
            }
        }

}

