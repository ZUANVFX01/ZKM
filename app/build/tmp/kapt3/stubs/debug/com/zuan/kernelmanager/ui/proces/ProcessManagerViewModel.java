package com.zuan.kernelmanager.ui.proces;

@kotlin.Metadata(mv = {2, 2, 0}, k = 1, xi = 48, d1 = {"\u0000@\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0002\b\u0007\n\u0002\u0010\u000b\n\u0002\b\u0005\n\u0002\u0010\b\n\u0002\b\u0006\n\u0002\u0018\u0002\n\u0002\b\u0007\n\u0002\u0010\u0002\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0002\b\u0003\u0018\u00002\u00020\u0001B\u0007\u00a2\u0006\u0004\b\u0002\u0010\u0003J\u000e\u0010\"\u001a\u00020#2\u0006\u0010$\u001a\u00020\u0014J\u000e\u0010%\u001a\u00020#2\u0006\u0010&\u001a\u00020\u001bJ\u000e\u0010\'\u001a\u00020#2\u0006\u0010(\u001a\u00020)J\u0006\u0010*\u001a\u00020#J\u0010\u0010+\u001a\u00020#2\u0006\u0010(\u001a\u00020)H\u0002R7\u0010\u0007\u001a\b\u0012\u0004\u0012\u00020\u00060\u00052\f\u0010\u0004\u001a\b\u0012\u0004\u0012\u00020\u00060\u00058F@BX\u0086\u008e\u0002\u00a2\u0006\u0012\n\u0004\b\f\u0010\r\u001a\u0004\b\b\u0010\t\"\u0004\b\n\u0010\u000bR+\u0010\u000f\u001a\u00020\u000e2\u0006\u0010\u0004\u001a\u00020\u000e8F@BX\u0086\u008e\u0002\u00a2\u0006\u0012\n\u0004\b\u0013\u0010\r\u001a\u0004\b\u000f\u0010\u0010\"\u0004\b\u0011\u0010\u0012R+\u0010\u0015\u001a\u00020\u00142\u0006\u0010\u0004\u001a\u00020\u00148F@BX\u0086\u008e\u0002\u00a2\u0006\u0012\n\u0004\b\u001a\u0010\r\u001a\u0004\b\u0016\u0010\u0017\"\u0004\b\u0018\u0010\u0019R+\u0010\u001c\u001a\u00020\u001b2\u0006\u0010\u0004\u001a\u00020\u001b8F@BX\u0086\u008e\u0002\u00a2\u0006\u0012\n\u0004\b!\u0010\r\u001a\u0004\b\u001d\u0010\u001e\"\u0004\b\u001f\u0010 \u00a8\u0006,"}, d2 = {"Lcom/zuan/kernelmanager/ui/proces/ProcessManagerViewModel;", "Landroidx/lifecycle/ViewModel;", "<init>", "()V", "<set-?>", "", "Lcom/zuan/kernelmanager/utils/ProcessData;", "processList", "getProcessList", "()Ljava/util/List;", "setProcessList", "(Ljava/util/List;)V", "processList$delegate", "Landroidx/compose/runtime/MutableState;", "", "isLoading", "()Z", "setLoading", "(Z)V", "isLoading$delegate", "", "limitOption", "getLimitOption", "()I", "setLimitOption", "(I)V", "limitOption$delegate", "Lcom/zuan/kernelmanager/utils/SortType;", "sortType", "getSortType", "()Lcom/zuan/kernelmanager/utils/SortType;", "setSortType", "(Lcom/zuan/kernelmanager/utils/SortType;)V", "sortType$delegate", "setLimit", "", "newLimit", "setSort", "newSort", "startMonitoring", "context", "Landroid/content/Context;", "refreshData", "updateData", "app_debug"})
public final class ProcessManagerViewModel extends androidx.lifecycle.ViewModel {
    @org.jetbrains.annotations.NotNull()
    private final androidx.compose.runtime.MutableState processList$delegate = null;
    @org.jetbrains.annotations.NotNull()
    private final androidx.compose.runtime.MutableState isLoading$delegate = null;
    @org.jetbrains.annotations.NotNull()
    private final androidx.compose.runtime.MutableState limitOption$delegate = null;
    @org.jetbrains.annotations.NotNull()
    private final androidx.compose.runtime.MutableState sortType$delegate = null;
    
    public ProcessManagerViewModel() {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.util.List<com.zuan.kernelmanager.utils.ProcessData> getProcessList() {
        return null;
    }
    
    private final void setProcessList(java.util.List<com.zuan.kernelmanager.utils.ProcessData> p0) {
    }
    
    public final boolean isLoading() {
        return false;
    }
    
    private final void setLoading(boolean p0) {
    }
    
    public final int getLimitOption() {
        return 0;
    }
    
    private final void setLimitOption(int p0) {
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.zuan.kernelmanager.utils.SortType getSortType() {
        return null;
    }
    
    private final void setSortType(com.zuan.kernelmanager.utils.SortType p0) {
    }
    
    public final void setLimit(int newLimit) {
    }
    
    public final void setSort(@org.jetbrains.annotations.NotNull()
    com.zuan.kernelmanager.utils.SortType newSort) {
    }
    
    public final void startMonitoring(@org.jetbrains.annotations.NotNull()
    android.content.Context context) {
    }
    
    public final void refreshData() {
    }
    
    private final void updateData(android.content.Context context) {
    }
}