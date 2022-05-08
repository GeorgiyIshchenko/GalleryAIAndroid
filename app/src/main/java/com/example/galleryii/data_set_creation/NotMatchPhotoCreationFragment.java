package com.example.galleryii.data_set_creation;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.widget.AppCompatButton;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.example.galleryii.MainActivity;
import com.example.galleryii.R;

import java.util.Objects;


public class NotMatchPhotoCreationFragment extends Fragment {

    AppCompatButton btnNotMatch;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_not_match_photo_creation, container, false);
        btnNotMatch = view.findViewById(R.id.btn_not_match);
        btnNotMatch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((DataSetCreationActivity) Objects.requireNonNull(getActivity())).pickImagesIntent( false, getContext(), true);
            }
        });
        return view;
    }
}