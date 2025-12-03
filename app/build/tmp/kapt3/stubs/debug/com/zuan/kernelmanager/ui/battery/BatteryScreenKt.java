package com.zuan.kernelmanager.ui.battery;

@kotlin.Metadata(mv = {2, 2, 0}, k = 2, xi = 48, d1 = {"\u0000b\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u000b\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u0007\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0010\u000e\n\u0002\b\u0005\n\u0002\u0010\u0000\n\u0002\b\u0006\n\u0002\u0018\u0002\n\u0002\b\u0007\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\u001a\u001a\u0010\u0003\u001a\u00020\u00042\b\b\u0002\u0010\u0005\u001a\u00020\u00062\u0006\u0010\u0007\u001a\u00020\bH\u0007\u001a$\u0010\t\u001a\u00020\u00042\u0006\u0010\n\u001a\u00020\u000b2\u0012\u0010\f\u001a\u000e\u0012\u0004\u0012\u00020\u000b\u0012\u0004\u0012\u00020\u00040\rH\u0007\u001ah\u0010\u000e\u001a\u00020\u00042\b\b\u0002\u0010\n\u001a\u00020\u000b2\u0014\b\u0002\u0010\f\u001a\u000e\u0012\u0004\u0012\u00020\u000b\u0012\u0004\u0012\u00020\u00040\r2\b\b\u0002\u0010\u000f\u001a\u00020\u00102\u0014\b\u0002\u0010\u0011\u001a\u000e\u0012\u0004\u0012\u00020\u0010\u0012\u0004\u0012\u00020\u00040\r2\u000e\b\u0002\u0010\u0012\u001a\b\u0012\u0004\u0012\u00020\u00040\u00132\u000e\b\u0002\u0010\u0014\u001a\b\u0012\u0004\u0012\u00020\u00040\u0013H\u0007\u001a\u0010\u0010\u0015\u001a\u00020\u00042\u0006\u0010\u0005\u001a\u00020\u0006H\u0007\u001a\u0018\u0010\u0016\u001a\u00020\u00042\u0006\u0010\u0017\u001a\u00020\u00182\u0006\u0010\u0019\u001a\u00020\u0018H\u0007\u001a\u0010\u0010\u001a\u001a\u00020\u00042\u0006\u0010\u0005\u001a\u00020\u0006H\u0007\u001a \u0010\u001b\u001a\u00020\u00042\u0006\u0010\u001c\u001a\u00020\u00182\u0006\u0010\u0019\u001a\u00020\u00182\u0006\u0010\u001d\u001a\u00020\u001eH\u0007\u001a\u0010\u0010\u001f\u001a\u00020\u00042\u0006\u0010\u0005\u001a\u00020\u0006H\u0007\u001a\u0010\u0010 \u001a\u00020\u00042\u0006\u0010\u0005\u001a\u00020\u0006H\u0007\u001a\u0010\u0010!\u001a\u00020\u00042\u0006\u0010\u0005\u001a\u00020\u0006H\u0007\u001a\u0010\u0010\"\u001a\u00020\u00042\u0006\u0010\u0005\u001a\u00020\u0006H\u0007\u001a\u0010\u0010#\u001a\u00020\u00042\u0006\u0010\u0005\u001a\u00020\u0006H\u0007\u001a/\u0010$\u001a\u00020\u00042\u0006\u0010\u001d\u001a\u00020%2\u0006\u0010\u001c\u001a\u00020\u00182\u0006\u0010\u0019\u001a\u00020\u00182\u0006\u0010&\u001a\u00020\u0001H\u0007\u00a2\u0006\u0004\b\'\u0010(\u001a<\u0010)\u001a\u00020\u00042\u0006\u0010\u001c\u001a\u00020\u00182\u0006\u0010*\u001a\u00020\u00182\u0006\u0010+\u001a\u00020\u000b2\u0012\u0010,\u001a\u000e\u0012\u0004\u0012\u00020\u000b\u0012\u0004\u0012\u00020\u00040\r2\u0006\u0010\u001d\u001a\u00020-H\u0007\u001a\u0018\u0010.\u001a\u00020\u00042\u0006\u0010\u0005\u001a\u00020\u00062\u0006\u0010/\u001a\u000200H\u0007\"\u0010\u0010\u0000\u001a\u00020\u0001X\u0082\u0004\u00a2\u0006\u0004\n\u0002\u0010\u0002\u00a8\u00061"}, d2 = {"DashboardGreen", "Landroidx/compose/ui/graphics/Color;", "J", "BatteryScreen", "", "viewModel", "Lcom/zuan/kernelmanager/ui/battery/BatteryViewModel;", "navController", "Landroidx/navigation/NavController;", "BatteryMonitorCard", "isEnabled", "", "onEnabledChange", "Lkotlin/Function1;", "SmartCutoffCard", "cutOffLevel", "", "onCutOffLevelChange", "onManualCut", "Lkotlin/Function0;", "onManualResume", "AmperageMeterCard", "AmperageStatItem", "label", "", "value", "BatteryDetailCard", "DetailRow", "title", "icon", "", "CapacityAnalyticsCard", "SystemActivityCard", "BatteryLevelSquareCard", "VoltageSquareCard", "HealthCircularCard", "SmallInfoCard", "Landroidx/compose/ui/graphics/vector/ImageVector;", "color", "SmallInfoCard-g2O1Hgs", "(Landroidx/compose/ui/graphics/vector/ImageVector;Ljava/lang/String;Ljava/lang/String;J)V", "ControlSwitchCard", "subtitle", "checked", "onCheckedChange", "Landroidx/compose/ui/graphics/painter/Painter;", "ThermalProfileDashboardItem", "hazeState", "Ldev/chrisbanes/haze/HazeState;", "app_debug"})
@kotlin.OptIn(markerClass = {androidx.compose.material3.ExperimentalMaterial3ExpressiveApi.class, androidx.compose.material3.ExperimentalMaterial3Api.class})
public final class BatteryScreenKt {
    private static final long DashboardGreen = 0L;
    
