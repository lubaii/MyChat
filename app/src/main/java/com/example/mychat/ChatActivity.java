package com.example.mychat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.List;

public class ChatActivity extends AppCompatActivity {

  private ListView messageListView;
  private AwesomeMessageAdapter adapter;
  private ProgressBar progressBar;
  private ImageButton sendImageButton;
  private Button sendMessageButton;
  private EditText messageEditText;

  private static final int RC_IMAGE_PICKER = 123;
  private String userName;
  private String recipientUserId;
  private String recipientUserName;

  private FirebaseAuth auth;
  private FirebaseDatabase database;
  private DatabaseReference messesDatabaseReference;
  private ChildEventListener messagesChildEventListener;

  private DatabaseReference userDatabaseReference;
  private ChildEventListener userChildEventListener;

  private FirebaseStorage storage; // для
  private StorageReference chatImagesStorageReference;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        auth = FirebaseAuth.getInstance();

        Intent intent = getIntent();// принимаем итент c определенной позицией
        if(intent!= null){
            userName = intent.getStringExtra("userName");
            recipientUserId = intent.getStringExtra("recipientUserId");// получаем значение их id
            recipientUserName = intent.getStringExtra("recipientUserName");
        }
          setTitle("Chat with " + recipientUserName);


        database = FirebaseDatabase.getInstance();
        storage = FirebaseStorage.getInstance();

        messesDatabaseReference = database.getReference().child("messages");
        userDatabaseReference = database.getReference().child("users");
        chatImagesStorageReference = storage.getReference().child("chat_images");

        progressBar = findViewById(R.id.progressBar);
        sendImageButton = findViewById(R.id.sendPhotoButton);
        sendMessageButton = findViewById(R.id.sendMessageButton);
        messageEditText = findViewById(R.id.messageEditText);




        messageListView = findViewById(R.id.messageListView);
        List<AwesomeMessage>  awesomeMessages = new ArrayList<>();
        adapter = new AwesomeMessageAdapter(this,R.layout.message_item,awesomeMessages);
        messageListView.setAdapter(adapter);

        progressBar.setVisibility(View.INVISIBLE);
        messageEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { // если пустая строка, то не отправляется она в чат
              if(s.toString().trim().length() > 0){
                  sendMessageButton.setEnabled(true); // по умолчанию в xml стоит false
              } else {
                  sendMessageButton.setEnabled(false);
              }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
         messageEditText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(500)}); // для написания ограниченного числа текста в EdiTextt

        sendMessageButton.setOnClickListener(v -> {

            AwesomeMessage message = new AwesomeMessage();
            message.setText(messageEditText.getText().toString());
            message.setName(userName);
            message.setSender(auth.getCurrentUser().getUid());
            message.setRecipient(recipientUserId);
            message.setImageUrl(null);
            messesDatabaseReference.push().setValue(message);
            messageEditText.setText(""); //по умолчанию пустая строка
        });
       sendImageButton.setOnClickListener(new View.OnClickListener() {

           @Override
           public void onClick(View v) {
               Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
               intent.setType("image/*");
               intent.putExtra(Intent.EXTRA_LOCAL_ONLY,true);
               startActivityForResult(Intent.createChooser(intent,"Choose an image"),RC_IMAGE_PICKER);
           }
       });
      userChildEventListener = new ChildEventListener() { // сохранение имени пользователя после выхода из приложения
          @Override
          public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
              User user = snapshot.getValue(User.class);
              if(user.getId().equals(FirebaseAuth.getInstance().getCurrentUser().getUid())){ //если будет совпадение id с узлом бд с текущем пользователем

                  userName = user.getName();
              }
          }

          @Override
          public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

          }

          @Override
          public void onChildRemoved(@NonNull DataSnapshot snapshot) {

          }

          @Override
          public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

          }

          @Override
          public void onCancelled(@NonNull DatabaseError error) {

          }
      };
       userDatabaseReference.addChildEventListener(userChildEventListener);

        messagesChildEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot snapshot, String previousChildName) { // информация когда добавляются сообщения
                AwesomeMessage message = snapshot.getValue(AwesomeMessage.class);
                if(message.getSender().equals(auth.getCurrentUser().getUid())&&
                        message.getRecipient().equals(recipientUserId))

                {   message.setMine(true);
                    adapter.add(message);
                } else if(
                        message.getRecipient().equals(auth.getCurrentUser().getUid())&&
                                message.getSender().equals(recipientUserId))
                {
                    message.setMine(false);
                    adapter.add(message);
                }

            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };
        messesDatabaseReference.addChildEventListener(messagesChildEventListener);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) { // функциональность выхода из приложения
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) { // функциональность выхода из приложения
       switch (item.getItemId()){
           case R.id.sign_out:
               FirebaseAuth.getInstance().signOut();
               startActivity(new Intent(ChatActivity.this,SindInActivity.class));
               return true;
           default:
               return super.onOptionsItemSelected(item);
       }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == RC_IMAGE_PICKER && resultCode == RESULT_OK){
            Uri selectedImageUri = data.getData();//
           final StorageReference imageReference = chatImagesStorageReference.child(selectedImageUri.getLastPathSegment());

            UploadTask uploadTask = imageReference.putFile(selectedImageUri);


           // final StorageReference ref = storageRef.child("images/mountains.jpg");
          //  uploadTask = ref.putFile(file);

            Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }
                    // Continue with the task to get the download URL
                    return imageReference.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if (task.isSuccessful()) {
                        Uri downloadUri = task.getResult();
                        AwesomeMessage message = new AwesomeMessage();
                        message.setImageUrl(downloadUri.toString());
                        message.setName(userName);
                        message.setSender(auth.getCurrentUser().getUid());
                        message.setRecipient(recipientUserId);
                        messesDatabaseReference.push().setValue(message);
                        //chatImagesStorageReference.push().setValue(message);
                    } else {
                        // Handle failures
                        // ...
                    }
                }
            });
        }


    }
}