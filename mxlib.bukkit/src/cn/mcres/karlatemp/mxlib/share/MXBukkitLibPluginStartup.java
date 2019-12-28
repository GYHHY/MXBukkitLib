/*
 * Copyright (c) 2018-2019 Karlatemp. All rights reserved.
 * Reserved.FileName: MXBukkitLibPluginStartup.java@author: karlatemp@vip.qq.com: 19-9-18 下午5:54@version: 2.0
 */

package cn.mcres.karlatemp.mxlib.share;

import cn.mcres.karlatemp.mxlib.MXBukkitLib;
import cn.mcres.karlatemp.mxlib.MXLib;
import cn.mcres.karlatemp.mxlib.configuration.IConfigurationProcessor;
import cn.mcres.karlatemp.mxlib.event.Event;
import cn.mcres.karlatemp.mxlib.event.HandlerList;
import cn.mcres.karlatemp.mxlib.exceptions.ScanException;
import cn.mcres.karlatemp.mxlib.share.system.MXBukkitAutoConfig;
import cn.mcres.karlatemp.mxlib.shared.ReadPropertiesAutoConfigs;
import cn.mcres.karlatemp.mxlib.tools.IClassScanner;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MXBukkitLibPluginStartup extends JavaPlugin {
    public static MXBukkitLibPluginStartup plugin;
    public static final List<Consumer<Boolean>> hooks = new ArrayList<>();

    static {
        ReadPropertiesAutoConfigs.resourceLoaders.add(res -> {
            List<InputStream> readers = new ArrayList<>();
            for (Plugin p : Bukkit.getPluginManager().getPlugins()) {
                final InputStream stream = p.getResource(res);
                if (stream != null) {
                    readers.add(stream);
                }
            }
            return readers;
        });
        HandlerList.setDefaultErrorCatch((posing, invoking, error) -> {
            Plugin own = BukkitToolkit.getPluginByClass(invoking.getClass());
            Logger logger = own == null ? Bukkit.getLogger() : own.getLogger();
            logger.log(Level.SEVERE, "Error in posing event " + posing.getClass() + " with handler " + invoking + " by plugin "
                    + (own == null ? "<unknown>" : own), error);
        });
    }

    public MXBukkitLibPluginStartup() {
        plugin = this;
    }

    public static final boolean DEBUG = System.getProperty("mxlib.debug") != null;

    @Override
    public void onEnable() {
        new Metrics(this);
        hooks.forEach(c -> c.accept(true));
        getLogger().setLevel(Level.INFO);
        if (DEBUG) getLogger().setLevel(Level.ALL);
        MXLib.boot();
        IClassScanner scanner = MXBukkitLib.getBeanManager().getBeanNonNull(IClassScanner.class);
        try {
            MXBukkitLib.getBeanManager().getBeanNonNull(IConfigurationProcessor.class).load(
                    scanner.scan(getFile(), new ArrayList<>())
            );
        } catch (ScanException e) {
            getLogger().log(Level.SEVERE, e.toString(), e);
        }
    }

    @Override
    public void onDisable() {
        hooks.forEach(c -> c.accept(false));
        super.onDisable();
    }

    @Override
    public File getFile() {
        return super.getFile();
    }
}