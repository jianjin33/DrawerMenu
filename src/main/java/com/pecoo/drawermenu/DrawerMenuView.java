package com.pecoo.drawermenu;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.LinearLayout;


import java.util.ArrayList;
import java.util.List;


/**
 * Created by jzm on 2018/3/22.
 * Description: 实现筛选功能的菜单栏
 * 仿造DropDownMenu作相应改进
 * 地址：https://github.com/dongjunkun/DropDownMenu
 */
public class DrawerMenuView extends LinearLayout {
    //记录tabTexts的顺序
    List<View> tabMenuViews = new ArrayList<>();
    List<Integer> tabHaveMenu = new ArrayList<>();

    private int tabBgColor;
    // 遮罩颜色
    private int maskColor = 0x88888888;
    private DrawerBaseAdapter adapter;
    private LinearLayout tabMenuView;
    // 底部容器，包含popupMenuViews，maskView
    private FrameLayout containerView;
    // 弹出菜单父布局
    private FrameLayout popupMenuViews;
    // 遮罩半透明View，点击可关闭DropDownMenu
    private View maskView;
    private int curTabPosition = -1;
    private int contentViewIndex;

    public DrawerMenuView(Context context) {
        this(context, null);
    }

    public DrawerMenuView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DrawerMenuView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setOrientation(VERTICAL);
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.DrawerMenuView);
        tabBgColor = typedArray.getColor(R.styleable.DrawerMenuView_backgroundColor, Color.WHITE);
        maskColor = typedArray.getColor(R.styleable.DrawerMenuView_maskColor, maskColor);

        typedArray.recycle();

        initTabAndContentView(context);

    }

    /**
     * 创建一个LinearLayout添加tab，这里为了简单直接用LinearLayout,
     * 如果tab过多需要滑动需要自定义
     *
     * @param context
     */
    private void initTabAndContentView(Context context) {
        tabMenuView = new LinearLayout(context);
        LayoutParams params = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup
                .LayoutParams.WRAP_CONTENT);
        tabMenuView.setOrientation(HORIZONTAL);
        tabMenuView.setBackgroundColor(tabBgColor);
        tabMenuView.setLayoutParams(params);
        addView(tabMenuView, 0);

        //初始化containerView并将其添加到DropDownMenu
        containerView = new FrameLayout(context);
        containerView.setLayoutParams(new FrameLayout.LayoutParams(FrameLayout.LayoutParams
                .MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));
        addView(containerView, 1);
    }

    public void setAdapter(DrawerBaseAdapter adapter) {
        if (this.adapter != null) {
            throw new IllegalStateException("can't repeat set adapter");
        }

        this.adapter = adapter;
        int tabCount = adapter.getTabCount();
//        tabMenuView.setWeightSum(tabCount);

        // 主内容布局添加进来
        if (null != adapter.getContentView()) {
            containerView.addView(adapter.getContentView(), contentViewIndex);
            contentViewIndex++;
        }

        // 弹窗后的蒙版
        maskView = new View(getContext());
        maskView.setLayoutParams(new FrameLayout.LayoutParams(FrameLayout.LayoutParams
                .MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));
        maskView.setBackgroundColor(maskColor);
        maskView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                closeMenu();
            }
        });
        containerView.addView(maskView, contentViewIndex);
        contentViewIndex++;
        maskView.setVisibility(GONE);

        // 弹窗小菜单布局
        popupMenuViews = new FrameLayout(getContext());
        popupMenuViews.setVisibility(GONE);
        containerView.addView(popupMenuViews, contentViewIndex);

        for (int i = 0; i < tabCount; i++) {
            DrawerHolder holder = adapter.createViewHolder(adapter.getViewType(i));
            adapter.registerAdapterDataObserver(new AdapterDataObserver(adapter, holder, i));
            adapter.onBindViewHolder(holder, i);
            addTabView(holder.itemView, i);
            View popupView = adapter.getPopupView(i, this);
            if (null != popupView) {
                if (popupView.getLayoutParams() == null) {
                    popupView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT
                            , ViewGroup.LayoutParams.WRAP_CONTENT));
                    popupMenuViews.addView(popupView, i);
                }
            } else {
                popupMenuViews.addView(new View(getContext()), i);
                // 记录没有下拉菜单的tab
                tabHaveMenu.add(i);
            }
        }
    }

    private void addTabView(View view, final int index) {
        view.setLayoutParams(new LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1.0f));
        tabMenuView.addView(view);
        tabMenuViews.add(view);
        view.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (itemMenuClickListener != null) {
                    itemMenuClickListener.OnItemMenuClick(v, index);
                }
                switchMenu(v);
            }
        });
    }

    /**
     * 切换菜单
     *
     * @param target
     */
    private void switchMenu(View target) {
        // 没有下拉菜单 关闭其他已经打开的菜单
        if (tabHaveMenu.contains(tabMenuViews.indexOf(target))) {
            closeMenu();
            return;
        }

        for (int i = 0; i < tabMenuView.getChildCount(); i++) {
            if (target == tabMenuView.getChildAt(i)) {
                if (curTabPosition == i) {
                    closeMenu();//关闭
                } else {//打开
                    if (curTabPosition == -1) {
                        popupMenuViews.setVisibility(View.VISIBLE);
                        popupMenuViews.setAnimation(AnimationUtils.loadAnimation(getContext(), R
                                .anim.dd_menu_in));
                        maskView.setVisibility(VISIBLE);
                        maskView.setAnimation(AnimationUtils.loadAnimation(getContext(), R.anim
                                .dd_mask_in));
                    }

                    View drawerView = getDrawerView(tabMenuView.getChildAt(i));
                    if (drawerView != null) {
                        drawerView.setVisibility(View.VISIBLE);
                    }

                    curTabPosition = i;
                    // 设置tab状态
                    adapter.setSelected(true, curTabPosition);
                }
            } else {//关闭
                View drawerView = getDrawerView(tabMenuView.getChildAt(i));
                if (drawerView != null) {
                    // 对外提供接口
                    drawerView.setVisibility(View.GONE);
                }
            }
        }
    }


    /**
     * 关闭菜单
     */
    public void closeMenu() {
        if (isShowing()) {
            // 更改状态
            adapter.setSelected(false, curTabPosition);
            popupMenuViews.setVisibility(View.GONE);
            popupMenuViews.setAnimation(AnimationUtils.loadAnimation(getContext(), R.anim
                    .dd_menu_out));
            maskView.setVisibility(GONE);
            maskView.setAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.dd_mask_out));
            curTabPosition = -1;
        }
    }

    /**
     * 是否处于可见状态
     *
     * @return
     */
    public boolean isShowing() {
        return curTabPosition != -1;
    }

    /**
     * 获取dropTabViews中对应popupMenuViews数组中的ListView
     *
     * @param view
     * @return
     */
    private View getDrawerView(View view) {
        if (tabMenuViews.contains(view)) {
            int index = tabMenuViews.indexOf(view);
            return popupMenuViews.getChildAt(index);
        } else {
            return null;
        }
    }


    private OnItemMenuClickListener itemMenuClickListener;

    public void setOnItemMenuClickListener(OnItemMenuClickListener listener) {
        itemMenuClickListener = listener;
    }

    public interface OnItemMenuClickListener {
        void OnItemMenuClick(View view, int position);
    }

    /**
     * 观察者
     * 数据源发送变化，客户端主动调用DrawerBaseAdapter的notify方法，被观察者发生变化，会走到这里
     * onChanged通过调用adapter的设置布局（onBindViewHolder）来改变控件上数据。
     * 若需要与Observable解耦，可抽象一个interface或abs的Observer
     */
    public static class AdapterDataObserver {
        private DrawerBaseAdapter adapter;
        private DrawerHolder holder;
        private int position;

        public AdapterDataObserver(DrawerBaseAdapter adapter, DrawerHolder holder, int i) {
            this.adapter = adapter;
            this.holder = holder;
            this.position = i;
        }

        public void onChanged() {
            adapter.onBindViewHolder(holder, position);
        }

        public void onChanged(int position) {
            adapter.onBindViewHolder(holder, position);
        }
    }
}
