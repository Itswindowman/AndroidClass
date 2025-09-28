package com.example.recyclerview;

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


    private static final int PICK_IMAGE_REQUEST = 1;
    private static final int CAMERA_REQUEST = 2;
    private Uri cameraImageUri;
    private ImageView selectedImageView;

    int selectedImageRedId;
    private final String[] userImages = {"p1","p2","p3","p4"};
    private final int[] userImagesId = {R.drawable.p1,R.drawable.p2,R.drawable.p3,R.drawable.p4};
    private RecyclerView recyclerView = null;
    private ArrayList<User> userList = null;

    private UserAdaptar userAdapter = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        setRecyclerView();
        setUpFloatingActionButton();
    }

    private void setRecyclerView()
    {
        recyclerView = findViewById(R.id.rc);
        userList = new ArrayList<>();

        userList.add(new User("1","Moshe","Intiligent",R.drawable.p1));
        userList.add(new User("2","Bearli","Haham",R.drawable.p2));
        userList.add(new User("3","Beari","DIBIL",R.drawable.p3));
        userList.add(new User("4","Shalom","Tipesh",R.drawable.p4));

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        userAdapter = new UserAdaptar(userList, this);
        recyclerView.setAdapter(userAdapter);

        recyclerView.setLayoutManager(new androidx.recyclerview.widget.LinearLayoutManager(this));

        ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {

            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                User user = userList.get(position);

                if (direction == ItemTouchHelper.LEFT) {
                    showUndoSnackBar(position, user);
                    userList.remove(position);
                    userAdapter.notifyDataSetChanged();
                }
                else if (direction == ItemTouchHelper.RIGHT) {
                    userAdapter.notifyItemChanged(position);

                    View DialogEdit = getLayoutInflater().inflate(R.layout.dialog_edit_user, null);
                    EditText EditNameInput = DialogEdit.findViewById(R.id.nameInput);
                    EditText EditPasswordInput = DialogEdit.findViewById(R.id.passwordInput);
                    ImageView EditImageView = DialogEdit.findViewById(R.id.imageView);


                    // Pre-fill user data
                    EditNameInput.setText(user.getName());
                    EditPasswordInput.setText(user.getPassword());
                    EditImageView.setImageResource(user.getPic());
                    EditImageView.setTag(user.getPic());

                    // ✅ Initialize spinner BEFORE showing the dialog
                    handleImageSelection(DialogEdit);


                    AlertDialog.Builder build = new AlertDialog.Builder(MainActivity.this);
                    build.setTitle("Edit User");
                    build.setView(DialogEdit);

                    build.setPositiveButton("Save", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            String name = EditNameInput.getText().toString();
                            String password = EditPasswordInput.getText().toString();
                            Object tag = EditImageView.getTag();
                            int pic = (tag instanceof Integer) ? (Integer) tag : userList.get(position).getPic();


                            if (!name.isEmpty() && !password.isEmpty()) {
                                User updatedUser = new User(userList.get(position).getId(), name, password, pic);
                                userList.set(position, updatedUser);
                                userAdapter.notifyItemChanged(position);
                                Toast.makeText(MainActivity.this, "User Updated: " + name, Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(MainActivity.this, "Please Fill All Fields", Toast.LENGTH_SHORT).show();
                            }


                        }


                    });

                    build.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
                    build.show();
                    build.setCancelable(true);
                }
            }







            private void showUndoSnackBar(int position, User user) {
                Snackbar.make(recyclerView, "Item Removed", Snackbar.LENGTH_LONG)
                        .setAction("UNDO", v -> {
                            userList.add(position, user);
                            userAdapter.notifyDataSetChanged();
                        }).show();
            }
        };



        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleCallback);
        itemTouchHelper.attachToRecyclerView(recyclerView);
    };

    public void setUpFloatingActionButton(){
        FloatingActionButton fab = findViewById(R.id.floatingActionButton2);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_edit, null);
                EditText nameInput = dialogView.findViewById(R.id.nameInput);
                EditText passwordInput = dialogView.findViewById(R.id.passwordInput);
                ImageView imageView = dialogView.findViewById(R.id.imageView);
                Button Gallerybtn = dialogView.findViewById(R.id.gallery);
                Button Camera = dialogView.findViewById(R.id.Camera);
                imageView.setImageResource(R.drawable.p1);
                imageView.setTag(R.drawable.p1);
                handleImageSelection(dialogView);


                Camera.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                        File photoFile = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), "temp.jpg");
                        cameraImageUri = FileProvider.getUriForFile(MainActivity.this, getPackageName() + ".provider", photoFile);
                        intent.putExtra(MediaStore.EXTRA_OUTPUT, cameraImageUri);
                        selectedImageView = imageView;
                        startActivityForResult(intent, CAMERA_REQUEST);
                    }
                });


                Gallerybtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent();
                        intent.setType("image/*");
                        intent.setAction(Intent.ACTION_GET_CONTENT);
                        selectedImageView = imageView; // Save reference for later
                        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
                    }
                });





                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("Add New User");
                builder.setView(dialogView);
                builder.setPositiveButton("Add", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {


                        String name = nameInput.getText().toString();
                        String password = passwordInput.getText().toString();

                        int pic = (int) imageView.getTag();
                        if (!name.isEmpty() && !password.isEmpty()) {
                            User newUser = new User(String.valueOf(userList.size() + 1), name, password, pic);
                            userList.add(newUser);
                            userAdapter.notifyItemInserted(userList.size() - 1);
                            recyclerView.scrollToPosition(userList.size() - 1);
                            Toast.makeText(MainActivity.this, "User Added" + name, Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(MainActivity.this, "Please Fill All Fields", Toast.LENGTH_SHORT).show();

                        }

                    }
                });

                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                builder.show();
                builder.setCancelable(true);

            }


        });




    }
    public void handleImageSelection(View dialogView) {
        Spinner spinner = dialogView.findViewById(R.id.BackTo2016);

        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, userImages);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(spinnerAdapter);

        ImageView imageViewUser = dialogView.findViewById(R.id.imageView);
        imageViewUser.setImageResource(userImagesId[0]);
        selectedImageRedId = userImagesId[0];
        imageViewUser.setTag(userImagesId[0]); // Make sure tag is updated

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                imageViewUser.setImageResource(userImagesId[position]);
                selectedImageRedId = userImagesId[position];
                imageViewUser.setTag(userImagesId[position]); // Update tag for retrieval later
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Optional: handle if needed
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            if (requestCode == PICK_IMAGE_REQUEST && data != null && data.getData() != null) {
                Uri imageUri = data.getData();
                if (selectedImageView != null) {
                    selectedImageView.setImageURI(imageUri);
                    selectedImageView.setTag(imageUri); // Optional
                }
            } else if (requestCode == CAMERA_REQUEST) {
                if (selectedImageView != null && cameraImageUri != null) {
                    selectedImageView.setImageURI(cameraImageUri);
                    selectedImageView.setTag(cameraImageUri); // Optional
                }
            }
        }
    }
}

