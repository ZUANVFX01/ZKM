package com.zuan.kernelmanager.utils;

@kotlin.Metadata(mv = {2, 2, 0}, k = 1, xi = 48, d1 = {"\u00002\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0003\n\u0002\u0010\u000e\n\u0000\n\u0002\u0010\u000b\n\u0000\n\u0002\u0010\t\n\u0000\n\u0002\u0010\b\n\u0000\n\u0002\u0010\u0007\n\u0000\n\u0002\u0010\u0002\n\u0002\b\u0002\b\u00c6\u0002\u0018\u00002\u00020\u0001B\t\b\u0002\u00a2\u0006\u0004\b\u0002\u0010\u0003J\u0006\u0010\f\u001a\u00020\rJ\b\u0010\u000e\u001a\u00020\u000fH\u0002J\b\u0010\u0010\u001a\u00020\rH\u0002R\u0010\u0010\u0004\u001a\u0004\u0018\u00010\u0005X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0006\u001a\u00020\u0007X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\b\u001a\u00020\tX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\n\u001a\u00020\u000bX\u0082\u000e\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u0011"}, d2 = {"Lcom/zuan/kernelmanager/utils/FpsReader;", "", "<init>", "()V", "fpsFilePath", "", "useSurfaceFlinger", "", "lastTime", "", "lastFrames", "", "getRealFps", "", "findFpsPath", "", "getSurfaceFlingerFps", "app_debug"})
public final class FpsReader {
    @org.jetbrains.annotations.Nullable()
    private static java.lang.String fpsFilePath;
    private static boolean useSurfaceFlinger = true;
    private static long lastTime = -1L;
    private static int lastFrames = -1;
    @org.jetbrains.annotations.NotNull()
    public static final com.zuan.kernelmanager.utils.FpsReader INSTANCE = null;
    
    private FpsReader() {
        super();
    }
    
    public final float getRealFps() {
        return 0.0F;
    }
    
    private final void findFpsPath() {
    }
    
    private final float getSurfaceFlingerFps() {
        return 0.0F;
    }
}