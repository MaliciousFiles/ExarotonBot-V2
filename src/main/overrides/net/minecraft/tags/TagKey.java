package net.minecraft.tags;

import com.google.common.collect.Interner;
import com.google.common.collect.Interners;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import java.util.Optional;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

public record TagKey<T>(ResourceKey<? extends Registry<T>> a, ResourceLocation b) {
    private static final Interner<TagKey<?>> c = Interners.newStrongInterner();

    /** @deprecated */
    @Deprecated
    public TagKey(ResourceKey<? extends Registry<T>> a, ResourceLocation b) {
        this.a = a;
        this.b = b;
    }

    public static <T> Codec<TagKey<?>> codec(ResourceKey<? extends Registry<T>> $$0) {
        return ResourceLocation.CODEC.xmap(($$1) -> create($$0, $$1), TagKey::location);
    }

    public static <T> Codec<TagKey<T>> hashedCodec(ResourceKey<? extends Registry<T>> $$0) {
        return Codec.STRING.comapFlatMap(($$1) -> {
            return $$1.startsWith("#") ? ResourceLocation.read($$1.substring(1)).map(($$1x) -> {
                return create($$0, $$1x);
            }) : DataResult.error("Not a tag id");
        }, ($$0x) -> {
            return "#" + $$0x.b;
        });
    }

    public static <T> TagKey<T> create(ResourceKey<? extends Registry<T>> $$0, ResourceLocation $$1) {
        return (TagKey<T>) c.intern(new TagKey<>($$0, $$1));
    }

    public boolean isFor(ResourceKey<? extends Registry<?>> $$0) {
        return this.a == $$0;
    }

    public <E> Optional<TagKey<T>> cast(ResourceKey<? extends Registry<E>> $$0) {
        return this.isFor($$0) ? Optional.of(this) : Optional.empty();
    }

    public String toString() {
        ResourceLocation var10000 = this.a.location();
        return "TagKey[" + var10000 + " / " + this.b + "]";
    }

    public ResourceKey<? extends Registry<T>> registry() {
        return this.a;
    }

    public ResourceLocation location() {
        return this.b;
    }
}