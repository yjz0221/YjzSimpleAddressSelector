package com.github.yjz.address;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.yjz.address.listener.OnAddressSelectedListener;
import com.github.yjz.address.model.AddressItem;
import com.github.yjz.address.provider.AddressProvider;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.tabs.TabLayout;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * 作者:yjz
 * 创建日期：2025/11/21
 * 描述: 支持自定义样式的地址选择器
 */
public class AddressSelector extends BottomSheetDialogFragment {

    private TabLayout tabLayout;
    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private TextView tvTitle;
    private TextView tvEmpty;

    private AddressProvider provider;
    private OnAddressSelectedListener listener;

    private InternalAdapter adapter;

    private List<AddressItem> selectedPath = new ArrayList<>();
    private List<AddressItem> currentList = new ArrayList<>();

    private int maxLevel = 4;

    // --- 自定义配置项 (默认值) ---
    private String titleText = "所在地区";
    private String tabHintText = "请选择";
    private String emptyText = "暂无下级数据";

    // --- 自定义尺寸配置  ---
    private int customWidth = 0;
    private int customHeight = 0;

    // 默认选中颜色
    @ColorInt
    private int selectedColor = Color.parseColor("#4CAF50");
    // 默认未选中颜色
    @ColorInt
    private int unSelectedColor = Color.parseColor("#333333");
    // ProgressBar 颜色 (默认跟选中颜色一致)
    @ColorInt
    private int progressBarColor = Color.parseColor("#4CAF50");

    // --- 交互配置 ---
    // 默认允许点击外部关闭
    private boolean isCanceledOnTouchOutside = true;


    public static AddressSelector newInstance(AddressProvider provider, OnAddressSelectedListener listener) {
        AddressSelector fragment = new AddressSelector();
        fragment.provider = provider;
        fragment.listener = listener;
        return fragment;
    }


    // --- 开放给开发者的配置方法 (支持链式调用) ---

    /**
     * 设置顶部标题
     */
    public AddressSelector setTitle(String title) {
        this.titleText = title;
        return this;
    }

    /**
     * 设置 Tab 未选中时的提示文案 (例如 "请选择", "Choose")
     */
    public AddressSelector setTabHint(String hint) {
        this.tabHintText = hint;
        return this;
    }

    /**
     * 设置选中状态的颜色 (Tab下划线、Tab文字、列表中高亮的文字)
     */
    public AddressSelector setSelectedColor(@ColorInt int color) {
        this.selectedColor = color;
        // 如果没有单独设置 ProgressBar 颜色，默认跟随主题色
        this.progressBarColor = color;
        return this;
    }

    /**
     * 设置未选中状态的文字颜色 (Tab文字、列表中普通文字)
     */
    public AddressSelector setUnSelectedColor(@ColorInt int color) {
        this.unSelectedColor = color;
        return this;
    }

    /**
     * 单独设置加载进度条的颜色
     */
    public AddressSelector setProgressBarColor(@ColorInt int color) {
        this.progressBarColor = color;
        return this;
    }

    /**
     * 设置最大层级
     */
    public AddressSelector setMaxLevel(int maxLevel) {
        this.maxLevel = maxLevel;
        return this;
    }


    /**
     * 设置没有数据时的提示文字
     */
    public AddressSelector setEmptyHint(String hint) {
        this.emptyText = hint;
        return this;
    }

    /**
     * 设置是否允许点击弹窗外部关闭
     */
    public AddressSelector setCanceledOnTouchOutside(boolean enable) {
        this.isCanceledOnTouchOutside = enable;
        return this;
    }


    /**
     * 设置弹窗宽度
     *
     * @param width 像素值 (px)，或者 ViewGroup.LayoutParams.MATCH_PARENT / WRAP_CONTENT
     */
    public AddressSelector setWidth(int width) {
        this.customWidth = width;
        return this;
    }

    /**
     * 设置弹窗高度
     *
     * @param height 像素值 (px)，或者 ViewGroup.LayoutParams.MATCH_PARENT / WRAP_CONTENT
     */
    public AddressSelector setHeight(int height) {
        this.customHeight = height;
        return this;
    }

    public AddressSelector showAt(FragmentManager manager, String tag) {
        super.show(manager, tag);
        return this;
    }


