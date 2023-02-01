package com.example.doanchuyennganh;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.core.view.MenuItemCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.example.doanchuyennganh.adapters.AdapterGroupChatList;
import com.example.doanchuyennganh.models.ModelGroupChat;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;


public class GroupChatFragment extends Fragment {
    private RecyclerView groupsRv;
    private FirebaseAuth firebaseAuth;
    private ArrayList<ModelGroupChat> groupChatLists;
    private AdapterGroupChatList adapterGroupChatList;

    public GroupChatFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_group_chat, container, false);

        groupsRv = view.findViewById(R.id.groupsRv);

        firebaseAuth = FirebaseAuth.getInstance();

        loadGroupChatList();

        return view;
    }

    private void loadGroupChatList() {
        groupChatLists = new ArrayList<>();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Groups");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                groupChatLists.clear();
                for(DataSnapshot ds: snapshot.getChildren()){

                    if(ds.child("Participants").child(firebaseAuth.getUid()).exists()){
                        ModelGroupChat model = ds.getValue(ModelGroupChat.class);
                        groupChatLists.add(model);
                    }
                }
                adapterGroupChatList = new AdapterGroupChatList(getActivity(), groupChatLists);
                groupsRv.setAdapter(adapterGroupChatList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void searchGroupChatList(String query) {
        groupChatLists = new ArrayList<>();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Groups");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                groupChatLists.clear();
                for(DataSnapshot ds: snapshot.getChildren()){

                    if(ds.child("Participants").child(firebaseAuth.getUid()).exists()){
                        if(ds.child("groupTitle").toString().toLowerCase().contains(query.toLowerCase())){
                            ModelGroupChat model = ds.getValue(ModelGroupChat.class);
                            groupChatLists.add(model);
                        }

                    }
                }
                adapterGroupChatList = new AdapterGroupChatList(getActivity(), groupChatLists);
                groupsRv.setAdapter(adapterGroupChatList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        super.onCreate(savedInstanceState);
    }

    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater){
        inflater.inflate(R.menu.menu_main, menu);

        menu.findItem(R.id.action_add_post).setVisible(false);
        menu.findItem(R.id.action_setting).setVisible(false);
        menu.findItem(R.id.action_add_participan).setVisible(false);
        menu.findItem(R.id.action_groupInfo).setVisible(false);


        MenuItem item = menu.findItem(R.id.action_search);
        SearchView searchView =(SearchView) MenuItemCompat.getActionView(item);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if(!TextUtils.isEmpty(query.trim())){
                    searchGroupChatList(query);
                }else {
                    loadGroupChatList();
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if(!TextUtils.isEmpty(newText.trim())){
                    searchGroupChatList(newText);
                }else {
                    loadGroupChatList();
                }
                return false;
            }
        });
        super.onCreateOptionsMenu(menu, inflater);
    }


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if(id==R.id.action_logout){
            firebaseAuth.signOut();
            checkUserStatus();
        }
        else if(id==R.id.action_create_group){
            startActivity(new Intent(getActivity(), CreateGroupActivity.class));
        }
        return super.onOptionsItemSelected(item);
    }

    private void checkUserStatus() {
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if(user == null){
            startActivity(new Intent(getActivity(), MainActivity.class));
            getActivity().finish();
        }
    }

}