package com.example.securephotovault;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class LockScreenActivity extends AppCompatActivity {
    private static final int PIN_LENGTH = 4;
    private static final String PIN_CODE_KEY = "pin_code";

    private Button deleteButton;
    private StringBuilder enteredPin;
    private View circle1, circle2, circle3, circle4;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lock_screen);

        enteredPin = new StringBuilder();

        circle1 = findViewById(R.id.circle1);
        circle2 = findViewById(R.id.circle2);
        circle3 = findViewById(R.id.circle3);
        circle4 = findViewById(R.id.circle4);

        // Get the SharedPreferences instance
        sharedPreferences = getSharedPreferences("AppPreferences", MODE_PRIVATE);

        // Check if a PIN code has been set
        if (!sharedPreferences.contains(PIN_CODE_KEY)) {
            // PIN code has not been set, allow the user to set a new PIN
            setNumberButtonClickListener(R.id.button0, "0");
            setNumberButtonClickListener(R.id.button1, "1");
            setNumberButtonClickListener(R.id.button2, "2");
            setNumberButtonClickListener(R.id.button3, "3");
            setNumberButtonClickListener(R.id.button4, "4");
            setNumberButtonClickListener(R.id.button5, "5");
            setNumberButtonClickListener(R.id.button6, "6");
            setNumberButtonClickListener(R.id.button7, "7");
            setNumberButtonClickListener(R.id.button8, "8");
            setNumberButtonClickListener(R.id.button9, "9");

            Button submitButton = findViewById(R.id.submitButton);
            submitButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    savePinCode();
                }
            });
        } else {
            // PIN code has been set, allow access
            setNumberButtonClickListener(R.id.button0, "0");
            setNumberButtonClickListener(R.id.button1, "1");
            setNumberButtonClickListener(R.id.button2, "2");
            setNumberButtonClickListener(R.id.button3, "3");
            setNumberButtonClickListener(R.id.button4, "4");
            setNumberButtonClickListener(R.id.button5, "5");
            setNumberButtonClickListener(R.id.button6, "6");
            setNumberButtonClickListener(R.id.button7, "7");
            setNumberButtonClickListener(R.id.button8, "8");
            setNumberButtonClickListener(R.id.button9, "9");

            Button submitButton = findViewById(R.id.submitButton);
            submitButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    checkPinCode();
                }
            });
        }

        deleteButton = findViewById(R.id.deleteButton);

        // click listener for the delete button
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteLastNumber();
            }
        });
    }

    private void setNumberButtonClickListener(int buttonId, final String number) {
        Button button = findViewById(buttonId);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Append the number to the enteredPin StringBuilder
                if (enteredPin.length() < PIN_LENGTH) {
                    enteredPin.append(number);
                    updateCircleViews();
                }
            }
        });
    }

    private void updateCircleViews() {
        // Update the circle views based on the enteredPin length
        int enteredLength = enteredPin.length();
        updateCircleView(circle1, enteredLength >= 1);
        updateCircleView(circle2, enteredLength >= 2);
        updateCircleView(circle3, enteredLength >= 3);
        updateCircleView(circle4, enteredLength == 4);
    }

    private void updateCircleView(View circleView, boolean filled) {
        // Update the background of the circle view
        circleView.setBackgroundResource(filled ? R.drawable.circle_black : R.drawable.circle_white);
    }

    private void deleteLastNumber() {
        int length = enteredPin.length();
        if (length > 0) {
            // Remove the last character from the enteredPin StringBuilder
            enteredPin.deleteCharAt(length - 1);
            updateCircleViews();
        }
    }

    private void savePinCode() {
        String pin = enteredPin.toString();
        if (pin.length() == PIN_LENGTH) {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString(PIN_CODE_KEY, pin);
            editor.apply();
            Toast.makeText(this, "PIN code saved", Toast.LENGTH_SHORT).show();
            //finish();
            Intent intent = new Intent(LockScreenActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        } else {
            Toast.makeText(this, "Invalid PIN code length", Toast.LENGTH_SHORT).show();
        }
    }

    private void checkPinCode() {
        String storedPin = sharedPreferences.getString(PIN_CODE_KEY, "");
        String enteredPin = this.enteredPin.toString();

        if (storedPin.equals(enteredPin)) {
            Toast.makeText(this, "PIN code correct", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(LockScreenActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        } else {
            Toast.makeText(this, "PIN code incorrect", Toast.LENGTH_SHORT).show();
            clearEnteredPin();
        }
    }

    private void clearEnteredPin() {
        enteredPin.setLength(0);
        updateCircleViews();
    }
}
