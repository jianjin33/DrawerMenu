#### 依赖
1. 项目级build.gradle
```
allprojects {
	repositories {
		...
		maven { url 'https://jitpack.io' }
	}
}
```
2. dependency
```
dependencies {
	compile 'com.github.jianjin33:DrawerMenu:-SNAPSHOT'
}
```
#### 使用
自定义DrawerMenuView各个tab布局，和RecyclerView使用方法类似，自定义Adapter继承DrawerBaseAdapter,通过setAdatper设置给DrawerMenuView。

