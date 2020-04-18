-keep class kotlin.Metadata { *; }
-keep class kotlin.reflect.jvm.internal.** { *; }

-keepclassmembers class * {
    @at.sunilson.unidirectionalviewmodel.Persist public *;
}

-keepclassmembers class * {
    @at.sunilson.unidirectionalviewmodel.PersistedState public *;
}

-keepclassmembernames class * {
                          @at.sunilson.unidirectionalviewmodel.Persist public *;
                     }

-keepclassmembernames class * {
                          @at.sunilson.unidirectionalviewmodel.PersistedState public *;
                     }

-keep class * {
    @at.sunilson.unidirectionalviewmodel.PersistedState public *;
}

-keep class * {
    @at.sunilson.unidirectionalviewmodel.Persist public *;
}

-keep @at.sunilson.unidirectionalviewmodel.Persist class *
-keep @at.sunilson.unidirectionalviewmodel.PersistedState class *

-keep @at.sunilson.unidirectionalviewmodel.PersistedState class *
-keep @at.sunilson.unidirectionalviewmodel.Persist public class *

-keepclassmembernames @at.sunilson.unidirectionalviewmodel.Persist public class *
-keepclassmembers @at.sunilson.unidirectionalviewmodel.Persist public class *

-keepclassmembernames @at.sunilson.unidirectionalviewmodel.Persist class *
-keepclassmembers @at.sunilson.unidirectionalviewmodel.Persist class *

-keepclassmembernames @at.sunilson.unidirectionalviewmodel.PersistedState public class *
-keepclassmembers @at.sunilson.unidirectionalviewmodel.PersistedState public class *

-keepclassmembernames @at.sunilson.unidirectionalviewmodel.PersistedState class *
-keepclassmembers @at.sunilson.unidirectionalviewmodel.PersistedState class *

-keepattributes RuntimeVisibleAnnotations, RuntimeVisibleParameterAnnotations,  *Annotation*, Signature, InnerClasses, EnclosingMethod