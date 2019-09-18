/*
 * Copyright (c) 2018-2019 Karlatemp. All rights reserved.
 * Reserved.FileName: IBeanManager.java@author: karlatemp@vip.qq.com: 19-9-18 下午5:54@version: 2.0
 */

package cn.mcres.karlatemp.mxlib.bean;

import cn.mcres.karlatemp.mxlib.MXBukkitLib;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Optional;

/**
 * 一个BeanManager, 处理核心
 * @see MXBukkitLib#getBeanManager()
 */
public interface IBeanManager {
    <T> void addBean(@NotNull Class<T> c, @NotNull T bean);

    @Nullable
    <T> T getBean(@NotNull Class<T> c);

    @NotNull
    default <T> T getBeanNonNull(@NotNull Class<T> c) {
        T b = getBean(c);
        if (b == null) throw new NullPointerException();
        return b;
    }

    @NotNull
    default <T> Optional<T> getOptional(@NotNull Class<T> c) {
        return Optional.ofNullable(getBean(c));
    }

    @NotNull
    Map<Class<?>, Object> getBeans();
}
