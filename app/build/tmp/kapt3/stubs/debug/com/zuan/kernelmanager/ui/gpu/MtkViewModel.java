package com.zuan.kernelmanager.ui.gpu;

@kotlin.Metadata(mv = {2, 2, 0}, k = 1, xi = 48, d1 = {"\u0000<\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\u0002\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0002\b\u0005\n\u0002\u0010\u000b\n\u0002\b\u000e\u0018\u00002\u00020\u0001:\u0002#$B\u000f\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0004\b\u0004\u0010\u0005J\u0006\u0010\r\u001a\u00020\u000eJ\u000e\u0010\u000f\u001a\u00020\u000e2\u0006\u0010\u0010\u001a\u00020\u0011J\u0006\u0010\u0012\u001a\u00020\u000eJ\u000e\u0010\u0013\u001a\u00020\u000e2\u0006\u0010\u0010\u001a\u00020\u0011J\u0016\u0010\u0014\u001a\u00020\u000e2\u0006\u0010\u0015\u001a\u00020\u00112\u0006\u0010\u0016\u001a\u00020\u0017J\u000e\u0010\u0018\u001a\u00020\u000e2\u0006\u0010\u0019\u001a\u00020\u0011J\u000e\u0010\u001a\u001a\u00020\u000e2\u0006\u0010\u0019\u001a\u00020\u0011J\u000e\u0010\u001b\u001a\u00020\u000e2\u0006\u0010\u0019\u001a\u00020\u0011J\u0016\u0010\u001c\u001a\u00020\u000e2\u0006\u0010\u001d\u001a\u00020\u00112\u0006\u0010\u0016\u001a\u00020\u0017J\u0016\u0010\u001e\u001a\u00020\u000e2\u0006\u0010\u001f\u001a\u00020\u00112\u0006\u0010 \u001a\u00020\u0011J\u000e\u0010!\u001a\u00020\u000e2\u0006\u0010\"\u001a\u00020\u0011R\u0014\u0010\u0006\u001a\b\u0012\u0004\u0012\u00020\b0\u0007X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0017\u0010\t\u001a\b\u0012\u0004\u0012\u00020\b0\n\u00a2\u0006\b\n\u0000\u001a\u0004\b\u000b\u0010\f\u00a8\u0006%"}, d2 = {"Lcom/zuan/kernelmanager/ui/gpu/MtkViewModel;", "Landroidx/lifecycle/AndroidViewModel;", "application", "Landroid/app/Application;", "<init>", "(Landroid/app/Application;)V", "_state", "Lkotlinx/coroutines/flow/MutableStateFlow;", "Lcom/zuan/kernelmanager/ui/gpu/MtkViewModel$MtkState;", "state", "Lkotlinx/coroutines/flow/StateFlow;", "getState", "()Lkotlinx/coroutines/flow/StateFlow;", "loadData", "", "updateFixedFreq", "selectedFreq", "", "resetFixedFreq", "updateMaxFreq", "toggleFeature", "feature", "enable", "", "setCciMode", "mode", "setPowerMode", "setSchedBoost", "togglePowerPolicy", "key", "setDramFreq", "displayFreq", "target", "setDramGov", "gov", "DramState", "MtkState", "app_debug"})
public final class MtkViewModel extends androidx.lifecycle.AndroidViewModel {
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<com.zuan.kernelmanager.ui.gpu.MtkViewModel.MtkState> _state = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<com.zuan.kernelmanager.ui.gpu.MtkViewModel.MtkState> state = null;
    
