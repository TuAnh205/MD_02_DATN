package com.anhnvt_ph55017.md_02_datn.fragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.anhnvt_ph55017.md_02_datn.R;

import org.jspecify.annotations.NonNull;

import javax.annotation.Nullable;

public class ProfileFragment extends Fragment {

    Button btnLogout;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        btnLogout = view.findViewById(R.id.btnLogout);

        btnLogout.setOnClickListener(v -> {

            Toast.makeText(getContext(),"Logged out",Toast.LENGTH_SHORT).show();

        });

        return view;
    }
}