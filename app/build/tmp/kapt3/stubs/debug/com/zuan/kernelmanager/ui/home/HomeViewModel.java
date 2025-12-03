package com.zuan.kernelmanager.ui.home;

@kotlin.Metadata(mv = {2, 2, 0}, k = 1, xi = 48, d1 = {"\u0000:\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0003\u0018\u00002\u00020\u0001B\u0007\u00a2\u0006\u0004\b\u0002\u0010\u0003J\b\u0010\u0010\u001a\u00020\u0011H\u0002J\u000e\u0010\u0012\u001a\u00020\u00112\u0006\u0010\u0013\u001a\u00020\u0014J\u000e\u0010\u0015\u001a\u00020\u00112\u0006\u0010\u0013\u001a\u00020\u0014J\b\u0010\u0016\u001a\u00020\u0011H\u0014R\u0014\u0010\u0004\u001a\b\u0012\u0004\u0012\u00020\u00060\u0005X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0017\u0010\u0007\u001a\b\u0012\u0004\u0012\u00020\u00060\b\u00a2\u0006\b\n\u0000\u001a\u0004\b\t\u0010\nR\u001a\u0010\u000b\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\r0\f0\u0005X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u001d\u0010\u000e\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\r0\f0\b\u00a2\u0006\b\n\u0000\u001a\u0004\b\u000f\u0010\n\u00a8\u0006\u0017"}, d2 = {"Lcom/zuan/kernelmanager/ui/home/HomeViewModel;", "Landroidx/lifecycle/ViewModel;", "<init>", "()V", "_deviceInfo", "Lkotlinx/coroutines/flow/MutableStateFlow;", "Lcom/zuan/kernelmanager/ui/home/DeviceInfo;", "deviceInfo", "Lkotlinx/coroutines/flow/StateFlow;", "getDeviceInfo", "()Lkotlinx/coroutines/flow/StateFlow;", "_extensionList", "", "Lcom/zuan/kernelmanager/ui/home/ExtensionItem;", "extensionList", "getExtensionList", "loadExtensions", "", "loadDeviceInfo", "context", "Landroid/content/Context;", "startRamMonitor", "onCleared", "app_debug"})
public final class HomeViewModel extends androidx.lifecycle.ViewModel {
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<com.zuan.kernelmanager.ui.home.DeviceInfo> _deviceInfo = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<com.zuan.kernelmanager.ui.home.DeviceInfo> deviceInfo = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<java.util.List<com.zuan.kernelmanager.ui.home.ExtensionItem>> _extensionList = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<java.util.List<com.zuan.kernelmanager.ui.home.ExtensionItem>> extensionList = null;
    
    public HomeViewModel() {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<com.zuan.kernelmanager.ui.home.DeviceInfo> getDeviceInfo() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<java.util.List<com.zuan.kernelmanager.ui.home.ExtensionItem>> getExtensionList() {
        return null;
    }
    
    private final void loadExtensions() {
    }
    
    public final void loadDeviceInfo(@org.jetbrains.annotations.NotNull()
    android.content.Context context) {
    }
    
    public final void startRamMonitor(@org.jetbrains.annotations.NotNull()
    android.content.Context context) {
    }
    
    @java.lang.Override()
    protected void onCleared() {
    }
}