    public MtkViewModel(@org.jetbrains.annotations.NotNull()
    android.app.Application application) {
        super(null);
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<com.zuan.kernelmanager.ui.gpu.MtkViewModel.MtkState> getState() {
        return null;
    }
    
    public final void loadData() {
    }
    
    public final void updateFixedFreq(@org.jetbrains.annotations.NotNull()
    java.lang.String selectedFreq) {
    }
    
    public final void resetFixedFreq() {
    }
    
    public final void updateMaxFreq(@org.jetbrains.annotations.NotNull()
    java.lang.String selectedFreq) {
    }
    
    public final void toggleFeature(@org.jetbrains.annotations.NotNull()
    java.lang.String feature, boolean enable) {
    }
    
    public final void setCciMode(@org.jetbrains.annotations.NotNull()
    java.lang.String mode) {
    }
    
    public final void setPowerMode(@org.jetbrains.annotations.NotNull()
    java.lang.String mode) {
    }
    
    public final void setSchedBoost(@org.jetbrains.annotations.NotNull()
    java.lang.String mode) {
    }
    
    public final void togglePowerPolicy(@org.jetbrains.annotations.NotNull()
    java.lang.String key, boolean enable) {
    }
    
    public final void setDramFreq(@org.jetbrains.annotations.NotNull()
    java.lang.String displayFreq, @org.jetbrains.annotations.NotNull()
    java.lang.String target) {
    }
    
    public final void setDramGov(@org.jetbrains.annotations.NotNull()
    java.lang.String gov) {
    }
    
    @kotlin.Metadata(mv = {2, 2, 0}, k = 1, xi = 48, d1 = {"\u00002\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010 \n\u0002\u0010\u000e\n\u0000\n\u0002\u0010$\n\u0002\b\u001d\n\u0002\u0010\u000b\n\u0002\b\u0002\n\u0002\u0010\b\n\u0002\b\u0002\b\u0086\b\u0018\u00002\u00020\u0001Bo\u0012\b\b\u0002\u0010\u0002\u001a\u00020\u0003\u0012\u000e\b\u0002\u0010\u0004\u001a\b\u0012\u0004\u0012\u00020\u00060\u0005\u0012\u0014\b\u0002\u0010\u0007\u001a\u000e\u0012\u0004\u0012\u00020\u0006\u0012\u0004\u0012\u00020\u00060\b\u0012\b\b\u0002\u0010\t\u001a\u00020\u0006\u0012\u000e\b\u0002\u0010\n\u001a\b\u0012\u0004\u0012\u00020\u00060\u0005\u0012\b\b\u0002\u0010\u000b\u001a\u00020\u0006\u0012\b\b\u0002\u0010\f\u001a\u00020\u0006\u0012\b\b\u0002\u0010\r\u001a\u00020\u0006\u00a2\u0006\u0004\b\u000e\u0010\u000fJ\t\u0010\u001c\u001a\u00020\u0003H\u00c6\u0003J\u000f\u0010\u001d\u001a\b\u0012\u0004\u0012\u00020\u00060\u0005H\u00c6\u0003J\u0015\u0010\u001e\u001a\u000e\u0012\u0004\u0012\u00020\u0006\u0012\u0004\u0012\u00020\u00060\bH\u00c6\u0003J\t\u0010\u001f\u001a\u00020\u0006H\u00c6\u0003J\u000f\u0010 \u001a\b\u0012\u0004\u0012\u00020\u00060\u0005H\u00c6\u0003J\t\u0010!\u001a\u00020\u0006H\u00c6\u0003J\t\u0010\"\u001a\u00020\u0006H\u00c6\u0003J\t\u0010#\u001a\u00020\u0006H\u00c6\u0003Jq\u0010$\u001a\u00020\u00002\b\b\u0002\u0010\u0002\u001a\u00020\u00032\u000e\b\u0002\u0010\u0004\u001a\b\u0012\u0004\u0012\u00020\u00060\u00052\u0014\b\u0002\u0010\u0007\u001a\u000e\u0012\u0004\u0012\u00020\u0006\u0012\u0004\u0012\u00020\u00060\b2\b\b\u0002\u0010\t\u001a\u00020\u00062\u000e\b\u0002\u0010\n\u001a\b\u0012\u0004\u0012\u00020\u00060\u00052\b\b\u0002\u0010\u000b\u001a\u00020\u00062\b\b\u0002\u0010\f\u001a\u00020\u00062\b\b\u0002\u0010\r\u001a\u00020\u0006H\u00c6\u0001J\u0013\u0010%\u001a\u00020&2\b\u0010\'\u001a\u0004\u0018\u00010\u0001H\u00d6\u0003J\t\u0010(\u001a\u00020)H\u00d6\u0001J\t\u0010*\u001a\u00020\u0006H\u00d6\u0001R\u0011\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0010\u0010\u0011R\u0017\u0010\u0004\u001a\b\u0012\u0004\u0012\u00020\u00060\u0005\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0012\u0010\u0013R\u001d\u0010\u0007\u001a\u000e\u0012\u0004\u0012\u00020\u0006\u0012\u0004\u0012\u00020\u00060\b\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0014\u0010\u0015R\u0011\u0010\t\u001a\u00020\u0006\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0016\u0010\u0017R\u0017\u0010\n\u001a\b\u0012\u0004\u0012\u00020\u00060\u0005\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0018\u0010\u0013R\u0011\u0010\u000b\u001a\u00020\u0006\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0019\u0010\u0017R\u0011\u0010\f\u001a\u00020\u0006\u00a2\u0006\b\n\u0000\u001a\u0004\b\u001a\u0010\u0017R\u0011\u0010\r\u001a\u00020\u0006\u00a2\u0006\b\n\u0000\u001a\u0004\b\u001b\u0010\u0017\u00a8\u0006+"}, d2 = {"Lcom/zuan/kernelmanager/ui/gpu/MtkViewModel$DramState;", "", "type", "Lcom/zuan/kernelmanager/utils/MtkUtils$DramType;", "availableFreqsDisplay", "", "", "freqMap", "", "currentGov", "availableGovs", "minFreq", "maxFreq", "currentIndex", "<init>", "(Lcom/zuan/kernelmanager/utils/MtkUtils$DramType;Ljava/util/List;Ljava/util/Map;Ljava/lang/String;Ljava/util/List;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)V", "getType", "()Lcom/zuan/kernelmanager/utils/MtkUtils$DramType;", "getAvailableFreqsDisplay", "()Ljava/util/List;", "getFreqMap", "()Ljava/util/Map;", "getCurrentGov", "()Ljava/lang/String;", "getAvailableGovs", "getMinFreq", "getMaxFreq", "getCurrentIndex", "component1", "component2", "component3", "component4", "component5", "component6", "component7", "component8", "copy", "equals", "", "other", "hashCode", "", "toString", "app_debug"})
    public static final class DramState {
        @org.jetbrains.annotations.NotNull()
        private final com.zuan.kernelmanager.utils.MtkUtils.DramType type = null;
        @org.jetbrains.annotations.NotNull()
        private final java.util.List<java.lang.String> availableFreqsDisplay = null;
        @org.jetbrains.annotations.NotNull()
        private final java.util.Map<java.lang.String, java.lang.String> freqMap = null;
        @org.jetbrains.annotations.NotNull()
        private final java.lang.String currentGov = null;
        @org.jetbrains.annotations.NotNull()
        private final java.util.List<java.lang.String> availableGovs = null;
        @org.jetbrains.annotations.NotNull()
        private final java.lang.String minFreq = null;
        @org.jetbrains.annotations.NotNull()
        private final java.lang.String maxFreq = null;
        @org.jetbrains.annotations.NotNull()
        private final java.lang.String currentIndex = null;
        
        public DramState(@org.jetbrains.annotations.NotNull()
        com.zuan.kernelmanager.utils.MtkUtils.DramType type, @org.jetbrains.annotations.NotNull()
        java.util.List<java.lang.String> availableFreqsDisplay, @org.jetbrains.annotations.NotNull()
        java.util.Map<java.lang.String, java.lang.String> freqMap, @org.jetbrains.annotations.NotNull()
        java.lang.String currentGov, @org.jetbrains.annotations.NotNull()
        java.util.List<java.lang.String> availableGovs, @org.jetbrains.annotations.NotNull()
        java.lang.String minFreq, @org.jetbrains.annotations.NotNull()
        java.lang.String maxFreq, @org.jetbrains.annotations.NotNull()
        java.lang.String currentIndex) {
            super();
        }
        
        @org.jetbrains.annotations.NotNull()
        public final com.zuan.kernelmanager.utils.MtkUtils.DramType getType() {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final java.util.List<java.lang.String> getAvailableFreqsDisplay() {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final java.util.Map<java.lang.String, java.lang.String> getFreqMap() {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final java.lang.String getCurrentGov() {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final java.util.List<java.lang.String> getAvailableGovs() {
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
        public final java.lang.String getCurrentIndex() {
            return null;
        }
        
        public DramState() {
            super();
        }
        
        @org.jetbrains.annotations.NotNull()
        public final com.zuan.kernelmanager.utils.MtkUtils.DramType component1() {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final java.util.List<java.lang.String> component2() {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final java.util.Map<java.lang.String, java.lang.String> component3() {
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
        public final java.lang.String component6() {
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
        public final com.zuan.kernelmanager.ui.gpu.MtkViewModel.DramState copy(@org.jetbrains.annotations.NotNull()
        com.zuan.kernelmanager.utils.MtkUtils.DramType type, @org.jetbrains.annotations.NotNull()
        java.util.List<java.lang.String> availableFreqsDisplay, @org.jetbrains.annotations.NotNull()
        java.util.Map<java.lang.String, java.lang.String> freqMap, @org.jetbrains.annotations.NotNull()
        java.lang.String currentGov, @org.jetbrains.annotations.NotNull()
        java.util.List<java.lang.String> availableGovs, @org.jetbrains.annotations.NotNull()
        java.lang.String minFreq, @org.jetbrains.annotations.NotNull()
        java.lang.String maxFreq, @org.jetbrains.annotations.NotNull()
        java.lang.String currentIndex) {
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
    
    @kotlin.Metadata(mv = {2, 2, 0}, k = 1, xi = 48, d1 = {"\u0000<\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\u000e\n\u0000\n\u0002\u0010 \n\u0000\n\u0002\u0010$\n\u0002\b\u0003\n\u0002\u0010\u000b\n\u0002\b\u000e\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b/\n\u0002\u0010\b\n\u0002\b\u0002\b\u0086\b\u0018\u00002\u00020\u0001B\u00eb\u0001\u0012\b\b\u0002\u0010\u0002\u001a\u00020\u0003\u0012\u000e\b\u0002\u0010\u0004\u001a\b\u0012\u0004\u0012\u00020\u00030\u0005\u0012\u0014\b\u0002\u0010\u0006\u001a\u000e\u0012\u0004\u0012\u00020\u0003\u0012\u0004\u0012\u00020\u00030\u0007\u0012\b\b\u0002\u0010\b\u001a\u00020\u0003\u0012\b\b\u0002\u0010\t\u001a\u00020\u0003\u0012\b\b\u0002\u0010\n\u001a\u00020\u000b\u0012\b\b\u0002\u0010\f\u001a\u00020\u000b\u0012\b\b\u0002\u0010\r\u001a\u00020\u000b\u0012\b\b\u0002\u0010\u000e\u001a\u00020\u000b\u0012\b\b\u0002\u0010\u000f\u001a\u00020\u000b\u0012\b\b\u0002\u0010\u0010\u001a\u00020\u000b\u0012\b\b\u0002\u0010\u0011\u001a\u00020\u000b\u0012\b\b\u0002\u0010\u0012\u001a\u00020\u0003\u0012\b\b\u0002\u0010\u0013\u001a\u00020\u000b\u0012\b\b\u0002\u0010\u0014\u001a\u00020\u0003\u0012\b\b\u0002\u0010\u0015\u001a\u00020\u000b\u0012\b\b\u0002\u0010\u0016\u001a\u00020\u0003\u0012\b\b\u0002\u0010\u0017\u001a\u00020\u000b\u0012\b\b\u0002\u0010\u0018\u001a\u00020\u000b\u0012\b\b\u0002\u0010\u0019\u001a\u00020\u001a\u0012\b\b\u0002\u0010\u001b\u001a\u00020\u001c\u00a2\u0006\u0004\b\u001d\u0010\u001eJ\t\u00103\u001a\u00020\u0003H\u00c6\u0003J\u000f\u00104\u001a\b\u0012\u0004\u0012\u00020\u00030\u0005H\u00c6\u0003J\u0015\u00105\u001a\u000e\u0012\u0004\u0012\u00020\u0003\u0012\u0004\u0012\u00020\u00030\u0007H\u00c6\u0003J\t\u00106\u001a\u00020\u0003H\u00c6\u0003J\t\u00107\u001a\u00020\u0003H\u00c6\u0003J\t\u00108\u001a\u00020\u000bH\u00c6\u0003J\t\u00109\u001a\u00020\u000bH\u00c6\u0003J\t\u0010:\u001a\u00020\u000bH\u00c6\u0003J\t\u0010;\u001a\u00020\u000bH\u00c6\u0003J\t\u0010<\u001a\u00020\u000bH\u00c6\u0003J\t\u0010=\u001a\u00020\u000bH\u00c6\u0003J\t\u0010>\u001a\u00020\u000bH\u00c6\u0003J\t\u0010?\u001a\u00020\u0003H\u00c6\u0003J\t\u0010@\u001a\u00020\u000bH\u00c6\u0003J\t\u0010A\u001a\u00020\u0003H\u00c6\u0003J\t\u0010B\u001a\u00020\u000bH\u00c6\u0003J\t\u0010C\u001a\u00020\u0003H\u00c6\u0003J\t\u0010D\u001a\u00020\u000bH\u00c6\u0003J\t\u0010E\u001a\u00020\u000bH\u00c6\u0003J\t\u0010F\u001a\u00020\u001aH\u00c6\u0003J\t\u0010G\u001a\u00020\u001cH\u00c6\u0003J\u00ed\u0001\u0010H\u001a\u00020\u00002\b\b\u0002\u0010\u0002\u001a\u00020\u00032\u000e\b\u0002\u0010\u0004\u001a\b\u0012\u0004\u0012\u00020\u00030\u00052\u0014\b\u0002\u0010\u0006\u001a\u000e\u0012\u0004\u0012\u00020\u0003\u0012\u0004\u0012\u00020\u00030\u00072\b\b\u0002\u0010\b\u001a\u00020\u00032\b\b\u0002\u0010\t\u001a\u00020\u00032\b\b\u0002\u0010\n\u001a\u00020\u000b2\b\b\u0002\u0010\f\u001a\u00020\u000b2\b\b\u0002\u0010\r\u001a\u00020\u000b2\b\b\u0002\u0010\u000e\u001a\u00020\u000b2\b\b\u0002\u0010\u000f\u001a\u00020\u000b2\b\b\u0002\u0010\u0010\u001a\u00020\u000b2\b\b\u0002\u0010\u0011\u001a\u00020\u000b2\b\b\u0002\u0010\u0012\u001a\u00020\u00032\b\b\u0002\u0010\u0013\u001a\u00020\u000b2\b\b\u0002\u0010\u0014\u001a\u00020\u00032\b\b\u0002\u0010\u0015\u001a\u00020\u000b2\b\b\u0002\u0010\u0016\u001a\u00020\u00032\b\b\u0002\u0010\u0017\u001a\u00020\u000b2\b\b\u0002\u0010\u0018\u001a\u00020\u000b2\b\b\u0002\u0010\u0019\u001a\u00020\u001a2\b\b\u0002\u0010\u001b\u001a\u00020\u001cH\u00c6\u0001J\u0013\u0010I\u001a\u00020\u000b2\b\u0010J\u001a\u0004\u0018\u00010\u0001H\u00d6\u0003J\t\u0010K\u001a\u00020LH\u00d6\u0001J\t\u0010M\u001a\u00020\u0003H\u00d6\u0001R\u0011\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u001f\u0010 R\u0017\u0010\u0004\u001a\b\u0012\u0004\u0012\u00020\u00030\u0005\u00a2\u0006\b\n\u0000\u001a\u0004\b!\u0010\"R\u001d\u0010\u0006\u001a\u000e\u0012\u0004\u0012\u00020\u0003\u0012\u0004\u0012\u00020\u00030\u0007\u00a2\u0006\b\n\u0000\u001a\u0004\b#\u0010$R\u0011\u0010\b\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b%\u0010 R\u0011\u0010\t\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b&\u0010 R\u0011\u0010\n\u001a\u00020\u000b\u00a2\u0006\b\n\u0000\u001a\u0004\b\n\u0010\'R\u0011\u0010\f\u001a\u00020\u000b\u00a2\u0006\b\n\u0000\u001a\u0004\b\f\u0010\'R\u0011\u0010\r\u001a\u00020\u000b\u00a2\u0006\b\n\u0000\u001a\u0004\b\r\u0010\'R\u0011\u0010\u000e\u001a\u00020\u000b\u00a2\u0006\b\n\u0000\u001a\u0004\b\u000e\u0010\'R\u0011\u0010\u000f\u001a\u00020\u000b\u00a2\u0006\b\n\u0000\u001a\u0004\b\u000f\u0010\'R\u0011\u0010\u0010\u001a\u00020\u000b\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0010\u0010\'R\u0011\u0010\u0011\u001a\u00020\u000b\u00a2\u0006\b\n\u0000\u001a\u0004\b(\u0010\'R\u0011\u0010\u0012\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b)\u0010 R\u0011\u0010\u0013\u001a\u00020\u000b\u00a2\u0006\b\n\u0000\u001a\u0004\b*\u0010\'R\u0011\u0010\u0014\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b+\u0010 R\u0011\u0010\u0015\u001a\u00020\u000b\u00a2\u0006\b\n\u0000\u001a\u0004\b,\u0010\'R\u0011\u0010\u0016\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b-\u0010 R\u0011\u0010\u0017\u001a\u00020\u000b\u00a2\u0006\b\n\u0000\u001a\u0004\b.\u0010\'R\u0011\u0010\u0018\u001a\u00020\u000b\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0018\u0010\'R\u0011\u0010\u0019\u001a\u00020\u001a\u00a2\u0006\b\n\u0000\u001a\u0004\b/\u00100R\u0011\u0010\u001b\u001a\u00020\u001c\u00a2\u0006\b\n\u0000\u001a\u0004\b1\u00102\u00a8\u0006N"}, d2 = {"Lcom/zuan/kernelmanager/ui/gpu/MtkViewModel$MtkState;", "", "currentFreq", "", "availableFreq", "", "mtkFreqMap", "", "mtkFixedIndex", "mtkMaxIndex", "isFpsGoEnabled", "", "isGedKpiEnabled", "isPerfmgrEnabled", "isGedBoostEnabled", "isGedGameMode", "isGedGpuBoost", "hasCciMode", "cciMode", "hasPowerMode", "powerMode", "hasSchedBoost", "schedBoost", "hasPpm", "isPpmEnabled", "powerPolicy", "Lcom/zuan/kernelmanager/utils/MtkUtils$MtkPowerPolicy;", "dramState", "Lcom/zuan/kernelmanager/ui/gpu/MtkViewModel$DramState;", "<init>", "(Ljava/lang/String;Ljava/util/List;Ljava/util/Map;Ljava/lang/String;Ljava/lang/String;ZZZZZZZLjava/lang/String;ZLjava/lang/String;ZLjava/lang/String;ZZLcom/zuan/kernelmanager/utils/MtkUtils$MtkPowerPolicy;Lcom/zuan/kernelmanager/ui/gpu/MtkViewModel$DramState;)V", "getCurrentFreq", "()Ljava/lang/String;", "getAvailableFreq", "()Ljava/util/List;", "getMtkFreqMap", "()Ljava/util/Map;", "getMtkFixedIndex", "getMtkMaxIndex", "()Z", "getHasCciMode", "getCciMode", "getHasPowerMode", "getPowerMode", "getHasSchedBoost", "getSchedBoost", "getHasPpm", "getPowerPolicy", "()Lcom/zuan/kernelmanager/utils/MtkUtils$MtkPowerPolicy;", "getDramState", "()Lcom/zuan/kernelmanager/ui/gpu/MtkViewModel$DramState;", "component1", "component2", "component3", "component4", "component5", "component6", "component7", "component8", "component9", "component10", "component11", "component12", "component13", "component14", "component15", "component16", "component17", "component18", "component19", "component20", "component21", "copy", "equals", "other", "hashCode", "", "toString", "app_debug"})
    public static final class MtkState {
        @org.jetbrains.annotations.NotNull()
        private final java.lang.String currentFreq = null;
        @org.jetbrains.annotations.NotNull()
        private final java.util.List<java.lang.String> availableFreq = null;
        @org.jetbrains.annotations.NotNull()
        private final java.util.Map<java.lang.String, java.lang.String> mtkFreqMap = null;
        @org.jetbrains.annotations.NotNull()
        private final java.lang.String mtkFixedIndex = null;
        @org.jetbrains.annotations.NotNull()
        private final java.lang.String mtkMaxIndex = null;
        private final boolean isFpsGoEnabled = false;
        private final boolean isGedKpiEnabled = false;
        private final boolean isPerfmgrEnabled = false;
        private final boolean isGedBoostEnabled = false;
        private final boolean isGedGameMode = false;
        private final boolean isGedGpuBoost = false;
        private final boolean hasCciMode = false;
        @org.jetbrains.annotations.NotNull()
        private final java.lang.String cciMode = null;
        private final boolean hasPowerMode = false;
        @org.jetbrains.annotations.NotNull()
        private final java.lang.String powerMode = null;
        private final boolean hasSchedBoost = false;
        @org.jetbrains.annotations.NotNull()
        private final java.lang.String schedBoost = null;
        private final boolean hasPpm = false;
        private final boolean isPpmEnabled = false;
        @org.jetbrains.annotations.NotNull()
        private final com.zuan.kernelmanager.utils.MtkUtils.MtkPowerPolicy powerPolicy = null;
        @org.jetbrains.annotations.NotNull()
        private final com.zuan.kernelmanager.ui.gpu.MtkViewModel.DramState dramState = null;
        
        public MtkState(@org.jetbrains.annotations.NotNull()
        java.lang.String currentFreq, @org.jetbrains.annotations.NotNull()
        java.util.List<java.lang.String> availableFreq, @org.jetbrains.annotations.NotNull()
        java.util.Map<java.lang.String, java.lang.String> mtkFreqMap, @org.jetbrains.annotations.NotNull()
        java.lang.String mtkFixedIndex, @org.jetbrains.annotations.NotNull()
        java.lang.String mtkMaxIndex, boolean isFpsGoEnabled, boolean isGedKpiEnabled, boolean isPerfmgrEnabled, boolean isGedBoostEnabled, boolean isGedGameMode, boolean isGedGpuBoost, boolean hasCciMode, @org.jetbrains.annotations.NotNull()
        java.lang.String cciMode, boolean hasPowerMode, @org.jetbrains.annotations.NotNull()
        java.lang.String powerMode, boolean hasSchedBoost, @org.jetbrains.annotations.NotNull()
        java.lang.String schedBoost, boolean hasPpm, boolean isPpmEnabled, @org.jetbrains.annotations.NotNull()
        com.zuan.kernelmanager.utils.MtkUtils.MtkPowerPolicy powerPolicy, @org.jetbrains.annotations.NotNull()
        com.zuan.kernelmanager.ui.gpu.MtkViewModel.DramState dramState) {
            super();
        }
        
        @org.jetbrains.annotations.NotNull()
        public final java.lang.String getCurrentFreq() {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final java.util.List<java.lang.String> getAvailableFreq() {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final java.util.Map<java.lang.String, java.lang.String> getMtkFreqMap() {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final java.lang.String getMtkFixedIndex() {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final java.lang.String getMtkMaxIndex() {
            return null;
        }
        
        public final boolean isFpsGoEnabled() {
            return false;
        }
        
        public final boolean isGedKpiEnabled() {
            return false;
        }
        
        public final boolean isPerfmgrEnabled() {
            return false;
        }
        
        public final boolean isGedBoostEnabled() {
            return false;
        }
        
        public final boolean isGedGameMode() {
            return false;
        }
        
        public final boolean isGedGpuBoost() {
            return false;
        }
        
        public final boolean getHasCciMode() {
            return false;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final java.lang.String getCciMode() {
            return null;
        }
        
        public final boolean getHasPowerMode() {
            return false;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final java.lang.String getPowerMode() {
            return null;
        }
        
        public final boolean getHasSchedBoost() {
            return false;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final java.lang.String getSchedBoost() {
            return null;
        }
        
        public final boolean getHasPpm() {
            return false;
        }
        
        public final boolean isPpmEnabled() {
            return false;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final com.zuan.kernelmanager.utils.MtkUtils.MtkPowerPolicy getPowerPolicy() {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final com.zuan.kernelmanager.ui.gpu.MtkViewModel.DramState getDramState() {
            return null;
        }
        
        public MtkState() {
            super();
        }
        
        @org.jetbrains.annotations.NotNull()
        public final java.lang.String component1() {
            return null;
        }
        
        public final boolean component10() {
            return false;
        }
        
        public final boolean component11() {
            return false;
        }
        
        public final boolean component12() {
            return false;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final java.lang.String component13() {
            return null;
        }
        
        public final boolean component14() {
            return false;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final java.lang.String component15() {
            return null;
        }
        
        public final boolean component16() {
            return false;
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
        public final java.util.List<java.lang.String> component2() {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final com.zuan.kernelmanager.utils.MtkUtils.MtkPowerPolicy component20() {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final com.zuan.kernelmanager.ui.gpu.MtkViewModel.DramState component21() {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final java.util.Map<java.lang.String, java.lang.String> component3() {
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
        
        public final boolean component6() {
            return false;
        }
        
        public final boolean component7() {
            return false;
        }
        
        public final boolean component8() {
            return false;
        }
        
        public final boolean component9() {
            return false;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final com.zuan.kernelmanager.ui.gpu.MtkViewModel.MtkState copy(@org.jetbrains.annotations.NotNull()
        java.lang.String currentFreq, @org.jetbrains.annotations.NotNull()
        java.util.List<java.lang.String> availableFreq, @org.jetbrains.annotations.NotNull()
        java.util.Map<java.lang.String, java.lang.String> mtkFreqMap, @org.jetbrains.annotations.NotNull()
        java.lang.String mtkFixedIndex, @org.jetbrains.annotations.NotNull()
        java.lang.String mtkMaxIndex, boolean isFpsGoEnabled, boolean isGedKpiEnabled, boolean isPerfmgrEnabled, boolean isGedBoostEnabled, boolean isGedGameMode, boolean isGedGpuBoost, boolean hasCciMode, @org.jetbrains.annotations.NotNull()
        java.lang.String cciMode, boolean hasPowerMode, @org.jetbrains.annotations.NotNull()
        java.lang.String powerMode, boolean hasSchedBoost, @org.jetbrains.annotations.NotNull()
        java.lang.String schedBoost, boolean hasPpm, boolean isPpmEnabled, @org.jetbrains.annotations.NotNull()
        com.zuan.kernelmanager.utils.MtkUtils.MtkPowerPolicy powerPolicy, @org.jetbrains.annotations.NotNull()
        com.zuan.kernelmanager.ui.gpu.MtkViewModel.DramState dramState) {
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