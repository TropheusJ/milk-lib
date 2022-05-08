# Milk lib
Literally just adds milk, to act as a bridge for any other mods that want to do the same.
See the `Milk` class for customisation; It allows for enabling of milk cauldrons and bottles.
Just call the various methods in your mod initializer.
## Setup
```groovy
repositories {
    maven {
        url = "https://mvn.devos.one/snapshots/"
    }
}
```
```groovy
dependencies {
    modImplementation(include("io.github.tropheusj:milk-lib:0.3.2"))
}
```