    @androidx.compose.runtime.Composable()
    public static final void BatteryScreen(@org.jetbrains.annotations.NotNull()
    com.zuan.kernelmanager.ui.battery.BatteryViewModel viewModel, @org.jetbrains.annotations.NotNull()
    androidx.navigation.NavController navController) {
    }
    
    @androidx.compose.runtime.Composable()
    public static final void BatteryMonitorCard(boolean isEnabled, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function1<? super java.lang.Boolean, kotlin.Unit> onEnabledChange) {
    }
    
    @androidx.compose.runtime.Composable()
    public static final void SmartCutoffCard(boolean isEnabled, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function1<? super java.lang.Boolean, kotlin.Unit> onEnabledChange, float cutOffLevel, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function1<? super java.lang.Float, kotlin.Unit> onCutOffLevelChange, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function0<kotlin.Unit> onManualCut, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function0<kotlin.Unit> onManualResume) {
    }
    
    @androidx.compose.runtime.Composable()
    public static final void AmperageMeterCard(@org.jetbrains.annotations.NotNull()
    com.zuan.kernelmanager.ui.battery.BatteryViewModel viewModel) {
    }
    
    @androidx.compose.runtime.Composable()
    public static final void AmperageStatItem(@org.jetbrains.annotations.NotNull()
    java.lang.String label, @org.jetbrains.annotations.NotNull()
    java.lang.String value) {
    }
    
    @androidx.compose.runtime.Composable()
    public static final void BatteryDetailCard(@org.jetbrains.annotations.NotNull()
    com.zuan.kernelmanager.ui.battery.BatteryViewModel viewModel) {
    }
    
    @androidx.compose.runtime.Composable()
    public static final void DetailRow(@org.jetbrains.annotations.NotNull()
    java.lang.String title, @org.jetbrains.annotations.NotNull()
    java.lang.String value, @org.jetbrains.annotations.NotNull()
    java.lang.Object icon) {
    }
    
    @androidx.compose.runtime.Composable()
    public static final void CapacityAnalyticsCard(@org.jetbrains.annotations.NotNull()
    com.zuan.kernelmanager.ui.battery.BatteryViewModel viewModel) {
    }
    
    @androidx.compose.runtime.Composable()
    public static final void SystemActivityCard(@org.jetbrains.annotations.NotNull()
    com.zuan.kernelmanager.ui.battery.BatteryViewModel viewModel) {
    }
    
    @androidx.compose.runtime.Composable()
    public static final void BatteryLevelSquareCard(@org.jetbrains.annotations.NotNull()
    com.zuan.kernelmanager.ui.battery.BatteryViewModel viewModel) {
    }
    
    @androidx.compose.runtime.Composable()
    public static final void VoltageSquareCard(@org.jetbrains.annotations.NotNull()
    com.zuan.kernelmanager.ui.battery.BatteryViewModel viewModel) {
    }
    
    @androidx.compose.runtime.Composable()
    public static final void HealthCircularCard(@org.jetbrains.annotations.NotNull()
    com.zuan.kernelmanager.ui.battery.BatteryViewModel viewModel) {
    }
    
    @androidx.compose.runtime.Composable()
    public static final void ControlSwitchCard(@org.jetbrains.annotations.NotNull()
    java.lang.String title, @org.jetbrains.annotations.NotNull()
    java.lang.String subtitle, boolean checked, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function1<? super java.lang.Boolean, kotlin.Unit> onCheckedChange, @org.jetbrains.annotations.NotNull()
    androidx.compose.ui.graphics.painter.Painter icon) {
    }
    
    @androidx.compose.runtime.Composable()
    public static final void ThermalProfileDashboardItem(@org.jetbrains.annotations.NotNull()
    com.zuan.kernelmanager.ui.battery.BatteryViewModel viewModel, @org.jetbrains.annotations.NotNull()
    dev.chrisbanes.haze.HazeState hazeState) {
    }
}