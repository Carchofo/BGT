# BGT proguard rules

# Keep line numbers for crash traces
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# Kotlin
-keepattributes *Annotation*, InnerClasses, Signature
-keepclassmembers class kotlin.Metadata { *; }
-dontwarn kotlin.**
-keepclassmembernames class kotlinx.** { volatile <fields>; }

# Kotlin coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembers class kotlinx.coroutines.** { volatile <fields>; }

# DataStore — keep preferences key names
-keepclassmembers class androidx.datastore.** { *; }
-keep class androidx.datastore.preferences.** { *; }

# Compose — runtime + animation reflection
-keep class androidx.compose.runtime.** { *; }
-dontwarn androidx.compose.**
-keep @androidx.compose.runtime.Stable class * { *; }
-keep @androidx.compose.runtime.Immutable class * { *; }

# Navigation
-keep class androidx.navigation.** { *; }

# BugReporter uses org.json (built into Android, no rules needed)
# Keep app data/model classes in case R8 inlines them unexpectedly
-keep class com.rafel.bgt.UpdateInfo { *; }
-keepclassmembers class com.rafel.bgt.** { public *; }

# Lifecycle ViewModel
-keep class * extends androidx.lifecycle.ViewModel { *; }
-keepclassmembers class * extends androidx.lifecycle.AndroidViewModel { <init>(android.app.Application); }
