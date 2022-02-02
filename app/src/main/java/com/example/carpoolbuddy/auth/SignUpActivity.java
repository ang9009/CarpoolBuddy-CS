package com.example.carpoolbuddy.auth;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.carpoolbuddy.MainActivity;
import com.example.carpoolbuddy.R;
import com.example.carpoolbuddy.user.Alumni;
import com.example.carpoolbuddy.user.Parent;
import com.example.carpoolbuddy.user.Student;
import com.example.carpoolbuddy.user.Teacher;
import com.example.carpoolbuddy.user.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.FirebaseFirestore;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import java.util.Locale;

public class SignUpActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {
    private EditText emailField;
    private EditText passwordField;
    private EditText nameField;
    private EditText infoField;
    private Spinner roleSpinner;
    private FirebaseAuth mAuth;
    private FirebaseFirestore firestore;
    private FirebaseUser user;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        emailField = findViewById(R.id.editTextEmail);
        passwordField = findViewById(R.id.editTextPassword);
        roleSpinner = findViewById(R.id.roleSpinner);
        nameField = findViewById(R.id.nameField);
        infoField = findViewById(R.id.infoField);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.roles, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        roleSpinner.setAdapter(adapter);
        roleSpinner.setOnItemSelectedListener(this);

        mAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        user = mAuth.getCurrentUser();

        roleSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                String role = roleSpinner.getItemAtPosition(position).toString();

                if(role.equalsIgnoreCase("teacher")) {
                    infoField.setVisibility(View.VISIBLE);
                    infoField.setHint("In-school title");
                } else if(role.equalsIgnoreCase("parent")) {
                    infoField.setVisibility(View.GONE);
                } else {
                    infoField.setVisibility(View.VISIBLE);
                    infoField.setHint("Graduation year");
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // your code here
            }

        });

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    public void signUp(View v) {
        String email = emailField.getText().toString();
        String password = passwordField.getText().toString();
        String name = nameField.getText().toString();
        String role = roleSpinner.getSelectedItem().toString();
        String info = infoField.getText().toString();

        if (email.isEmpty() || password.isEmpty() || name.isEmpty() || info.isEmpty()) {
            Toast.makeText(SignUpActivity.this, "One or more fields are empty.", Toast.LENGTH_SHORT).show();
            return;
        } else if (!email.toLowerCase().endsWith("cis.edu.hk")) {
            Toast.makeText(SignUpActivity.this, "You do not belong to CIS!", Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(SignUpActivity.this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> createUserTask) {
                if (createUserTask.isSuccessful()) {
                    User newUser = null;
                    String id = user.getUid();

                    if(role.equalsIgnoreCase("teacher")) {
                        newUser = new Teacher(name, email, role, id, info);
                    } else if (role.equalsIgnoreCase("student")) {
                        newUser = new Student(name, email, role, id, info);
                    } else if (role.equalsIgnoreCase("alumni")) {
                        newUser = new Alumni(name, email, role, id, info);
                    } else if (role.equalsIgnoreCase("parent")) {
                        newUser = new Parent(name, email, role, id);
                    }

                    firestore.collection("users").document(id).set(newUser);

                    //Change email
                    UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                            .setDisplayName(name)
                            .build();

                    user.updateProfile(profileUpdates)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        Intent intent = new Intent(SignUpActivity.this, MainActivity.class);
                                        startActivity(intent);
                                    }
                                }
                            });
                } else {
                    Log.d("SIGN UP", "An error occurred", createUserTask.getException());
                    Toast.makeText(SignUpActivity.this, createUserTask.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }
}