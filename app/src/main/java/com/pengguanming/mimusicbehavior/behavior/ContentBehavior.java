package com.pengguanming.mimusicbehavior.behavior;

import android.animation.ValueAnimator;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.OverScroller;

import com.pengguanming.mimusicbehavior.R;

import java.lang.reflect.Field;

/**
 * @function: Content部分的Behavior
 */
public class ContentBehavior extends CoordinatorLayout.Behavior<View> {
    private static final long ANIM_DURATION_FRACTION = 200L;

    private int topBarHeight;//topBar内容高度
    private float contentTransY;//滑动内容初始化TransY
    private float downEndY;//下滑时终点值
    private ValueAnimator restoreAnimator;//收起内容时执行的动画
    private View mLlContent;//Content部分
    private boolean flingFromCollaps=false;//fling是否从折叠状态发生的

    @SuppressWarnings("unused")
    public ContentBehavior(Context context) {
        this(context, null);
    }

    @SuppressWarnings("WeakerAccess")
    public ContentBehavior(Context context, AttributeSet attrs) {
        super(context, attrs);
        int resourceId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
        int statusBarHeight = context.getResources().getDimensionPixelSize(resourceId);
        topBarHeight= (int) context.getResources().getDimension(R.dimen.top_bar_height)+statusBarHeight;
        contentTransY= (int) context.getResources().getDimension(R.dimen.content_trans_y);
        downEndY= (int) context.getResources().getDimension(R.dimen.content_trans_down_end_y);

        restoreAnimator = new ValueAnimator();
        restoreAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                translation(mLlContent, (float) animation.getAnimatedValue());
            }
        });
    }

    @Override
    public boolean onMeasureChild(@NonNull CoordinatorLayout parent, View child,
                                  int parentWidthMeasureSpec, int widthUsed, int parentHeightMeasureSpec,
                                  int heightUsed) {
        final int childLpHeight = child.getLayoutParams().height;
        if (childLpHeight == ViewGroup.LayoutParams.MATCH_PARENT
                || childLpHeight == ViewGroup.LayoutParams.WRAP_CONTENT) {
            //先获取CoordinatorLayout的测量规格信息，若不指定具体高度则使用CoordinatorLayout的高度
            int availableHeight = View.MeasureSpec.getSize(parentHeightMeasureSpec);
            if (availableHeight == 0) {
                availableHeight = parent.getHeight();
            }
            //设置Content部分高度
            final int height = availableHeight - topBarHeight;
            final int heightMeasureSpec = View.MeasureSpec.makeMeasureSpec(height,
                    childLpHeight == ViewGroup.LayoutParams.MATCH_PARENT
                            ? View.MeasureSpec.EXACTLY
                            : View.MeasureSpec.AT_MOST);
            //执行指定高度的测量，并返回true表示使用Behavior来代理测量子View
            parent.onMeasureChild(child, parentWidthMeasureSpec,
                    widthUsed, heightMeasureSpec, heightUsed);
            return true;
        }
        return false;
    }

    @Override
    public boolean onLayoutChild(@NonNull CoordinatorLayout parent, @NonNull View child, int layoutDirection) {
        boolean handleLayout = super.onLayoutChild(parent, child, layoutDirection);
        //绑定Content View
        mLlContent=child;
        return handleLayout;
    }

    //---NestedScrollingParent---//
    @Override
    @SuppressWarnings("deprecation")
    public boolean onStartNestedScroll(@NonNull CoordinatorLayout coordinatorLayout, @NonNull View child,
                                       @NonNull View directTargetChild, @NonNull View target, int axes) {
        return onStartNestedScroll(coordinatorLayout, child, directTargetChild, target, axes, ViewCompat.TYPE_TOUCH);
    }

    @Override
    @SuppressWarnings("deprecation")
    public void onNestedScrollAccepted(@NonNull CoordinatorLayout coordinatorLayout, @NonNull View child,
                                       @NonNull View directTargetChild, @NonNull View target, int axes) {
        onNestedScrollAccepted(coordinatorLayout, child, directTargetChild, target, axes,ViewCompat.TYPE_TOUCH);
    }

    @Override
    @SuppressWarnings("deprecation")
    public void onNestedPreScroll(@NonNull CoordinatorLayout coordinatorLayout, @NonNull View child, @NonNull View target, int dx, int dy, @NonNull int[] consumed) {
        onNestedPreScroll(coordinatorLayout, child, target, dx, dy, consumed,ViewCompat.TYPE_TOUCH);
    }

    @Override
    @SuppressWarnings("deprecation")
    public void onStopNestedScroll(@NonNull CoordinatorLayout coordinatorLayout, @NonNull View child, @NonNull View target) {
        onStopNestedScroll(coordinatorLayout, child, target,ViewCompat.TYPE_TOUCH);
    }

    @Override
    public boolean onNestedPreFling(@NonNull CoordinatorLayout coordinatorLayout, @NonNull View child, @NonNull View target, float velocityX, float velocityY) {
        flingFromCollaps=(child.getTranslationY()<=contentTransY);
        return false;
    }

    //---NestedScrollingParent2---//
    @Override
    public boolean onStartNestedScroll(@NonNull CoordinatorLayout coordinatorLayout, @NonNull View child,
                                       @NonNull View directTargetChild, @NonNull View target, int axes, int type) {
        //只接受内容View的垂直滑动
        return directTargetChild.getId() == R.id.ll_content
                &&axes== ViewCompat.SCROLL_AXIS_VERTICAL;
    }

    @Override
    public void onNestedScrollAccepted(@NonNull CoordinatorLayout coordinatorLayout, @NonNull View child,
                                       @NonNull View directTargetChild, @NonNull View target, int axes, int type) {
        if (restoreAnimator.isStarted()) {
            restoreAnimator.cancel();
        }
    }

    @Override
    public void onStopNestedScroll(@NonNull CoordinatorLayout coordinatorLayout, @NonNull View child, @NonNull View target, int type) {
        //如果是从初始状态转换到展开状态过程触发收起动画
        if (child.getTranslationY() > contentTransY) {
            restore();
        }
    }

    @Override
    public void onNestedPreScroll(@NonNull CoordinatorLayout coordinatorLayout, @NonNull View child,
                                  @NonNull View target, int dx, int dy, @NonNull int[] consumed, int type) {
        float transY = child.getTranslationY() - dy;

        if (type == ViewCompat.TYPE_NON_TOUCH && !flingFromCollaps) {
            return;
        }
        
        //处理上滑
        if (dy > 0) {
            if (transY >= topBarHeight) {
                translationByConsume(child, transY, consumed, dy);
            } else {
                translationByConsume(child, topBarHeight, consumed, (child.getTranslationY() - topBarHeight));
            }
        }

        if (dy < 0 && !target.canScrollVertically(-1)) {
            //下滑时处理Fling,折叠时下滑Recycler(或NestedScrollView) Fling滚动到contentTransY停止Fling
            if (type == ViewCompat.TYPE_NON_TOUCH&&transY >= contentTransY&&flingFromCollaps) {
                flingFromCollaps=false;
                translationByConsume(child, contentTransY, consumed, dy);
                stopViewScroll(target);
                return;
            }

            //处理下滑
            if (transY >= topBarHeight && transY <= downEndY) {
                translationByConsume(child, transY, consumed, dy);
            } else {
                translationByConsume(child, downEndY, consumed, (downEndY-child.getTranslationY()));
                stopViewScroll(target);
            }
        }
    }

    private void stopViewScroll(View target){
        if (target instanceof RecyclerView) {
            ((RecyclerView) target).stopScroll();
        }
        if (target instanceof NestedScrollView) {
            try {
                Class<? extends NestedScrollView> clazz = ((NestedScrollView) target).getClass();
                Field mScroller = clazz.getDeclaredField("mScroller");
                mScroller.setAccessible(true);
                OverScroller overScroller = (OverScroller) mScroller.get(target);
                overScroller.abortAnimation();
            } catch (NoSuchFieldException | IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }

    private void translationByConsume(View view, float translationY, int[] consumed, float consumedDy) {
        consumed[1] = (int) consumedDy;
        view.setTranslationY(translationY);
    }

    private void translation(View view, float translationY) {
        view.setTranslationY(translationY);
    }

    private void restore(){
        if (restoreAnimator.isStarted()) {
            restoreAnimator.cancel();
            restoreAnimator.removeAllListeners();
        }
        restoreAnimator.setFloatValues(mLlContent.getTranslationY(), contentTransY);
        restoreAnimator.setDuration(ANIM_DURATION_FRACTION);
        restoreAnimator.start();
    }

    @Override
    public void onDetachedFromLayoutParams() {
        if (restoreAnimator.isStarted()) {
            restoreAnimator.cancel();
            restoreAnimator.removeAllUpdateListeners();
            restoreAnimator.removeAllListeners();
            restoreAnimator = null;
        }
        super.onDetachedFromLayoutParams();
    }
}
