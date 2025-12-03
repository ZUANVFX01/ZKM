package com.zuan.kernelmanager.ui.proces;

@kotlin.Metadata(mv = {2, 2, 0}, k = 2, xi = 48, d1 = {"\u0000n\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000b\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\b\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\u001a\u001a\u0010\u0000\u001a\u00020\u00012\u0006\u0010\u0002\u001a\u00020\u00032\b\b\u0002\u0010\u0004\u001a\u00020\u0005H\u0007\u001a5\u0010\u0006\u001a\u00020\u00012\u0006\u0010\u0007\u001a\u00020\b2\u0006\u0010\t\u001a\u00020\n2\u0006\u0010\u000b\u001a\u00020\f2\f\u0010\r\u001a\b\u0012\u0004\u0012\u00020\u00010\u000eH\u0007\u00a2\u0006\u0004\b\u000f\u0010\u0010\u001a\u001c\u0010\u0011\u001a\u00020\u00012\b\u0010\u0012\u001a\u0004\u0018\u00010\u00132\b\b\u0002\u0010\u0014\u001a\u00020\u0015H\u0007\u001a$\u0010\u0016\u001a\u00020\u00012\u0006\u0010\u0017\u001a\u00020\u00182\u0012\u0010\u0019\u001a\u000e\u0012\u0004\u0012\u00020\u0018\u0012\u0004\u0012\u00020\u00010\u001aH\u0007\u001a\u0018\u0010\u001b\u001a\u00020\u00012\u0006\u0010\u001c\u001a\u00020\u001d2\u0006\u0010\u001e\u001a\u00020\u001fH\u0007\u001a;\u0010 \u001a\u00020\u00012\u0006\u0010!\u001a\u00020\b2\u0006\u0010\t\u001a\u00020\n2\u0006\u0010\"\u001a\u00020#2\n\b\u0002\u0010$\u001a\u0004\u0018\u00010%2\u0006\u0010&\u001a\u00020\'H\u0007\u00a2\u0006\u0004\b(\u0010)\u00a8\u0006*"}, d2 = {"ProcessManagerScreen", "", "navController", "Landroidx/navigation/NavController;", "viewModel", "Lcom/zuan/kernelmanager/ui/proces/ProcessManagerViewModel;", "SortableHeader", "title", "", "width", "Landroidx/compose/ui/unit/Dp;", "isActive", "", "onClick", "Lkotlin/Function0;", "SortableHeader-ziNgDLE", "(Ljava/lang/String;FZLkotlin/jvm/functions/Function0;)V", "AppIcon", "drawable", "Landroid/graphics/drawable/Drawable;", "modifier", "Landroidx/compose/ui/Modifier;", "LimitSelector", "currentLimit", "", "onLimitSelected", "Lkotlin/Function1;", "ProcessItem", "process", "Lcom/zuan/kernelmanager/utils/ProcessData;", "sortType", "Lcom/zuan/kernelmanager/utils/SortType;", "Text", "text", "fontSize", "Landroidx/compose/ui/unit/TextUnit;", "fontWeight", "Landroidx/compose/ui/text/font/FontWeight;", "color", "Landroidx/compose/ui/graphics/Color;", "Text-tSHONfM", "(Ljava/lang/String;FJLandroidx/compose/ui/text/font/FontWeight;J)V", "app_debug"})
@kotlin.OptIn(markerClass = {androidx.compose.material3.ExperimentalMaterial3Api.class})
public final class ProcessManagerScreenKt {
    
    @androidx.compose.runtime.Composable()
    public static final void ProcessManagerScreen(@org.jetbrains.annotations.NotNull()
    androidx.navigation.NavController navController, @org.jetbrains.annotations.NotNull()
    com.zuan.kernelmanager.ui.proces.ProcessManagerViewModel viewModel) {
    }
    
    @androidx.compose.runtime.Composable()
    public static final void AppIcon(@org.jetbrains.annotations.Nullable()
    android.graphics.drawable.Drawable drawable, @org.jetbrains.annotations.NotNull()
    androidx.compose.ui.Modifier modifier) {
    }
    
    @androidx.compose.runtime.Composable()
    public static final void LimitSelector(int currentLimit, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function1<? super java.lang.Integer, kotlin.Unit> onLimitSelected) {
    }
    
    @androidx.compose.runtime.Composable()
    public static final void ProcessItem(@org.jetbrains.annotations.NotNull()
    com.zuan.kernelmanager.utils.ProcessData process, @org.jetbrains.annotations.NotNull()
    com.zuan.kernelmanager.utils.SortType sortType) {
    }
}