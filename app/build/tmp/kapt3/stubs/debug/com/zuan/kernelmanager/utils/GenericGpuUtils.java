package com.zuan.kernelmanager.utils;

@kotlin.Metadata(mv = {2, 2, 0}, k = 1, xi = 48, d1 = {"\u0000*\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0003\n\u0002\u0010\u000e\n\u0000\n\u0002\u0010 \n\u0002\b\u0003\n\u0002\u0010\u000b\n\u0002\b\t\n\u0002\u0010\u0002\n\u0002\b\u0007\b\u00c6\u0002\u0018\u00002\u00020\u0001B\t\b\u0002\u00a2\u0006\u0004\b\u0002\u0010\u0003J\u0010\u0010\b\u001a\u0004\u0018\u00010\u0005H\u0086@\u00a2\u0006\u0002\u0010\tJ\u0010\u0010\n\u001a\u00020\u000b2\u0006\u0010\f\u001a\u00020\u0005H\u0002J\u0016\u0010\r\u001a\u00020\u00052\u0006\u0010\f\u001a\u00020\u0005H\u0086@\u00a2\u0006\u0002\u0010\u000eJ\u0016\u0010\u000f\u001a\u00020\u00052\u0006\u0010\f\u001a\u00020\u0005H\u0086@\u00a2\u0006\u0002\u0010\u000eJ\u0016\u0010\u0010\u001a\u00020\u00052\u0006\u0010\f\u001a\u00020\u0005H\u0086@\u00a2\u0006\u0002\u0010\u000eJ\u0016\u0010\u0011\u001a\u00020\u00052\u0006\u0010\f\u001a\u00020\u0005H\u0086@\u00a2\u0006\u0002\u0010\u000eJ\u001c\u0010\u0012\u001a\b\u0012\u0004\u0012\u00020\u00050\u00072\u0006\u0010\f\u001a\u00020\u0005H\u0086@\u00a2\u0006\u0002\u0010\u000eJ\u001c\u0010\u0013\u001a\b\u0012\u0004\u0012\u00020\u00050\u00072\u0006\u0010\f\u001a\u00020\u0005H\u0086@\u00a2\u0006\u0002\u0010\u000eJ&\u0010\u0014\u001a\u00020\u00152\u0006\u0010\f\u001a\u00020\u00052\u0006\u0010\u0016\u001a\u00020\u00052\u0006\u0010\u0017\u001a\u00020\u0005H\u0086@\u00a2\u0006\u0002\u0010\u0018J\u001e\u0010\u0019\u001a\u00020\u00152\u0006\u0010\f\u001a\u00020\u00052\u0006\u0010\u001a\u001a\u00020\u0005H\u0086@\u00a2\u0006\u0002\u0010\u001bR\u0010\u0010\u0004\u001a\u0004\u0018\u00010\u0005X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0014\u0010\u0006\u001a\b\u0012\u0004\u0012\u00020\u00050\u0007X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u001c"}, d2 = {"Lcom/zuan/kernelmanager/utils/GenericGpuUtils;", "", "<init>", "()V", "cachedGpuPath", "", "POTENTIAL_PATHS", "", "getGpuPath", "(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "isValidDevfreq", "", "path", "getMinFreq", "(Ljava/lang/String;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "getMaxFreq", "getCurFreq", "getGov", "getAvailableFreqs", "getAvailableGovs", "setFreq", "", "type", "freqMHz", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "setGov", "gov", "(Ljava/lang/String;Ljava/lang/String;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "app_debug"})
public final class GenericGpuUtils {
    @org.jetbrains.annotations.Nullable()
    private static java.lang.String cachedGpuPath;
    @org.jetbrains.annotations.NotNull()
    private static final java.util.List<java.lang.String> POTENTIAL_PATHS = null;
    @org.jetbrains.annotations.NotNull()
    public static final com.zuan.kernelmanager.utils.GenericGpuUtils INSTANCE = null;
    
    private GenericGpuUtils() {
        super();
    }
    
    /**
     * Mencari path devfreq GPU yang valid.
     * Updated: Support Exynos Xclipse (*.sgpu) dan pencarian lebih luas.
     */
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object getGpuPath(@org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.lang.String> $completion) {
        return null;
    }
    
    private final boolean isValidDevfreq(java.lang.String path) {
        return false;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object getMinFreq(@org.jetbrains.annotations.NotNull()
    java.lang.String path, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.lang.String> $completion) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object getMaxFreq(@org.jetbrains.annotations.NotNull()
    java.lang.String path, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.lang.String> $completion) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object getCurFreq(@org.jetbrains.annotations.NotNull()
    java.lang.String path, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.lang.String> $completion) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object getGov(@org.jetbrains.annotations.NotNull()
    java.lang.String path, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.lang.String> $completion) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object getAvailableFreqs(@org.jetbrains.annotations.NotNull()
    java.lang.String path, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.util.List<java.lang.String>> $completion) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object getAvailableGovs(@org.jetbrains.annotations.NotNull()
    java.lang.String path, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.util.List<java.lang.String>> $completion) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object setFreq(@org.jetbrains.annotations.NotNull()
    java.lang.String path, @org.jetbrains.annotations.NotNull()
    java.lang.String type, @org.jetbrains.annotations.NotNull()
    java.lang.String freqMHz, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object setGov(@org.jetbrains.annotations.NotNull()
    java.lang.String path, @org.jetbrains.annotations.NotNull()
    java.lang.String gov, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
}