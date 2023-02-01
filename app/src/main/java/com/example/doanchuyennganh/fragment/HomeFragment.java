package com.example.doanchuyennganh.fragment;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.core.view.MenuItemCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import java.util.ArrayList;
import java.util.Locale;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import android.widget.Toast;

import com.example.doanchuyennganh.AddPostActivity;
import com.example.doanchuyennganh.MainActivity;
import com.example.doanchuyennganh.R;
import com.example.doanchuyennganh.SettingActivity;
import com.example.doanchuyennganh.adapters.AdapterPosts;
import com.example.doanchuyennganh.adapters.StoryAdapter;
import com.example.doanchuyennganh.models.ModelPosts;
import com.example.doanchuyennganh.models.ModelsStory;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


public class HomeFragment extends Fragment {
    FirebaseAuth firebaseAuth;
    RecyclerView recyclerView ;
    List<ModelPosts> postsList;
    List<ModelsStory> storyList;
    private StoryAdapter storyAdapter;
    AdapterPosts adapterPosts;

    private RecyclerView recyclerView_story;

    public HomeFragment() {

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        firebaseAuth = FirebaseAuth.getInstance();
        // Inflate the layout for this com.example.doanchuyennganh.fragment
        View view =  inflater.inflate(R.layout.fragment_home, container, false);

        firebaseAuth = FirebaseAuth.getInstance();
        recyclerView = view.findViewById(R.id.postRecycleview);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());

        layoutManager.setStackFromEnd(true);
        layoutManager.setReverseLayout(true);

        recyclerView.setLayoutManager(layoutManager);

        postsList = new ArrayList<>();



        recyclerView_story = view.findViewById(R.id.recycle_view_story);
        recyclerView_story.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager1 = new LinearLayoutManager(getContext(),
                LinearLayoutManager.HORIZONTAL,false);
        recyclerView_story.setLayoutManager(linearLayoutManager1);
        storyList = new ArrayList<>();
        storyAdapter = new StoryAdapter(getContext(),storyList);
        recyclerView_story.setAdapter(storyAdapter);


        loadPosts();
        readStory();

        return view;
    }

    private void loadPosts() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Posts");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                postsList.clear();
                for(DataSnapshot ds: snapshot.getChildren()){
                    ModelPosts modelPosts = ds.getValue(ModelPosts.class);

                    postsList.add(modelPosts);

                    adapterPosts = new AdapterPosts(getActivity(), postsList);
                    recyclerView.setAdapter(adapterPosts);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getActivity(), ""+error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private  void searchPosts(final String searchQuery){
         DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Posts");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                postsList.clear();
                for(DataSnapshot ds: snapshot.getChildren()){
                    ModelPosts modelPosts = ds.getValue(ModelPosts.class);

                    if(modelPosts.getpTitle().toLowerCase().contains(searchQuery.toLowerCase())
                            || modelPosts.getpDescr().toLowerCase().contains(searchQuery.toLowerCase())){
                        postsList.add(modelPosts);
                    }

                    adapterPosts = new AdapterPosts(getActivity(), postsList);
                    recyclerView.setAdapter(adapterPosts);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getActivity(), ""+error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        super.onCreate(savedInstanceState);
    }
    private void checkUserStatus(){
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if(user != null){
//            mProfileTv.setText(user.getEmail());
        }else{
            startActivity(new Intent(getActivity(), MainActivity.class));
            getActivity().finish();
        }
    }


    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater){
        inflater.inflate(R.menu.menu_main, menu);

        MenuItem item = menu.findItem(R.id.action_search);
        menu.findItem(R.id.action_create_group).setVisible(false);
        menu.findItem(R.id.action_add_participan).setVisible(false);
        menu.findItem(R.id.action_groupInfo).setVisible(false);


        SearchView searchView = (SearchView) MenuItemCompat.getActionView(item);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                if(TextUtils.isEmpty(s)){
                    loadPosts();
                }else {
                    searchPosts(s);
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                if(TextUtils.isEmpty(s)){
                    loadPosts();
                }else {
                    searchPosts(s);
                }
                return false;
            }
        });

        super.onCreateOptionsMenu(menu, inflater);
    }


    private void readStory(){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Story");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot datasnapshot) {
                long timecurrent = System.currentTimeMillis();
                storyList.clear();
                storyList.add(new ModelsStory("",0,0,"",
                        FirebaseAuth.getInstance().getCurrentUser().getUid()));
                    int countStory = 0;
                    ModelsStory story = null;
                    for (DataSnapshot snapshot : datasnapshot.child(firebaseAuth.getUid()).getChildren()){
                        story = snapshot.getValue(ModelsStory.class);
                        if (timecurrent > story.getTimestart() && timecurrent < story.getTimeend()){
                            countStory++;
                        }
                    }
                    if (countStory>0){
                        storyList.add(story);
                    }
                    storyAdapter.notifyDataSetChanged();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if(id==R.id.action_logout){
            firebaseAuth.signOut();
            checkUserStatus();
        }else

        if(id==R.id.action_add_post){
            startActivity(new Intent(getActivity(), AddPostActivity.class));
        }else if(id==R.id.action_setting){
            startActivity(new Intent(getActivity(), SettingActivity.class));
        }
        return super.onOptionsItemSelected(item);
    }
}