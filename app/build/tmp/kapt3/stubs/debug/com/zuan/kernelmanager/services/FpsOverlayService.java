package com.zuan.kernelmanager.services;

@kotlin.Metadata(mv = {2, 2, 0}, k = 1, xi = 48, d1 = {"\u0000l\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0010\u000e\n\u0002\b\u0007\n\u0002\u0010\u0007\n\u0002\b\n\n\u0002\u0010\u000b\n\u0002\b\u0013\n\u0002\u0010\b\n\u0002\b\u0004\n\u0002\u0010\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\f\u0018\u0000 R2\u00020\u00012\u00020\u00022\u00020\u0003:\u0001RB\u0007\u00a2\u0006\u0004\b\u0004\u0010\u0005J\b\u0010C\u001a\u00020DH\u0016J\"\u0010E\u001a\u00020?2\b\u0010F\u001a\u0004\u0018\u00010G2\u0006\u0010H\u001a\u00020?2\u0006\u0010I\u001a\u00020?H\u0016J\b\u0010J\u001a\u00020DH\u0002J\u0010\u0010K\u001a\u00020D2\u0006\u0010L\u001a\u00020\u0018H\u0002J\u0018\u0010M\u001a\u00020\u000b2\u0006\u0010N\u001a\u00020?2\u0006\u0010O\u001a\u00020?H\u0002J\b\u0010P\u001a\u00020DH\u0002J\b\u0010Q\u001a\u00020DH\u0016R\u000e\u0010\u0006\u001a\u00020\u0007X\u0082.\u00a2\u0006\u0002\n\u0000R\u0010\u0010\b\u001a\u0004\u0018\u00010\tX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\n\u001a\u00020\u000bX\u0082.\u00a2\u0006\u0002\n\u0000R\u000e\u0010\f\u001a\u00020\rX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0014\u0010\u000e\u001a\u00020\u000f8VX\u0096\u0004\u00a2\u0006\u0006\u001a\u0004\b\u0010\u0010\u0011R\u000e\u0010\u0012\u001a\u00020\u0013X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0014\u0010\u0014\u001a\u00020\u00138VX\u0096\u0004\u00a2\u0006\u0006\u001a\u0004\b\u0015\u0010\u0016R+\u0010\u0019\u001a\u00020\u00182\u0006\u0010\u0017\u001a\u00020\u00188B@BX\u0082\u008e\u0002\u00a2\u0006\u0012\n\u0004\b\u001e\u0010\u001f\u001a\u0004\b\u001a\u0010\u001b\"\u0004\b\u001c\u0010\u001dR+\u0010!\u001a\u00020 2\u0006\u0010\u0017\u001a\u00020 8B@BX\u0082\u008e\u0002\u00a2\u0006\u0012\n\u0004\b&\u0010\u001f\u001a\u0004\b\"\u0010#\"\u0004\b$\u0010%R+\u0010\'\u001a\u00020 2\u0006\u0010\u0017\u001a\u00020 8B@BX\u0082\u008e\u0002\u00a2\u0006\u0012\n\u0004\b*\u0010\u001f\u001a\u0004\b(\u0010#\"\u0004\b)\u0010%R+\u0010,\u001a\u00020+2\u0006\u0010\u0017\u001a\u00020+8B@BX\u0082\u008e\u0002\u00a2\u0006\u0012\n\u0004\b1\u0010\u001f\u001a\u0004\b-\u0010.\"\u0004\b/\u00100R+\u00102\u001a\u00020+2\u0006\u0010\u0017\u001a\u00020+8B@BX\u0082\u008e\u0002\u00a2\u0006\u0012\n\u0004\b5\u0010\u001f\u001a\u0004\b3\u0010.\"\u0004\b4\u00100R+\u00106\u001a\u00020+2\u0006\u0010\u0017\u001a\u00020+8B@BX\u0082\u008e\u0002\u00a2\u0006\u0012\n\u0004\b9\u0010\u001f\u001a\u0004\b7\u0010.\"\u0004\b8\u00100R+\u0010:\u001a\u00020+2\u0006\u0010\u0017\u001a\u00020+8B@BX\u0082\u008e\u0002\u00a2\u0006\u0012\n\u0004\b=\u0010\u001f\u001a\u0004\b;\u0010.\"\u0004\b<\u00100R\u000e\u0010>\u001a\u00020?X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010@\u001a\u00020?X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010A\u001a\u00020 X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010B\u001a\u00020 X\u0082\u000e\u00a2\u0006\u0002\n\u0000\u00a8\u0006S"}, d2 = {"Lcom/zuan/kernelmanager/services/FpsOverlayService;", "Landroidx/lifecycle/LifecycleService;", "Landroidx/savedstate/SavedStateRegistryOwner;", "Landroidx/lifecycle/ViewModelStoreOwner;", "<init>", "()V", "windowManager", "Landroid/view/WindowManager;", "overlayView", "Landroidx/compose/ui/platform/ComposeView;", "layoutParams", "Landroid/view/WindowManager$LayoutParams;", "savedStateRegistryController", "Landroidx/savedstate/SavedStateRegistryController;", "savedStateRegistry", "Landroidx/savedstate/SavedStateRegistry;", "getSavedStateRegistry", "()Landroidx/savedstate/SavedStateRegistry;", "store", "Landroidx/lifecycle/ViewModelStore;", "viewModelStore", "getViewModelStore", "()Landroidx/lifecycle/ViewModelStore;", "<set-?>", "", "colorHex", "getColorHex", "()Ljava/lang/String;", "setColorHex", "(Ljava/lang/String;)V", "colorHex$delegate", "Landroidx/compose/runtime/MutableState;", "", "textSizeSp", "getTextSizeSp", "()F", "setTextSizeSp", "(F)V", "textSizeSp$delegate", "bgAlpha", "getBgAlpha", "setBgAlpha", "bgAlpha$delegate", "", "showFps", "getShowFps", "()Z", "setShowFps", "(Z)V", "showFps$delegate", "showCpu", "getShowCpu", "setShowCpu", "showCpu$delegate", "showWatts", "getShowWatts", "setShowWatts", "showWatts$delegate", "showTemp", "getShowTemp", "setShowTemp", "showTemp$delegate", "initialX", "", "initialY", "initialTouchX", "initialTouchY", "onCreate", "", "onStartCommand", "intent", "Landroid/content/Intent;", "flags", "startId", "setupOverlay", "resetPosition", "position", "createLayoutParams", "xPos", "yPos", "startForegroundNotification", "onDestroy", "Companion", "app_debug"})
public final class FpsOverlayService extends androidx.lifecycle.LifecycleService implements androidx.savedstate.SavedStateRegistryOwner, androidx.lifecycle.ViewModelStoreOwner {
    private static boolean isRunning = false;
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String TAG = "FpsOverlayService";
    private android.view.WindowManager windowManager;
    @org.jetbrains.annotations.Nullable()
    private androidx.compose.ui.platform.ComposeView overlayView;
    private android.view.WindowManager.LayoutParams layoutParams;
    @org.jetbrains.annotations.NotNull()
    private final androidx.savedstate.SavedStateRegistryController savedStateRegistryController = null;
    @org.jetbrains.annotations.NotNull()
    private final androidx.lifecycle.ViewModelStore store = null;
    @org.jetbrains.annotations.NotNull()
    private final androidx.compose.runtime.MutableState colorHex$delegate = null;
    @org.jetbrains.annotations.NotNull()
    private final androidx.compose.runtime.MutableState textSizeSp$delegate = null;
    @org.jetbrains.annotations.NotNull()
    private final androidx.compose.runtime.MutableState bgAlpha$delegate = null;
    @org.jetbrains.annotations.NotNull()
    private final androidx.compose.runtime.MutableState showFps$delegate = null;
    @org.jetbrains.annotations.NotNull()
    private final androidx.compose.runtime.MutableState showCpu$delegate = null;
    @org.jetbrains.annotations.NotNull()
    private final androidx.compose.runtime.MutableState showWatts$delegate = null;
    @org.jetbrains.annotations.NotNull()
    private final androidx.compose.runtime.MutableState showTemp$delegate = null;
    private int initialX = 0;
    private int initialY = 0;
    private float initialTouchX = 0.0F;
    private float initialTouchY = 0.0F;
    @org.jetbrains.annotations.NotNull()
    public static final com.zuan.kernelmanager.services.FpsOverlayService.Companion Companion = null;
    
