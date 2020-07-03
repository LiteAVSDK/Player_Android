## 目录结构说明

本目录包含 Android 版 超级播放器(SuperPlayer) SDK 的Demo 源代码，主要演示接口如何调用以及最基本的功能。

```
├─ Demo // 超级播放器Demo，包括音视频播放，后台播放，变速播放，直播、点播秒开，清晰度无缝切换
|  ├─ app                   // 程序入口界面
|  ├─ superplayerkit        // 超级播放器组件
|  ├─ superplayerdemo       // 超级播放器 Demo
|  
├─ SDK 
│  ├─ LiteAVSDK_Player_x.y.zzzz.aar        // 如果您下载的是 Player 专用 zip 包，解压后将出现此文件夹，其中 x.y.zzzz 表示 SDK 版本号 
```

## SDK 分类和下载

腾讯云 Player SDK 基于 LiteAVSDK 统一框架设计和实现，该框架包含直播、点播、短视频、RTC、AI美颜在内的多项功能：

- 如果您追求最小化体积增量，可以下载 Player 版：[TXLiteAVSDK_Player.zip](https://cloud.tencent.com/document/product/881/20205)
- 如果您需要使用多个功能而不希望打包多个 SDK，可以下载专业版：[TXLiteAVSDK_Professional.zip](https://cloud.tencent.com/document/product/647/32689#Professional)
- 如果您已经通过腾讯云商务购买了 AI 美颜 License，可以下载企业版：[TXLiteAVSDK_Enterprise.zip](https://cloud.tencent.com/document/product/647/32689#Enterprise)

## 相关文档链接

- [SDK 的版本更新历史](https://github.com/tencentyun/SuperPlayer_Android/releases)
- [SDK 的 API 文档](https://github.com/tencentyun/SuperPlayer_Android/wiki)
- [SDK 的官方体验 App](https://cloud.tencent.com/document/product/881/20204)
