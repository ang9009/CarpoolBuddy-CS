package com.example.carpoolbuddy.vehicle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.carpoolbuddy.R;
import com.example.carpoolbuddy.models.Vehicle;
import com.example.carpoolbuddy.models.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

public class VehicleProfileActivity extends AppCompatActivity {
    private TextView vehicleModel;
    private TextView price;
    private TextView riders;
    private TextView owner;
    private TextView capacity;
    private TextView distance;
    private FirebaseAuth mAuth;
    private FirebaseUser currUser;
    private FirebaseFirestore firestore;
    private Vehicle currVehicle;
    private ImageButton actionsButton;
    private User currUserObject;
    private double balance;
    private double rideCost;
    private double lagosBalance;
    private double lagosBalanceGain;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vehicle_profile);

        mAuth = FirebaseAuth.getInstance();
        currUser = mAuth.getCurrentUser();
        firestore = FirebaseFirestore.getInstance();

        vehicleModel = findViewById(R.id.vehicleModel);
        price = findViewById(R.id.price);
        riders = findViewById(R.id.riders);
        owner = findViewById(R.id.owner);
        capacity = findViewById(R.id.capacity);
        actionsButton = findViewById(R.id.actionsButton);
        distance = findViewById(R.id.distance);

        // Getting current vehicle from intent
        Intent intent = getIntent();
        currVehicle = (Vehicle) intent.getSerializableExtra("currVehicle");
        // Getting and setting car information
        String modelString = currVehicle.getModel();
        String maxCapacityString = Integer.toString(currVehicle.getCapacity());
        String riderNumberString = Integer.toString(currVehicle.getRidersNames().size());
        String ownerString = currVehicle.getOwner();
        String capacityString = riderNumberString + "/" + maxCapacityString;
        String ridersString = "";
        rideCost = currVehicle.getRideCost();
        lagosBalanceGain = ((currVehicle.getDistance() / 1000) * 1);

        if(currVehicle.getRidersNames().isEmpty()) {
            ridersString = "none";
        } else {
            ridersString = String.join(", ", currVehicle.getRidersNames());
        }

        owner.setText("Owner: " + ownerString);
        capacity.setText("Capacity: " + capacityString + " people");
        vehicleModel.setText("Model: " + modelString);
        riders.setText("Riders: " + ridersString);
        distance.setText("Distance: " + (currVehicle.getDistance() / 1000) + "km");
        price.setText("Price: " + rideCost);

        getData();
        //Changing button image and method
        setUpButtons();
        //adding back button
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    public void getData() {
        firestore.collection("users").document(currUser.getUid()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()) {
                    DocumentSnapshot ds = task.getResult();

                    currUserObject = ds.toObject(User.class);
                    balance = currUserObject.getBalance();
                    lagosBalance = currUserObject.getLagosBalance();
                } else {
                    Toast.makeText(VehicleProfileActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public void setUpButtons() {
        if(currVehicle.getOwnerID().equals(currUser.getUid())) {
            actionsButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    openCloseCar(view);
                }
            });

            if(currVehicle.isOpen()) {
                actionsButton.setImageResource(R.drawable.ic_baseline_lock_open_24);
            } else {
                actionsButton.setImageResource(R.drawable.ic_baseline_lock_24);
            }
        } else {
            actionsButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    bookLeaveRide(view);
                }
            });

            if(currVehicle.getRidersUIDs().contains(currUser.getUid())) {
                actionsButton.setImageResource(R.drawable.ic_baseline_logout_24);
            } else {
                actionsButton.setImageResource(R.drawable.ic_baseline_group_add_24);
            }
        }
    }

    public void openCloseCar(View v) {
        if(currVehicle.isOpen()) {
            firestore.collection("vehicles").document(currVehicle.getVehicleID()).update("open", false);
            currVehicle.setOpen(false);
            actionsButton.setImageResource(R.drawable.ic_baseline_lock_24);

            Toast.makeText(VehicleProfileActivity.this, "Ride closed successfully.", Toast.LENGTH_SHORT).show();
        } else {
            firestore.collection("vehicles").document(currVehicle.getVehicleID()).update("open", true);
            currVehicle.setOpen(true);
            actionsButton.setImageResource(R.drawable.ic_baseline_lock_open_24);

            Toast.makeText(VehicleProfileActivity.this, "Ride opened successfully.", Toast.LENGTH_SHORT).show();
        }
    }

    public void bookLeaveRide(View v) {
        if(currVehicle.getRidersUIDs().contains(currUser.getUid())) {
            leaveRide();
        } else {
            bookRide();
        }
    }

    public void bookRide() {
        if(currVehicle.getCapacity() == currVehicle.getRidersUIDs().size()) {
            Toast.makeText(VehicleProfileActivity.this, "This ride has already reached maximum capacity.", Toast.LENGTH_SHORT).show();
            return;
        } else if(balance < rideCost) {
            Toast.makeText(VehicleProfileActivity.this, "Insufficient funds!", Toast.LENGTH_SHORT).show();
            return;
        }

        currVehicle.addRiderUID(currUser.getUid());
        currVehicle.addRiderName(currUser.getDisplayName());

        String ridersNames = currVehicle.getRidersNames().toString();
        String riderNumberString = Integer.toString(currVehicle.getRidersNames().size());
        String maxCapacityString = Integer.toString(currVehicle.getCapacity());
        String capacityString = riderNumberString + "/" + maxCapacityString;

        ridersNames = ridersNames.substring(1, ridersNames.length() - 1);

        riders.setText("Riders: " + ridersNames);
        capacity.setText("Capacity: " + capacityString + " people");

        firestore.collection("vehicles").document(currVehicle.getVehicleID()).set(currVehicle);

        //Update user balances
        firestore.collection("users").document(currUser.getUid()).update("balance", FieldValue.increment(-rideCost));
        firestore.collection("users").document(currUser.getUid()).update("lagosBalance", FieldValue.increment(lagosBalanceGain));
        firestore.collection("users").document(currVehicle.getOwnerID()).update("balance", FieldValue.increment(rideCost));

        Toast.makeText(VehicleProfileActivity.this, "You have been added to this ride!", Toast.LENGTH_SHORT).show();
        actionsButton.setImageResource(R.drawable.ic_baseline_logout_24);
    }

    public void leaveRide() {
        currVehicle.removeRiderUID(currUser.getUid());
        currVehicle.removeRiderName(currUser.getDisplayName());

        String ridersNames = currVehicle.getRidersNames().toString();
        String riderNumberString = Integer.toString(currVehicle.getRidersNames().size());
        String maxCapacityString = Integer.toString(currVehicle.getCapacity());
        String capacityString = riderNumberString + "/" + maxCapacityString;

        if(currVehicle.getRidersNames().isEmpty()) {
            ridersNames = "none";
        } else {
            ridersNames = ridersNames.substring(1, ridersNames.length() - 1);
        }

        riders.setText("Riders: " + ridersNames);
        capacity.setText("Capacity: " + capacityString + " people");

        firestore.collection("vehicles").document(currVehicle.getVehicleID()).update("ridersUIDs", FieldValue.arrayRemove(currUser.getUid()));
        firestore.collection("vehicles").document(currVehicle.getVehicleID()).update("ridersNames", FieldValue.arrayRemove(currUser.getDisplayName()));

        //Update user balances
        firestore.collection("users").document(currUser.getUid()).update("balance", FieldValue.increment(rideCost));
        firestore.collection("users").document(currUser.getUid()).update("balance", FieldValue.increment(-lagosBalanceGain));

        firestore.collection("users").document(currVehicle.getOwnerID()).update("balance", FieldValue.increment(-rideCost));

        Toast.makeText(VehicleProfileActivity.this, "You have been removed from this ride!", Toast.LENGTH_SHORT).show();
        actionsButton.setImageResource(R.drawable.ic_baseline_group_add_24);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        Intent myIntent = new Intent(getApplicationContext(), VehiclesInfoActivity.class);
        startActivityForResult(myIntent, 0);
        finish();
        return true;
    }
}