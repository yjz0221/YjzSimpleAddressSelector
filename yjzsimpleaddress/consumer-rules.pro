# ----------------------------------------------------------------------------
# AddressSelector Library Consumer Rules
# 这些规则会自动应用到依赖此库的 App 项目中
# ----------------------------------------------------------------------------

# 1. 保持数据模型类不被混淆
# 这一步至关重要！防止用户使用 Gson/FastJson 解析数据时因字段重命名导致解析失败
-keep class com.github.yjz.address.model.AddressItem {
    <fields>;
    <methods>;
}

# 2. 保持对外暴露的接口不被混淆
# 确保回调和数据提供者的接口名在堆栈跟踪中清晰可见
-keep interface com.github.yjz.address.provider.AddressProvider
-keep interface com.github.yjz.address.provider.AddressProvider$DataCallback
-keep interface com.github.yjz.address.listener.OnAddressSelectedListener

# 3. 保持核心组件的公共方法（可选，通常 R8 会根据使用情况自动保留，但加上更稳妥）
-keep class com.github.yjz.address.AddressSelector {
    public <methods>;
}

# 4. 如果你的库中有用到 XML 布局引用的 View 自定义属性，通常也建议保留
# (本库使用的是原生 View，暂无自定义 View 类，所以忽略)

# 5. 消除因库内部使用反射调用系统 API (Display.getRawHeight) 可能产生的警告
-dontwarn android.view.Display