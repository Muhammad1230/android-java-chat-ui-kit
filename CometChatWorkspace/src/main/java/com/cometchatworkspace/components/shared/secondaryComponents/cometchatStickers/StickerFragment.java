package com.cometchatworkspace.components.shared.secondaryComponents.cometchatStickers;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.cometchatworkspace.R;
import com.cometchatworkspace.components.shared.secondaryComponents.cometchatStickers.listener.StickerClickListener;
import com.cometchatworkspace.components.shared.secondaryComponents.cometchatStickers.model.Sticker;

import java.util.ArrayList;
import java.util.List;

import com.cometchatworkspace.components.shared.secondaryComponents.cometchatStickers.adapter.StickersAdapter;
import com.cometchatworkspace.resources.utils.recycler_touch.ClickListener;
import com.cometchatworkspace.resources.utils.recycler_touch.RecyclerTouchListener;

/**
 * Purpose - It is a Fragment which is used in StickerView to display stickers sent or received to a
 * particular receiver.
 *
 * Created On - 20th March 2020
 *
 * Modified On - 23rd March 2020
 *
 */
public class StickerFragment extends Fragment {

    private RecyclerView rvStickers;
    private StickersAdapter adapter;
    private String Id;
    private final String TAG = "StickerView";
    private String type;
    private List<Sticker> stickers = new ArrayList<>();
    StickerClickListener stickerClickListener;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_stickers_view,container,false);
        rvStickers = view.findViewById(R.id.rvStickers);
        rvStickers.setLayoutManager(new GridLayoutManager(getContext(),4));
        Id = this.getArguments().getString("Id");
        type = this.getArguments().getString("type");
        List<Sticker> list = this.getArguments().getParcelableArrayList("stickerList");
        stickers = list;
        adapter = new StickersAdapter(getContext(),stickers);
        rvStickers.setAdapter(adapter);

        rvStickers.addOnItemTouchListener(new RecyclerTouchListener(getContext(), rvStickers, new ClickListener() {
            @Override
            public void onClick(View var1, int var2) {
                Sticker sticker = (Sticker)var1.getTag(R.string.sticker);
                stickerClickListener.onClickListener(sticker);
            }
        }));
        return view;
    }

    public void setStickerClickListener(StickerClickListener stickerClickListener) {
        this.stickerClickListener = stickerClickListener;
    }
}
