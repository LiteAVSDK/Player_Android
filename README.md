

简体中文| [English](./README-EN.md)

## 目录结构说明

本目录包含 Android 版 播放器(Player) SDK 的Demo 源代码，主要演示接口如何调用以及最基本的功能。

```
├─ LiteAVDemo(Player) // 超级播放器Demo，包括音视频播放，后台播放，变速播放，直播、点播秒开，清晰度无缝切换
|  ├─ app                   // 程序入口界面
|  ├─ superplayerkit        // 超级播放器组件
|  ├─ superplayerdemo       // 超级播放器 Demo
|  ├─ common                // common工具类
|
├─ SDK 
│  ├─README.md     // Android Player SDK 的下载地址
```

## **升级说明**

播放器 SDK 移动端10.1（Android & iOS & Flutter）开始 版本采用“腾讯视频”同款播放内核打造，视频播放能力获得全面优化升级。

同时从该版本开始将增加对“视频播放”功能模块的授权校验，**如果您的APP已经拥有直播推流 License 或者短视频 License 授权，当您升级至10.1 版本后仍可以继续正常使用，**不受到此次变更影响，您可以登录 [腾讯云视立方控制台](https://console.cloud.tencent.com/vcube) 查看您当前的 License 授权信息。

如果您在此之前从未获得过上述License授权**，且需要使用新版本SDK（10.1及其更高版本）中的直播播放或点播播放功能，则需购买指定 License 获得授权**，详情参见[授权说明](https://cloud.tencent.com/document/product/881/74199#.E6.8E.88.E6.9D.83.E8.AF.B4.E6.98.8E)；若您无需使用相关功能或未升级至最新版本SDK，将不受到此次变更的影响。

## SDK 分类和下载

腾讯云 Player SDK 基于 LiteAVSDK 统一框架设计和实现，该框架包含直播、点播、短视频、RTC、AI美颜在内的多项功能：

- 如果您追求最小化体积增量，可以下载 Player 版：[TXLiteAVSDK_Player.zip](https://cloud.tencent.com/document/product/881/20205)
- 如果您需要使用多个功能而不希望打包多个 SDK，可以下载专业版：[TXLiteAVSDK_Professional.zip](https://cloud.tencent.com/document/product/647/32689#Professional)
- 如果您已经通过腾讯云商务购买了 AI 美颜 License，可以下载企业版：[TXLiteAVSDK_Enterprise.zip](https://cloud.tencent.com/document/product/647/32689#Enterprise)

## 集成指引和文档链接

- [Player SDK 集成指引](https://cloud.tencent.com/document/product/881/20216#sdk.E9.9B.86.E6.88.90)
- [Player SDK 能力清单](https://cloud.tencent.com/document/product/881/61375)
- [常见移动端播放问题](https://cloud.tencent.com/document/product/881/73976)
- Player SDK 常见功能描述和使用说明，[请点击这里](https://cloud.tencent.com/document/product/881/20216#.E5.8A.9F.E8.83.BD.E4.BD.BF.E7.94.A8)。在Player SDK的基础上，我们提供了[超级播放器组件](https://cloud.tencent.com/document/product/266/7938)，集质量监控、视频加密、极速高清、清晰度切换、小窗播放等功能于一体，适用于所有点播、直播播放场景。封装了完整功能并提供上层 UI，可帮助您在短时间内，打造一个媲美市面上各种流行视频 App 的播放软件。
- [SDK 的 API 文档](https://cloud.tencent.com/document/product/881/67113)
- [SDK 的版本更新历史](https://cloud.tencent.com/document/product/881/62169)
- [SDK 的官方体验 App](https://cloud.tencent.com/document/product/881/20204)

