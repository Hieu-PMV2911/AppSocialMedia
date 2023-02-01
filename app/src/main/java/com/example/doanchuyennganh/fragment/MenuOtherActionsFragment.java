package com.example.doanchuyennganh.fragment;

import static android.content.Context.MODE_PRIVATE;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.fragment.app.FragmentManager;

import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.example.doanchuyennganh.ChatListFragment;
import com.example.doanchuyennganh.DashboardActivity;
import com.example.doanchuyennganh.GroupChatFragment;
import com.example.doanchuyennganh.MainActivity;
import com.example.doanchuyennganh.R;
import com.example.doanchuyennganh.notifications.MenuOtherActionsActivity;
import com.example.doanchuyennganh.notifications.Token;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;


public class MenuOtherActionsFragment extends Fragment {
    ActionBar actionBar;
    BottomNavigationView navigationView;
    FirebaseAuth firebaseAuth;

    String mUID;

    public MenuOtherActionsFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_menu_other_actions, container, false);

        firebaseAuth = FirebaseAuth.getInstance();
        navigationView = view.findViewById(R.id.navigation_view1);

        navigationView.setOnNavigationItemSelectedListener(selectedListener1);
        //actionBar.setTitle("Trang Cá Nhân");
        ProfileFragment fragment6 = new ProfileFragment();
        FragmentManager fragmentManager6 = getActivity().getSupportFragmentManager();
        fragmentManager6.beginTransaction().replace(R.id.content1, fragment6, "").commit();

        checkUserStatus();
        return view;
    }

    private BottomNavigationView.OnNavigationItemSelectedListener selectedListener1 = new BottomNavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.nav_profile:
                    //actionBar.setTitle("Trang Cá Nhân");
                    ProfileFragment fragment6 = new ProfileFragment();
                    FragmentManager fragmentManager6 = getActivity().getSupportFragmentManager();
                    fragmentManager6.beginTransaction().replace(R.id.content1, fragment6, "").commit();
                    return true;
                case R.id.nav_chatlits:
                    //actionBar.setTitle("Nhóm Chat");
                    GroupChatFragment fragment7 = new GroupChatFragment();
                    FragmentManager fragmentManager7 = getActivity().getSupportFragmentManager();
                    fragmentManager7.beginTransaction().replace(R.id.content1, fragment7, "").commit();
                    return true;
            }
            return false;
        }
    };

    @Override
    public void onResume() {
        checkUserStatus();
        super.onResume();
    }

    public void updateToken(String token){
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Tokens");
        Token mtoKen = new Token(token);
        ref.child(mUID).setValue(mtoKen);

    }

    private void checkUserStatus(){
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if(user != null){
//            mProfileTv.setText(user.getEmail());
            mUID = user.getUid();
            SharedPreferences sp = getActivity().getSharedPreferences("SP_USER", MODE_PRIVATE);
            SharedPreferences.Editor editor = sp.edit();
            editor.putString("Current_USERID", mUID);
            editor.apply();

            updateToken(FirebaseInstanceId.getInstance().getToken());

        }else{
            startActivity(new Intent(getActivity(), MainActivity.class));
            getActivity().finish();
        }
    }


    public void onStart(){
        checkUserStatus();
        super.onStart();
    }
}