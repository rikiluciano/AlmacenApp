# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in sdk/tools/proguard/proguard-android.txt

# Keep Room entities
-keep class com.cartones.almacen.data.** { *; }

# Keep Coil
-keep class coil.** { *; }
