package com.example.recyclerview;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import java.io.File;
import java.util.ArrayList;


public class MainActivity extends AppCompatActivity {

    private ActivityResultLauncher<Uri> cameraLauncher;
    private ActivityResultLauncher<Intent> galleryLauncher;
    private Uri photoUri;

    private ImageView selectedImageView;
    private int selectedImageResId;

    private final String[] userImages = {"p1", "p2", "p3", "p4"};
    private final int[] userImagesId = {
            R.drawable.p1,
            R.drawable.p2,
            R.drawable.p3,
            R.drawable.p4
    };

    private RecyclerView recyclerView;
    private ArrayList<User> userList;
    private UserAdaptar userAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        setRecyclerView();
        setUpFloatingActionButton();
        setupGalleryLauncher();
        setupCameraLauncher();
    }

    private void setupGalleryLauncher() {
        galleryLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        Uri imageUri = result.getData().getData();
                        if (selectedImageView != null) {
                            selectedImageView.setImageURI(imageUri);
                            selectedImageView.setTag(imageUri);
                        }
                    }
                }
        );
    }

    private void setupCameraLauncher() {
        cameraLauncher = registerForActivityResult(
                new ActivityResultContracts.TakePicture(),
                result -> {
                    if (result && selectedImageView != null) {
                        selectedImageView.setImageURI(photoUri);
                        selectedImageView.setTag(photoUri);
                    }
                }
        );
    }

    private void setRecyclerView() {
        recyclerView = findViewById(R.id.rc);
        userList = new ArrayList<>();

        userList.add(new User("1", "Moshe", "Intelligent", R.drawable.p1));
        userList.add(new User("2", "Bearli", "Haham", R.drawable.p2));
        userList.add(new User("3", "Beari", "DIBIL", R.drawable.p3));
        userList.add(new User("4", "Shalom", "Tipesh", R.drawable.p4));

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        userAdapter = new UserAdaptar(userList, this);
        recyclerView.setAdapter(userAdapter);

        ItemTouchHelper.SimpleCallback simpleCallback =
                new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
                    @Override
                    public boolean onMove(@NonNull RecyclerView recyclerView,
                                          @NonNull RecyclerView.ViewHolder viewHolder,
                                          @NonNull RecyclerView.ViewHolder target) {
                        return false;
                    }

                    @Override
                    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                        int position = viewHolder.getAdapterPosition();
                        User user = userList.get(position);

                        if (direction == ItemTouchHelper.LEFT) {
                            showUndoSnackBar(position, user);
                            userList.remove(position);
                            userAdapter.notifyItemRemoved(position);
                        } else if (direction == ItemTouchHelper.RIGHT) {
                            userAdapter.notifyItemChanged(position); // reset swipe
                            openEditUserDialog(position); // call the new method
                        }
                    }

                    private void showUndoSnackBar(int position, User user) {
                        Snackbar.make(recyclerView, "Item Removed", Snackbar.LENGTH_LONG)
                                .setAction("UNDO", v -> {
                                    userList.add(position, user);
                                    userAdapter.notifyItemInserted(position);
                                }).show();
                    }
                };

        new ItemTouchHelper(simpleCallback).attachToRecyclerView(recyclerView);
    }

    public void setUpFloatingActionButton() {
        FloatingActionButton fab = findViewById(R.id.floatingActionButton2);

        fab.setOnClickListener(v -> {
            View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_edit, null);
            EditText nameInput = dialogView.findViewById(R.id.nameInput);
            EditText passwordInput = dialogView.findViewById(R.id.passwordInput);
            ImageView imageView = dialogView.findViewById(R.id.imageView);
            Button galleryBtn = dialogView.findViewById(R.id.gallery);
            Button cameraBtn = dialogView.findViewById(R.id.Camera);

            imageView.setImageResource(R.drawable.p1);
            imageView.setTag(R.drawable.p1);
            handleImageSelection(dialogView, R.drawable.p1);
            selectedImageView = imageView;

            cameraBtn.setOnClickListener(v1 -> {
                File photoFile = new File(getFilesDir(), "photo.jpg");
                photoUri = FileProvider.getUriForFile(
                        MainActivity.this,
                        getApplicationContext().getPackageName() + ".provider",
                        photoFile
                );
                selectedImageView = imageView;
                cameraLauncher.launch(photoUri);
            });

            galleryBtn.setOnClickListener(v12 -> {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                selectedImageView = imageView;
                galleryLauncher.launch(Intent.createChooser(intent, "Select Picture"));
            });

            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setTitle("Add New User");
            builder.setView(dialogView);

            builder.setPositiveButton("Add", (dialog, which) -> {
                String name = nameInput.getText().toString();
                String password = passwordInput.getText().toString();
                Object picTag = imageView.getTag();

                if (!name.isEmpty() && !password.isEmpty()) {
                    User newUser = new User(
                            String.valueOf(userList.size() + 1),
                            name,
                            password,
                            picTag
                    );
                    userList.add(newUser);
                    userAdapter.notifyItemInserted(userList.size() - 1);
                    recyclerView.scrollToPosition(userList.size() - 1);
                    Toast.makeText(MainActivity.this,
                            "User Added: " + name, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MainActivity.this,
                            "Please Fill All Fields", Toast.LENGTH_SHORT).show();
                }
            });

            builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
            builder.setCancelable(true);
            builder.show();
        });
    }

    public void handleImageSelection(View dialogView, Object defaultPic) {
        Spinner spinner = dialogView.findViewById(R.id.BackTo2016);

        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                userImages
        );
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(spinnerAdapter);

        ImageView imageViewUser = dialogView.findViewById(R.id.imageView);

        // If the user already has a picture, show it
        if (defaultPic instanceof Integer) {
            imageViewUser.setImageResource((Integer) defaultPic);
            imageViewUser.setTag(defaultPic);

            // also move the spinner to the right position
            for (int i = 0; i < userImagesId.length; i++) {
                if (userImagesId[i] == (Integer) defaultPic) {
                    spinner.setSelection(i);
                    break;
                }
            }

        } else if (defaultPic instanceof Uri) {
            imageViewUser.setImageURI((Uri) defaultPic);
            imageViewUser.setTag(defaultPic);
            spinner.setSelection(0); // fallback
        } else {
            // fallback to first image
            imageViewUser.setImageResource(userImagesId[0]);
            imageViewUser.setTag(userImagesId[0]);
            spinner.setSelection(0);
        }

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // Only set drawable if it’s one of the defaults
                if (defaultPic instanceof Integer) {
                    imageViewUser.setImageResource(userImagesId[position]);
                    imageViewUser.setTag(userImagesId[position]);
                }
                // If defaultPic was Uri, do nothing here
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void openEditUserDialog(int position) {
        User user = userList.get(position);

        View dialogEdit = getLayoutInflater().inflate(R.layout.dialog_edit_user, null);
        EditText editNameInput = dialogEdit.findViewById(R.id.nameInput);
        EditText editPasswordInput = dialogEdit.findViewById(R.id.passwordInput);
        Button galleryBtn = dialogEdit.findViewById(R.id.gallery2);
        Button cameraBtn = dialogEdit.findViewById(R.id.camera);
        ImageView editImageView = dialogEdit.findViewById(R.id.imageView);

        selectedImageView = editImageView;
        handleImageSelection(dialogEdit, user.getPic());

        // Pre-fill user data
        editNameInput.setText(user.getName());
        editPasswordInput.setText(user.getPassword());

        // Gallery button
        galleryBtn.setOnClickListener(v -> {
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            selectedImageView = editImageView;
            galleryLauncher.launch(Intent.createChooser(intent, "Select Picture"));
        });

        // Camera button
        cameraBtn.setOnClickListener(v -> {
            File photoFile = new File(getFilesDir(), "photo.jpg");
            photoUri = FileProvider.getUriForFile(
                    MainActivity.this,
                    getApplicationContext().getPackageName() + ".provider",
                    photoFile
            );
            selectedImageView = editImageView;
            cameraLauncher.launch(photoUri);
        });

        AlertDialog.Builder build = new AlertDialog.Builder(MainActivity.this);
        build.setTitle("Edit User");
        build.setView(dialogEdit);

        build.setPositiveButton("Save", (dialog, which) -> {
            String name = editNameInput.getText().toString();
            String password = editPasswordInput.getText().toString();
            Object tag = editImageView.getTag();
            Object updatedPic = (tag != null) ? tag : userList.get(position).getPic();

            if (!name.isEmpty() && !password.isEmpty()) {
                User updatedUser = new User(
                        userList.get(position).getId(),
                        name,
                        password,
                        updatedPic
                );
                userList.set(position, updatedUser);
                userAdapter.notifyItemChanged(position);
                Toast.makeText(MainActivity.this, "User Updated: " + name, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(MainActivity.this, "Please Fill All Fields", Toast.LENGTH_SHORT).show();
            }
        });

        build.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
        build.setCancelable(true);
        build.show();
    }

}

