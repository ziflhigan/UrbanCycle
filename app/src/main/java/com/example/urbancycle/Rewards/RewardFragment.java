package com.example.urbancycle.Rewards;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import java.sql.Connection;

import com.example.urbancycle.R;

import org.w3c.dom.Text;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class RewardFragment extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_reward, container, false);
    }

    private void showToast(String message) {
        Toast.makeText(getActivity(), message, Toast.LENGTH_LONG).show();
    }
}
//My brain can't handle that database, so I will complete everything other than database first.