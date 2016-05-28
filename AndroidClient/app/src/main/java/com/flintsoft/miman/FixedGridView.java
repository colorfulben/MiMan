package com.flintsoft.miman;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.GridView;

/**
 * Created by Xin on 2015/11/21.
 */
public class FixedGridView extends GridView {
    public FixedGridView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    public FixedGridView(Context context) {
        super(context);
    }
    public FixedGridView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }
    @Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int expandSpec = MeasureSpec.makeMeasureSpec(
                MEASURED_SIZE_MASK, MeasureSpec.AT_MOST);
        super.onMeasure(widthMeasureSpec, expandSpec);
    }
}
