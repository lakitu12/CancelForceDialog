package me.lakitu.cancelforcedialog;

import android.app.Dialog;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

public class MainHook implements IXposedHookLoadPackage {

    @Override
    public void handleLoadPackage(LoadPackageParam lpparam) throws Throwable {
        // 在 LSPosed 日志中记录，确认模块已进入目标 App 进程
        XposedBridge.log("CancelDialog 模块已加载，目标包名: " + lpparam.packageName);

        // --- 逻辑 1: 强制允许返回键取消 ---
        XposedHelpers.findAndHookMethod(
                Dialog.class,
                "setCancelable",
                boolean.class, // 参数类型
                new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        // param.args[0] 代表 setCancelable 的第一个参数
                        // 无论 App 传入 true 还是 false，我们强制覆盖为 true
                        param.args[0] = true;
                    }
                }
        );

        // --- 逻辑 2: 强制允许点击外部取消 ---
        XposedHelpers.findAndHookMethod(
                Dialog.class,
                "show",
                new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        // param.thisObject 代表当前的 Dialog 实例
                        Dialog dialog = (Dialog) param.thisObject;
                        dialog.setCanceledOnTouchOutside(true);
                    }
                }
        );
    }
}