    public FpsOverlayService() {
        super();
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.NotNull()
    public androidx.savedstate.SavedStateRegistry getSavedStateRegistry() {
        return null;
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.NotNull()
    public androidx.lifecycle.ViewModelStore getViewModelStore() {
        return null;
    }
    
    private final java.lang.String getColorHex() {
        return null;
    }
    
    private final void setColorHex(java.lang.String p0) {
    }
    
    private final float getTextSizeSp() {
        return 0.0F;
    }
    
    private final void setTextSizeSp(float p0) {
    }
    
    private final float getBgAlpha() {
        return 0.0F;
    }
    
    private final void setBgAlpha(float p0) {
    }
    
    private final boolean getShowFps() {
        return false;
    }
    
    private final void setShowFps(boolean p0) {
    }
    
    private final boolean getShowCpu() {
        return false;
    }
    
    private final void setShowCpu(boolean p0) {
    }
    
    private final boolean getShowWatts() {
        return false;
    }
    
    private final void setShowWatts(boolean p0) {
    }
    
    private final boolean getShowTemp() {
        return false;
    }
    
    private final void setShowTemp(boolean p0) {
    }
    
    @java.lang.Override()
    public void onCreate() {
    }
    
    @java.lang.Override()
    public int onStartCommand(@org.jetbrains.annotations.Nullable()
    android.content.Intent intent, int flags, int startId) {
        return 0;
    }
    
    private final void setupOverlay() {
    }
    
    private final void resetPosition(java.lang.String position) {
    }
    
    private final android.view.WindowManager.LayoutParams createLayoutParams(int xPos, int yPos) {
        return null;
    }
    
    private final void startForegroundNotification() {
    }
    
    @java.lang.Override()
    public void onDestroy() {
    }
    
    @kotlin.Metadata(mv = {2, 2, 0}, k = 1, xi = 48, d1 = {"\u0000\u001a\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0003\n\u0002\u0010\u000b\n\u0002\b\u0004\n\u0002\u0010\u000e\n\u0000\b\u0086\u0003\u0018\u00002\u00020\u0001B\t\b\u0002\u00a2\u0006\u0004\b\u0002\u0010\u0003R\u001a\u0010\u0004\u001a\u00020\u0005X\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\u0004\u0010\u0006\"\u0004\b\u0007\u0010\bR\u000e\u0010\t\u001a\u00020\nX\u0086T\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u000b"}, d2 = {"Lcom/zuan/kernelmanager/services/FpsOverlayService$Companion;", "", "<init>", "()V", "isRunning", "", "()Z", "setRunning", "(Z)V", "TAG", "", "app_debug"})
    public static final class Companion {
        
        private Companion() {
            super();
        }
        
        public final boolean isRunning() {
            return false;
        }
        
        public final void setRunning(boolean p0) {
        }
    }
}