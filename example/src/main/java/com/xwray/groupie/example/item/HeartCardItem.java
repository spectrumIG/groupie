package com.xwray.groupie.example.item;

import android.graphics.drawable.Animatable;
import android.support.annotation.ColorInt;
import android.view.View;

import com.xwray.groupie.Item;
import com.xwray.groupie.example.MainActivity;
import com.xwray.groupie.example.databinding.ItemHeartCardBinding;
import com.xwray.groupie.example.R;

import java.util.List;

public class HeartCardItem extends Item<ItemHeartCardBinding> {

    public static final String FAVORITE = "FAVORITE";

    @ColorInt private int colorRes;
    private OnFavoriteListener onFavoriteListener;
    private boolean checked = false;
    private boolean inProgress = false;

    public HeartCardItem(@ColorInt int colorRes, long id, OnFavoriteListener onFavoriteListener) {
        super(id);
        this.colorRes = colorRes;
        this.onFavoriteListener = onFavoriteListener;
        getExtras().put(MainActivity.INSET_TYPE_KEY, MainActivity.INSET);
    }

    @Override
    public int getLayout() {
        return R.layout.item_heart_card;
    }

    @Override
    public void bind(final ItemHeartCardBinding binding, int position) {
        //binding.getRoot().setBackgroundColor(colorRes);
        bindHeart(binding);
        binding.text.setText(String.valueOf(getId() + 1));

        binding.favorite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                inProgress = true;
                animateProgress(binding);

                onFavoriteListener.onFavorite(HeartCardItem.this, !checked);
            }
        });
    }

    private void bindHeart(ItemHeartCardBinding binding) {
        if (inProgress) {
            animateProgress(binding);
        } else {
            binding.favorite.setImageResource(R.drawable.favorite_state_list);
        }
        binding.favorite.setChecked(checked);
    }

    private void animateProgress(ItemHeartCardBinding binding) {
        binding.favorite.setImageResource(R.drawable.avd_favorite_progress);
        ((Animatable) binding.favorite.getDrawable()).start();
    }

    public void setFavorite(boolean favorite) {
        inProgress = false;
        checked = favorite;
    }

    @Override
    public void bind(ItemHeartCardBinding binding, int position, List<Object> payloads) {
        if (payloads.contains(FAVORITE)) {
            bindHeart(binding);
        } else {
            bind(binding, position);
        }
    }

    @Override
    public boolean isClickable() {
        return false;
    }

    public interface OnFavoriteListener {
        void onFavorite(HeartCardItem item, boolean favorite);
    }
}
