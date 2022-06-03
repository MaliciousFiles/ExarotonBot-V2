//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package net.minecraft.util;

import com.google.common.base.Suppliers;

import java.util.Objects;
import java.util.function.Supplier;

/** @deprecated */
@Deprecated
public class LazyLoadedValue<T> {
    private final Supplier<T> factory;

    public LazyLoadedValue(Supplier<T> $$0) {
        Objects.requireNonNull($$0);
        this.factory = Suppliers.memoize($$0::get)::get;
    }

    public T get() {
        return this.factory.get();
    }
}
