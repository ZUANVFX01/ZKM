package com.zuan.kernelmanager.utils;

@kotlin.Metadata(mv = {2, 2, 0}, k = 1, xi = 48, d1 = {"\u00000\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0003\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\b\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0002\b\u00c6\u0002\u0018\u00002\u00020\u0001B\t\b\u0002\u00a2\u0006\u0004\b\u0002\u0010\u0003J$\u0010\u0004\u001a\b\u0012\u0004\u0012\u00020\u00060\u00052\u0006\u0010\u0007\u001a\u00020\b2\u0006\u0010\t\u001a\u00020\n2\u0006\u0010\u000b\u001a\u00020\fJ\u0010\u0010\r\u001a\u00020\u000e2\u0006\u0010\u000f\u001a\u00020\u000eH\u0002\u00a8\u0006\u0010"}, d2 = {"Lcom/zuan/kernelmanager/utils/ProcessUtils;", "", "<init>", "()V", "getTopProcesses", "", "Lcom/zuan/kernelmanager/utils/ProcessData;", "context", "Landroid/content/Context;", "limit", "", "sortType", "Lcom/zuan/kernelmanager/utils/SortType;", "formatRes", "", "rssVal", "app_debug"})
public final class ProcessUtils {
    @org.jetbrains.annotations.NotNull()
    public static final com.zuan.kernelmanager.utils.ProcessUtils INSTANCE = null;
    
    private ProcessUtils() {
        super();
    }
    
    /**
     * Mendapatkan daftar process.
     * @param limit Jumlah maksimal baris.
     * @param sortType Jenis pengurutan (CPU atau RES/RAM).
     */
    @org.jetbrains.annotations.NotNull()
    public final java.util.List<com.zuan.kernelmanager.utils.ProcessData> getTopProcesses(@org.jetbrains.annotations.NotNull()
    android.content.Context context, int limit, @org.jetbrains.annotations.NotNull()
    com.zuan.kernelmanager.utils.SortType sortType) {
        return null;
    }
    
    private final java.lang.String formatRes(java.lang.String rssVal) {
        return null;
    }
}