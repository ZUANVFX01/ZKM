package com.zuan.kernelmanager.ui.home;

@kotlin.Metadata(mv = {2, 2, 0}, k = 2, xi = 48, d1 = {"\u0000N\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0000\n\u0002\u0018\u0002\n\u0002\b\b\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0002\u001a\u001a\u0010\u0000\u001a\u00020\u00012\b\b\u0002\u0010\u0002\u001a\u00020\u00032\u0006\u0010\u0004\u001a\u00020\u0005H\u0007\u001a\u0010\u0010\u0006\u001a\u00020\u00012\u0006\u0010\u0007\u001a\u00020\bH\u0007\u001a\u0010\u0010\t\u001a\u00020\u00012\u0006\u0010\u0007\u001a\u00020\bH\u0007\u001a\u001a\u0010\n\u001a\u00020\u00012\u0006\u0010\u0007\u001a\u00020\b2\b\b\u0002\u0010\u000b\u001a\u00020\fH\u0007\u001a:\u0010\r\u001a\u00020\u00012\u0006\u0010\u000e\u001a\u00020\u000f2\u0006\u0010\u0010\u001a\u00020\u00112\u0006\u0010\u0012\u001a\u00020\u000f2\u0006\u0010\u0013\u001a\u00020\u000f2\u0006\u0010\u0014\u001a\u00020\u000f2\b\b\u0002\u0010\u000b\u001a\u00020\fH\u0007\u001a\u0010\u0010\u0015\u001a\u00020\u00012\u0006\u0010\u0007\u001a\u00020\bH\u0007\u001a\u0018\u0010\u0016\u001a\u00020\u00012\u0006\u0010\u0017\u001a\u00020\u000f2\u0006\u0010\u0012\u001a\u00020\u000fH\u0007\u001a*\u0010\u0018\u001a\u00020\u00012\f\u0010\u0019\u001a\b\u0012\u0004\u0012\u00020\u001b0\u001a2\u0012\u0010\u001c\u001a\u000e\u0012\u0004\u0012\u00020\u000f\u0012\u0004\u0012\u00020\u00010\u001dH\u0007\u001a\u001e\u0010\u001e\u001a\u00020\u00012\u0006\u0010\u001f\u001a\u00020\u001b2\f\u0010 \u001a\b\u0012\u0004\u0012\u00020\u00010!H\u0007\u001a\u0016\u0010\"\u001a\u00020\u00012\f\u0010 \u001a\b\u0012\u0004\u0012\u00020\u00010!H\u0007\u00a8\u0006#"}, d2 = {"HomeScreen", "", "viewModel", "Lcom/zuan/kernelmanager/ui/home/HomeViewModel;", "navController", "Landroidx/navigation/NavController;", "DeviceInfoSection", "deviceInfo", "Lcom/zuan/kernelmanager/ui/home/DeviceInfo;", "SystemInfoSection", "KernelInfoCard", "modifier", "Landroidx/compose/ui/Modifier;", "SystemStatsCard", "title", "", "icon", "Landroidx/compose/ui/graphics/vector/ImageVector;", "value", "unit", "trend", "GpuInfoSection", "GpuDetailRow", "label", "ExtensionMenuSection", "extensions", "", "Lcom/zuan/kernelmanager/ui/home/ExtensionItem;", "onItemClick", "Lkotlin/Function1;", "ExtensionItemCard", "extension", "onClick", "Lkotlin/Function0;", "FpsManagerSettingsSection", "app_debug"})
@kotlin.OptIn(markerClass = {androidx.compose.material3.ExperimentalMaterial3Api.class})
public final class HomeScreenKt {
    
    @androidx.compose.runtime.Composable()
    public static final void HomeScreen(@org.jetbrains.annotations.NotNull()
    com.zuan.kernelmanager.ui.home.HomeViewModel viewModel, @org.jetbrains.annotations.NotNull()
    androidx.navigation.NavController navController) {
    }
    
    @androidx.compose.runtime.Composable()
    public static final void DeviceInfoSection(@org.jetbrains.annotations.NotNull()
    com.zuan.kernelmanager.ui.home.DeviceInfo deviceInfo) {
    }
    
    @androidx.compose.runtime.Composable()
    public static final void SystemInfoSection(@org.jetbrains.annotations.NotNull()
    com.zuan.kernelmanager.ui.home.DeviceInfo deviceInfo) {
    }
    
    @androidx.compose.runtime.Composable()
    public static final void KernelInfoCard(@org.jetbrains.annotations.NotNull()
    com.zuan.kernelmanager.ui.home.DeviceInfo deviceInfo, @org.jetbrains.annotations.NotNull()
    androidx.compose.ui.Modifier modifier) {
    }
    
    @androidx.compose.runtime.Composable()
    public static final void SystemStatsCard(@org.jetbrains.annotations.NotNull()
    java.lang.String title, @org.jetbrains.annotations.NotNull()
    androidx.compose.ui.graphics.vector.ImageVector icon, @org.jetbrains.annotations.NotNull()
    java.lang.String value, @org.jetbrains.annotations.NotNull()
    java.lang.String unit, @org.jetbrains.annotations.NotNull()
    java.lang.String trend, @org.jetbrains.annotations.NotNull()
    androidx.compose.ui.Modifier modifier) {
    }
    
    @androidx.compose.runtime.Composable()
    public static final void GpuInfoSection(@org.jetbrains.annotations.NotNull()
    com.zuan.kernelmanager.ui.home.DeviceInfo deviceInfo) {
    }
    
    @androidx.compose.runtime.Composable()
    public static final void GpuDetailRow(@org.jetbrains.annotations.NotNull()
    java.lang.String label, @org.jetbrains.annotations.NotNull()
    java.lang.String value) {
    }
    
    @androidx.compose.runtime.Composable()
    public static final void ExtensionMenuSection(@org.jetbrains.annotations.NotNull()
    java.util.List<com.zuan.kernelmanager.ui.home.ExtensionItem> extensions, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function1<? super java.lang.String, kotlin.Unit> onItemClick) {
    }
    
    @androidx.compose.runtime.Composable()
    public static final void ExtensionItemCard(@org.jetbrains.annotations.NotNull()
    com.zuan.kernelmanager.ui.home.ExtensionItem extension, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function0<kotlin.Unit> onClick) {
    }
    
    @androidx.compose.runtime.Composable()
    public static final void FpsManagerSettingsSection(@org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function0<kotlin.Unit> onClick) {
    }
}