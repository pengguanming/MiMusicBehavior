package com.pengguanming.mimusicbehavior.behavior;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.math.MathUtils;
import android.support.v7.graphics.Palette;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;

import com.pengguanming.mimusicbehavior.R;

/**
 * @function: face部分的Behavior
 */
public class FaceBehavior extends CoordinatorLayout.Behavior {
    private int topBarHeight;//topBar内容高度
    private float contentTransY;//滑动内容初始化TransY
    private float downEndY;//下滑时终点值
    private float faceTransY;//图片往上位移值
    private GradientDrawable drawable;//蒙层的背景

    @SuppressWarnings("unused")
    public FaceBehavior(Context context) {
        this(context,null);
    }

    @SuppressWarnings("WeakerAccess")
    public FaceBehavior(Context context, AttributeSet attrs) {
        super(context, attrs);
        //引入尺寸值
        int resourceId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
        int statusBarHeight = context.getResources().getDimensionPixelSize(resourceId);
        topBarHeight= (int) context.getResources().getDimension(R.dimen.top_bar_height)+statusBarHeight;
        contentTransY= (int) context.getResources().getDimension(R.dimen.content_trans_y);
        downEndY= (int) context.getResources().getDimension(R.dimen.content_trans_down_end_y);
        faceTransY= context.getResources().getDimension(R.dimen.face_trans_y);

        //抽取图片资源的亮色或者暗色作为蒙层的背景渐变色
        Palette palette = Palette.from(BitmapFactory.decodeResource(context.getResources(), R.mipmap.jj))
                .generate();
        Palette.Swatch vibrantSwatch = palette.getVibrantSwatch();
        Palette.Swatch mutedSwatch = palette.getMutedSwatch();
        int []colors=new int[2];
        if (mutedSwatch!=null){
            colors[0]=mutedSwatch.getRgb();
            colors[1]=getTranslucentColor(0.6f,mutedSwatch.getRgb());
        }else if (vibrantSwatch!=null){
            colors[0]=vibrantSwatch.getRgb();
            colors[1]=getTranslucentColor(0.6f,vibrantSwatch.getRgb());
        }else {
            colors[0]=Color.parseColor("#4D000000");
            colors[1]=getTranslucentColor(0.6f,Color.parseColor("#4D000000"));
        }
        drawable = new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT,colors);
    }

    @Override
    public boolean layoutDependsOn(@NonNull CoordinatorLayout parent, @NonNull View child, @NonNull View dependency) {
        //依赖Content View
        return dependency.getId() == R.id.ll_content;
    }

    @Override
    public boolean onLayoutChild(@NonNull CoordinatorLayout parent, @NonNull View child, int layoutDirection) {
        //设置蒙层背景
        child.findViewById(R.id.v_mask).setBackground(drawable);
        return false;
    }

    @Override
    public boolean onDependentViewChanged(@NonNull CoordinatorLayout parent, @NonNull View child, @NonNull View dependency) {
        //计算Content的上滑百分比、下滑百分比
        float upPro = (contentTransY- MathUtils.clamp(dependency.getTranslationY(), topBarHeight, contentTransY)) / (contentTransY - topBarHeight);
        float downPro = (downEndY- MathUtils.clamp(dependency.getTranslationY(), contentTransY, downEndY)) / (downEndY - contentTransY);

        ImageView iamgeview = child.findViewById(R.id.iv_face);
        View maskView =  child.findViewById(R.id.v_mask);

        if (dependency.getTranslationY()>=contentTransY){
            //根据Content上滑百分比位移图片TransitionY
            iamgeview.setTranslationY(downPro*faceTransY);
        }else {
            //根据Content下滑百分比位移图片TransitionY
            iamgeview.setTranslationY(faceTransY+4*upPro*faceTransY);
        }
        //根据Content上滑百分比设置图片和蒙层的透明度
        iamgeview.setAlpha(1-upPro);
        maskView.setAlpha(upPro);
        //因为改变了child的位置，所以返回true
        return true;
    }

    @SuppressWarnings("SameParameterValue")
    private int getTranslucentColor(float percent, int rgb) {
        int blue = Color.blue(rgb);
        int green = Color.green(rgb);
        int red = Color.red(rgb);
        int alpha = Color.alpha(rgb);
        alpha = Math.round(alpha*percent);
        return Color.argb(alpha, red, green, blue);
    }
}
