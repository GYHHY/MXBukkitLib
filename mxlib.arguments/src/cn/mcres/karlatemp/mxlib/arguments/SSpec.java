/*
 * Copyright (c) 2018-2020 Karlatemp. All rights reserved.
 * @author Karlatemp <karlatemp@vip.qq.com> <https://github.com/Karlatemp>
 * @create 2020/02/15 15:32:55
 *
 * MXLib/mxlib.arguments/SSpec.java
 */

package cn.mcres.karlatemp.mxlib.arguments;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.*;

public class SSpec<V> {
    private final String[] keys;
    private BiFunction<Iterator<String>, SParser.ParseResult, V> parser;
    private BiConsumer<Iterator<String>, Consumer<String>> tabCompiler;
    private Supplier<V> defaultValue;
    private boolean multiple;
    private Map<SAttributeKey<?>, Object> attributes = new ConcurrentHashMap<>();

    public SSpec<V> removeAttribute(SAttributeKey<?> key) {
        attributes.remove(key);
        return this;
    }

    @SuppressWarnings("unchecked")
    public <T> T getAttribute(SAttributeKey<T> key) {
        return (T) attributes.get(key);
    }

    public <T> SSpec<V> putAttribute(SAttributeKey<T> key, T value) {
        attributes.put(key, value);
        return this;
    }

    public SSpec<V> multiple() {
        multiple = true;
        return this;
    }

    public boolean isMultiple() {
        return multiple;
    }

    public static boolean read(Iterator<String> iterator, String[] pool) {
        if (pool == null || iterator == null) return false;
        int end = pool.length;
        for (int i = 0; i < end; i++) {
            if (iterator.hasNext())
                pool[i] = iterator.next();
            else return false;
        }
        return true;
    }

    public static String last(String[] pool) {
        if (pool == null) return "";
        var end = pool.length;
        while (end-- > 0) {
            var v = pool[end];
            if (v != null) return v;
        }
        return "";
    }

    @Contract("null -> null; !null -> !null")
    public static String[] build(String[] val) {
        if (val == null) return null;
        if (val.length == 0) return val;
        var end = val.length;
        boolean hasNull = false;
        for (var i = 1; i < end; i++) {
            var o1 = val[i];
            if (o1 == null) {
                hasNull = true;
                continue;
            }
            for (var k = 0; k < i; k++) {
                var o2 = val[k];
                if (o2 == null) {
                    hasNull = true;
                    continue;
                }
                if (o1.equalsIgnoreCase(o2)) {
                    val[i] = null;
                    hasNull = true;
                    break;
                }
            }
        }
        if (!hasNull) {
            return val;
        }
        Arrays.sort(val, Comparator.nullsLast(String::compareTo));
        int region = 0;
        for (; region < end; region++) {
            if (val[region] == null) break;
        }
        return Arrays.copyOf(val, region);
    }

    public SSpec(@NotNull String... keys) {
        keys = build(keys);
        if (keys.length == 0) throw new NoSuchElementException();
        for (var s : keys) Objects.requireNonNull(s);
        this.keys = Arrays.copyOf(keys, keys.length);
    }

    public String[] getKeys() {
        return keys.clone();
    }

    public BiFunction<Iterator<String>, SParser.ParseResult, V> getParser() {
        return parser;
    }

    public BiConsumer<Iterator<String>, Consumer<String>> getTabCompiler() {
        return tabCompiler;
    }

    public Supplier<V> getDefaultValue() {
        return defaultValue;
    }

    @SuppressWarnings("unchecked")
    public <O> SSpec<O> parser(BiFunction<Iterator<String>, SParser.ParseResult, O> parser) {
        SSpec<O> a = (SSpec<O>) this;
        if (a.parser != null) return a;
        a.parser = parser;
        return a;
    }

    public SSpec<V> defaultValue(Supplier<V> supplier) {
        if (this.defaultValue != null) return this;
        if (this.parser == null) throw new IllegalStateException("parser unset.");
        this.defaultValue = supplier;
        return this;
    }

    public SSpec<V> tabCompiler(BiConsumer<Iterator<String>, Consumer<String>> tabCompiler) {
        if (this.tabCompiler != null) return this;
        if (this.parser == null) throw new IllegalStateException("parser unset.");
        this.tabCompiler = tabCompiler;
        return this;
    }
}
