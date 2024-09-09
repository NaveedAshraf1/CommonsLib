## Keep all class names
#-keepnames class ** { *; }
#
## Keep all method names
#-keepclassmembers class ** {
#    public *;
#}
#
## Additional rules to handle common issues
#-keepattributes *Annotation*
#
## Keep specific packages if needed
#-keep class com.example.myapp.** { *; }
#
## Keep the Kotlin metadata
#-keepclassmembers class kotlin.Metadata { *; }
#
## Keep the Kotlin reflection implementation
#-keep class kotlin.reflect.** { *; }
#-keep class kotlinx.coroutines.** { *; }
#
## Keep other Kotlin classes
#-keepclassmembers class kotlin.** { *; }
#-keepclassmembers class kotlinx.** { *; }
#
## Keep everything in the kotlin package
#-keep class kotlin.** { *; }
