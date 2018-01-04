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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class StatusActivity extends AppCompatActivity {
    private Toolbar mToolbar;
    private Button updateStatusButton;
    private EditText statusInput;
    private ProgressDialog loadingBar;

    private FirebaseAuth mAuth;
    private DatabaseReference changeStatusRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_status);
        mAuth = FirebaseAuth.getInstance();

        String user_id = mAuth.getCurrentUser().getUid();
        changeStatusRef = FirebaseDatabase.getInstance().getReference().child("Users").child(user_id);

        loadingBar = new ProgressDialog(this);
        mToolbar = (Toolbar) findViewById(R.id.status_app_bar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Thay Đổi Status");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        statusInput = (EditText) findViewById(R.id.status_input);
        updateStatusButton = (Button) findViewById(R.id.update_status_button);


        updateStatusButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String new_status = statusInput.getText().toString();

                changeProfileStatus(new_status);
            }
        });
     }

    private void changeProfileStatus(String new_status) {
        if(TextUtils.isEmpty(new_status)) {
            Toast.makeText(StatusActivity.this, "Status không được để trống !", Toast.LENGTH_LONG).show();
        }else{
            loadingBar.setTitle("Updating...");
            loadingBar.setMessage("Đợi xíu...Đang thay đổi trạng thái status của bạn!");
            loadingBar.show();
            changeStatusRef.child("user_status").setValue(new_status).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if(task.isSuccessful()){
                        loadingBar.dismiss();
                        Intent settingsIntent = new Intent(StatusActivity.this,SettingsActivity.class);
                        Toast.makeText(StatusActivity.this, "Update thành công !", Toast.LENGTH_LONG).show();
                        startActivity(settingsIntent);
                    }
                    else{
                        Toast.makeText(StatusActivity.this, "Update không thành công, vui lòng kiểm tra lại kết nối!", Toast.LENGTH_LONG).show();
                    }
                }
            });
        }
    }
}
