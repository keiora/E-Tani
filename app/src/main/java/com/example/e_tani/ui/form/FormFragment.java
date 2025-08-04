package com.example.e_tani.ui.form;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.example.e_tani.R;

public class FormFragment extends Fragment {

    public FormFragment() {
        // Konstruktor kosong
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate layout fragment_form.xml
        return inflater.inflate(R.layout.form, container, false);
    }
}
