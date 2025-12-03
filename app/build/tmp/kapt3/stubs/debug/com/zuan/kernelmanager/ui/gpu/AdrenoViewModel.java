package com.zuan.kernelmanager.ui.gpu;

@kotlin.Metadata(mv = {2, 2, 0}, k = 1, xi = 48, d1 = {"\u0000<\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\u0002\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0002\b\b\n\u0002\u0010\u000b\n\u0002\b\f\u0018\u00002\u00020\u0001:\u0002$%B\u000f\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0004\b\u0004\u0010\u0005J\u0006\u0010\r\u001a\u00020\u000eJ\u0016\u0010\u000f\u001a\u00020\u000e2\u0006\u0010\u0010\u001a\u00020\u00112\u0006\u0010\u0012\u001a\u00020\u0011J\u000e\u0010\u0013\u001a\u00020\u000e2\u0006\u0010\u0014\u001a\u00020\u0011J\u000e\u0010\u0015\u001a\u00020\u000e2\u0006\u0010\u0016\u001a\u00020\u0011J\u000e\u0010\u0017\u001a\u00020\u000e2\u0006\u0010\u0016\u001a\u00020\u0011J\u000e\u0010\u0018\u001a\u00020\u000e2\u0006\u0010\u0019\u001a\u00020\u001aJ\u000e\u0010\u001b\u001a\u00020\u000e2\u0006\u0010\u001c\u001a\u00020\u001aJ\u0016\u0010\u001d\u001a\u00020\u000e2\u0006\u0010\u001e\u001a\u00020\u00112\u0006\u0010\u0016\u001a\u00020\u0011J\u000e\u0010\u001f\u001a\u00020\u000e2\u0006\u0010\u001c\u001a\u00020\u001aJ\u0016\u0010 \u001a\u00020\u000e2\u0006\u0010\u001e\u001a\u00020\u00112\u0006\u0010\u0016\u001a\u00020\u0011J\u001e\u0010!\u001a\u00020\u000e2\u0006\u0010\"\u001a\u00020\u00112\u0006\u0010\u0010\u001a\u00020\u00112\u0006\u0010#\u001a\u00020\u0011R\u0014\u0010\u0006\u001a\b\u0012\u0004\u0012\u00020\b0\u0007X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0017\u0010\t\u001a\b\u0012\u0004\u0012\u00020\b0\n\u00a2\u0006\b\n\u0000\u001a\u0004\b\u000b\u0010\f\u00a8\u0006&"}, d2 = {"Lcom/zuan/kernelmanager/ui/gpu/AdrenoViewModel;", "Landroidx/lifecycle/AndroidViewModel;", "application", "Landroid/app/Application;", "<init>", "(Landroid/app/Application;)V", "_state", "Lkotlinx/coroutines/flow/MutableStateFlow;", "Lcom/zuan/kernelmanager/ui/gpu/AdrenoViewModel$AdrenoState;", "state", "Lkotlinx/coroutines/flow/StateFlow;", "getState", "()Lkotlinx/coroutines/flow/StateFlow;", "loadData", "", "updateFreq", "target", "", "selectedFreq", "updateGov", "selectedGov", "updateDefaultPwrlevel", "value", "updateAdrenoBoost", "updateGPUThrottling", "isChecked", "", "toggleIdler", "enable", "updateIdlerParam", "param", "toggleSimpleGpu", "updateSimpleGpuParam", "updateBusFreq", "busName", "freq", "BusState", "AdrenoState", "app_debug"})
public final class AdrenoViewModel extends androidx.lifecycle.AndroidViewModel {
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<com.zuan.kernelmanager.ui.gpu.AdrenoViewModel.AdrenoState> _state = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<com.zuan.kernelmanager.ui.gpu.AdrenoViewModel.AdrenoState> state = null;
    
