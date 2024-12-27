package com.example.firebaseapp;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.ArrayList;

public class EnrollmentActivity extends AppCompatActivity {
    private ArrayList<String> selectedSubjects;
    private ArrayList<Subject> subjects; // Daftar mata kuliah
    private int totalCredits;
    private final int MAX_CREDITS = 24;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_enrollment);

        // Check if the user is logged in
        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() == null) {
            Toast.makeText(this, "Please log in first", Toast.LENGTH_SHORT).show();
            finish(); // Tutup aktivitas jika belum login
            return;
        }

        selectedSubjects = new ArrayList<>();
        totalCredits = 0;

        // Initialize the course list
        subjects = new ArrayList<>();
        subjects.add(new Subject("Discrete Math", 3));
        subjects.add(new Subject("Java Programming", 4));
        subjects.add(new Subject("Data Structure", 3));
        subjects.add(new Subject("Database", 3));
        subjects.add(new Subject("Mobile Programming", 5));
        subjects.add(new Subject("Data Structure dan Algorithm", 6));
        subjects.add(new Subject("English Language", 0));
        subjects.add(new Subject("C++ Programming", 3));
        subjects.add(new Subject("3D Graphics", 4));

        // Find RecyclerView and set its layout
        RecyclerView subjectList = findViewById(R.id.subjectList);
        subjectList.setLayoutManager(new LinearLayoutManager(this));

        // Initialize the adapter and connect it to the RecyclerView
        SubjectAdapter adapter = new SubjectAdapter(subjects, (subject, isSelected) -> {
            if (isSelected) {
                selectedSubjects.add(subject.name);
                totalCredits += subject.credits;
            } else {
                selectedSubjects.remove(subject.name);
                totalCredits -= subject.credits;
            }

            // Update summary text
            TextView summaryText = findViewById(R.id.summary);
            summaryText.setText("Selected Credits: " + totalCredits);
        });
        subjectList.setAdapter(adapter);

        // Other components, such as the submit button
        TextView summaryText = findViewById(R.id.summary);
        Button submitButton = findViewById(R.id.submitButton);

        submitButton.setOnClickListener(view -> {
            if (totalCredits > MAX_CREDITS) {
                Toast.makeText(this, "Total credits cannot exceed " + MAX_CREDITS, Toast.LENGTH_SHORT).show();
            } else {
                // Get User ID from Firebase Authentication
                String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

                // Create an Enrollment object to store in Firestore
                Enrollment enrollment = new Enrollment(selectedSubjects, totalCredits);

                // Save data to Firestore in collection "enrollments"
                FirebaseFirestore firestore = FirebaseFirestore.getInstance();
                firestore.collection("enrollments").document(userId).set(enrollment)
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(this, "Enrollment Successful!", Toast.LENGTH_SHORT).show();
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(this, "Enrollment Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        });
            }
        });
    }

    public static class Enrollment {
        public ArrayList<String> subjects;
        public int totalCredits;

        // Empty constructor is required by Firestore
        public Enrollment() {
        }

        public Enrollment(ArrayList<String> subjects, int totalCredits) {
            this.subjects = subjects;
            this.totalCredits = totalCredits;
        }
    }

    // Class Subject
    public static class Subject {
        public String name;
        public int credits;

        public Subject(String name, int credits) {
            this.name = name;
            this.credits = credits;
        }
    }
}
