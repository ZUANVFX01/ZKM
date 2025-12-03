package com.zuan.kernelmanager.ui.fpsmanager;

@kotlin.Metadata(mv = {2, 2, 0}, k = 2, xi = 48, d1 = {"\u0000.\n\u0000\n\u0002\u0010\u0002\n\u0002\b\u0003\n\u0002\u0010\u000e\n\u0002\b\u0003\n\u0002\u0010\u000b\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0007\u001a\b\u0010\u0000\u001a\u00020\u0001H\u0007\u001a\b\u0010\u0002\u001a\u00020\u0001H\u0007\u001a\u0018\u0010\u0003\u001a\u00020\u00012\u0006\u0010\u0004\u001a\u00020\u00052\u0006\u0010\u0006\u001a\u00020\u0005H\u0007\u001a\u001e\u0010\u0007\u001a\u00020\u00012\u0006\u0010\b\u001a\u00020\t2\f\u0010\n\u001a\b\u0012\u0004\u0012\u00020\u00010\u000bH\u0007\u001a$\u0010\f\u001a\u00020\u00012\u0006\u0010\r\u001a\u00020\u000e2\u0012\u0010\u000f\u001a\u000e\u0012\u0004\u0012\u00020\u000e\u0012\u0004\u0012\u00020\u00010\u0010H\u0007\u001a6\u0010\u0011\u001a\u00020\u00012\u0006\u0010\u0012\u001a\u00020\u00052\u0006\u0010\u0013\u001a\u00020\u00052\u0006\u0010\u0014\u001a\u00020\t2\u0006\u0010\u0015\u001a\u00020\t2\f\u0010\u0016\u001a\b\u0012\u0004\u0012\u00020\u00010\u000bH\u0007\u00a8\u0006\u0017"}, d2 = {"FpsManagerHomeContent", "", "DeviceInfoCard", "DeviceInfoRow", "label", "", "value", "ServiceStatusCard", "isRunning", "", "onToggleService", "Lkotlin/Function0;", "ModeSelectionCard", "currentMode", "Lcom/zuan/kernelmanager/utils/ShellExecutor$AccessType;", "onModeSelected", "Lkotlin/Function1;", "ModeOptionItem", "title", "desc", "isSelected", "isEnabled", "onClick", "app_debug"})
public final class FpsHomeTabKt {
    
    @androidx.compose.runtime.Composable()
    public static final void FpsManagerHomeContent() {
    }
    
    @androidx.compose.runtime.Composable()
    public static final void DeviceInfoCard() {
    }
    
    @androidx.compose.runtime.Composable()
    public static final void DeviceInfoRow(@org.jetbrains.annotations.NotNull()
    java.lang.String label, @org.jetbrains.annotations.NotNull()
    java.lang.String value) {
    }
    
    @androidx.compose.runtime.Composable()
    public static final void ServiceStatusCard(boolean isRunning, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function0<kotlin.Unit> onToggleService) {
    }
    
    @androidx.compose.runtime.Composable()
    public static final void ModeSelectionCard(@org.jetbrains.annotations.NotNull()
    com.zuan.kernelmanager.utils.ShellExecutor.AccessType currentMode, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function1<? super com.zuan.kernelmanager.utils.ShellExecutor.AccessType, kotlin.Unit> onModeSelected) {
    }
    
    @androidx.compose.runtime.Composable()
    public static final void ModeOptionItem(@org.jetbrains.annotations.NotNull()
    java.lang.String title, @org.jetbrains.annotations.NotNull()
    java.lang.String desc, boolean isSelected, boolean isEnabled, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function0<kotlin.Unit> onClick) {
    }
}