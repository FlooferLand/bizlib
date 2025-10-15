# bizlib

Low-level library for the [Showbiz Minecraft mod](https://github.com/FlooferLand/showbiz)

Responsible for handling things like reading and parsing `rshw` files.

Old archived version: [link](https://github.com/FlooferLand/Showbiz-LowLevel/tree/b3b7e07cb17f6fc777756cc815970296405c7f4e)

### Usage

This library is mainly maintained for Showbiz Mod, however, with luck you can probably compile it yourself to a C library as it's written with Kotlin.

If you'd like to use it inside another Java/Kotlin JVM project, you can do so like this:

```kotlin
repositories {
    maven("https://www.jitpack.io") {
        name = "Jitpack"
    }
}

// Note that `main-SNAPSHOT` will update every time there is a commit
// There might be unexpected breaking changes,  but I will probably add stable releases from time to time
// so use those if they're available (see the GitHub releases section).
dependencies {
    implementation("com.flooferland:bizlib:main-SNAPSHOT")
}
```
