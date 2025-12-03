package com.zuan.kernelmanager.utils;

@kotlin.Metadata(mv = {2, 2, 0}, k = 1, xi = 48, d1 = {"\u0000$\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0003\n\u0002\u0010\u000e\n\u0002\b\u0004\n\u0002\u0010 \n\u0002\b\u0011\n\u0002\u0018\u0002\n\u0002\b\u0004\b\u00c6\u0002\u0018\u00002\u00020\u0001B\t\b\u0002\u00a2\u0006\u0004\b\u0002\u0010\u0003J\u0014\u0010\t\u001a\b\u0012\u0004\u0012\u00020\u00050\nH\u0086@\u00a2\u0006\u0002\u0010\u000bJ\u001e\u0010\f\u001a\u00020\u00052\u0006\u0010\r\u001a\u00020\u00052\u0006\u0010\u000e\u001a\u00020\u0005H\u0086@\u00a2\u0006\u0002\u0010\u000fJ&\u0010\u0010\u001a\u00020\u00012\u0006\u0010\r\u001a\u00020\u00052\u0006\u0010\u000e\u001a\u00020\u00052\u0006\u0010\u0011\u001a\u00020\u0005H\u0086@\u00a2\u0006\u0002\u0010\u0012J\u001e\u0010\u0013\u001a\u00020\u00012\u0006\u0010\r\u001a\u00020\u00052\u0006\u0010\u0014\u001a\u00020\u0005H\u0086@\u00a2\u0006\u0002\u0010\u000fJ\u001c\u0010\u0015\u001a\b\u0012\u0004\u0012\u00020\u00050\n2\u0006\u0010\r\u001a\u00020\u0005H\u0086@\u00a2\u0006\u0002\u0010\u0016J\u001c\u0010\u0017\u001a\b\u0012\u0004\u0012\u00020\u00050\n2\u0006\u0010\r\u001a\u00020\u0005H\u0086@\u00a2\u0006\u0002\u0010\u0016J\u001c\u0010\u0018\u001a\b\u0012\u0004\u0012\u00020\u00050\n2\u0006\u0010\r\u001a\u00020\u0005H\u0086@\u00a2\u0006\u0002\u0010\u0016J\u0016\u0010\u0019\u001a\u00020\u00052\u0006\u0010\r\u001a\u00020\u0005H\u0086@\u00a2\u0006\u0002\u0010\u0016J\u000e\u0010\u001a\u001a\u00020\u0005H\u0086@\u00a2\u0006\u0002\u0010\u000bJ$\u0010\u001b\u001a\b\u0012\u0004\u0012\u00020\u001c0\n2\u0006\u0010\r\u001a\u00020\u00052\u0006\u0010\u0014\u001a\u00020\u0005H\u0086@\u00a2\u0006\u0002\u0010\u000fJ\u001e\u0010\u001d\u001a\u00020\u00012\u0006\u0010\u001e\u001a\u00020\u00052\u0006\u0010\u001f\u001a\u00020\u0005H\u0086@\u00a2\u0006\u0002\u0010\u000fR\u000e\u0010\u0004\u001a\u00020\u0005X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0006\u001a\u00020\u0005X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0007\u001a\u00020\u0005X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\b\u001a\u00020\u0005X\u0086T\u00a2\u0006\u0002\n\u0000\u00a8\u0006 "}, d2 = {"Lcom/zuan/kernelmanager/utils/CpuUtils;", "", "<init>", "()V", "CPU_BASE_PATH", "", "CPU_TEMP", "CPU_INPUT_BOOST_MS", "CPU_SCHED_BOOST_ON_INPUT", "getCpuPolicies", "", "(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "readFreq", "policyPath", "file", "(Ljava/lang/String;Ljava/lang/String;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "writeFreq", "frequency", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "writeGov", "governor", "readAvailableFreq", "(Ljava/lang/String;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "readAvailableBoostFreq", "readAvailableGov", "readGovernor", "getCpuUsage", "getGovernorTunables", "Lcom/zuan/kernelmanager/utils/GovTunable;", "writeTunable", "path", "value", "app_debug"})
public final class CpuUtils {
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String CPU_BASE_PATH = "/sys/devices/system/cpu/cpufreq";
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String CPU_TEMP = "/sys/class/thermal/thermal_zone0/temp";
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String CPU_INPUT_BOOST_MS = "/sys/devices/system/cpu/cpu_boost/input_boost_ms";
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String CPU_SCHED_BOOST_ON_INPUT = "/sys/devices/system/cpu/cpu_boost/sched_boost_on_input";
    @org.jetbrains.annotations.NotNull()
    public static final com.zuan.kernelmanager.utils.CpuUtils INSTANCE = null;
    
    private CpuUtils() {
        super();
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object getCpuPolicies(@org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.util.List<java.lang.String>> $completion) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object readFreq(@org.jetbrains.annotations.NotNull()
    java.lang.String policyPath, @org.jetbrains.annotations.NotNull()
    java.lang.String file, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.lang.String> $completion) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object writeFreq(@org.jetbrains.annotations.NotNull()
    java.lang.String policyPath, @org.jetbrains.annotations.NotNull()
    java.lang.String file, @org.jetbrains.annotations.NotNull()
    java.lang.String frequency, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<java.lang.Object> $completion) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object writeGov(@org.jetbrains.annotations.NotNull()
    java.lang.String policyPath, @org.jetbrains.annotations.NotNull()
    java.lang.String governor, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<java.lang.Object> $completion) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object readAvailableFreq(@org.jetbrains.annotations.NotNull()
    java.lang.String policyPath, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.util.List<java.lang.String>> $completion) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object readAvailableBoostFreq(@org.jetbrains.annotations.NotNull()
    java.lang.String policyPath, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.util.List<java.lang.String>> $completion) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object readAvailableGov(@org.jetbrains.annotations.NotNull()
    java.lang.String policyPath, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.util.List<java.lang.String>> $completion) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object readGovernor(@org.jetbrains.annotations.NotNull()
    java.lang.String policyPath, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.lang.String> $completion) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object getCpuUsage(@org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.lang.String> $completion) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object getGovernorTunables(@org.jetbrains.annotations.NotNull()
    java.lang.String policyPath, @org.jetbrains.annotations.NotNull()
    java.lang.String governor, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.util.List<com.zuan.kernelmanager.utils.GovTunable>> $completion) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object writeTunable(@org.jetbrains.annotations.NotNull()
    java.lang.String path, @org.jetbrains.annotations.NotNull()
    java.lang.String value, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<java.lang.Object> $completion) {
        return null;
    }
}