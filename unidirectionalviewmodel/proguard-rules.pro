-keep class kotlin.Metadata { *; }

-keepclassmembers class * {
    @at.sunilson.unidirectionalviewmodel.Persist *;
}

-keepclassmembers class * {
    @at.sunilson.unidirectionalviewmodel.PersistedState *;
}

-keepclassmembernames class * {
                          @at.sunilson.unidirectionalviewmodel.Persist *;
                     }

-keepclassmembernames class * {
                          @at.sunilson.unidirectionalviewmodel.PersistedState *;
                     }

-keep class * {
    @at.sunilson.unidirectionalviewmodel.PersistedState *;
}

-keep class * {
    @at.sunilson.unidirectionalviewmodel.Persist *;
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

-keepattributes RuntimeVisibleAnnotations, RuntimeVisibleParameterAnnotations
-keepattributes  *Annotation*, Signature, InnerClasses, EnclosingMethod