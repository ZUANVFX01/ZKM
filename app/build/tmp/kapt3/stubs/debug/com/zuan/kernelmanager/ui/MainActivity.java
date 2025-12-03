package com.zuan.kernelmanager.ui;

@kotlin.Metadata(mv = {2, 2, 0}, k = 1, xi = 48, d1 = {"\u00002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\u000b\n\u0002\b\t\n\u0002\u0018\u0002\n\u0002\u0010\u000e\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\u0018\u0000 \u00182\u00020\u0001:\u0001\u0018B\u0007\u00a2\u0006\u0004\b\u0002\u0010\u0003J\u0012\u0010\u0013\u001a\u00020\u00142\b\u0010\u0015\u001a\u0004\u0018\u00010\u0016H\u0014J\b\u0010\u0017\u001a\u00020\u0014H\u0002R\u000e\u0010\u0004\u001a\u00020\u0005X\u0082\u000e\u00a2\u0006\u0002\n\u0000R+\u0010\u0007\u001a\u00020\u00052\u0006\u0010\u0006\u001a\u00020\u00058B@BX\u0082\u008e\u0002\u00a2\u0006\u0012\n\u0004\b\f\u0010\r\u001a\u0004\b\b\u0010\t\"\u0004\b\n\u0010\u000bR\u0014\u0010\u000e\u001a\b\u0012\u0004\u0012\u00020\u00100\u000fX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0011\u001a\u00020\u0012X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u0019"}, d2 = {"Lcom/zuan/kernelmanager/ui/MainActivity;", "Landroidx/activity/ComponentActivity;", "<init>", "()V", "isRoot", "", "<set-?>", "showRootDialog", "getShowRootDialog", "()Z", "setShowRootDialog", "(Z)V", "showRootDialog$delegate", "Landroidx/compose/runtime/MutableState;", "requestPermissionLauncher", "Landroidx/activity/result/ActivityResultLauncher;", "", "checkRoot", "Ljava/lang/Runnable;", "onCreate", "", "savedInstanceState", "Landroid/os/Bundle;", "askNotificationPermission", "Companion", "app_debug"})
public final class MainActivity extends androidx.activity.ComponentActivity {
    private boolean isRoot = false;
    @org.jetbrains.annotations.NotNull()
    private final androidx.compose.runtime.MutableState showRootDialog$delegate = null;
    @org.jetbrains.annotations.NotNull()
    private final androidx.activity.result.ActivityResultLauncher<java.lang.String> requestPermissionLauncher = null;
    @org.jetbrains.annotations.NotNull()
    private final java.lang.Runnable checkRoot = null;
    @org.jetbrains.annotations.NotNull()
    public static final com.zuan.kernelmanager.ui.MainActivity.Companion Companion = null;
    
    public MainActivity() {
        super();
    }
    
    private final boolean getShowRootDialog() {
        return false;
    }
    
    private final void setShowRootDialog(boolean p0) {
    }
    
    @java.lang.Override()
    protected void onCreate(@org.jetbrains.annotations.Nullable()
    android.os.Bundle savedInstanceState) {
    }
    
    private final void askNotificationPermission() {
    }
    
    @kotlin.Metadata(mv = {2, 2, 0}, k = 1, xi = 48, d1 = {"\u0000\f\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0003\b\u0086\u0003\u0018\u00002\u00020\u0001B\t\b\u0002\u00a2\u0006\u0004\b\u0002\u0010\u0003\u00a8\u0006\u0004"}, d2 = {"Lcom/zuan/kernelmanager/ui/MainActivity$Companion;", "", "<init>", "()V", "app_debug"})
    public static final class Companion {
        
        private Companion() {
            super();
        }
    }
}