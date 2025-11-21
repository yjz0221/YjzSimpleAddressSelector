package com.github.yjz.address.listener;

import com.github.yjz.address.model.AddressItem;

import java.util.List;


/**
 * 作者:yjz
 * 创建日期：2025/11/21
 * 描述: 结果回调接口
 */
public interface OnAddressSelectedListener {
    /**
     * 最终选择完成的回调（所有层级选完，或者没有下一级了）
     * @param selectItems 完整的路径
     */
    default void onAddressSelected(List<AddressItem> selectItems){}

    /**
     * 每一级选择后的回调
     * @param item 当前选中的对象
     * @param level 当前层级 (0=省, 1=市, 2=区...)
     */
    default void onItemSelect(AddressItem item, int level){}
}
