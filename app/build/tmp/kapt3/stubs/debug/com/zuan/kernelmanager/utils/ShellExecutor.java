package com.zuan.kernelmanager.utils;

@kotlin.Metadata(mv = {2, 2, 0}, k = 1, xi = 48, d1 = {"\u00000\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0010\u000b\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0002\b\u0007\b\u00c6\u0002\u0018\u00002\u00020\u0001:\u0001\u0018B\t\b\u0002\u00a2\u0006\u0004\b\u0002\u0010\u0003J\u000e\u0010\f\u001a\u00020\r2\u0006\u0010\u000e\u001a\u00020\u000fJ\b\u0010\u0010\u001a\u00020\rH\u0002J\u000e\u0010\u0011\u001a\u00020\u00122\u0006\u0010\u0013\u001a\u00020\u0012J\u0010\u0010\u0014\u001a\u00020\u00122\u0006\u0010\u0013\u001a\u00020\u0012H\u0002J\u0006\u0010\u0015\u001a\u00020\u000bJ\u0006\u0010\u0016\u001a\u00020\u000bJ\u0006\u0010\u0017\u001a\u00020\rR\u001a\u0010\u0004\u001a\u00020\u0005X\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\u0006\u0010\u0007\"\u0004\b\b\u0010\tR\u000e\u0010\n\u001a\u00020\u000bX\u0082\u000e\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u0019"}, d2 = {"Lcom/zuan/kernelmanager/utils/ShellExecutor;", "", "<init>", "()V", "preferredAccessType", "Lcom/zuan/kernelmanager/utils/ShellExecutor$AccessType;", "getPreferredAccessType", "()Lcom/zuan/kernelmanager/utils/ShellExecutor$AccessType;", "setPreferredAccessType", "(Lcom/zuan/kernelmanager/utils/ShellExecutor$AccessType;)V", "hasLoggedError", "", "init", "", "context", "Landroid/content/Context;", "refreshAccess", "executeWithResult", "", "command", "executeShizuku", "isShizukuAvailable", "isShizukuPermissionGranted", "requestShizukuPermission", "AccessType", "app_debug"})
public final class ShellExecutor {
    @org.jetbrains.annotations.NotNull()
    private static com.zuan.kernelmanager.utils.ShellExecutor.AccessType preferredAccessType = com.zuan.kernelmanager.utils.ShellExecutor.AccessType.NONE;
    private static boolean hasLoggedError = false;
    @org.jetbrains.annotations.NotNull()
    public static final com.zuan.kernelmanager.utils.ShellExecutor INSTANCE = null;
    
    private ShellExecutor() {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.zuan.kernelmanager.utils.ShellExecutor.AccessType getPreferredAccessType() {
        return null;
    }
    
    public final void setPreferredAccessType(@org.jetbrains.annotations.NotNull()
    com.zuan.kernelmanager.utils.ShellExecutor.AccessType p0) {
    }
    
    public final void init(@org.jetbrains.annotations.NotNull()
    android.content.Context context) {
    }
    
    private final void refreshAccess() {
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String executeWithResult(@org.jetbrains.annotations.NotNull()
    java.lang.String command) {
        return null;
    }
    
    private final java.lang.String executeShizuku(java.lang.String command) {
        return null;
    }
    
    public final boolean isShizukuAvailable() {
        return false;
    }
    
    public final boolean isShizukuPermissionGranted() {
        return false;
    }
    
    public final void requestShizukuPermission() {
    }
    
    @kotlin.Metadata(mv = {2, 2, 0}, k = 1, xi = 48, d1 = {"\u0000\f\n\u0002\u0018\u0002\n\u0002\u0010\u0010\n\u0002\b\u0006\b\u0086\u0081\u0002\u0018\u00002\b\u0012\u0004\u0012\u00020\u00000\u0001B\t\b\u0002\u00a2\u0006\u0004\b\u0002\u0010\u0003j\u0002\b\u0004j\u0002\b\u0005j\u0002\b\u0006\u00a8\u0006\u0007"}, d2 = {"Lcom/zuan/kernelmanager/utils/ShellExecutor$AccessType;", "", "<init>", "(Ljava/lang/String;I)V", "ROOT", "SHIZUKU", "NONE", "app_debug"})
    public static enum AccessType {
        /*public static final*/ ROOT /* = new ROOT() */,
        /*public static final*/ SHIZUKU /* = new SHIZUKU() */,
        /*public static final*/ NONE /* = new NONE() */;
        
        AccessType() {
        }
        
        @org.jetbrains.annotations.NotNull()
        public static kotlin.enums.EnumEntries<com.zuan.kernelmanager.utils.ShellExecutor.AccessType> getEntries() {
            return null;
        }
    }
}