package com.zuan.kernelmanager.ui.navigation;

@kotlin.Metadata(mv = {2, 2, 0}, k = 1, xi = 48, d1 = {"\u0000H\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0014\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\b6\u0018\u00002\u00020\u0001:\u000b\u0010\u0011\u0012\u0013\u0014\u0015\u0016\u0017\u0018\u0019\u001aB)\b\u0004\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0003\u0012\u0006\u0010\u0005\u001a\u00020\u0006\u0012\u0006\u0010\u0007\u001a\u00020\u0006\u00a2\u0006\u0004\b\b\u0010\tR\u0011\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\n\u0010\u000bR\u0011\u0010\u0004\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\f\u0010\u000bR\u0011\u0010\u0005\u001a\u00020\u0006\u00a2\u0006\b\n\u0000\u001a\u0004\b\r\u0010\u000eR\u0011\u0010\u0007\u001a\u00020\u0006\u00a2\u0006\b\n\u0000\u001a\u0004\b\u000f\u0010\u000e\u0082\u0001\u000b\u001b\u001c\u001d\u001e\u001f !\"#$%\u00a8\u0006&"}, d2 = {"Lcom/zuan/kernelmanager/ui/navigation/NavigationRoute;", "", "route", "", "title", "selectedIcon", "Landroidx/compose/ui/graphics/vector/ImageVector;", "unselectedIcon", "<init>", "(Ljava/lang/String;Ljava/lang/String;Landroidx/compose/ui/graphics/vector/ImageVector;Landroidx/compose/ui/graphics/vector/ImageVector;)V", "getRoute", "()Ljava/lang/String;", "getTitle", "getSelectedIcon", "()Landroidx/compose/ui/graphics/vector/ImageVector;", "getUnselectedIcon", "Home", "SoC", "Battery", "KernelParameter", "Terminal", "SetEdit", "FpsManager", "ProcessManager", "Adreno", "Mtk", "GenericGpu", "Lcom/zuan/kernelmanager/ui/navigation/NavigationRoute$Adreno;", "Lcom/zuan/kernelmanager/ui/navigation/NavigationRoute$Battery;", "Lcom/zuan/kernelmanager/ui/navigation/NavigationRoute$FpsManager;", "Lcom/zuan/kernelmanager/ui/navigation/NavigationRoute$GenericGpu;", "Lcom/zuan/kernelmanager/ui/navigation/NavigationRoute$Home;", "Lcom/zuan/kernelmanager/ui/navigation/NavigationRoute$KernelParameter;", "Lcom/zuan/kernelmanager/ui/navigation/NavigationRoute$Mtk;", "Lcom/zuan/kernelmanager/ui/navigation/NavigationRoute$ProcessManager;", "Lcom/zuan/kernelmanager/ui/navigation/NavigationRoute$SetEdit;", "Lcom/zuan/kernelmanager/ui/navigation/NavigationRoute$SoC;", "Lcom/zuan/kernelmanager/ui/navigation/NavigationRoute$Terminal;", "app_debug"})
public abstract class NavigationRoute {
    @org.jetbrains.annotations.NotNull()
    private final java.lang.String route = null;
    @org.jetbrains.annotations.NotNull()
    private final java.lang.String title = null;
    @org.jetbrains.annotations.NotNull()
    private final androidx.compose.ui.graphics.vector.ImageVector selectedIcon = null;
    @org.jetbrains.annotations.NotNull()
    private final androidx.compose.ui.graphics.vector.ImageVector unselectedIcon = null;
    
