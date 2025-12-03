package com.zuan.kernelmanager.ui.gpu;

@kotlin.Metadata(mv = {2, 2, 0}, k = 1, xi = 48, d1 = {"\u00004\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\u0002\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0002\b\u0005\u0018\u00002\u00020\u0001:\u0001\u0015B\u000f\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0004\b\u0004\u0010\u0005J\u0006\u0010\r\u001a\u00020\u000eJ\u0016\u0010\u000f\u001a\u00020\u000e2\u0006\u0010\u0010\u001a\u00020\u00112\u0006\u0010\u0012\u001a\u00020\u0011J\u000e\u0010\u0013\u001a\u00020\u000e2\u0006\u0010\u0014\u001a\u00020\u0011R\u0014\u0010\u0006\u001a\b\u0012\u0004\u0012\u00020\b0\u0007X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0017\u0010\t\u001a\b\u0012\u0004\u0012\u00020\b0\n\u00a2\u0006\b\n\u0000\u001a\u0004\b\u000b\u0010\f\u00a8\u0006\u0016"}, d2 = {"Lcom/zuan/kernelmanager/ui/gpu/GenericGpuViewModel;", "Landroidx/lifecycle/AndroidViewModel;", "application", "Landroid/app/Application;", "<init>", "(Landroid/app/Application;)V", "_state", "Lkotlinx/coroutines/flow/MutableStateFlow;", "Lcom/zuan/kernelmanager/ui/gpu/GenericGpuViewModel$GenericGpuState;", "state", "Lkotlinx/coroutines/flow/StateFlow;", "getState", "()Lkotlinx/coroutines/flow/StateFlow;", "loadData", "", "updateFreq", "type", "", "freq", "updateGov", "gov", "GenericGpuState", "app_debug"})
public final class GenericGpuViewModel extends androidx.lifecycle.AndroidViewModel {
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<com.zuan.kernelmanager.ui.gpu.GenericGpuViewModel.GenericGpuState> _state = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<com.zuan.kernelmanager.ui.gpu.GenericGpuViewModel.GenericGpuState> state = null;
    
