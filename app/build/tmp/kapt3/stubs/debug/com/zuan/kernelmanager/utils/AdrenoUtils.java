package com.zuan.kernelmanager.utils;

@kotlin.Metadata(mv = {2, 2, 0}, k = 1, xi = 48, d1 = {"\u0000,\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0003\n\u0002\u0010\u000e\n\u0002\b\u0015\n\u0002\u0010\u000b\n\u0002\b\u0007\n\u0002\u0010\u0002\n\u0002\b\u0002\n\u0002\u0010 \n\u0002\b\u000b\b\u00c6\u0002\u0018\u00002\u00020\u0001B\t\b\u0002\u00a2\u0006\u0004\b\u0002\u0010\u0003J\u0006\u0010\u001a\u001a\u00020\u001bJ\u0006\u0010\u001c\u001a\u00020\u001bJ\u0006\u0010\u001d\u001a\u00020\u001bJ\u0006\u0010\u001e\u001a\u00020\u001bJ\u0006\u0010\u001f\u001a\u00020\u001bJ\u000e\u0010 \u001a\u00020\u00052\u0006\u0010!\u001a\u00020\u0005J\u0016\u0010\"\u001a\u00020#2\u0006\u0010!\u001a\u00020\u00052\u0006\u0010$\u001a\u00020\u0005J\f\u0010%\u001a\b\u0012\u0004\u0012\u00020\u00050&J\u000e\u0010\'\u001a\u00020\u00052\u0006\u0010(\u001a\u00020\u0005J\f\u0010)\u001a\b\u0012\u0004\u0012\u00020\u00050&J\u0014\u0010*\u001a\b\u0012\u0004\u0012\u00020\u00050&2\u0006\u0010+\u001a\u00020\u0005J\u000e\u0010,\u001a\u00020\u00052\u0006\u0010+\u001a\u00020\u0005J\u000e\u0010-\u001a\u00020\u00052\u0006\u0010+\u001a\u00020\u0005J\u001e\u0010.\u001a\u00020#2\u0006\u0010+\u001a\u00020\u00052\u0006\u0010/\u001a\u00020\u00052\u0006\u00100\u001a\u00020\u0005R\u000e\u0010\u0004\u001a\u00020\u0005X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0006\u001a\u00020\u0005X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0007\u001a\u00020\u0005X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\b\u001a\u00020\u0005X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\t\u001a\u00020\u0005X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\n\u001a\u00020\u0005X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u000b\u001a\u00020\u0005X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\f\u001a\u00020\u0005X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\r\u001a\u00020\u0005X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u000e\u001a\u00020\u0005X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u000f\u001a\u00020\u0005X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0010\u001a\u00020\u0005X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0011\u001a\u00020\u0005X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0012\u001a\u00020\u0005X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0013\u001a\u00020\u0005X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0014\u001a\u00020\u0005X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0015\u001a\u00020\u0005X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0016\u001a\u00020\u0005X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0017\u001a\u00020\u0005X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0018\u001a\u00020\u0005X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0019\u001a\u00020\u0005X\u0086T\u00a2\u0006\u0002\n\u0000\u00a8\u00061"}, d2 = {"Lcom/zuan/kernelmanager/utils/AdrenoUtils;", "", "<init>", "()V", "MIN_FREQ_GPU", "", "MAX_FREQ_GPU", "CURRENT_FREQ_GPU", "AVAILABLE_FREQ_GPU", "GOV_GPU", "AVAILABLE_GOV_GPU", "MAX_PWRLEVEL", "MIN_PWRLEVEL", "DEFAULT_PWRLEVEL", "ADRENO_BOOST", "GPU_THROTTLING", "BUS_DCVS_DIR", "IDLER_DIR", "IDLER_ACTIVE", "IDLER_IDLEWAIT", "IDLER_DOWNDIFF", "IDLER_WORKLOAD", "SIMPLE_GPU_DIR", "SIMPLE_GPU_ACTIVATE", "SIMPLE_GPU_LAZINESS", "SIMPLE_RAMP_THRESHOLD", "isAdreno", "", "hasAdrenoIdler", "hasSimpleGpu", "hasGpuThrottling", "hasBusDcvs", "readFreqGPU", "filePath", "writeFreqGPU", "", "frequencyMHz", "readAvailableFreqGPU", "", "readParam", "path", "getBusComponents", "getBusAvailableFreqs", "busName", "getBusMinFreq", "getBusMaxFreq", "setBusFreq", "target", "freq", "app_debug"})
public final class AdrenoUtils {
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String MIN_FREQ_GPU = "/sys/class/kgsl/kgsl-3d0/min_clock_mhz";
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String MAX_FREQ_GPU = "/sys/class/kgsl/kgsl-3d0/max_clock_mhz";
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String CURRENT_FREQ_GPU = "/sys/class/kgsl/kgsl-3d0/gpuclk";
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String AVAILABLE_FREQ_GPU = "/sys/class/kgsl/kgsl-3d0/gpu_available_frequencies";
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String GOV_GPU = "/sys/class/kgsl/kgsl-3d0/devfreq/governor";
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String AVAILABLE_GOV_GPU = "/sys/class/kgsl/kgsl-3d0/devfreq/available_governors";
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String MAX_PWRLEVEL = "/sys/class/kgsl/kgsl-3d0/max_pwrlevel";
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String MIN_PWRLEVEL = "/sys/class/kgsl/kgsl-3d0/min_pwrlevel";
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String DEFAULT_PWRLEVEL = "/sys/class/kgsl/kgsl-3d0/default_pwrlevel";
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String ADRENO_BOOST = "/sys/class/kgsl/kgsl-3d0/devfreq/adrenoboost";
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String GPU_THROTTLING = "/sys/class/kgsl/kgsl-3d0/throttling";
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String BUS_DCVS_DIR = "/sys/devices/system/cpu/bus_dcvs";
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String IDLER_DIR = "/sys/module/adreno_idler/parameters";
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String IDLER_ACTIVE = "/sys/module/adreno_idler/parameters/adreno_idler_active";
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String IDLER_IDLEWAIT = "/sys/module/adreno_idler/parameters/adreno_idler_idlewait";
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String IDLER_DOWNDIFF = "/sys/module/adreno_idler/parameters/adreno_idler_downdifferential";
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String IDLER_WORKLOAD = "/sys/module/adreno_idler/parameters/adreno_idler_idleworkload";
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String SIMPLE_GPU_DIR = "/sys/module/simple_gpu_algorithm/parameters";
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String SIMPLE_GPU_ACTIVATE = "/sys/module/simple_gpu_algorithm/parameters/simple_gpu_activate";
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String SIMPLE_GPU_LAZINESS = "/sys/module/simple_gpu_algorithm/parameters/simple_laziness";
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String SIMPLE_RAMP_THRESHOLD = "/sys/module/simple_gpu_algorithm/parameters/simple_ramp_threshold";
    @org.jetbrains.annotations.NotNull()
    public static final com.zuan.kernelmanager.utils.AdrenoUtils INSTANCE = null;
    
