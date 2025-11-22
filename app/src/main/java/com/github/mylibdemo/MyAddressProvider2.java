package com.github.mylibdemo;

import com.github.yjz.address.model.AddressItem;
import com.github.yjz.address.provider.AddressProvider;

import java.util.ArrayList;
import java.util.List;

/**
 * 作者:yjz
 * 创建日期：2025/11/22
 * 描述: 模拟提供地址数据——适合多接口查询地址
 */
public class MyAddressProvider2 implements AddressProvider {

    private class LevelType {
        public static final int PROVINCE = 1; // 省
        public static final int CITY = 2;     // 市
        public static final int DISTRICT = 3; // 区/县
        public static final int STREET = 4;   // 街道
    }


    @Override
    public void provideData(AddressItem parentItem, DataCallback callback) {

//        // --- 情况 1: parent 为空，说明是初始状态，请求省份 ---
//        if (parentItem == null) {
//            // 调用 【GET 查询省份】 接口
//            api.getProvinces().subscribe(list -> {
//                // 【关键步骤】在解析/转换数据时，给生成的 item 标记层级
//                List<AddressItem> result = new ArrayList<>();
//                for (ProvinceBean p : list) {
//                    AddressItem item = new AddressItem(p.name, p.adcode);
//                    item.extra = LevelType.PROVINCE; // <--- 标记它是省
//                    result.add(item);
//                }
//                callback.onSuccess(result);
//            });
//            return;
//        }
//
//        // --- 情况 2: parent 不为空，检查它的 extra 标记 ---
//        int level = (int) parentItem.extra;
//
//        switch (level) {
//            case LevelType.PROVINCE:
//                // 标记是省 -> 说明要查询它的下级：【GET 查询市】
//                api.getCities(parentItem.code).subscribe(list -> {
//                    List<AddressItem> result = new ArrayList<>();
//                    for (CityBean c : list) {
//                        AddressItem item = new AddressItem(c.name, c.adcode);
//                        item.extra = LevelType.CITY; // <--- 标记它是市
//                        result.add(item);
//                    }
//                    callback.onSuccess(result);
//                });
//                break;
//
//            case LevelType.CITY:
//                // 标记是市 -> 说明要查询它的下级：【GET 查询区/县】
//                api.getDistricts(parentItem.code).subscribe(list -> {
//                    List<AddressItem> result = new ArrayList<>();
//                    for (DistrictBean d : list) {
//                        AddressItem item = new AddressItem(d.name, d.adcode);
//                        item.extra = LevelType.DISTRICT; // <--- 标记它是区
//                        result.add(item);
//                    }
//                    callback.onSuccess(result);
//                });
//                break;
//
//            case LevelType.DISTRICT:
//                // 标记是区 -> 说明要查询它的下级：【GET 查询街道/镇】
//                api.getStreets(parentItem.code).subscribe(list -> {
//                    List<AddressItem> result = new ArrayList<>();
//                    for (StreetBean s : list) {
//                        AddressItem item = new AddressItem(s.name, s.adcode);
//                        item.extra = LevelType.STREET; // <--- 标记它是街道 (如果是最后一级，可以不标)
//                        result.add(item);
//                    }
//                    callback.onSuccess(result);
//                });
//                break;
//
//            default:
//                // 未知层级或已经到底了
//                callback.onSuccess(new ArrayList<>());
//                break;
//        }
    }
}
