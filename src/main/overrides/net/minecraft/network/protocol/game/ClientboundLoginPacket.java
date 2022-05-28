package net.minecraft.network.protocol.game;

import com.google.common.collect.Sets;
import java.util.Set;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.DimensionType;

public record ClientboundLoginPacket(int a, boolean b, GameType c, GameType d, Set<ResourceKey<Level>> e, RegistryAccess.Frozen f, Holder<DimensionType> g, ResourceKey<Level> h, long i, int j, int k, int l, boolean m, boolean n, boolean o, boolean p) implements Packet<ClientGamePacketListener> {

    public ClientboundLoginPacket(FriendlyByteBuf $$0) {
        this($$0.readInt(), $$0.readBoolean(), GameType.byId($$0.readByte()), GameType.byNullableId($$0.readByte()), null, null, null, null, $$0.readLong(), $$0.readVarInt(), $$0.readVarInt(), $$0.readVarInt(), $$0.readBoolean(), $$0.readBoolean(), $$0.readBoolean(), $$0.readBoolean());
        $$0.readBytes($$0.readableBytes());
    }

    public ClientboundLoginPacket(int a, boolean b, GameType c, GameType d, Set<ResourceKey<Level>> e, RegistryAccess.Frozen f, Holder<DimensionType> g, ResourceKey<Level> h, long i, int j, int k, int l, boolean m, boolean n, boolean o, boolean p) {
        this.a = a;
        this.b = b;
        this.c = c;
        this.d = d;
        this.e = e;
        this.f = f;
        this.g = g;
        this.h = h;
        this.i = i;
        this.j = j;
        this.k = k;
        this.l = l;
        this.m = m;
        this.n = n;
        this.o = o;
        this.p = p;
    }

    public void write(FriendlyByteBuf $$0) {
        $$0.writeInt(this.a);
        $$0.writeBoolean(this.b);
        $$0.writeByte(this.c.getId());
        $$0.writeByte(GameType.getNullableId(this.d));
        $$0.writeCollection(this.e, ($$0x, $$1) -> {
            $$0x.writeResourceLocation($$1.location());
        });
        $$0.writeWithCodec(RegistryAccess.NETWORK_CODEC, this.f);
        $$0.writeWithCodec(DimensionType.CODEC, this.g);
        $$0.writeResourceLocation(this.h.location());
        $$0.writeLong(this.i);
        $$0.writeVarInt(this.j);
        $$0.writeVarInt(this.k);
        $$0.writeVarInt(this.l);
        $$0.writeBoolean(this.m);
        $$0.writeBoolean(this.n);
        $$0.writeBoolean(this.o);
        $$0.writeBoolean(this.p);
    }

    public void handle(ClientGamePacketListener $$0) {
        $$0.handleLogin(this);
    }

    public int playerId() {
        return this.a;
    }

    public boolean hardcore() {
        return this.b;
    }

    public GameType gameType() {
        return this.c;
    }

    public GameType previousGameType() {
        return this.d;
    }

    public Set<ResourceKey<Level>> levels() {
        return this.e;
    }

    public RegistryAccess.Frozen registryHolder() {
        return this.f;
    }

    public Holder<DimensionType> dimensionType() {
        return this.g;
    }

    public ResourceKey<Level> dimension() {
        return this.h;
    }

    public long seed() {
        return this.i;
    }

    public int maxPlayers() {
        return this.j;
    }

    public int chunkRadius() {
        return this.k;
    }

    public int simulationDistance() {
        return this.l;
    }

    public boolean reducedDebugInfo() {
        return this.m;
    }

    public boolean showDeathScreen() {
        return this.n;
    }

    public boolean isDebug() {
        return this.o;
    }

    public boolean isFlat() {
        return this.p;
    }
}
