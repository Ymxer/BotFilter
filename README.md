BungeeCord, 但是内置反假人+反MOTD攻击
--------
BotFilter MotdLimit UltimateAntibot
==========

视频演示 BotFilter-MotdLimit
--------
验证码+重力检查:
[![Only captcha](https://i.ytimg.com/vi/S27EbttIG-8/1.jpg)](1)
重力检查:
[![Only captcha](https://i.ytimg.com/vi/23O16oJyvl8/1.jpg)](1)

特征
-------
* Fully customizable (config & messages)
* VPN system for detecting slow attacks.
* Hook to IPSet & IPTables for handling large attacks.
* A great variety of checks to detect as many bots as possible.
* An intelligent filter that activates only during attacks to avoid filtering important server errors.
* An easy verification system for non-bot players during attacks.
* An ID based blacklist system that will allow you to manage your blacklist more easily!
* Automatic notifications when there is an attack.
* Automatic attack logging system!
* An autopurger for blacklist, whitelist and attack logs.

下载
--------
你可以在[Releases](https://github.com/Loyisa/BungeeCord-BotFilter-ZHCN/releases/) 下载

特别鸣谢
--------
[Leymooo](https://github.com/Leymooo) (BotFilter 开发者)<br>
[koloslolya](https://github.com/SleepyKolosLolya) (帮助了我翻译 BotFilter (俄->英))<br>
[Maxsimuss](https://github.com/Maxsimuss) (帮助了我翻译 BotFilter (俄->英))<br>

疑难解答
--------
Q: 我在启动时遇到了这个错误:
```
java.lang.reflect.InaccessibleObjectException: Unable to make private native java.lang.reflect.Field[] java.lang.Class.getDeclaredFields0(boolean) accessible: module java.base does not "opens java.lang" to unnamed module
```

A: 你需要在启动参数中添加以下内容:
```
--add-opens java.base/java.lang=ALL-UNNAMED --add-opens java.base/java.lang.reflect=ALL-UNNAMED --add-opens java.base/java.lang.invoke=ALL-UNNAMED
```
