## Keep public classes and methods for API usage
#-keep public class com.example.mylibrary.** {
#    public *;
#}
#
## Keep class members for all public classes
#-keepclassmembers public class com.example.mylibrary.** {
#    public *;
#}
#
## Obfuscate everything else
#-dontwarn **
#
## Optionally, keep debug information
#-keepattributes SourceFile,LineNumberTable
#
## Enable obfuscation and optimization
#-optimizationpasses 5
#-dontusemixedcaseclassnames
#-dontpreverify
#-verbose
#-optimizations !code/simplification/arithmetic,!field/*,!class/merging/*
# Keep all other classes without obfuscation


# Keep everything by default
-keep class * { *; }
-keep interface * { *; }

# Allow obfuscation only for com.lymors.lycommons.data.database.MainRepositoryImpl
-keep,allowobfuscation class com.lymors.lycommons.data.database.MainRepositoryImpl { *; }

