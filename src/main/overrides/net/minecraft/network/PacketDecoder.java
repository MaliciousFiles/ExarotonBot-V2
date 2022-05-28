package net.minecraft.network;

import com.mojang.logging.LogUtils;
import io.github.maliciousfiles.ExarotonBot;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import java.io.IOException;
import java.util.List;

import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.game.ClientboundDisconnectPacket;
import net.minecraft.util.profiling.jfr.JvmProfiler;
import org.slf4j.Logger;

public class PacketDecoder extends ByteToMessageDecoder {
    private static final Logger LOGGER = LogUtils.getLogger();
    private final PacketFlow flow;

    public PacketDecoder(PacketFlow $$0) {
        this.flow = $$0;
    }

    protected void decode(ChannelHandlerContext $$0, ByteBuf $$1, List<Object> $$2) throws Exception {
        int $$3 = $$1.readableBytes();
        if ($$3 != 0) {
            FriendlyByteBuf $$4 = new FriendlyByteBuf($$1);
            int $$5;
//            try {
                $$5 = $$4.readVarInt();
//            } catch (IndexOutOfBoundsException e) {
//                $$4.readBytes($$4.readableBytes());
//                return;
//            }

            ConnectionProtocol protocol = $$0.channel().attr(Connection.ATTRIBUTE_PROTOCOL).get();

            // START CUSTOM CODE
            Class<? extends Packet<?>> packetClass = protocol.getPacketsByIds(this.flow).get($$5);
//            System.out.println("decoding: "+packetClass);
            if (packetClass == null || !ExarotonBot.ALLOWED_PACKETS.contains(packetClass)) {
                $$4.readBytes($$4.readableBytes());
                return;
            }
//            System.out.println("decoding: "+packetClass);
            // END CUSTOM CODE

            Packet<?> $$6 = protocol.createPacket(this.flow, $$5, $$4);

            if ($$6 == null) {
                throw new IOException("Bad packet id " + $$5);
            } else {
                int $$7 = protocol.getId();
                JvmProfiler.INSTANCE.onPacketReceived($$7, $$5, $$0.channel().remoteAddress(), $$3);
                if ($$4.readableBytes() > 0) {
                    int var10002 = protocol.getId();
                    throw new IOException("Packet " + var10002 + "/" + $$5 + " (" + $$6.getClass().getSimpleName() + ") was larger than I expected, found " + $$4.readableBytes() + " bytes extra whilst reading packet " + $$5);
                } else {
                    $$2.add($$6);
                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.debug(Connection.PACKET_RECEIVED_MARKER, " IN: [{}:{}] {}", protocol, $$5, $$6.getClass().getName());
                    }

                }
            }
        }
    }
}