# 省市县区地址选择UI库

[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)
[![Platform](https://img.shields.io/badge/platform-Android-green.svg)](https://developer.android.com)
[![Language](https://img.shields.io/badge/language-Java-orange.svg)](https://www.java.com)

**YjzSimpleAddress** 是一个可定制的 Android 地址选择器库，Tab 点击式的地区选择库。

它采用 **UI 与数据解耦** 的设计理念，**不内置任何行政区划数据**。你可以通过实现接口，轻松对接自己的后端 API、本地 JSON 文件或数据库。

## ✨ 核心特性

* **多级联动**：支持省、市、区、街道（3级或4级，可配置）。
* **数据解耦**：通过 `AddressProvider` 接口自定义数据来源（网络/本地）。
* **高度自定义**：
    * 支持自定义标题、Tab 提示文案、空数据提示文案（支持国际化）。
    * 支持自定义选中颜色、未选中颜色、加载条颜色。
    * 支持自定义弹窗宽高（适配平板/手机）。
* **兼容性**：支持 Android 4.3+ (API 18+)。

## 📸 截图示例

| 默认样式 | 空数据状态 | 搜索状态 |
|:----:|:---:|:---:|
| <img src="https://github.com/yjz0221/YjzSimpleAddressSelector/blob/main/%E9%80%89%E5%8F%96%E5%AE%8C%E6%88%90.png" style="zoom:25%;" /> | <img src="https://github.com/yjz0221/YjzSimpleAddressSelector/blob/main/%E7%A9%BA%E6%95%B0%E6%8D%AE.png" style="zoom:25%;" /> | <img src="https://github.com/yjz0221/YjzSimpleAddressSelector/blob/main/%E6%90%9C%E7%B4%A2.png" style="zoom:25%;" /> |



## 📦 引入依赖



1. 在项目根目录的 `build.gradle` 中添加 JitPack 仓库：

```groovy
allprojects {
    repositories {
        ...
        maven { url '[https://jitpack.io](https://jitpack.io)' }
    }
}

```


2. 在 Module 的 `build.gradle` 中添加依赖：

```groovy
   dependencies {
      implementation 'com.github.yjz0221:YjzSimpleAddressSelector:2.0.0'
   }
```



## 🚀 快速开始



### 1. 实现数据提供者 (`AddressProvider`)

你需要实现 `AddressProvider` 接口，告诉选择器如何获取数据。这里以模拟网络请求为例：

```java
public class MyAddressProvider implements AddressProvider {
    @Override
    public void provideData(AddressItem parentItem, DataCallback callback) {
        // 模拟网络延迟，实际开发请使用 Retrofit/OkHttp
        new Thread(() -> {
            List<AddressItem> list = new ArrayList<>();
            
            // parentItem 为 null 表示获取第一级（省）
            if (parentItem == null) {
                list.add(new AddressItem("浙江省", "330000"));
                list.add(new AddressItem("江苏省", "320000"));
            } else {
                // 根据 parentItem.code 获取下级数据
                // list = api.getCityList(parentItem.code)...
            }

            // 必须在主线程回调
            new Handler(Looper.getMainLooper()).post(() -> {
                if (list.isEmpty()) {
                    // 即使没数据也要回调，库会自动处理空状态
                    callback.onSuccess(null); 
                } else {
                    callback.onSuccess(list);
                }
                // callback.onFailure("网络错误");
            });
        }).start();
    }
}
```



### 2. 显示选择器

```java
AddressSelector.newInstance(new MyAddressProvider(), new OnAddressSelectedListener() {
    @Override
    public void onAddressSelected(List<AddressItem> selectItems) {
        // 所有层级选择完毕的回调
        StringBuilder sb = new StringBuilder();
        for (AddressItem item : selectItems) {
            sb.append(item.name).append(" ");
        }
        textView.setText(sb.toString());
    }

    @Override
    public void onItemSelect(AddressItem item, int level) {
        // 每一级选中的回调（可选）
    }
}).show(getSupportFragmentManager());
```



## 🎨 自定义

该库支持链式调用，满足 UI 需求。

```java
// 构造默认选中数据
List<AddressItem> history = new ArrayList<>();
        history.add(new AddressItem("浙江省", "330000"));
        history.add(new AddressItem("杭州市", "330100"));
        history.add(new AddressItem("西湖区", "330106"));
        
AddressSelector.newInstance(provider, listener)
    // --- 基础设置 ---
    .setTitle("请选择收货地址")          // 设置标题
    .setTabHint("请选择")              // 设置 Tab 未选中时的提示文字
    .setEmptyHint("暂无下级数据")       // 设置数据为空时的提示文字
    .setMaxLevel(4)                   // 设置层级 (3级或4级)
    // --- 颜色设置 ---
    .setSelectedColor(Color.parseColor("#F44336"))   // 选中颜色 (京东红)
    .setUnSelectedColor(Color.parseColor("#333333")) // 未选中颜色
    .setProgressBarColor(Color.BLACK)                // 加载条颜色
    // --- 尺寸设置 (适配平板) ---
    .setWidth(ViewGroup.LayoutParams.MATCH_PARENT)   // 设置弹窗宽度
    .setHeight(ViewGroup.LayoutParams.WRAP_CONTENT)  // 设置弹窗高度
    // --- 交互设置 ---
    .setCanceledOnTouchOutside(true)  // 点击外部是否关闭
    .setSearchOpen(true)    // 显示搜索控件
    .setDefaultSelection(history) // 传入默认数据
    .show(getSupportFragmentManager());
```



## 🛠 API 说明



### AddressItem

数据实体类。

- `name`: 显示的名称 (String)
- `code`: 唯一标识/行政编码 (String)
- `extra`: 扩展字段 (Object)



### AddressSelector 方法概览

| **方法名**                           | **说明**       | **默认值**     |
| ------------------------------------ | -------------- | -------------- |
| `setTitle(String)`                   | 弹窗标题       | "所在地区"     |
| `setTabHint(String)`                 | Tab 待选提示词 | "请选择"       |
| `setEmptyHint(String)`               | 无下级数据提示 | "暂无下级数据" |
| `setMaxLevel(int)`                   | 最大层级数     | 4              |
| `setSelectedColor(int)`              | 选中状态颜色   | #4CAF50 (绿色) |
| `setUnSelectedColor(int)`            | 未选中状态颜色 | #333333        |
| `setProgressBarColor(int)`           | Loading 颜色   | 跟随选中色     |
| `setWidth(int)`                      | 弹窗宽度 (px)  | MATCH_PARENT   |
| `setHeight(int)`                     | 弹窗高度 (px)  | WRAP_CONTENT   |
| `setCanceledOnTouchOutside(boolean)` | 点击外部关闭   | true           |
|                                      |                |                |



## 📄 License

```
Copyright [2025] [Your Name]

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    [http://www.apache.org/licenses/LICENSE-2.0](http://www.apache.org/licenses/LICENSE-2.0)

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
