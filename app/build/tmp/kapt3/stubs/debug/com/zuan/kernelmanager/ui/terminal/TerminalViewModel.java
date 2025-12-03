package com.zuan.kernelmanager.ui.terminal;

@kotlin.Metadata(mv = {2, 2, 0}, k = 1, xi = 48, d1 = {"\u0000R\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010 \n\u0002\b\u0003\n\u0002\u0010\u000e\n\u0002\b\u0007\n\u0002\u0010\u000b\n\u0002\b\n\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010!\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\b\n\u0002\b\u0002\n\u0002\u0010\u0002\n\u0002\b\n\u0018\u00002\u00020\u0001B\u0007\u00a2\u0006\u0004\b\u0002\u0010\u0003J\b\u0010(\u001a\u00020)H\u0002J\b\u0010*\u001a\u00020)H\u0002J\u0010\u0010+\u001a\u00020)2\u0006\u0010,\u001a\u00020\fH\u0002J\u0006\u0010-\u001a\u00020)J\u0006\u0010.\u001a\u00020)J\u000e\u0010/\u001a\u00020)2\u0006\u00100\u001a\u00020\fJ\u0006\u00101\u001a\u00020)J\b\u00102\u001a\u00020)H\u0014R\u0014\u0010\u0004\u001a\b\u0012\u0004\u0012\u00020\u00060\u0005X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0017\u0010\u0007\u001a\b\u0012\u0004\u0012\u00020\u00060\b8F\u00a2\u0006\u0006\u001a\u0004\b\t\u0010\nR+\u0010\r\u001a\u00020\f2\u0006\u0010\u000b\u001a\u00020\f8F@FX\u0086\u008e\u0002\u00a2\u0006\u0012\n\u0004\b\u0012\u0010\u0013\u001a\u0004\b\u000e\u0010\u000f\"\u0004\b\u0010\u0010\u0011R+\u0010\u0015\u001a\u00020\u00142\u0006\u0010\u000b\u001a\u00020\u00148F@FX\u0086\u008e\u0002\u00a2\u0006\u0012\n\u0004\b\u001a\u0010\u0013\u001a\u0004\b\u0016\u0010\u0017\"\u0004\b\u0018\u0010\u0019R+\u0010\u001b\u001a\u00020\u00142\u0006\u0010\u000b\u001a\u00020\u00148F@FX\u0086\u008e\u0002\u00a2\u0006\u0012\n\u0004\b\u001d\u0010\u0013\u001a\u0004\b\u001b\u0010\u0017\"\u0004\b\u001c\u0010\u0019R\u0010\u0010\u001e\u001a\u0004\u0018\u00010\u001fX\u0082\u000e\u00a2\u0006\u0002\n\u0000R2\u0010 \u001a&\u0012\f\u0012\n \"*\u0004\u0018\u00010\f0\f \"*\u0012\u0012\f\u0012\n \"*\u0004\u0018\u00010\f0\f\u0018\u00010\b0!X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0010\u0010#\u001a\u0004\u0018\u00010$X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010%\u001a\u00020&X\u0082D\u00a2\u0006\u0002\n\u0000R\u000e\u0010\'\u001a\u00020&X\u0082D\u00a2\u0006\u0002\n\u0000\u00a8\u00063"}, d2 = {"Lcom/zuan/kernelmanager/ui/terminal/TerminalViewModel;", "Landroidx/lifecycle/ViewModel;", "<init>", "()V", "_logs", "Landroidx/compose/runtime/snapshots/SnapshotStateList;", "Lcom/zuan/kernelmanager/ui/terminal/LogItem;", "logs", "", "getLogs", "()Ljava/util/List;", "<set-?>", "", "commandInput", "getCommandInput", "()Ljava/lang/String;", "setCommandInput", "(Ljava/lang/String;)V", "commandInput$delegate", "Landroidx/compose/runtime/MutableState;", "", "shouldScrollToBottom", "getShouldScrollToBottom", "()Z", "setShouldScrollToBottom", "(Z)V", "shouldScrollToBottom$delegate", "isRunning", "setRunning", "isRunning$delegate", "currentShell", "Lcom/topjohnwu/superuser/Shell;", "logBuffer", "", "kotlin.jvm.PlatformType", "flushJob", "Lkotlinx/coroutines/Job;", "MAX_LOG_HISTORY", "", "LINES_PER_CHUNK", "startLogFlusher", "", "checkRoot", "addLogSafe", "text", "runCommand", "stopCommand", "updateInput", "newInput", "onScrolledToBottom", "onCleared", "app_debug"})
public final class TerminalViewModel extends androidx.lifecycle.ViewModel {
    @org.jetbrains.annotations.NotNull()
    private final androidx.compose.runtime.snapshots.SnapshotStateList<com.zuan.kernelmanager.ui.terminal.LogItem> _logs = null;
    @org.jetbrains.annotations.NotNull()
    private final androidx.compose.runtime.MutableState commandInput$delegate = null;
    @org.jetbrains.annotations.NotNull()
    private final androidx.compose.runtime.MutableState shouldScrollToBottom$delegate = null;
    @org.jetbrains.annotations.NotNull()
    private final androidx.compose.runtime.MutableState isRunning$delegate = null;
    @org.jetbrains.annotations.Nullable()
    private com.topjohnwu.superuser.Shell currentShell;
    private final java.util.List<java.lang.String> logBuffer = null;
    @org.jetbrains.annotations.Nullable()
    private kotlinx.coroutines.Job flushJob;
    private final int MAX_LOG_HISTORY = 300;
    private final int LINES_PER_CHUNK = 100;
    
    public TerminalViewModel() {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.util.List<com.zuan.kernelmanager.ui.terminal.LogItem> getLogs() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getCommandInput() {
        return null;
    }
    
    public final void setCommandInput(@org.jetbrains.annotations.NotNull()
    java.lang.String p0) {
    }
    
    public final boolean getShouldScrollToBottom() {
        return false;
    }
    
    public final void setShouldScrollToBottom(boolean p0) {
    }
    
    public final boolean isRunning() {
        return false;
    }
    
    public final void setRunning(boolean p0) {
    }
    
    private final void startLogFlusher() {
    }
    
    private final void checkRoot() {
    }
    
    private final void addLogSafe(java.lang.String text) {
    }
    
    public final void runCommand() {
    }
    
    public final void stopCommand() {
    }
    
    public final void updateInput(@org.jetbrains.annotations.NotNull()
    java.lang.String newInput) {
    }
    
    public final void onScrolledToBottom() {
    }
    
    @java.lang.Override()
    protected void onCleared() {
    }
}