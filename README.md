BungeeCord with built in AntiBot protection. (English lang)
==========

Videos
--------
Captcha+Falling check:
[![Only captcha](https://i.ytimg.com/vi/S27EbttIG-8/1.jpg)](https://youtu.be/S27EbttIG-8)
Falling check:
[![Only captcha](https://i.ytimg.com/vi/23O16oJyvl8/1.jpg)](https://youtu.be/23O16oJyvl8)

Download
--------
You can download this protection at [Releases](https://github.com/DionaMC/BungeeCord-BotFilter-ENG/releases/)

Credit
--------
[Leymooo](https://github.com/Leymooo) (Original BotFilter developer)<br>
[koloslolya](https://github.com/SleepyKolosLolya) (Help me translate BotFilter)<br>
[Maxsimuss](https://github.com/Maxsimuss) (Help me translate BotFilter)

Troubleshooting
--------
Q: Why I get an error when I start the server like this:
```
java.lang.reflect.InaccessibleObjectException: Unable to make private native java.lang.reflect.Field[] java.lang.Class.getDeclaredFields0(boolean) accessible: module java.base does not "opens java.lang" to unnamed module
```

A: You need to add this to your JVM arguments:
```
--add-opens java.base/java.lang=ALL-UNNAMED --add-opens java.base/java.lang.reflect=ALL-UNNAMED --add-opens java.base/java.lang.invoke=ALL-UNNAMED
```
