package net.minecraft.network.protocol.game;

import java.util.Map;
import java.util.UUID;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.protocol.Packet;

public class ClientboundChatPacket implements Packet<ClientGamePacketListener> {
    private final Component message;
    private final ChatType type;
    private final UUID sender;

    public ClientboundChatPacket(Component $$0, ChatType $$1, UUID $$2) {
        this.message = $$0;
        this.type = $$1;
        this.sender = $$2;
    }

    public ClientboundChatPacket(FriendlyByteBuf $$0) {
        JsonObject json = new Gson().fromJson($$0.readUtf(262144), JsonObject.class);

        removeColorAndEvents(json);

        this.message = Component.Serializer.fromJson(json);
        this.type = null;
        this.sender = null;
//        this.message = $$0.readComponent();
//        this.type = ChatType.getForIndex($$0.readByte());
//        this.sender = $$0.readUUID();
        $$0.readBytes($$0.readableBytes());
    }

    private static void removeColorAndEvents(JsonElement element) {
        if (element.isJsonArray()) {
            for (JsonElement element1 : element.getAsJsonArray()) {
                removeColorAndEvents(element1);
            }
        } else if (element.isJsonObject()) {
            JsonObject object = element.getAsJsonObject();
            object.remove("color");
            object.remove("clickEvent");
            object.remove("hoverEvent");
            for (Map.Entry<String, JsonElement> entry : object.entrySet()) {
                removeColorAndEvents(entry.getValue());
            }
        }
    }

    public void write(FriendlyByteBuf $$0) {
        $$0.writeComponent(this.message);
        $$0.writeByte(this.type.getIndex());
        $$0.writeUUID(this.sender);
    }

    public void handle(ClientGamePacketListener $$0) {
        $$0.handleChat(this);
    }

    public Component getMessage() {
        return this.message;
    }

    public ChatType getType() {
        return this.type;
    }

    public UUID getSender() {
        return this.sender;
    }

    public boolean isSkippable() {
        return true;
    }
}