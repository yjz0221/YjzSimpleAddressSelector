package com.github.yjz.address.provider;


import com.github.yjz.address.model.AddressItem;

import java.util.List;


/**
 * 作者:yjz
 * 创建日期：2025/11/21
 * 描述: 数据提供者接口
 */
public interface AddressProvider {
    /**
     * 提供数据的方法
     * @param parentItem 上一级选中的节点。如果为 null，表示请求第0级（省）数据
     * @param callback 数据加载完成后的回调
     */
    void provideData(AddressItem parentItem, DataCallback callback);

    interface DataCallback {
        void onSuccess(List<AddressItem> data); // 成功加载数据
        void onFailure(String errorMsg);        // 加载失败
    }
}