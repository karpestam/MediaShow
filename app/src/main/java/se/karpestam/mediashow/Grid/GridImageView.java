package se.karpestam.mediashow.Grid;

import android.content.Context;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.PorterDuff;
import android.support.v7.widget.AppCompatImageView;
import android.util.AttributeSet;
import android.widget.ImageView;

public class GridImageView extends AppCompatImageView {
    public GridImageView(Context context) {
        super(context);
    }

    public GridImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public GridImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public void setSelected(boolean selected) {
        if (selected) {
            setColorFilter(0x99FFFFFF, PorterDuff.Mode.SRC_ATOP);
        } else {
            clearColorFilter();
        }
        super.setSelected(selected);
    }

    @Override
    public void setPressed(boolean pressed) {
        if (!isSelected()) {
            if (pressed) {
                setColorFilter(0x55FFFFFF, PorterDuff.Mode.SRC_ATOP);
            } else {
                clearColorFilter();
            }
        }
        super.setPressed(pressed);
    }

}
