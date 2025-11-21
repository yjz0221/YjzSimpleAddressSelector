package com.github.mylibdemo;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.github.yjz.address.AddressSelector;
import com.github.yjz.address.listener.OnAddressSelectedListener;
import com.github.yjz.address.model.AddressItem;

import java.util.List;

@SuppressLint("MissingInflatedId")
public class MainActivity extends AppCompatActivity {


    private TextView tvAddress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tvAddress = findViewById(R.id.tvAddress);

        findViewById(R.id.btnAddress).setOnClickListener(v -> {
            showPicker();
        });
    }


    public void showPicker() {
        AddressSelector.newInstance(
                        new MyAddressProvider(), // 注入你的数据逻辑
                        new OnAddressSelectedListener() {
                            @Override
                            public void onAddressSelected(List<AddressItem> selectItems) {
                                // 处理结果
                                StringBuilder nameBuilder = new StringBuilder();
                                StringBuilder codeBuilder = new StringBuilder();

                                // 遍历选中的所有层级
                                for (AddressItem item : selectItems) {
                                    nameBuilder.append(item.name).append(" ");
                                    codeBuilder.append(item.code).append(" ");
                                }

                                // 显示
                                tvAddress.setText(nameBuilder.toString());

                                // 这里的 selectItems 列表里，
                                // index 0 = 省, index 1 = 市, index 2 = 区, index 3 = 街道

                            }

                            @Override
                            public void onItemSelect(AddressItem item, int level) {

                            }
                        }
                )
//                .setTitle("请选择收货地址")            // 设置标题
//                .setTabHint("请选择")              // 设置Tab提示语
//                .setEmptyHint("没有数据啦")        // 数据为空时的提示文字
//                .setSelectedColor(Color.BLUE)     // 设置选中颜色（Tab下划线、文字高亮）
//                .setUnSelectedColor(Color.GRAY)   // 设置未选中文字颜色
//                .setProgressBarColor(Color.BLACK) // 单独设置Loading颜色（不设则跟随选中色）
                .setMaxLevel(4)                   // 联动级数，设置只选到省市区/县街道
                .show(getSupportFragmentManager());
    }

}