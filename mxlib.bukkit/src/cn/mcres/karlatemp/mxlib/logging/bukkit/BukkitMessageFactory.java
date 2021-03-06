/*
 * Copyright (c) 2018-2019 Karlatemp. All rights reserved.
 * Reserved.FileName: BukkitMessageFactory.java@author: karlatemp@vip.qq.com: 2019/12/24 下午10:32@version: 2.0
 */

package cn.mcres.karlatemp.mxlib.logging.bukkit;

import cn.mcres.karlatemp.mxlib.logging.MessageFactoryAnsi;
import cn.mcres.karlatemp.mxlib.tools.Toolkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.invoke.MethodHandle;
import java.net.URL;

@SuppressWarnings("Java9ReflectionClassVisibility")
public class BukkitMessageFactory extends MessageFactoryAnsi {
    private static MethodHandle getPlugin;

    private static MethodHandle get() throws Throwable {
        if (getPlugin != null) return getPlugin;
        Class<?> PluginClassLoader = Class.forName("org.bukkit.plugin.java.PluginClassLoader");
        return getPlugin = Toolkit.Reflection.getRoot().findGetter(PluginClassLoader, "plugin", JavaPlugin.class);
    }

    @Override
    protected String getStackTraceElementMessage$version(Class<?> c, URL url, StackTraceElement stack) throws Throwable {
        String v = super.getStackTraceElementMessage$version(c, url, stack);
        if (v == null) {
            ClassLoader loader = Toolkit.Reflection.getClassLoader(c);
            if (loader == null) return null;
            if (loader.getClass().getName().equalsIgnoreCase(
                    "org.bukkit.plugin.java.PluginClassLoader")) {
                JavaPlugin plugin = (JavaPlugin) get().invoke(loader);
                return plugin.getDescription().getVersion();
            }
        }
        return v;
    }
}
