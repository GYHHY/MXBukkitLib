/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cn.mcres.gyhhy.MXLib;

import java.util.Properties;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.plugin.Plugin;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.configuration.file.FileConfiguration;
import java.nio.charset.StandardCharsets;

public class FileParser {

    public static class RunResponse<T> {

        public static final String PLUGIN_RESOURCE_NOT_FOUND = "plugin.reource.not.found",
                PLUGIN_FILE_EXISTS = " plugin.file.exists";
        public String error;
        public File file;
        public boolean success;
        public T param;

        public RunResponse(boolean success, File file, String errorMessage) {
            this(success, file, errorMessage, null);
        }

        public RunResponse(boolean success, File file, String errorMessage, T param) {
            this.success = success;
            this.file = file;
            this.error = errorMessage;
            this.param = param;
        }
    }

    private FileParser() {
        throw new IllegalArgumentException();
    }

    /**
     * 复制一个文件
     *
     * @param <T> 一个自定义的结果, 供其他模块使用(执行执行copyFile获取的param永远是null)
     * @param pl 主插件
     * @param path 路径
     * @param cover 是否覆盖(当为false时,插件文件目录存在文件时将不会执行任何操作)
     * @return 执行结果
     * @throws IOException io错误
     * @throws NullPointerException 插件/路径为null时报错
     * @throws java.io.FileNotFoundException {@link FileOutputStream}
     * 初始化错误
     */
    public static <T> RunResponse<T> copyFile(Plugin pl, String path, boolean cover) throws IOException {
        pl.getClass();
        path.getClass(); // 检测null
        File df = pl.getDataFolder();
        File to = new File(df, path);
        if (to.isFile() && (!cover)) {
            return new RunResponse<>(true, to, RunResponse.PLUGIN_FILE_EXISTS);
        }
        InputStream is = pl.getResource(path);
        if (is == null) {
            return new RunResponse<>(false, to, RunResponse.PLUGIN_RESOURCE_NOT_FOUND);
        }
        try (InputStream ct = is) {
            byte[] buffer = new byte[1024];
            new File(to, "..").mkdirs();
            to.createNewFile();
            try (FileOutputStream fos = new FileOutputStream(to)) {
                while (true) {
                    int leng = ct.read(buffer);
                    if (leng == -1) {
                        break;
                    }
                    fos.write(buffer, 0, leng);
                }
            }
        }
        return new RunResponse<>(true, to, null);
    }

    /**
     * @param main 主插件
     * @param path 路径
     * @return 执行结果,使用 ${returnvalue}.param 获取
     * @throws IOException IO错误
     * @throws java.io.FileNotFoundException IO错误
     */
    public static RunResponse<Properties> copyAndLoadProperties(Plugin main, String path) throws IOException {
        RunResponse<Properties> r = copyFile(main, path, false /*不覆盖原有文件*/);// 执行结果
        if (!r.success) {
            // 错误........
            return r;
        } else {
            // 使用 FileConfiguration 的原因是 Plugin 的 getConfig() 返回的也是一个 FileConfiguration
            Properties p = new Properties();
            try (FileInputStream fis = new FileInputStream(r.file)) {
                try (InputStreamReader reader = new InputStreamReader(fis, StandardCharsets.UTF_8)) {
                    p.load(reader);
                }
            }
            r.param = p;
            return r;
        }
    }

    /**
     * @param main 主插件
     * @param path 路径
     * @return 执行结果,使用 ${returnvalue}.param 获取
     * @throws IOException IO错误
     * @throws java.io.FileNotFoundException IO错误
     * @throws InvalidConfigurationException Yaml读取错误
     */
    public static RunResponse<FileConfiguration> copyAndLoad(Plugin main, String path) throws IOException, InvalidConfigurationException {
        RunResponse<FileConfiguration> r = copyFile(main, path, false /*不覆盖原有文件*/);// 执行结果
        if (!r.success) {
            // 错误........
            return r;
        } else {
            // 使用 FileConfiguration 的原因是 Plugin 的 getConfig() 返回的也是一个 FileConfiguration
            FileConfiguration config = new YamlConfiguration();
            config.load(r.file);
            r.param = config;
            return r;
        }
    }

    public static FileConfiguration load(String path) throws IOException, InvalidConfigurationException {
        Plugin pl = null; // 这里换成主插件
        return copyAndLoad(pl, path).param;
    }

    private static void example() {
        try {
            //执行实例
            FileConfiguration lang = copyAndLoad(null, "lang.yml").param;
            if (lang == null) {
                throw new NullPointerException("Cannot load lang.yml");
            }
        } catch (IOException | InvalidConfigurationException ex) {
            throw new RuntimeException(ex);
        }
    }
}
