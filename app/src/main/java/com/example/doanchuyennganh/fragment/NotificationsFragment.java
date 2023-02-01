package com.example.doanchuyennganh.fragment;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

import com.example.doanchuyennganh.R;
import com.example.doanchuyennganh.adapters.AdapterNotifications;
import com.example.doanchuyennganh.models.ModelsNotifications;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


public class NotificationsFragment extends Fragment {
    RecyclerView notificationsRv;

    private AdapterNotifications adapterNotifications;

    private ArrayList<ModelsNotifications> notificationsArrayList;

    private FirebaseAuth firebaseAuth;

    public NotificationsFragment() {

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_notifications, container, false);

        firebaseAuth = FirebaseAuth.getInstance();

        notificationsRv = view.findViewById(R.id.notificaionsRv);

        getAllNotifications();

        return view;
    }

    private void getAllNotifications() {
        notificationsArrayList = new ArrayList<>();

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(firebaseAuth.getUid()).child("Notifications").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                notificationsArrayList.clear();
                for(DataSnapshot ds: snapshot.getChildren()){
                    ModelsNotifications model = ds.getValue(ModelsNotifications.class);
                    notificationsArrayList.add(model);
                }

                adapterNotifications = new AdapterNotifications(getActivity(), notificationsArrayList);
                notificationsRv.setAdapter(adapterNotifications);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

}