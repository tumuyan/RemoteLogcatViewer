# RemoteLogcatViewer
[![](https://jitpack.io/v/tumuyan/RemoteLogcatViewer.svg)](https://jitpack.io/#tumuyan/RemoteLogcatViewer)
在浏览器上远程查看logcat日志，或保存logcat日志到外置存储。
Fork自[8enet/RemoteLogcatViewer](https://github.com/8enet/RemoteLogcatViewer)
，并没有大的修改，主要是为方便自用做了微调，然后加了依赖链接。

## 用法
引入依赖
```gradle
compile project(':remotelogcat')
```
或者
```gradle
//Add it in your root build.gradle at the end of repositories:
allprojects {
    repositories {
        ...
        maven { url 'https://jitpack.io' }
    }
}
//Add the dependency
dependencies {
        implementation 'com.github.tumuyan:RemoteLogcatViewer:Tag'
}
```

```java
//start
LogcatRunner.getInstance()
        .config(LogcatRunner.LogConfig.builder()
                .setWsCanReceiveMsg(false)
                .setLogFilePrefix("tag")
                .write2File(true))
        .with(getApplicationContext())
        .start();
...
//获取ws链接地址
LogcatRunner.getWebSocketLink();
...
//stop
LogcatRunner.Stop();
```



然后打开任何一个websocket客户端都可以连接你的手机。比如安装Firefox扩展：
https://addons.mozilla.org/en-US/firefox/addon/simple-websocket-client/?src=search 并打输入对应局域网ip和端口`ws://ip:port/logcat` (注:logcat别名可以修改)。

> 因为一些安全原因,chrome禁止了部分不安全的请求地址`ws`,可以`允许加载不安全脚本`继续使用或者下载[index.html](https://raw.githubusercontent.com/tumuyan/RemoteLogcatViewer/master/index.html) 文件到本地并打开。
<img src="images/web_ui_1.jpg" width="40%" height="40%" />

如果不希望修改现有项目，可以新建一个其他的项目依赖本库，然后通过配置相同的 `android:sharedUserId=""` 和签名相同，
可以在新app运行时中读取所有sharedUserId相同的 log。

## 实现原理
原理非常简单，在内部使用`Runtime.getRuntime().exec("logcat");` 执行命令去获取logcat输出流，然后逐行读取后通过websocket输出要远端，为了尽可能节省性能，只会维护一个客户端输出。   
注意只能输出自己包下的log日志，当然相同sharedUserId和签名的也可以，多进程情况下建议在常驻后台的Service中启动本监听。

## 作用
某些Android设备没有调试接口，比如电视或者各种盒子终端，没法连接usb调试当然也不能查看logcat日志了，这个项目是在浏览器上远程显示和保存logcat输出，帮助调试开发使用。

## 功能
目前可以完整的查看、过滤、保存logcat信息。
支持日志文件写入、下载。  
后期会加入shell支持。

## License
Apache License 2.0