    public GenericGpuViewModel(@org.jetbrains.annotations.NotNull()
    android.app.Application application) {
        super(null);
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<com.zuan.kernelmanager.ui.gpu.GenericGpuViewModel.GenericGpuState> getState() {
        return null;
    }
    
    public final void loadData() {
    }
    
    public final void updateFreq(@org.jetbrains.annotations.NotNull()
    java.lang.String type, @org.jetbrains.annotations.NotNull()
    java.lang.String freq) {
    }
    
    public final void updateGov(@org.jetbrains.annotations.NotNull()
    java.lang.String gov) {
    }
    
    @kotlin.Metadata(mv = {2, 2, 0}, k = 1, xi = 48, d1 = {"\u0000(\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\u000b\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0005\n\u0002\u0010 \n\u0002\b\u0019\n\u0002\u0010\b\n\u0002\b\u0002\b\u0086\b\u0018\u00002\u00020\u0001Bc\u0012\b\b\u0002\u0010\u0002\u001a\u00020\u0003\u0012\b\b\u0002\u0010\u0004\u001a\u00020\u0005\u0012\b\b\u0002\u0010\u0006\u001a\u00020\u0005\u0012\b\b\u0002\u0010\u0007\u001a\u00020\u0005\u0012\b\b\u0002\u0010\b\u001a\u00020\u0005\u0012\b\b\u0002\u0010\t\u001a\u00020\u0005\u0012\u000e\b\u0002\u0010\n\u001a\b\u0012\u0004\u0012\u00020\u00050\u000b\u0012\u000e\b\u0002\u0010\f\u001a\b\u0012\u0004\u0012\u00020\u00050\u000b\u00a2\u0006\u0004\b\r\u0010\u000eJ\t\u0010\u0019\u001a\u00020\u0003H\u00c6\u0003J\t\u0010\u001a\u001a\u00020\u0005H\u00c6\u0003J\t\u0010\u001b\u001a\u00020\u0005H\u00c6\u0003J\t\u0010\u001c\u001a\u00020\u0005H\u00c6\u0003J\t\u0010\u001d\u001a\u00020\u0005H\u00c6\u0003J\t\u0010\u001e\u001a\u00020\u0005H\u00c6\u0003J\u000f\u0010\u001f\u001a\b\u0012\u0004\u0012\u00020\u00050\u000bH\u00c6\u0003J\u000f\u0010 \u001a\b\u0012\u0004\u0012\u00020\u00050\u000bH\u00c6\u0003Je\u0010!\u001a\u00020\u00002\b\b\u0002\u0010\u0002\u001a\u00020\u00032\b\b\u0002\u0010\u0004\u001a\u00020\u00052\b\b\u0002\u0010\u0006\u001a\u00020\u00052\b\b\u0002\u0010\u0007\u001a\u00020\u00052\b\b\u0002\u0010\b\u001a\u00020\u00052\b\b\u0002\u0010\t\u001a\u00020\u00052\u000e\b\u0002\u0010\n\u001a\b\u0012\u0004\u0012\u00020\u00050\u000b2\u000e\b\u0002\u0010\f\u001a\b\u0012\u0004\u0012\u00020\u00050\u000bH\u00c6\u0001J\u0013\u0010\"\u001a\u00020\u00032\b\u0010#\u001a\u0004\u0018\u00010\u0001H\u00d6\u0003J\t\u0010$\u001a\u00020%H\u00d6\u0001J\t\u0010&\u001a\u00020\u0005H\u00d6\u0001R\u0011\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0002\u0010\u000fR\u0011\u0010\u0004\u001a\u00020\u0005\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0010\u0010\u0011R\u0011\u0010\u0006\u001a\u00020\u0005\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0012\u0010\u0011R\u0011\u0010\u0007\u001a\u00020\u0005\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0013\u0010\u0011R\u0011\u0010\b\u001a\u00020\u0005\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0014\u0010\u0011R\u0011\u0010\t\u001a\u00020\u0005\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0015\u0010\u0011R\u0017\u0010\n\u001a\b\u0012\u0004\u0012\u00020\u00050\u000b\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0016\u0010\u0017R\u0017\u0010\f\u001a\b\u0012\u0004\u0012\u00020\u00050\u000b\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0018\u0010\u0017\u00a8\u0006\'"}, d2 = {"Lcom/zuan/kernelmanager/ui/gpu/GenericGpuViewModel$GenericGpuState;", "", "isSupported", "", "gpuPath", "", "minFreq", "maxFreq", "curFreq", "gov", "availableFreqs", "", "availableGovs", "<init>", "(ZLjava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/util/List;Ljava/util/List;)V", "()Z", "getGpuPath", "()Ljava/lang/String;", "getMinFreq", "getMaxFreq", "getCurFreq", "getGov", "getAvailableFreqs", "()Ljava/util/List;", "getAvailableGovs", "component1", "component2", "component3", "component4", "component5", "component6", "component7", "component8", "copy", "equals", "other", "hashCode", "", "toString", "app_debug"})
    public static final class GenericGpuState {
        private final boolean isSupported = false;
        @org.jetbrains.annotations.NotNull()
        private final java.lang.String gpuPath = null;
        @org.jetbrains.annotations.NotNull()
        private final java.lang.String minFreq = null;
        @org.jetbrains.annotations.NotNull()
        private final java.lang.String maxFreq = null;
        @org.jetbrains.annotations.NotNull()
        private final java.lang.String curFreq = null;
        @org.jetbrains.annotations.NotNull()
        private final java.lang.String gov = null;
        @org.jetbrains.annotations.NotNull()
        private final java.util.List<java.lang.String> availableFreqs = null;
        @org.jetbrains.annotations.NotNull()
        private final java.util.List<java.lang.String> availableGovs = null;
        
        public GenericGpuState(boolean isSupported, @org.jetbrains.annotations.NotNull()
        java.lang.String gpuPath, @org.jetbrains.annotations.NotNull()
        java.lang.String minFreq, @org.jetbrains.annotations.NotNull()
        java.lang.String maxFreq, @org.jetbrains.annotations.NotNull()
        java.lang.String curFreq, @org.jetbrains.annotations.NotNull()
        java.lang.String gov, @org.jetbrains.annotations.NotNull()
        java.util.List<java.lang.String> availableFreqs, @org.jetbrains.annotations.NotNull()
        java.util.List<java.lang.String> availableGovs) {
            super();
        }
        
        public final boolean isSupported() {
            return false;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final java.lang.String getGpuPath() {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final java.lang.String getMinFreq() {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final java.lang.String getMaxFreq() {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final java.lang.String getCurFreq() {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final java.lang.String getGov() {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final java.util.List<java.lang.String> getAvailableFreqs() {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final java.util.List<java.lang.String> getAvailableGovs() {
            return null;
        }
        
        public GenericGpuState() {
            super();
        }
        
        public final boolean component1() {
            return false;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final java.lang.String component2() {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final java.lang.String component3() {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final java.lang.String component4() {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final java.lang.String component5() {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final java.lang.String component6() {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final java.util.List<java.lang.String> component7() {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final java.util.List<java.lang.String> component8() {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final com.zuan.kernelmanager.ui.gpu.GenericGpuViewModel.GenericGpuState copy(boolean isSupported, @org.jetbrains.annotations.NotNull()
        java.lang.String gpuPath, @org.jetbrains.annotations.NotNull()
        java.lang.String minFreq, @org.jetbrains.annotations.NotNull()
        java.lang.String maxFreq, @org.jetbrains.annotations.NotNull()
        java.lang.String curFreq, @org.jetbrains.annotations.NotNull()
        java.lang.String gov, @org.jetbrains.annotations.NotNull()
        java.util.List<java.lang.String> availableFreqs, @org.jetbrains.annotations.NotNull()
        java.util.List<java.lang.String> availableGovs) {
            return null;
        }
        
        @java.lang.Override()
        public boolean equals(@org.jetbrains.annotations.Nullable()
        java.lang.Object other) {
            return false;
        }
        
        @java.lang.Override()
        public int hashCode() {
            return 0;
        }
        
        @java.lang.Override()
        @org.jetbrains.annotations.NotNull()
        public java.lang.String toString() {
            return null;
        }
    }
}