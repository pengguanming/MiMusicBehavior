package com.pengguanming.mimusicbehavior.fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.pengguanming.mimusicbehavior.R;
import com.pengguanming.mimusicbehavior.adapter.SongAdater;

import java.util.ArrayList;

public class SongFragment extends Fragment {
    private RecyclerView mRecyclerView;
    private SongAdater mAdater;
    private ArrayList<Integer> mDatas = new ArrayList<>();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initData();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_song_layout, container, false);
        initView(view);
        initEvent();
        return view;
    }

    private void initView(View view) {
        mRecyclerView = view.findViewById(R.id.recyclerView);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
    }

    private void initEvent() {
        mRecyclerView.setAdapter(mAdater);
    }

    private void initData() {
        for (int i = 0; i < 32; i++) {
            mDatas.add(i);
        }
        mAdater = new SongAdater(mDatas);
    }
}