    private AdrenoUtils() {
        super();
    }
    
    public final boolean isAdreno() {
        return false;
    }
    
    public final boolean hasAdrenoIdler() {
        return false;
    }
    
    public final boolean hasSimpleGpu() {
        return false;
    }
    
    public final boolean hasGpuThrottling() {
        return false;
    }
    
    public final boolean hasBusDcvs() {
        return false;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String readFreqGPU(@org.jetbrains.annotations.NotNull()
    java.lang.String filePath) {
        return null;
    }
    
    public final void writeFreqGPU(@org.jetbrains.annotations.NotNull()
    java.lang.String filePath, @org.jetbrains.annotations.NotNull()
    java.lang.String frequencyMHz) {
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.util.List<java.lang.String> readAvailableFreqGPU() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String readParam(@org.jetbrains.annotations.NotNull()
    java.lang.String path) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.util.List<java.lang.String> getBusComponents() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.util.List<java.lang.String> getBusAvailableFreqs(@org.jetbrains.annotations.NotNull()
    java.lang.String busName) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getBusMinFreq(@org.jetbrains.annotations.NotNull()
    java.lang.String busName) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getBusMaxFreq(@org.jetbrains.annotations.NotNull()
    java.lang.String busName) {
        return null;
    }
    
    /**
     * SMART SET FREQUENCY
     * Menangani konflik di mana kernel menolak jika Max < Min atau Min > Max.
     */
    public final void setBusFreq(@org.jetbrains.annotations.NotNull()
    java.lang.String busName, @org.jetbrains.annotations.NotNull()
    java.lang.String target, @org.jetbrains.annotations.NotNull()
    java.lang.String freq) {
    }
}