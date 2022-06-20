English| [简体中文](./README.md)

## Directory Structure

This directory contains the demo source code of the Player SDK for Android, which shows you how to call the APIs to implement basic features.

```
├─ LiteAVDemo(Player) // Superplayer demo, providing features such as audio/video playback, background music playback, playback speed change, live streaming, VOD instant streaming, and seamless video quality change
|  ├─ app                   // Program entry UI
|  ├─ superplayerkit        // Superplayer component
|  ├─ superplayerdemo       // Superplayer demo
|  ├─ common                // Common tool class
|
├─ SDK 
│  ├─README.md     // Download URL of the Player SDK for Android
```

## Upgrade Notes

Player SDKs for Android, iOS, and Flutter 10.1 or later are developed based on the same playback kernel of Tencent Video with fully optimized and upgraded video playback capabilities.

In addition, those SDKs require license verification for the video playback feature module. **If your app has already been granted the live push or UGSV license, you can still use the license after upgrading the SDK to 10.1 or later.** The license won't be affected by the upgrade. You can log in to the [RT-Cube console](https://console.cloud.tencent.com/vcube) to view the current license information.

If you don't have the necessary license and **need to use the live playback or VOD playback feature in the Player SDK 10.1 or later, you need to purchase the license.** For more information, see [here](https://cloud.tencent.com/document/product/881/74199#.E6.8E.88.E6.9D.83.E8.AF.B4.E6.98.8E). If you don't need to use those features or haven't upgraded the SDK to the latest version, you won't be affected by this change.

## SDK Editions and Download

The Player SDK is designed and implemented based on the TXLiteAVSDK framework, which contains multiple features such as live streaming, VOD, UGSV, RTC, and AI beauty filters.

- If you want the smallest incremental size, you can download the Player Edition: [TXLiteAVSDK_Player.zip](https://cloud.tencent.com/document/product/881/20205).
- If you want to use multiple features but don't want to package multiple SDKs, you can download the Professional Edition: [TXLiteAVSDK_Professional.zip](https://cloud.tencent.com/document/product/647/32689#Professional).
- If you have already purchased the AI beauty filter license from Tencent Cloud, you can download the Enterprise Edition: [TXLiteAVSDK_Enterprise.zip](https://cloud.tencent.com/document/product/647/32689#Enterprise).

## Integration Guide and Documentation

- [Player SDK integration guide](https://cloud.tencent.com/document/product/881/20216#sdk.E9.9B.86.E6.88.90)
- [Player SDK capability list](https://cloud.tencent.com/document/product/881/61375)
- [FAQs about mobile playback](https://cloud.tencent.com/document/product/881/73976)
- [Descriptions and use instructions of common Player SDK features](https://cloud.tencent.com/document/product/881/20216#.E5.8A.9F.E8.83.BD.E4.BD.BF.E7.94.A8). In addition to the Player SDK, the [Superplayer component](https://cloud.tencent.com/document/product/266/7938) is provided, which integrates quality monitoring, video encryption, TESHD, video quality change, and small window playback. It is suitable for all VOD and live playback scenarios. It encapsulates complete features and provides upper-layer UIs to help you quickly create your own playback service comparable with popular video apps.
- [SDK API documentation](https://cloud.tencent.com/document/product/881/67113)
- [SDK release notes](https://cloud.tencent.com/document/product/881/62169)
- [SDK demo app](https://cloud.tencent.com/document/product/881/20204)