    public AdrenoViewModel(@org.jetbrains.annotations.NotNull()
    android.app.Application application) {
        super(null);
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<com.zuan.kernelmanager.ui.gpu.AdrenoViewModel.AdrenoState> getState() {
        return null;
    }
    
    public final void loadData() {
    }
    
    public final void updateFreq(@org.jetbrains.annotations.NotNull()
    java.lang.String target, @org.jetbrains.annotations.NotNull()
    java.lang.String selectedFreq) {
    }
    
    public final void updateGov(@org.jetbrains.annotations.NotNull()
    java.lang.String selectedGov) {
    }
    
    public final void updateDefaultPwrlevel(@org.jetbrains.annotations.NotNull()
    java.lang.String value) {
    }
    
    public final void updateAdrenoBoost(@org.jetbrains.annotations.NotNull()
    java.lang.String value) {
    }
    
    public final void updateGPUThrottling(boolean isChecked) {
    }
    
    public final void toggleIdler(boolean enable) {
    }
    
    public final void updateIdlerParam(@org.jetbrains.annotations.NotNull()
    java.lang.String param, @org.jetbrains.annotations.NotNull()
    java.lang.String value) {
    }
    
    public final void toggleSimpleGpu(boolean enable) {
    }
    
    public final void updateSimpleGpuParam(@org.jetbrains.annotations.NotNull()
    java.lang.String param, @org.jetbrains.annotations.NotNull()
    java.lang.String value) {
    }
    
    public final void updateBusFreq(@org.jetbrains.annotations.NotNull()
    java.lang.String busName, @org.jetbrains.annotations.NotNull()
    java.lang.String target, @org.jetbrains.annotations.NotNull()
    java.lang.String freq) {
    }
    
    @kotlin.Metadata(mv = {2, 2, 0}, k = 1, xi = 48, d1 = {"\u00002\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0004\n\u0002\u0010 \n\u0002\b\u0006\n\u0002\u0010\u000b\n\u0002\b\f\n\u0002\u0018\u0002\n\u0002\b7\n\u0002\u0010\b\n\u0002\b\u0002\b\u0086\b\u0018\u00002\u00020\u0001B\u00ff\u0001\u0012\b\b\u0002\u0010\u0002\u001a\u00020\u0003\u0012\b\b\u0002\u0010\u0004\u001a\u00020\u0003\u0012\b\b\u0002\u0010\u0005\u001a\u00020\u0003\u0012\b\b\u0002\u0010\u0006\u001a\u00020\u0003\u0012\u000e\b\u0002\u0010\u0007\u001a\b\u0012\u0004\u0012\u00020\u00030\b\u0012\u000e\b\u0002\u0010\t\u001a\b\u0012\u0004\u0012\u00020\u00030\b\u0012\b\b\u0002\u0010\n\u001a\u00020\u0003\u0012\b\b\u0002\u0010\u000b\u001a\u00020\u0003\u0012\b\b\u0002\u0010\f\u001a\u00020\u0003\u0012\b\b\u0002\u0010\r\u001a\u00020\u0003\u0012\b\b\u0002\u0010\u000e\u001a\u00020\u000f\u0012\b\b\u0002\u0010\u0010\u001a\u00020\u0003\u0012\b\b\u0002\u0010\u0011\u001a\u00020\u000f\u0012\b\b\u0002\u0010\u0012\u001a\u00020\u000f\u0012\b\b\u0002\u0010\u0013\u001a\u00020\u0003\u0012\b\b\u0002\u0010\u0014\u001a\u00020\u0003\u0012\b\b\u0002\u0010\u0015\u001a\u00020\u0003\u0012\b\b\u0002\u0010\u0016\u001a\u00020\u000f\u0012\b\b\u0002\u0010\u0017\u001a\u00020\u000f\u0012\b\b\u0002\u0010\u0018\u001a\u00020\u0003\u0012\b\b\u0002\u0010\u0019\u001a\u00020\u0003\u0012\b\b\u0002\u0010\u001a\u001a\u00020\u000f\u0012\u000e\b\u0002\u0010\u001b\u001a\b\u0012\u0004\u0012\u00020\u001c0\b\u00a2\u0006\u0004\b\u001d\u0010\u001eJ\t\u00109\u001a\u00020\u0003H\u00c6\u0003J\t\u0010:\u001a\u00020\u0003H\u00c6\u0003J\t\u0010;\u001a\u00020\u0003H\u00c6\u0003J\t\u0010<\u001a\u00020\u0003H\u00c6\u0003J\u000f\u0010=\u001a\b\u0012\u0004\u0012\u00020\u00030\bH\u00c6\u0003J\u000f\u0010>\u001a\b\u0012\u0004\u0012\u00020\u00030\bH\u00c6\u0003J\t\u0010?\u001a\u00020\u0003H\u00c6\u0003J\t\u0010@\u001a\u00020\u0003H\u00c6\u0003J\t\u0010A\u001a\u00020\u0003H\u00c6\u0003J\t\u0010B\u001a\u00020\u0003H\u00c6\u0003J\t\u0010C\u001a\u00020\u000fH\u00c6\u0003J\t\u0010D\u001a\u00020\u0003H\u00c6\u0003J\t\u0010E\u001a\u00020\u000fH\u00c6\u0003J\t\u0010F\u001a\u00020\u000fH\u00c6\u0003J\t\u0010G\u001a\u00020\u0003H\u00c6\u0003J\t\u0010H\u001a\u00020\u0003H\u00c6\u0003J\t\u0010I\u001a\u00020\u0003H\u00c6\u0003J\t\u0010J\u001a\u00020\u000fH\u00c6\u0003J\t\u0010K\u001a\u00020\u000fH\u00c6\u0003J\t\u0010L\u001a\u00020\u0003H\u00c6\u0003J\t\u0010M\u001a\u00020\u0003H\u00c6\u0003J\t\u0010N\u001a\u00020\u000fH\u00c6\u0003J\u000f\u0010O\u001a\b\u0012\u0004\u0012\u00020\u001c0\bH\u00c6\u0003J\u0081\u0002\u0010P\u001a\u00020\u00002\b\b\u0002\u0010\u0002\u001a\u00020\u00032\b\b\u0002\u0010\u0004\u001a\u00020\u00032\b\b\u0002\u0010\u0005\u001a\u00020\u00032\b\b\u0002\u0010\u0006\u001a\u00020\u00032\u000e\b\u0002\u0010\u0007\u001a\b\u0012\u0004\u0012\u00020\u00030\b2\u000e\b\u0002\u0010\t\u001a\b\u0012\u0004\u0012\u00020\u00030\b2\b\b\u0002\u0010\n\u001a\u00020\u00032\b\b\u0002\u0010\u000b\u001a\u00020\u00032\b\b\u0002\u0010\f\u001a\u00020\u00032\b\b\u0002\u0010\r\u001a\u00020\u00032\b\b\u0002\u0010\u000e\u001a\u00020\u000f2\b\b\u0002\u0010\u0010\u001a\u00020\u00032\b\b\u0002\u0010\u0011\u001a\u00020\u000f2\b\b\u0002\u0010\u0012\u001a\u00020\u000f2\b\b\u0002\u0010\u0013\u001a\u00020\u00032\b\b\u0002\u0010\u0014\u001a\u00020\u00032\b\b\u0002\u0010\u0015\u001a\u00020\u00032\b\b\u0002\u0010\u0016\u001a\u00020\u000f2\b\b\u0002\u0010\u0017\u001a\u00020\u000f2\b\b\u0002\u0010\u0018\u001a\u00020\u00032\b\b\u0002\u0010\u0019\u001a\u00020\u00032\b\b\u0002\u0010\u001a\u001a\u00020\u000f2\u000e\b\u0002\u0010\u001b\u001a\b\u0012\u0004\u0012\u00020\u001c0\bH\u00c6\u0001J\u0013\u0010Q\u001a\u00020\u000f2\b\u0010R\u001a\u0004\u0018\u00010\u0001H\u00d6\u0003J\t\u0010S\u001a\u00020TH\u00d6\u0001J\t\u0010U\u001a\u00020\u0003H\u00d6\u0001R\u0011\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u001f\u0010 R\u0011\u0010\u0004\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b!\u0010 R\u0011\u0010\u0005\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\"\u0010 R\u0011\u0010\u0006\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b#\u0010 R\u0017\u0010\u0007\u001a\b\u0012\u0004\u0012\u00020\u00030\b\u00a2\u0006\b\n\u0000\u001a\u0004\b$\u0010%R\u0017\u0010\t\u001a\b\u0012\u0004\u0012\u00020\u00030\b\u00a2\u0006\b\n\u0000\u001a\u0004\b&\u0010%R\u0011\u0010\n\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\'\u0010 R\u0011\u0010\u000b\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b(\u0010 R\u0011\u0010\f\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b)\u0010 R\u0011\u0010\r\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b*\u0010 R\u0011\u0010\u000e\u001a\u00020\u000f\u00a2\u0006\b\n\u0000\u001a\u0004\b+\u0010,R\u0011\u0010\u0010\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b-\u0010 R\u0011\u0010\u0011\u001a\u00020\u000f\u00a2\u0006\b\n\u0000\u001a\u0004\b.\u0010,R\u0011\u0010\u0012\u001a\u00020\u000f\u00a2\u0006\b\n\u0000\u001a\u0004\b/\u0010,R\u0011\u0010\u0013\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b0\u0010 R\u0011\u0010\u0014\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b1\u0010 R\u0011\u0010\u0015\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b2\u0010 R\u0011\u0010\u0016\u001a\u00020\u000f\u00a2\u0006\b\n\u0000\u001a\u0004\b3\u0010,R\u0011\u0010\u0017\u001a\u00020\u000f\u00a2\u0006\b\n\u0000\u001a\u0004\b4\u0010,R\u0011\u0010\u0018\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b5\u0010 R\u0011\u0010\u0019\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b6\u0010 R\u0011\u0010\u001a\u001a\u00020\u000f\u00a2\u0006\b\n\u0000\u001a\u0004\b7\u0010,R\u0017\u0010\u001b\u001a\b\u0012\u0004\u0012\u00020\u001c0\b\u00a2\u0006\b\n\u0000\u001a\u0004\b8\u0010%\u00a8\u0006V"}, d2 = {"Lcom/zuan/kernelmanager/ui/gpu/AdrenoViewModel$AdrenoState;", "", "minFreq", "", "maxFreq", "currentFreq", "gov", "availableFreq", "", "availableGov", "maxPwrlevel", "minPwrlevel", "defaultPwrlevel", "adrenoBoost", "hasGpuThrottling", "", "gpuThrottling", "hasAdrenoIdler", "idlerActive", "idlerIdleWait", "idlerDownDiff", "idlerWorkload", "hasSimpleGpu", "simpleGpuActive", "simpleLaziness", "simpleRampThreshold", "hasBusDcvs", "busComponents", "Lcom/zuan/kernelmanager/ui/gpu/AdrenoViewModel$BusState;", "<init>", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/util/List;Ljava/util/List;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;ZLjava/lang/String;ZZLjava/lang/String;Ljava/lang/String;Ljava/lang/String;ZZLjava/lang/String;Ljava/lang/String;ZLjava/util/List;)V", "getMinFreq", "()Ljava/lang/String;", "getMaxFreq", "getCurrentFreq", "getGov", "getAvailableFreq", "()Ljava/util/List;", "getAvailableGov", "getMaxPwrlevel", "getMinPwrlevel", "getDefaultPwrlevel", "getAdrenoBoost", "getHasGpuThrottling", "()Z", "getGpuThrottling", "getHasAdrenoIdler", "getIdlerActive", "getIdlerIdleWait", "getIdlerDownDiff", "getIdlerWorkload", "getHasSimpleGpu", "getSimpleGpuActive", "getSimpleLaziness", "getSimpleRampThreshold", "getHasBusDcvs", "getBusComponents", "component1", "component2", "component3", "component4", "component5", "component6", "component7", "component8", "component9", "component10", "component11", "component12", "component13", "component14", "component15", "component16", "component17", "component18", "component19", "component20", "component21", "component22", "component23", "copy", "equals", "other", "hashCode", "", "toString", "app_debug"})
    public static final class AdrenoState {
        @org.jetbrains.annotations.NotNull()
        private final java.lang.String minFreq = null;
        @org.jetbrains.annotations.NotNull()
        private final java.lang.String maxFreq = null;
        @org.jetbrains.annotations.NotNull()
        private final java.lang.String currentFreq = null;
        @org.jetbrains.annotations.NotNull()
        private final java.lang.String gov = null;
        @org.jetbrains.annotations.NotNull()
        private final java.util.List<java.lang.String> availableFreq = null;
        @org.jetbrains.annotations.NotNull()
        private final java.util.List<java.lang.String> availableGov = null;
        @org.jetbrains.annotations.NotNull()
        private final java.lang.String maxPwrlevel = null;
        @org.jetbrains.annotations.NotNull()
        private final java.lang.String minPwrlevel = null;
        @org.jetbrains.annotations.NotNull()
        private final java.lang.String defaultPwrlevel = null;
        @org.jetbrains.annotations.NotNull()
        private final java.lang.String adrenoBoost = null;
        private final boolean hasGpuThrottling = false;
        @org.jetbrains.annotations.NotNull()
        private final java.lang.String gpuThrottling = null;
        private final boolean hasAdrenoIdler = false;
        private final boolean idlerActive = false;
        @org.jetbrains.annotations.NotNull()
        private final java.lang.String idlerIdleWait = null;
        @org.jetbrains.annotations.NotNull()
        private final java.lang.String idlerDownDiff = null;
        @org.jetbrains.annotations.NotNull()
        private final java.lang.String idlerWorkload = null;
        private final boolean hasSimpleGpu = false;
        private final boolean simpleGpuActive = false;
        @org.jetbrains.annotations.NotNull()
        private final java.lang.String simpleLaziness = null;
        @org.jetbrains.annotations.NotNull()
        private final java.lang.String simpleRampThreshold = null;
        private final boolean hasBusDcvs = false;
        @org.jetbrains.annotations.NotNull()
        private final java.util.List<com.zuan.kernelmanager.ui.gpu.AdrenoViewModel.BusState> busComponents = null;
        
        public AdrenoState(@org.jetbrains.annotations.NotNull()
        java.lang.String minFreq, @org.jetbrains.annotations.NotNull()
        java.lang.String maxFreq, @org.jetbrains.annotations.NotNull()
        java.lang.String currentFreq, @org.jetbrains.annotations.NotNull()
        java.lang.String gov, @org.jetbrains.annotations.NotNull()
        java.util.List<java.lang.String> availableFreq, @org.jetbrains.annotations.NotNull()
        java.util.List<java.lang.String> availableGov, @org.jetbrains.annotations.NotNull()
        java.lang.String maxPwrlevel, @org.jetbrains.annotations.NotNull()
        java.lang.String minPwrlevel, @org.jetbrains.annotations.NotNull()
        java.lang.String defaultPwrlevel, @org.jetbrains.annotations.NotNull()
        java.lang.String adrenoBoost, boolean hasGpuThrottling, @org.jetbrains.annotations.NotNull()
        java.lang.String gpuThrottling, boolean hasAdrenoIdler, boolean idlerActive, @org.jetbrains.annotations.NotNull()
        java.lang.String idlerIdleWait, @org.jetbrains.annotations.NotNull()
        java.lang.String idlerDownDiff, @org.jetbrains.annotations.NotNull()
        java.lang.String idlerWorkload, boolean hasSimpleGpu, boolean simpleGpuActive, @org.jetbrains.annotations.NotNull()
        java.lang.String simpleLaziness, @org.jetbrains.annotations.NotNull()
        java.lang.String simpleRampThreshold, boolean hasBusDcvs, @org.jetbrains.annotations.NotNull()
        java.util.List<com.zuan.kernelmanager.ui.gpu.AdrenoViewModel.BusState> busComponents) {
            super();
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
        public final java.lang.String getCurrentFreq() {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final java.lang.String getGov() {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final java.util.List<java.lang.String> getAvailableFreq() {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final java.util.List<java.lang.String> getAvailableGov() {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final java.lang.String getMaxPwrlevel() {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final java.lang.String getMinPwrlevel() {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final java.lang.String getDefaultPwrlevel() {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final java.lang.String getAdrenoBoost() {
            return null;
        }
        
        public final boolean getHasGpuThrottling() {
            return false;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final java.lang.String getGpuThrottling() {
            return null;
        }
        
        public final boolean getHasAdrenoIdler() {
            return false;
        }
        
        public final boolean getIdlerActive() {
            return false;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final java.lang.String getIdlerIdleWait() {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final java.lang.String getIdlerDownDiff() {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final java.lang.String getIdlerWorkload() {
            return null;
        }
        
        public final boolean getHasSimpleGpu() {
            return false;
        }
        
        public final boolean getSimpleGpuActive() {
            return false;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final java.lang.String getSimpleLaziness() {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final java.lang.String getSimpleRampThreshold() {
            return null;
        }
        
        public final boolean getHasBusDcvs() {
            return false;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final java.util.List<com.zuan.kernelmanager.ui.gpu.AdrenoViewModel.BusState> getBusComponents() {
            return null;
        }
        
        public AdrenoState() {
            super();
        }
        
        @org.jetbrains.annotations.NotNull()
        public final java.lang.String component1() {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final java.lang.String component10() {
            return null;
        }
        
        public final boolean component11() {
            return false;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final java.lang.String component12() {
            return null;
        }
        
        public final boolean component13() {
            return false;
        }
        
        public final boolean component14() {
            return false;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final java.lang.String component15() {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final java.lang.String component16() {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final java.lang.String component17() {
            return null;
        }
        
        public final boolean component18() {
            return false;
        }
        
        public final boolean component19() {
            return false;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final java.lang.String component2() {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final java.lang.String component20() {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final java.lang.String component21() {
            return null;
        }
        
        public final boolean component22() {
            return false;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final java.util.List<com.zuan.kernelmanager.ui.gpu.AdrenoViewModel.BusState> component23() {
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
        public final java.util.List<java.lang.String> component5() {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final java.util.List<java.lang.String> component6() {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final java.lang.String component7() {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final java.lang.String component8() {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final java.lang.String component9() {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final com.zuan.kernelmanager.ui.gpu.AdrenoViewModel.AdrenoState copy(@org.jetbrains.annotations.NotNull()
        java.lang.String minFreq, @org.jetbrains.annotations.NotNull()
        java.lang.String maxFreq, @org.jetbrains.annotations.NotNull()
        java.lang.String currentFreq, @org.jetbrains.annotations.NotNull()
        java.lang.String gov, @org.jetbrains.annotations.NotNull()
        java.util.List<java.lang.String> availableFreq, @org.jetbrains.annotations.NotNull()
        java.util.List<java.lang.String> availableGov, @org.jetbrains.annotations.NotNull()
        java.lang.String maxPwrlevel, @org.jetbrains.annotations.NotNull()
        java.lang.String minPwrlevel, @org.jetbrains.annotations.NotNull()
        java.lang.String defaultPwrlevel, @org.jetbrains.annotations.NotNull()
        java.lang.String adrenoBoost, boolean hasGpuThrottling, @org.jetbrains.annotations.NotNull()
        java.lang.String gpuThrottling, boolean hasAdrenoIdler, boolean idlerActive, @org.jetbrains.annotations.NotNull()
        java.lang.String idlerIdleWait, @org.jetbrains.annotations.NotNull()
        java.lang.String idlerDownDiff, @org.jetbrains.annotations.NotNull()
        java.lang.String idlerWorkload, boolean hasSimpleGpu, boolean simpleGpuActive, @org.jetbrains.annotations.NotNull()
        java.lang.String simpleLaziness, @org.jetbrains.annotations.NotNull()
        java.lang.String simpleRampThreshold, boolean hasBusDcvs, @org.jetbrains.annotations.NotNull()
        java.util.List<com.zuan.kernelmanager.ui.gpu.AdrenoViewModel.BusState> busComponents) {
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
    
    @kotlin.Metadata(mv = {2, 2, 0}, k = 1, xi = 48, d1 = {"\u0000*\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0003\n\u0002\u0010 \n\u0002\b\u000e\n\u0002\u0010\u000b\n\u0002\b\u0002\n\u0002\u0010\b\n\u0002\b\u0002\b\u0086\b\u0018\u00002\u00020\u0001B-\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0003\u0012\u0006\u0010\u0005\u001a\u00020\u0003\u0012\f\u0010\u0006\u001a\b\u0012\u0004\u0012\u00020\u00030\u0007\u00a2\u0006\u0004\b\b\u0010\tJ\t\u0010\u0010\u001a\u00020\u0003H\u00c6\u0003J\t\u0010\u0011\u001a\u00020\u0003H\u00c6\u0003J\t\u0010\u0012\u001a\u00020\u0003H\u00c6\u0003J\u000f\u0010\u0013\u001a\b\u0012\u0004\u0012\u00020\u00030\u0007H\u00c6\u0003J7\u0010\u0014\u001a\u00020\u00002\b\b\u0002\u0010\u0002\u001a\u00020\u00032\b\b\u0002\u0010\u0004\u001a\u00020\u00032\b\b\u0002\u0010\u0005\u001a\u00020\u00032\u000e\b\u0002\u0010\u0006\u001a\b\u0012\u0004\u0012\u00020\u00030\u0007H\u00c6\u0001J\u0013\u0010\u0015\u001a\u00020\u00162\b\u0010\u0017\u001a\u0004\u0018\u00010\u0001H\u00d6\u0003J\t\u0010\u0018\u001a\u00020\u0019H\u00d6\u0001J\t\u0010\u001a\u001a\u00020\u0003H\u00d6\u0001R\u0011\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\n\u0010\u000bR\u0011\u0010\u0004\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\f\u0010\u000bR\u0011\u0010\u0005\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\r\u0010\u000bR\u0017\u0010\u0006\u001a\b\u0012\u0004\u0012\u00020\u00030\u0007\u00a2\u0006\b\n\u0000\u001a\u0004\b\u000e\u0010\u000f\u00a8\u0006\u001b"}, d2 = {"Lcom/zuan/kernelmanager/ui/gpu/AdrenoViewModel$BusState;", "", "name", "", "minFreq", "maxFreq", "availableFreqs", "", "<init>", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/util/List;)V", "getName", "()Ljava/lang/String;", "getMinFreq", "getMaxFreq", "getAvailableFreqs", "()Ljava/util/List;", "component1", "component2", "component3", "component4", "copy", "equals", "", "other", "hashCode", "", "toString", "app_debug"})
    public static final class BusState {
        @org.jetbrains.annotations.NotNull()
        private final java.lang.String name = null;
        @org.jetbrains.annotations.NotNull()
        private final java.lang.String minFreq = null;
        @org.jetbrains.annotations.NotNull()
        private final java.lang.String maxFreq = null;
        @org.jetbrains.annotations.NotNull()
        private final java.util.List<java.lang.String> availableFreqs = null;
        
        public BusState(@org.jetbrains.annotations.NotNull()
        java.lang.String name, @org.jetbrains.annotations.NotNull()
        java.lang.String minFreq, @org.jetbrains.annotations.NotNull()
        java.lang.String maxFreq, @org.jetbrains.annotations.NotNull()
        java.util.List<java.lang.String> availableFreqs) {
            super();
        }
        
        @org.jetbrains.annotations.NotNull()
        public final java.lang.String getName() {
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
        public final java.util.List<java.lang.String> getAvailableFreqs() {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final java.lang.String component1() {
            return null;
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
        public final java.util.List<java.lang.String> component4() {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final com.zuan.kernelmanager.ui.gpu.AdrenoViewModel.BusState copy(@org.jetbrains.annotations.NotNull()
        java.lang.String name, @org.jetbrains.annotations.NotNull()
        java.lang.String minFreq, @org.jetbrains.annotations.NotNull()
        java.lang.String maxFreq, @org.jetbrains.annotations.NotNull()
        java.util.List<java.lang.String> availableFreqs) {
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