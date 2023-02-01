package com.example.doanchuyennganh;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.PopupMenu;

import com.example.doanchuyennganh.fragment.HomeFragment;
import com.example.doanchuyennganh.fragment.MenuOtherActionsFragment;
import com.example.doanchuyennganh.fragment.NotificationsFragment;
import com.example.doanchuyennganh.fragment.ProfileFragment;
import com.example.doanchuyennganh.fragment.UsersFragment;
import com.example.doanchuyennganh.notifications.MenuOtherActionsActivity;
import com.example.doanchuyennganh.notifications.Token;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

public class DashboardActivity extends AppCompatActivity {
    FirebaseAuth firebaseAuth;
    ActionBar actionBar;
    Context context;
    String mUID;
    ActionBarDrawerToggle actionBarDrawerToggle;

    private BottomNavigationView navigationView;
    private NavigationView navigationView2;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        navigationView2 = findViewById(R.id.navigation_view1);


        firebaseAuth = FirebaseAuth.getInstance();

        navigationView = findViewById(R.id.navigation_view);
        navigationView.setOnNavigationItemSelectedListener(selectedListener);

        actionBar = getSupportActionBar();

        actionBar.setTitle("Trang Chủ");
        HomeFragment fragment = new HomeFragment();
        FragmentTransaction ft1 = getSupportFragmentManager().beginTransaction();
        ft1.replace(R.id.content, fragment,"");
        ft1.commit();

        checkUserStatus();

    }

    @Override
    protected void onResume() {
        checkUserStatus();
        super.onResume();
    }

    public void updateToken(String token){
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Tokens");
        Token mtoKen = new Token(token);
        ref.child(mUID).setValue(mtoKen);

    }

private  BottomNavigationView.OnNavigationItemSelectedListener selectedListener = new BottomNavigationView.OnNavigationItemSelectedListener() {
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
                case R.id.nav_home:
                    actionBar.setTitle("Trang Chủ");
                    HomeFragment fragment1 = new HomeFragment();
                    FragmentTransaction ft1 = getSupportFragmentManager().beginTransaction();
                    ft1.replace(R.id.content, fragment1,"");
                    ft1.commit();
                    return true;
                case R.id.nav_users:
                    actionBar.setTitle("Bạn Bè");
                    UsersFragment fragment2 = new UsersFragment();
                    FragmentTransaction ft2 = getSupportFragmentManager().beginTransaction();
                    ft2.replace(R.id.content, fragment2,"");
                    ft2.commit();
                    return true;
                case R.id.nav_chat:
                    actionBar.setTitle("Trò Chuyện");
                    ChatListFragment fragment3 = new ChatListFragment();
                    FragmentTransaction ft3 = getSupportFragmentManager().beginTransaction();
                    ft3.replace(R.id.content, fragment3,"");
                    ft3.commit();
                    return true;

                case R.id.nav_notification:
                    actionBar.setTitle("Thông Báo");
                    NotificationsFragment fragment4 = new NotificationsFragment();
                    FragmentTransaction ft4 = getSupportFragmentManager().beginTransaction();
                    ft4.replace(R.id.content, fragment4,"");
                    ft4.commit();
                    return true;

//                case R.id.nav_group:
//                    actionBar.setTitle("Nhóm");
//                    GroupChatFragment fragment5 = new GroupChatFragment();
//                    FragmentTransaction ft5 = getSupportFragmentManager().beginTransaction();
//                    ft5.replace(R.id.content, fragment5,"");
//                    ft5.commit();
//                    return true;
//
//
//                case R.id.nav_profile:
//                    actionBar.setTitle("Trang Cá Nhân");
//                    ProfileFragment fragment6 = new ProfileFragment();
//                    FragmentTransaction ft6 = getSupportFragmentManager().beginTransaction();
//                    ft6.replace(R.id.content, fragment6,"");
//                    ft6.commit();
//                    return true;

                case R.id.nav_more:
                    actionBar.setTitle("Khác");
                    MenuOtherActionsFragment fragment5 = new MenuOtherActionsFragment();
                    FragmentTransaction ft5 = getSupportFragmentManager().beginTransaction();
                    ft5.replace(R.id.content, fragment5,"");
                    ft5.commit();
//                    showMoreOptions();
                    return true;

            }
        return false;
    }
};

    private void showMoreOptions() {
        PopupMenu popupMenu = new PopupMenu(this, navigationView, Gravity.END);
        popupMenu.getMenu().add(Menu.NONE, 0,100, "Trang cá nhân");
        popupMenu.getMenu().add(Menu.NONE, 1,100, "Nhóm Chat");
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                int id = menuItem.getItemId();
                if(id == 0){
//                    actionBar.setTitle("Trang cá nhân");
                    ProfileFragment fragment = new ProfileFragment();
                    FragmentTransaction fragmentManager = getSupportFragmentManager().beginTransaction();
                    fragmentManager.replace(R.id.content, fragment, "");
                    fragmentManager.commit();
                }else if(id == 1){
//                    actionBar.setTitle("Nhóm Chat");
                    GroupChatFragment fragment3 = new GroupChatFragment();
                    FragmentTransaction fragmentManager3 = getSupportFragmentManager().beginTransaction();
                    fragmentManager3.replace(R.id.content, fragment3, "");
                    fragmentManager3.commit();
                }
                return false;
            }
        });
        popupMenu.show();

    }

    //menu options
//    @Override
//    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
//        if (actionBarDrawerToggle.onOptionsItemSelected(item)){
//
//            return true;
//        }
//        return super.onOptionsItemSelected(item);
//    }

    private void checkUserStatus(){
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if(user != null){
//            mProfileTv.setText(user.getEmail());
            mUID = user.getUid();
            SharedPreferences sp = getSharedPreferences("SP_USER", MODE_PRIVATE);
            SharedPreferences.Editor editor = sp.edit();
            editor.putString("Current_USERID", mUID);
            editor.apply();

            updateToken(FirebaseInstanceId.getInstance().getToken());

        }else{
            startActivity(new Intent(DashboardActivity.this, MainActivity.class));
            finish();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    protected void onStart(){
        checkUserStatus();
        super.onStart();
    }

}