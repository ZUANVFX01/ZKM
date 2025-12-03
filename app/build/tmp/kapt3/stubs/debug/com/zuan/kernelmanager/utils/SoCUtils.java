package com.zuan.kernelmanager.utils;

@kotlin.Metadata(mv = {2, 2, 0}, k = 1, xi = 48, d1 = {"\u0000B\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0003\n\u0002\u0010\u000e\n\u0002\b\u0002\n\u0002\u0010\t\n\u0002\b\u0002\n\u0002\u0010\b\n\u0002\b\u0007\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\t\n\u0002\u0010\u0007\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0002\b\u0003\b\u00c6\u0002\u0018\u00002\u00020\u0001:\u0001&B\t\b\u0002\u00a2\u0006\u0004\b\u0002\u0010\u0003J\u000e\u0010\u0010\u001a\u00020\u0005H\u0086@\u00a2\u0006\u0002\u0010\u0011J \u0010\u0012\u001a\u0014\u0012\u0004\u0012\u00020\u0005\u0012\u0004\u0012\u00020\u0005\u0012\u0004\u0012\u00020\u00050\u0013H\u0086@\u00a2\u0006\u0002\u0010\u0011J\u000e\u0010\u0014\u001a\u00020\u0015H\u0086@\u00a2\u0006\u0002\u0010\u0011J\u0016\u0010\u0016\u001a\u00020\u00052\u0006\u0010\u0017\u001a\u00020\u0005H\u0086@\u00a2\u0006\u0002\u0010\u0018J\u000e\u0010\u0019\u001a\u00020\u0005H\u0086@\u00a2\u0006\u0002\u0010\u0011J\u0006\u0010\u001a\u001a\u00020\u000bJ\u000e\u0010\u001b\u001a\u00020\b2\u0006\u0010\u001c\u001a\u00020\u000bJ\u000e\u0010\u001d\u001a\u00020\u00052\u0006\u0010\u001c\u001a\u00020\u000bJ\u0016\u0010\u001e\u001a\u00020\u001f2\u0006\u0010\u001c\u001a\u00020\u000b2\u0006\u0010 \u001a\u00020\bJ\u000e\u0010!\u001a\u00020\u0005H\u0086@\u00a2\u0006\u0002\u0010\u0011J\u0016\u0010\"\u001a\u00020\u00052\u0006\u0010#\u001a\u00020$H\u0086@\u00a2\u0006\u0002\u0010%R\u000e\u0010\u0004\u001a\u00020\u0005X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0006\u001a\u00020\u0005X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0007\u001a\u00020\bX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\t\u001a\u00020\bX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u001b\u0010\n\u001a\u00020\u000b8BX\u0082\u0084\u0002\u00a2\u0006\f\n\u0004\b\u000e\u0010\u000f\u001a\u0004\b\f\u0010\r\u00a8\u0006\'"}, d2 = {"Lcom/zuan/kernelmanager/utils/SoCUtils;", "", "<init>", "()V", "GPU_TEMP", "", "CURRENT_FREQ_GPU", "sPrevTotal", "", "sPrevIdle", "numCores", "", "getNumCores", "()I", "numCores$delegate", "Lkotlin/Lazy;", "getCpuInfo", "(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "getGpuInfo", "Lkotlin/Triple;", "getGpuType", "Lcom/zuan/kernelmanager/utils/SoCUtils$GpuType;", "readFreqGPU", "filePath", "(Ljava/lang/String;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "calculateCpuUsage", "getCoreCount", "getCoreFreq", "index", "getCoreGov", "getCoreLoadBasedOnFreq", "", "currentFreq", "getGpuUsage", "getTotalRam", "context", "Landroid/content/Context;", "(Landroid/content/Context;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "GpuType", "app_debug"})
public final class SoCUtils {
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String GPU_TEMP = "/sys/class/kgsl/kgsl-3d0/temp";
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String CURRENT_FREQ_GPU = "/sys/class/kgsl/kgsl-3d0/gpuclk";
    private static long sPrevTotal = -1L;
    private static long sPrevIdle = -1L;
    @org.jetbrains.annotations.NotNull()
    private static final kotlin.Lazy numCores$delegate = null;
    @org.jetbrains.annotations.NotNull()
    public static final com.zuan.kernelmanager.utils.SoCUtils INSTANCE = null;
    
    private SoCUtils() {
        super();
    }
    
    private final int getNumCores() {
        return 0;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object getCpuInfo(@org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.lang.String> $completion) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object getGpuInfo(@org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Triple<java.lang.String, java.lang.String, java.lang.String>> $completion) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object getGpuType(@org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super com.zuan.kernelmanager.utils.SoCUtils.GpuType> $completion) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object readFreqGPU(@org.jetbrains.annotations.NotNull()
    java.lang.String filePath, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.lang.String> $completion) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object calculateCpuUsage(@org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.lang.String> $completion) {
        return null;
    }
    
    public final int getCoreCount() {
        return 0;
    }
    
    public final long getCoreFreq(int index) {
        return 0L;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getCoreGov(int index) {
        return null;
    }
    
    public final float getCoreLoadBasedOnFreq(int index, long currentFreq) {
        return 0.0F;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object getGpuUsage(@org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.lang.String> $completion) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object getTotalRam(@org.jetbrains.annotations.NotNull()
    android.content.Context context, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.lang.String> $completion) {
        return null;
    }
    
    @kotlin.Metadata(mv = {2, 2, 0}, k = 1, xi = 48, d1 = {"\u0000\f\n\u0002\u0018\u0002\n\u0002\u0010\u0010\n\u0002\b\b\b\u0086\u0081\u0002\u0018\u00002\b\u0012\u0004\u0012\u00020\u00000\u0001B\t\b\u0002\u00a2\u0006\u0004\b\u0002\u0010\u0003j\u0002\b\u0004j\u0002\b\u0005j\u0002\b\u0006j\u0002\b\u0007j\u0002\b\b\u00a8\u0006\t"}, d2 = {"Lcom/zuan/kernelmanager/utils/SoCUtils$GpuType;", "", "<init>", "(Ljava/lang/String;I)V", "ADRENO", "MEDIATEK_V2", "MEDIATEK_LEGACY", "GENERIC_DEVFREQ", "UNKNOWN", "app_debug"})
    public static enum GpuType {
        /*public static final*/ ADRENO /* = new ADRENO() */,
        /*public static final*/ MEDIATEK_V2 /* = new MEDIATEK_V2() */,
        /*public static final*/ MEDIATEK_LEGACY /* = new MEDIATEK_LEGACY() */,
        /*public static final*/ GENERIC_DEVFREQ /* = new GENERIC_DEVFREQ() */,
        /*public static final*/ UNKNOWN /* = new UNKNOWN() */;
        
        GpuType() {
        }
        
        @org.jetbrains.annotations.NotNull()
        public static kotlin.enums.EnumEntries<com.zuan.kernelmanager.utils.SoCUtils.GpuType> getEntries() {
            return null;
        }
    }
}