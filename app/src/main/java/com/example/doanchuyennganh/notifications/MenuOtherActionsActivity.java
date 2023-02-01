package com.example.doanchuyennganh.notifications;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;

import com.example.doanchuyennganh.ChatListFragment;
import com.example.doanchuyennganh.DashboardActivity;
import com.example.doanchuyennganh.MainActivity;
import com.example.doanchuyennganh.R;
import com.example.doanchuyennganh.fragment.ProfileFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

public class MenuOtherActionsActivity extends AppCompatActivity {
//    FirebaseAuth firebaseAuth;
//    ActionBar actionBar;
//
//    String mUID;
//    private BottomNavigationView navigationView;
//    private NavigationView navigationView2;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
//        navigationView = findViewById(R.id.navigation_view1);
//        navigationView.setOnNavigationItemSelectedListener(selectedListener1);

        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_menu_other_actions);
//
//        checkUserStatus();
    }
//
//    @Override
//    protected void onResume() {
//        checkUserStatus();
//        super.onResume();
//    }
//
//    public void updateToken(String token){
//        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Tokens");
//        Token mtoKen = new Token(token);
//        ref.child(mUID).setValue(mtoKen);
//
//    }
//
//    private BottomNavigationView.OnNavigationItemSelectedListener selectedListener1 = new BottomNavigationView.OnNavigationItemSelectedListener() {
//        @Override
//        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
//            switch (item.getItemId()) {
//                case R.id.nav_profile:
//                    actionBar.setTitle("Trang Cá Nhân");
//                    ProfileFragment fragment = new ProfileFragment();
//                    FragmentTransaction ft1 = getSupportFragmentManager().beginTransaction();
//                    ft1.replace(R.id.content, fragment, "");
//                    ft1.commit();
//                    return true;
//                case R.id.nav_chatlits:
//                    actionBar.setTitle("Nhóm Chat");
//                    ChatListFragment fragment3 = new ChatListFragment();
//                    FragmentTransaction ft3 = getSupportFragmentManager().beginTransaction();
//                    ft3.replace(R.id.content, fragment3, "");
//                    ft3.commit();
//                    return true;
//            }
//            return false;
//        }
//    };
//
//    private void checkUserStatus(){
//        FirebaseUser user = firebaseAuth.getCurrentUser();
//        if(user != null){
////            mProfileTv.setText(user.getEmail());
//            mUID = user.getUid();
//            SharedPreferences sp = getSharedPreferences("SP_USER", MODE_PRIVATE);
//            SharedPreferences.Editor editor = sp.edit();
//            editor.putString("Current_USERID", mUID);
//            editor.apply();
//
//            updateToken(FirebaseInstanceId.getInstance().getToken());
//
//        }else{
//            startActivity(new Intent(MenuOtherActionsActivity.this, MainActivity.class));
//            finish();
//        }
//    }
//
//    @Override
//    public void onBackPressed() {
//        super.onBackPressed();
//        finish();
//    }
//
//    protected void onStart(){
//        checkUserStatus();
//        super.onStart();
//    }


}