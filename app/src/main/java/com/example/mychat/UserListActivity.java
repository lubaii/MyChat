package com.example.mychat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;

import java.util.ArrayList;

public class UserListActivity extends AppCompatActivity {

    private DatabaseReference usersDatabaseReference;
    private ChildEventListener usersChildEventListener;
    private FirebaseAuth auth;

    private String userName; //



    private ArrayList<User> userArrayList;
    private RecyclerView userRecyclerView;
    private UserAdapter userAdapter;
    private RecyclerView.LayoutManager userlayoutManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_list);


        Intent intent = getIntent();
        if(intent != null) {
            userName = intent.getStringExtra("userName"); // инициализируем из предыдущей активити и отправляем в  ChatActiviti
        }
        auth = FirebaseAuth.getInstance();
        userArrayList = new ArrayList<>();

        attachUserDatabaseReferenceListener();
        userbuildRecyclerView();
    }

    private void attachUserDatabaseReferenceListener() {
        usersDatabaseReference = FirebaseDatabase.getInstance().getReference().child("users");
        if(usersChildEventListener == null){
            usersChildEventListener = new ChildEventListener() {
                @Override
                public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                    User user = snapshot.getValue(User.class);
                    if(!user.getId().equals(auth.getUid())){
                        user.setAvatarMockUpResource(R.drawable.ic_baseline_person_24);
                        userArrayList.add(user);
                        userAdapter.notifyDataSetChanged();
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

            }; usersDatabaseReference.addChildEventListener(usersChildEventListener);
        }
    }

    private void userbuildRecyclerView() {
        userRecyclerView = findViewById(R.id.userListRecyclerView);
        userRecyclerView.setHasFixedSize(true);
        userRecyclerView.addItemDecoration(new DividerItemDecoration(
                userRecyclerView.getContext(), DividerItemDecoration.VERTICAL));// поля отделяющие контакты
        userlayoutManager = new LinearLayoutManager(this);
        userAdapter = new UserAdapter(userArrayList);

        userRecyclerView.setLayoutManager(userlayoutManager);
        userRecyclerView.setAdapter(userAdapter);

        userAdapter.setOnClickListener(new UserAdapter.OnUserClickListener() { // для перехода в другую активити
            @Override
            public void onUserClick(int position) { // при кликанье на позицию извлекается из той же позиции извлекается объект и кладется в итент
                goToChat(position);
            }
        });
    }

    private void goToChat(int position) { // для перехода общение в чате
        Intent intent = new Intent(UserListActivity.this,ChatActivity.class);
        intent.putExtra("recipientUserId",userArrayList.get(position).getId()); // получатель
        intent.putExtra("recipientUserName", userArrayList.get(position).getName());
        intent.putExtra("userName", userName); // отправляем переменную, проиницализированную из предыдущей активити в ChatActivity
        startActivity(intent);

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
                startActivity(new Intent(UserListActivity.this,SindInActivity.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }
}