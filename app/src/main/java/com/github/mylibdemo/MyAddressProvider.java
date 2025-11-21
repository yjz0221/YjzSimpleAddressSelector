package com.github.mylibdemo;

import com.github.yjz.address.model.AddressItem;
import com.github.yjz.address.provider.AddressProvider;

import java.util.ArrayList;
import java.util.List;

/**
 * 作者:yjz
 * 创建日期：2025/11/21
 * 描述: 模拟提供地址数据
 */
public class MyAddressProvider implements AddressProvider {
    @Override
    public void provideData(AddressItem parentItem, DataCallback callback) {
        // 模拟网络请求，实际请使用 Retrofit
        new Thread(() -> {
            try {
                Thread.sleep(300); // 模拟延迟

                List<AddressItem> result = new ArrayList<>();

                if (parentItem == null) {
                    // parent为空 -> 请求省份
                    result.add(new AddressItem("浙江省", "330000"));
                    result.add(new AddressItem("江苏省", "320000"));
                } else if (parentItem.code.equals("330000")) {
                    // 浙江省 -> 请求市
                    result.add(new AddressItem("杭州市", "330100"));
                    result.add(new AddressItem("宁波市", "330200"));
                } else if (parentItem.code.equals("330100")) {
                    // 杭州市 -> 请求区
                    result.add(new AddressItem("西湖区", "330106"));
                } else if (parentItem.code.equals("330106")) {
                    // 西湖区 -> 请求街道
                    result.add(new AddressItem("北山街道", "330106001"));
                    result.add(new AddressItem("西溪街道", "330106002"));
                }
                // 如果 result 为空，库会自动判断为结束

                // 切回主线程回调
                new android.os.Handler(android.os.Looper.getMainLooper()).post(() -> {
                    callback.onSuccess(result);
                });

            } catch (Exception e) {
                callback.onFailure("网络错误");
            }
        }).start();
    }
}
