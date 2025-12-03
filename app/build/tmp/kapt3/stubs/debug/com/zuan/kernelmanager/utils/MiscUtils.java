package com.zuan.kernelmanager.utils;

@kotlin.Metadata(mv = {2, 2, 0}, k = 1, xi = 48, d1 = {"\u0000,\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0003\n\u0002\u0010\u000e\n\u0002\b\u0003\n\u0002\u0010 \n\u0002\b\u0012\n\u0002\u0010\u0002\n\u0002\b\u0002\n\u0002\u0010\u000b\n\u0002\b\u0002\b\u00c6\u0002\u0018\u00002\u00020\u0001B\t\b\u0002\u00a2\u0006\u0004\b\u0002\u0010\u0003J\u0010\u0010\u0018\u001a\u0004\u0018\u00010\u0005H\u0086@\u00a2\u0006\u0002\u0010\u0019J\u0010\u0010\u001a\u001a\u0004\u0018\u00010\u0005H\u0086@\u00a2\u0006\u0002\u0010\u0019J\u001e\u0010\u001b\u001a\u00020\u001c2\u0006\u0010\u001d\u001a\u00020\u00052\u0006\u0010\u001e\u001a\u00020\u001fH\u0086@\u00a2\u0006\u0002\u0010 R\u000e\u0010\u0004\u001a\u00020\u0005X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0006\u001a\u00020\u0005X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0007\u001a\u00020\u0005X\u0086T\u00a2\u0006\u0002\n\u0000R\u0017\u0010\b\u001a\b\u0012\u0004\u0012\u00020\u00050\t\u00a2\u0006\b\n\u0000\u001a\u0004\b\n\u0010\u000bR\u000e\u0010\f\u001a\u00020\u0005X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\r\u001a\u00020\u0005X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u000e\u001a\u00020\u0005X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u000f\u001a\u00020\u0005X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0010\u001a\u00020\u0005X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0011\u001a\u00020\u0005X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0012\u001a\u00020\u0005X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0013\u001a\u00020\u0005X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0014\u001a\u00020\u0005X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0015\u001a\u00020\u0005X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0016\u001a\u00020\u0005X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0017\u001a\u00020\u0005X\u0086T\u00a2\u0006\u0002\n\u0000\u00a8\u0006!"}, d2 = {"Lcom/zuan/kernelmanager/utils/MiscUtils;", "", "<init>", "()V", "THERMAL_ZONE0_POLICY", "", "THERMAL_ZONE0_AVAIL", "THERMAL_ZONE0_AVAIL_ALT", "IO_SCHED_PATHS", "", "getIO_SCHED_PATHS", "()Ljava/util/List;", "TP_GAME_MODE", "TP_LIMIT_ENABLE", "TP_DIRECTION", "DT2W_PATH_A", "DT2W_PATH_B", "DT2W_PATH_C", "MTK_VIBRATOR_LEVEL", "MTK_PBM_STOP", "MTK_BATOC_STOP", "MTK_EARA_ENABLE", "MTK_EARA_FAKE", "DEVFREQ_BASE", "getIoSchedPath", "(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "getDt2wPath", "toggleGeneric", "", "path", "enable", "", "(Ljava/lang/String;ZLkotlin/coroutines/Continuation;)Ljava/lang/Object;", "app_debug"})
public final class MiscUtils {
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String THERMAL_ZONE0_POLICY = "/sys/class/thermal/thermal_zone0/policy";
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String THERMAL_ZONE0_AVAIL = "/sys/class/thermal/thermal_zone0/available_policies";
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String THERMAL_ZONE0_AVAIL_ALT = "/sys/class/thermal/thermal_zone0/scaling_available_governors";
    @org.jetbrains.annotations.NotNull()
    private static final java.util.List<java.lang.String> IO_SCHED_PATHS = null;
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String TP_GAME_MODE = "/proc/touchpanel/game_switch_enable";
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String TP_LIMIT_ENABLE = "/proc/touchpanel/oplus_tp_limit_enable";
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String TP_DIRECTION = "/proc/touchpanel/oplus_tp_direction";
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String DT2W_PATH_A = "/proc/touchpanel/double_tap_enable";
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String DT2W_PATH_B = "/sys/touchpanel/double_tap";
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String DT2W_PATH_C = "/sys/android_touch/doubletap2wake";
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String MTK_VIBRATOR_LEVEL = "/sys/kernel/thunderquake_engine/level";
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String MTK_PBM_STOP = "/proc/pbm/pbm_stop";
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String MTK_BATOC_STOP = "/proc/mtk_batoc_throttling/battery_oc_protect_stop";
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String MTK_EARA_ENABLE = "/sys/kernel/eara_thermal/enable";
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String MTK_EARA_FAKE = "/sys/kernel/eara_thermal/fake_throttle";
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String DEVFREQ_BASE = "/sys/class/devfreq";
    @org.jetbrains.annotations.NotNull()
    public static final com.zuan.kernelmanager.utils.MiscUtils INSTANCE = null;
    
    private MiscUtils() {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.util.List<java.lang.String> getIO_SCHED_PATHS() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object getIoSchedPath(@org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.lang.String> $completion) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object getDt2wPath(@org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.lang.String> $completion) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object toggleGeneric(@org.jetbrains.annotations.NotNull()
    java.lang.String path, boolean enable, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
}