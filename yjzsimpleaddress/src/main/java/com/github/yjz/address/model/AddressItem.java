package com.github.yjz.address.model;

/**
 * 作者:yjz
 * 创建日期：2025/11/21
 * 描述: 通用的地址数据对象
 */
public class AddressItem {
   public String name;   // 显示名称
   public String code;   // 行政编码
   public Object extra;  //以此扩展其他数据

   public AddressItem(String name, String code) {
      this.name = name;
      this.code = code;
   }
}