    private NavigationRoute(java.lang.String route, java.lang.String title, androidx.compose.ui.graphics.vector.ImageVector selectedIcon, androidx.compose.ui.graphics.vector.ImageVector unselectedIcon) {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getRoute() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getTitle() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final androidx.compose.ui.graphics.vector.ImageVector getSelectedIcon() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final androidx.compose.ui.graphics.vector.ImageVector getUnselectedIcon() {
        return null;
    }
    
    @kotlin.Metadata(mv = {2, 2, 0}, k = 1, xi = 48, d1 = {"\u0000\f\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0003\b\u00c6\u0002\u0018\u00002\u00020\u0001B\t\b\u0002\u00a2\u0006\u0004\b\u0002\u0010\u0003\u00a8\u0006\u0004"}, d2 = {"Lcom/zuan/kernelmanager/ui/navigation/NavigationRoute$Adreno;", "Lcom/zuan/kernelmanager/ui/navigation/NavigationRoute;", "<init>", "()V", "app_debug"})
    public static final class Adreno extends com.zuan.kernelmanager.ui.navigation.NavigationRoute {
        @org.jetbrains.annotations.NotNull()
        public static final com.zuan.kernelmanager.ui.navigation.NavigationRoute.Adreno INSTANCE = null;
        
        private Adreno() {
        }
    }
    
    @kotlin.Metadata(mv = {2, 2, 0}, k = 1, xi = 48, d1 = {"\u0000\f\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0003\b\u00c6\u0002\u0018\u00002\u00020\u0001B\t\b\u0002\u00a2\u0006\u0004\b\u0002\u0010\u0003\u00a8\u0006\u0004"}, d2 = {"Lcom/zuan/kernelmanager/ui/navigation/NavigationRoute$Battery;", "Lcom/zuan/kernelmanager/ui/navigation/NavigationRoute;", "<init>", "()V", "app_debug"})
    public static final class Battery extends com.zuan.kernelmanager.ui.navigation.NavigationRoute {
        @org.jetbrains.annotations.NotNull()
        public static final com.zuan.kernelmanager.ui.navigation.NavigationRoute.Battery INSTANCE = null;
        
        private Battery() {
        }
    }
    
    @kotlin.Metadata(mv = {2, 2, 0}, k = 1, xi = 48, d1 = {"\u0000\f\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0003\b\u00c6\u0002\u0018\u00002\u00020\u0001B\t\b\u0002\u00a2\u0006\u0004\b\u0002\u0010\u0003\u00a8\u0006\u0004"}, d2 = {"Lcom/zuan/kernelmanager/ui/navigation/NavigationRoute$FpsManager;", "Lcom/zuan/kernelmanager/ui/navigation/NavigationRoute;", "<init>", "()V", "app_debug"})
    public static final class FpsManager extends com.zuan.kernelmanager.ui.navigation.NavigationRoute {
        @org.jetbrains.annotations.NotNull()
        public static final com.zuan.kernelmanager.ui.navigation.NavigationRoute.FpsManager INSTANCE = null;
        
        private FpsManager() {
        }
    }
    
    @kotlin.Metadata(mv = {2, 2, 0}, k = 1, xi = 48, d1 = {"\u0000\f\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0003\b\u00c6\u0002\u0018\u00002\u00020\u0001B\t\b\u0002\u00a2\u0006\u0004\b\u0002\u0010\u0003\u00a8\u0006\u0004"}, d2 = {"Lcom/zuan/kernelmanager/ui/navigation/NavigationRoute$GenericGpu;", "Lcom/zuan/kernelmanager/ui/navigation/NavigationRoute;", "<init>", "()V", "app_debug"})
    public static final class GenericGpu extends com.zuan.kernelmanager.ui.navigation.NavigationRoute {
        @org.jetbrains.annotations.NotNull()
        public static final com.zuan.kernelmanager.ui.navigation.NavigationRoute.GenericGpu INSTANCE = null;
        
        private GenericGpu() {
        }
    }
    
    @kotlin.Metadata(mv = {2, 2, 0}, k = 1, xi = 48, d1 = {"\u0000\f\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0003\b\u00c6\u0002\u0018\u00002\u00020\u0001B\t\b\u0002\u00a2\u0006\u0004\b\u0002\u0010\u0003\u00a8\u0006\u0004"}, d2 = {"Lcom/zuan/kernelmanager/ui/navigation/NavigationRoute$Home;", "Lcom/zuan/kernelmanager/ui/navigation/NavigationRoute;", "<init>", "()V", "app_debug"})
    public static final class Home extends com.zuan.kernelmanager.ui.navigation.NavigationRoute {
        @org.jetbrains.annotations.NotNull()
        public static final com.zuan.kernelmanager.ui.navigation.NavigationRoute.Home INSTANCE = null;
        
        private Home() {
        }
    }
    
    @kotlin.Metadata(mv = {2, 2, 0}, k = 1, xi = 48, d1 = {"\u0000\f\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0003\b\u00c6\u0002\u0018\u00002\u00020\u0001B\t\b\u0002\u00a2\u0006\u0004\b\u0002\u0010\u0003\u00a8\u0006\u0004"}, d2 = {"Lcom/zuan/kernelmanager/ui/navigation/NavigationRoute$KernelParameter;", "Lcom/zuan/kernelmanager/ui/navigation/NavigationRoute;", "<init>", "()V", "app_debug"})
    public static final class KernelParameter extends com.zuan.kernelmanager.ui.navigation.NavigationRoute {
        @org.jetbrains.annotations.NotNull()
        public static final com.zuan.kernelmanager.ui.navigation.NavigationRoute.KernelParameter INSTANCE = null;
        
        private KernelParameter() {
        }
    }
    
    @kotlin.Metadata(mv = {2, 2, 0}, k = 1, xi = 48, d1 = {"\u0000\f\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0003\b\u00c6\u0002\u0018\u00002\u00020\u0001B\t\b\u0002\u00a2\u0006\u0004\b\u0002\u0010\u0003\u00a8\u0006\u0004"}, d2 = {"Lcom/zuan/kernelmanager/ui/navigation/NavigationRoute$Mtk;", "Lcom/zuan/kernelmanager/ui/navigation/NavigationRoute;", "<init>", "()V", "app_debug"})
    public static final class Mtk extends com.zuan.kernelmanager.ui.navigation.NavigationRoute {
        @org.jetbrains.annotations.NotNull()
        public static final com.zuan.kernelmanager.ui.navigation.NavigationRoute.Mtk INSTANCE = null;
        
        private Mtk() {
        }
    }
    
    @kotlin.Metadata(mv = {2, 2, 0}, k = 1, xi = 48, d1 = {"\u0000\f\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0003\b\u00c6\u0002\u0018\u00002\u00020\u0001B\t\b\u0002\u00a2\u0006\u0004\b\u0002\u0010\u0003\u00a8\u0006\u0004"}, d2 = {"Lcom/zuan/kernelmanager/ui/navigation/NavigationRoute$ProcessManager;", "Lcom/zuan/kernelmanager/ui/navigation/NavigationRoute;", "<init>", "()V", "app_debug"})
    public static final class ProcessManager extends com.zuan.kernelmanager.ui.navigation.NavigationRoute {
        @org.jetbrains.annotations.NotNull()
        public static final com.zuan.kernelmanager.ui.navigation.NavigationRoute.ProcessManager INSTANCE = null;
        
        private ProcessManager() {
        }
    }
    
    @kotlin.Metadata(mv = {2, 2, 0}, k = 1, xi = 48, d1 = {"\u0000\f\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0003\b\u00c6\u0002\u0018\u00002\u00020\u0001B\t\b\u0002\u00a2\u0006\u0004\b\u0002\u0010\u0003\u00a8\u0006\u0004"}, d2 = {"Lcom/zuan/kernelmanager/ui/navigation/NavigationRoute$SetEdit;", "Lcom/zuan/kernelmanager/ui/navigation/NavigationRoute;", "<init>", "()V", "app_debug"})
    public static final class SetEdit extends com.zuan.kernelmanager.ui.navigation.NavigationRoute {
        @org.jetbrains.annotations.NotNull()
        public static final com.zuan.kernelmanager.ui.navigation.NavigationRoute.SetEdit INSTANCE = null;
        
        private SetEdit() {
        }
    }
    
    @kotlin.Metadata(mv = {2, 2, 0}, k = 1, xi = 48, d1 = {"\u0000\f\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0003\b\u00c6\u0002\u0018\u00002\u00020\u0001B\t\b\u0002\u00a2\u0006\u0004\b\u0002\u0010\u0003\u00a8\u0006\u0004"}, d2 = {"Lcom/zuan/kernelmanager/ui/navigation/NavigationRoute$SoC;", "Lcom/zuan/kernelmanager/ui/navigation/NavigationRoute;", "<init>", "()V", "app_debug"})
    public static final class SoC extends com.zuan.kernelmanager.ui.navigation.NavigationRoute {
        @org.jetbrains.annotations.NotNull()
        public static final com.zuan.kernelmanager.ui.navigation.NavigationRoute.SoC INSTANCE = null;
        
        private SoC() {
        }
    }
    
    @kotlin.Metadata(mv = {2, 2, 0}, k = 1, xi = 48, d1 = {"\u0000\f\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0003\b\u00c6\u0002\u0018\u00002\u00020\u0001B\t\b\u0002\u00a2\u0006\u0004\b\u0002\u0010\u0003\u00a8\u0006\u0004"}, d2 = {"Lcom/zuan/kernelmanager/ui/navigation/NavigationRoute$Terminal;", "Lcom/zuan/kernelmanager/ui/navigation/NavigationRoute;", "<init>", "()V", "app_debug"})
    public static final class Terminal extends com.zuan.kernelmanager.ui.navigation.NavigationRoute {
        @org.jetbrains.annotations.NotNull()
        public static final com.zuan.kernelmanager.ui.navigation.NavigationRoute.Terminal INSTANCE = null;
        
        private Terminal() {
        }
    }
}