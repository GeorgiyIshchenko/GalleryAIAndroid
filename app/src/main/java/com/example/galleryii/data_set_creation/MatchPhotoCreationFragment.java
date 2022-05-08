package com.example.galleryii.data_set_creation;

import android.os.Bundle;

import androidx.appcompat.widget.AppCompatButton;
import androidx.fragment.app.Fragment;

import android.provider.ContactsContract;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.galleryii.R;

import java.util.Objects;

public class MatchPhotoCreationFragment extends Fragment {

    AppCompatButton btnMatch;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_match_photo_creation, container, false);
        btnMatch = view.findViewById(R.id.btn_match);
        btnMatch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((DataSetCreationActivity) Objects.requireNonNull(getActivity())).pickImagesIntent( true, getContext(), false);
                getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.data_set_creation, new NotMatchPhotoCreationFragment()).commit();
            }
        });
        return view;
    }
}