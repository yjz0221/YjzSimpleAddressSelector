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
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
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
    private EditText etSearch;
    private View searchContainer;

    private AddressProvider provider;
    private OnAddressSelectedListener listener;

    private InternalAdapter adapter;

    private List<AddressItem> selectedPath = new ArrayList<>();
    private List<AddressItem> currentList = new ArrayList<>();
    // 保存当前层级的完整数据源 (用于搜索过滤)
    private List<AddressItem> sourceList = new ArrayList<>();

    private int maxLevel = 4;

    // --- 自定义配置项 (默认值) ---
    private String titleText = "所在地区";
    private String tabHintText = "请选择";
    private String emptyText = "暂无下级数据";
    private boolean isSearchOpen = false; // 是否开启搜索
    // 默认选中的路径 (用于回显)
    private List<AddressItem> defaultPath;


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
     * 是否开启搜索功能
     */
    public AddressSelector setSearchOpen(boolean open) {
        this.isSearchOpen = open;
        return this;
    }

    /**
     * 设置默认选中的地址路径 (用于回显)
     * 注意：列表中的 AddressItem 必须包含 name 和 code，且顺序必须是 省->市->区...
     */
    public AddressSelector setDefaultSelection(List<AddressItem> path) {
        this.defaultPath = path;
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
        searchContainer = view.findViewById(R.id.lib_fl_search_container);
        etSearch = view.findViewById(R.id.lib_et_search);


        // 1. 应用自定义标题
        tvTitle.setText(titleText);

        // 根据配置显示/隐藏搜索框
        searchContainer.setVisibility(isSearchOpen ? View.VISIBLE : View.GONE);
        // 监听输入框变化
        etSearch.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterList(s.toString());
            }

            @Override
            public void afterTextChanged(android.text.Editable s) {}
        });

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

        // 判断是否有默认数据
        if (defaultPath != null && !defaultPath.isEmpty()) {

            // 如果 maxLevel 为 0，直接显示提示，不进行后续的数据加载
            if (maxLevel <= 0) {
                tvEmpty.setVisibility(View.VISIBLE);
                tvEmpty.setText(getString(R.string.address_max_level_error, maxLevel));
                return;
            }


            // 1. 根据 maxLevel (数量) 截取数据
            // 语义明确：maxLevel 为 3，就最多只取前 3 个默认数据
            int targetSize = Math.min(defaultPath.size(), maxLevel);
            for (int i = 0; i < targetSize; i++) {
                this.selectedPath.add(defaultPath.get(i));
            }

            // 2. 恢复 Tab 显示
            for (AddressItem item : this.selectedPath) {
                addTab(item.name);
            }

            // 3. 决定下一步加载什么数据
            // 如果当前选中的数量还没达到 maxLevel (数量)，说明还可以继续选下一级
            if (this.selectedPath.size() < maxLevel) {
                // 场景 A: 还没选满 (例如 maxLevel=3, 但只传了 2 个默认数据)
                // 动作: 加一个 "请选择" Tab，并加载下一级
                addTab(tabHintText);
                loadData(this.selectedPath.get(this.selectedPath.size() - 1));
            } else {
                // 场景 B: 已经选满了 (selectedPath.size() == maxLevel)
                // 动作: 停留在最后一级，加载该级对应的列表，以便用户修改当前级
                // 逻辑：要加载第 N 级的数据，需要传入第 N-1 级的对象作为 parent
                AddressItem parent = null;
                if (this.selectedPath.size() > 1) {
                    // 比如有 [省, 市]，size=2。要加载“市”列表，parent 应该是“省”(index 0)
                    // index = size - 2
                    parent = this.selectedPath.get(this.selectedPath.size() - 2);
                } else {
                    // 比如只有 [省]，size=1。要加载“省”列表，parent 是 null
                    parent = null;
                }
                loadData(parent);
            }
        } else {
            // --- 无默认数据的情况 ---
            // 只有当允许显示的层级大于 0 时才加载
            if (maxLevel > 0) {
                addTab(tabHintText);
                loadData(null);
            } else {
                // maxLevel 为 0，理论上不显示任何数据
                // 可以显示个空提示，或者干脆什么都不做
                tvEmpty.setVisibility(View.VISIBLE);
                tvEmpty.setText(getString(R.string.address_max_level_error,maxLevel));
            }
        }

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
            // 强制让软键盘默认不弹起
            dialog.getWindow().setSoftInputMode(
                    android.view.WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN |
                            android.view.WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN
            );
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
     * 搜索过滤逻辑
     */
    private void filterList(String keyword) {
        if (sourceList == null || sourceList.isEmpty()) {
            return;
        }

        currentList.clear();

        if (android.text.TextUtils.isEmpty(keyword)) {
            // 关键字为空，显示所有数据
            currentList.addAll(sourceList);
        } else {
            // 关键字不为空，遍历查找
            for (AddressItem item : sourceList) {
                if (item.name != null && item.name.contains(keyword)) {
                    currentList.add(item);
                }
            }
        }
        // 刷新列表
        adapter.notifyDataSetChanged();
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
        if (provider == null) {
            return;
        }
        setLoading(true);

        // 切换层级时，必须清空搜索框，否则会带着上一次的搜索词去搜新数据
        if (etSearch != null) {
            // 临时移除监听器防止触发 filterList 导致 crash 或逻辑错误
            // 这里简单处理：直接设为空字符串，filterList 会自动还原 currentList
            etSearch.setText("");
        }

        provider.provideData(parent, new AddressProvider.DataCallback() {
            @Override
            public void onSuccess(List<AddressItem> data) {
                if (!isAdded()) {
                    return;
                }
                setLoading(false);

                //  保存原始数据到 sourceList
                sourceList = (data == null) ? new ArrayList<>() : data;
                // 初始化显示数据 (深拷贝一份，或者直接 addAll)
                currentList = new ArrayList<>(sourceList);
                adapter.notifyDataSetChanged();

                if (currentList.isEmpty()) {
                    // 数据加载成功，但是为空
                    recyclerView.setVisibility(View.GONE);
                    tvEmpty.setVisibility(View.VISIBLE);
                    tvEmpty.setText(emptyText);
                } else {
                    // 有数据
                    tvEmpty.setVisibility(View.GONE);
                    recyclerView.setVisibility(View.VISIBLE);

                    AddressItem itemToHighlight = null;
                    int currentTabPosition = tabLayout.getSelectedTabPosition();
                    if (currentTabPosition >= 0 && currentTabPosition < selectedPath.size()) {
                        itemToHighlight = selectedPath.get(currentTabPosition);
                    }
                    adapter.setSelectedItem(itemToHighlight);
                }
            }

            @Override
            public void onFailure(String errorMsg) {
                if (!isAdded()) return;
                setLoading(false);

                // ---错误信息不再 Toast，而是显示在空布局上 ---
                recyclerView.setVisibility(View.GONE);
                tvEmpty.setVisibility(View.VISIBLE);
                tvEmpty.setText(errorMsg);
            }
        });
    }

    private void onListItemClick(AddressItem item) {
        int currentTabPos = tabLayout.getSelectedTabPosition();

        // 防止 maxLevel=0 或 Tab 初始化失败时，点击列表导致数组越界
        if (currentTabPos != -1 && currentTabPos < selectedPath.size()) {
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

        // 选中某项后，关闭软键盘
        if (isSearchOpen && etSearch != null) {
            InputMethodManager imm = (InputMethodManager) requireContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.hideSoftInputFromWindow(etSearch.getWindowToken(), 0);
            }
            etSearch.clearFocus(); // 失去焦点
        }


        updateCurrentTab(item.name);
        selectedPath.add(item);
        adapter.setSelectedItem(item);

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
        private AddressItem selectedItem = null;

        public void setSelectedItem(AddressItem item) {
            this.selectedItem = item;
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

            // 同时比较 code 和 name，防止因 code 重复导致的高亮错误
            boolean isSelected = false;
            if (selectedItem != null) {
                boolean codeSame = (selectedItem.code == null && item.code == null) ||
                        (selectedItem.code != null && selectedItem.code.equals(item.code));
                boolean nameSame = (selectedItem.name == null && item.name == null) ||
                        (selectedItem.name != null && selectedItem.name.equals(item.name));

                isSelected = codeSame && nameSame;
            }

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