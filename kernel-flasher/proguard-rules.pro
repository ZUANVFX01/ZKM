# ==========================================
# Kernel Flasher Module - ProGuard Rules
# ==========================================

# Keep ALL classes in kernel flasher package
-keep class com.github.capntrips.kernelflasher.** { *; }

# Keep AIDL Interface & Stub specifically
-keep interface com.github.capntrips.kernelflasher.IFilesystemService { *; }
-keep class com.github.capntrips.kernelflasher.IFilesystemService$Stub { *; }
-keep class com.github.capntrips.kernelflasher.IFilesystemService$Stub$Proxy { *; }

# Keep ViewModels (created via ViewModelProvider)
-keep class * extends androidx.lifecycle.ViewModel { 
    <init>(...);
    *; 
}

# Keep RootService subclass
-keep class * extends com.topjohnwu.superuser.ipc.RootService { *; }

# Keep Parcelable entities
-keep class * implements android.os.Parcelable { *; }

# Keep Serializable (if any)
-keep class * implements java.io.Serializable { *; }

# Keep Kotlin Metadata & Reflection
-keepattributes *Annotation*, Signature, Exception, InnerClasses, EnclosingMethod, RuntimeVisibleAnnotations, RuntimeInvisibleAnnotations, SourceFile, LineNumberTable
-keepclassmembers class **$WhenMappings { <fields>; }
-keepclassmembers class kotlin.Metadata { *; }

# Don't warn about internal dependencies
-dontwarn com.github.capntrips.kernelflasher.**
-dontwarn com.topjohnwu.superuser.**
