package com.pengguanming.mimusicbehavior.behavior;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.math.MathUtils;
import android.util.AttributeSet;
import android.view.View;

import com.pengguanming.mimusicbehavior.R;

/**
 * @function: TopBar部分的Behavior
 */
public class TopBarBehavior extends CoordinatorLayout.Behavior {
    private float contentTransY;//滑动内容初始化TransY
    private int topBarHeight;//topBar内容高度

    @SuppressWarnings("unused")
    public TopBarBehavior(Context context) {
        this(context,null);
    }

    @SuppressWarnings("WeakerAccess")
    public TopBarBehavior(Context context, AttributeSet attrs) {
        super(context, attrs);
        //引入尺寸值
        contentTransY= (int) context.getResources().getDimension(R.dimen.content_trans_y);
        int resourceId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
        int statusBarHeight = context.getResources().getDimensionPixelSize(resourceId);
        topBarHeight= (int) context.getResources().getDimension(R.dimen.top_bar_height)+statusBarHeight;
    }

    @Override
    public boolean layoutDependsOn(@NonNull CoordinatorLayout parent, @NonNull View child, @NonNull View dependency) {
        //依赖Content
        return dependency.getId() == R.id.ll_content;
    }

    @Override
    public boolean onDependentViewChanged(@NonNull CoordinatorLayout parent, @NonNull View child, @NonNull View dependency) {
        //计算Content上滑的百分比，设置子view的透明度
        float upPro = (contentTransY- MathUtils.clamp(dependency.getTranslationY(), topBarHeight, contentTransY)) / (contentTransY - topBarHeight);
        View tvName=child.findViewById(R.id.tv_top_bar_name);
        View tvColl=child.findViewById(R.id.tv_top_bar_coll);
        tvName.setAlpha(upPro);
        tvColl.setAlpha(upPro);
        return true;
    }
}