    public AddressSelector show(FragmentManager manager) {
        super.show(manager, "address_selector");
        return this;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.lib_layout_address_selector, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tabLayout = view.findViewById(R.id.lib_tab_layout);
        recyclerView = view.findViewById(R.id.lib_recycler_view);
        progressBar = view.findViewById(R.id.lib_progress_bar);
        tvTitle = view.findViewById(R.id.lib_tv_title);
        tvEmpty = view.findViewById(R.id.lib_tv_empty);

        // 1. 应用自定义标题
        tvTitle.setText(titleText);

        // 2. 应用自定义颜色到 TabLayout
        tabLayout.setSelectedTabIndicatorColor(selectedColor);
        // 设置 Tab 文字颜色：正常状态 unSelectedColor, 选中状态 selectedColor
        tabLayout.setTabTextColors(unSelectedColor, selectedColor);

        tvEmpty.setText(emptyText);

        // 3. 应用自定义颜色到 ProgressBar
        updateProgressColor();

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new InternalAdapter();
        recyclerView.setAdapter(adapter);

        addTab(tabHintText);
        loadData(null);

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                int position = tab.getPosition();
                AddressItem parent = null;
                if (position > 0) {
                    if (position - 1 < selectedPath.size()) {
                        parent = selectedPath.get(position - 1);
                    }
                }
                loadData(parent);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });
    }


    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        dialog.setOnShowListener(dialogInterface -> {
            com.google.android.material.bottomsheet.BottomSheetDialog bottomSheetDialog =
                    (com.google.android.material.bottomsheet.BottomSheetDialog) dialogInterface;

            View bottomSheet = bottomSheetDialog.findViewById(com.google.android.material.R.id.design_bottom_sheet);

            if (bottomSheet != null) {
                bottomSheet.setBackground(null);

                // 初始化默认宽高
                if (customWidth == 0) {
                    customWidth = ViewGroup.LayoutParams.MATCH_PARENT;
                }
                if (customHeight == 0) {
                    customHeight = (int) (getScreenHeight(requireActivity()) * 0.5f);
                }

                ViewGroup.LayoutParams layoutParams = bottomSheet.getLayoutParams();
                layoutParams.width = customWidth;
                layoutParams.height = customHeight;
                bottomSheet.setLayoutParams(layoutParams);

                com.google.android.material.bottomsheet.BottomSheetBehavior<View> behavior =
                        com.google.android.material.bottomsheet.BottomSheetBehavior.from(bottomSheet);

                // 1. 将 PeekHeight 设置为屏幕高度（或足够大的值）
                // 这样弹窗启动时的“预览动画”就会直接覆盖整个高度，消除“停顿感”
                behavior.setPeekHeight(getScreenHeight(requireActivity()));

                // 2. 设置为展开状态
                behavior.setState(com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_EXPANDED);
                // 3. 禁用折叠（可选，防止用户向下滑动时变成半截状态）
                behavior.setSkipCollapsed(true);
            }

            bottomSheetDialog.setCanceledOnTouchOutside(isCanceledOnTouchOutside);
        });

        return dialog;
    }


    private void addTab(String text) {
        TabLayout.Tab tab = tabLayout.newTab().setText(text);
        tabLayout.addTab(tab);
        tab.select();
    }

    private void updateCurrentTab(String text) {
        int position = tabLayout.getSelectedTabPosition();
        if (position >= 0 && position < tabLayout.getTabCount()) {
            tabLayout.getTabAt(position).setText(text);
        }
    }

    /**
     * 兼容低版本的 ProgressBar 颜色设置方法
     */
    private void updateProgressColor() {
        if (progressBar == null) return;

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            // Android 5.0+ 使用原生 API
            progressBar.setIndeterminateTintList(ColorStateList.valueOf(progressBarColor));
        } else {
            // Android 4.3 等低版本使用 ColorFilter
            android.graphics.drawable.Drawable drawable = progressBar.getIndeterminateDrawable();
            if (drawable != null) {
                drawable.mutate().setColorFilter(progressBarColor, android.graphics.PorterDuff.Mode.SRC_IN);
            }
        }
    }

    private void loadData(AddressItem parent) {
        if (provider == null) return;
        setLoading(true);

        provider.provideData(parent, new AddressProvider.DataCallback() {
            @Override
            public void onSuccess(List<AddressItem> data) {
                if (!isAdded()) return;
                setLoading(false);

                currentList = (data == null) ? new ArrayList<>() : data;
                adapter.notifyDataSetChanged();

                if (currentList.isEmpty()) {
                    recyclerView.setVisibility(View.GONE);
                    tvEmpty.setVisibility(View.VISIBLE);
                } else {
                    tvEmpty.setVisibility(View.GONE);
                    recyclerView.setVisibility(View.VISIBLE);

                    String codeToHighlight = null;
                    int currentTabPosition = tabLayout.getSelectedTabPosition();
                    if (currentTabPosition >= 0 && currentTabPosition < selectedPath.size()) {
                        codeToHighlight = selectedPath.get(currentTabPosition).code;
                    }
                    adapter.setSelectedCode(codeToHighlight);
                }
            }

            @Override
            public void onFailure(String errorMsg) {
                if (!isAdded()) return;
                setLoading(false);
                Toast.makeText(getContext(), errorMsg, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void onListItemClick(AddressItem item) {
        int currentTabPos = tabLayout.getSelectedTabPosition();

        if (currentTabPos < selectedPath.size()) {
            AddressItem oldItem = selectedPath.get(currentTabPos);
            if (oldItem.code.equals(item.code)) {
                if (currentTabPos + 1 < tabLayout.getTabCount()) {
                    tabLayout.getTabAt(currentTabPos + 1).select();
                }
                return;
            }
            selectedPath.subList(currentTabPos, selectedPath.size()).clear();
            int tabCount = tabLayout.getTabCount();
            for (int i = tabCount - 1; i > currentTabPos; i--) {
                tabLayout.removeTabAt(i);
            }
        }

        updateCurrentTab(item.name);
        selectedPath.add(item);
        adapter.setSelectedCode(item.code);

        if (listener != null) {
            listener.onItemSelect(item, currentTabPos);
        }

        if (selectedPath.size() >= maxLevel) {
            finishSelection();
            return;
        }

        addTab(tabHintText); // 使用配置的提示文案
    }

    private void finishSelection() {
        if (listener != null) {
            listener.onAddressSelected(selectedPath);
        }
    }

    private void setLoading(boolean isLoading) {
        int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);
        if (isLoading) {
            progressBar.setVisibility(View.VISIBLE);
            progressBar.setAlpha(0f);
            progressBar.animate().alpha(1f).setDuration(shortAnimTime).setListener(null);

            recyclerView.animate().alpha(0f).setDuration(shortAnimTime).setListener(null);
            tvEmpty.setVisibility(View.GONE);
        } else {
            progressBar.animate().alpha(0f).setDuration(shortAnimTime).setListener(new android.animation.AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(android.animation.Animator animation) {
                    progressBar.setVisibility(View.GONE);
                }
            });
            recyclerView.setVisibility(View.VISIBLE);
            recyclerView.animate().alpha(1f).setDuration(shortAnimTime).setListener(null);
        }
    }


    private int getScreenHeight(Activity activity) {
        if (activity == null) {
            return 0;
        }
        Display display = activity.getWindowManager().getDefaultDisplay();
        int realHeight = 0;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            final DisplayMetrics metrics = new DisplayMetrics();
            display.getRealMetrics(metrics);
            realHeight = metrics.heightPixels;
        } else {
            try {
                Method mGetRawH = Display.class.getMethod("getRawHeight");
                realHeight = (Integer) mGetRawH.invoke(display);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return realHeight;
    }


    private int getScreenWidth(Activity activity) {
        if (activity == null) {
            return 0;
        }
        Display display = activity.getWindowManager().getDefaultDisplay();
        int realWidth = 0;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            final DisplayMetrics metrics = new DisplayMetrics();
            display.getRealMetrics(metrics);
            realWidth = metrics.widthPixels;
        } else {
            try {
                Method mGetRawW = Display.class.getMethod("getRawWidth");
                realWidth = (Integer) mGetRawW.invoke(display);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return realWidth;
    }


    // ---------------- Internal Adapter ----------------

    private class InternalAdapter extends RecyclerView.Adapter<InternalAdapter.Holder> {
        private String selectedCode = null;

        public void setSelectedCode(String code) {
            this.selectedCode = code;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_address_text, parent, false);
            return new Holder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull Holder holder, int position) {
            AddressItem item = currentList.get(position);
            holder.tvName.setText(item.name);

            boolean isSelected = selectedCode != null && selectedCode.equals(item.code);

            if (isSelected) {
                // 使用配置的选中颜色
                holder.tvName.setTextColor(selectedColor);
                holder.tvCheck.setVisibility(View.VISIBLE);
                // 如果 tvCheck 是文字(√)，也可以设置颜色：
                if (holder.tvCheck instanceof TextView) {
                    ((TextView) holder.tvCheck).setTextColor(selectedColor);
                }
            } else {
                // 使用配置的未选中颜色
                holder.tvName.setTextColor(unSelectedColor);
                holder.tvCheck.setVisibility(View.GONE);
            }

            holder.itemView.setOnClickListener(v -> onListItemClick(item));
        }

        @Override
        public int getItemCount() {
            return currentList == null ? 0 : currentList.size();
        }

        class Holder extends RecyclerView.ViewHolder {
            TextView tvName;
            View tvCheck;

            public Holder(@NonNull View itemView) {
                super(itemView);
                tvName = itemView.findViewById(R.id.tv_item_name);
                tvCheck = itemView.findViewById(R.id.tv_item_check);
            }
        }
    }
}