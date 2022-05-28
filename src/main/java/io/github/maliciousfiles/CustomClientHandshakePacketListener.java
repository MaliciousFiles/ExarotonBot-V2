package io.github.maliciousfiles;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.exceptions.AuthenticationException;
import com.mojang.authlib.exceptions.AuthenticationUnavailableException;
import com.mojang.authlib.exceptions.InsufficientPrivilegesException;
import com.mojang.authlib.exceptions.InvalidCredentialsException;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import net.minecraft.client.User;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.Connection;
import net.minecraft.network.ConnectionProtocol;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.network.protocol.login.*;
import net.minecraft.util.Crypt;
import net.minecraft.util.CryptException;
import net.minecraft.util.HttpUtil;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import java.math.BigInteger;
import java.security.PublicKey;

public class CustomClientHandshakePacketListener implements ClientLoginPacketListener {
    private final Connection connection;
    private final User user;
    private final MinecraftSessionService service;

    public CustomClientHandshakePacketListener(Connection connection, User user, MinecraftSessionService service) {
        this.connection = connection;
        this.user = user;
        this.service = service;
    }

    public void handleHello(ClientboundHelloPacket $$0) {
        System.out.println("HELLO");
        Cipher $$8;
        Cipher $$9;
        String $$10;
        ServerboundKeyPacket $$11;
        try {
            SecretKey $$1 = Crypt.generateSecretKey();
            PublicKey $$2 = $$0.getPublicKey();
            $$10 = (new BigInteger(Crypt.digestData($$0.getServerId(), $$2, $$1))).toString(16);
            $$8 = Crypt.getCipher(2, $$1);
            $$9 = Crypt.getCipher(1, $$1);
            $$11 = new ServerboundKeyPacket($$1, $$2, $$0.getNonce());
        } catch (CryptException var8) {
            throw new IllegalStateException("Protocol error", var8);
        }

        HttpUtil.DOWNLOAD_EXECUTOR.submit(() -> {
            Component $$4 = this.authenticateServer($$10);
            if ($$4 != null) {
//                if (this.minecraft.getCurrentServer() == null || !this.minecraft.getCurrentServer().isLan()) {
//                    this.connection.disconnect($$4);
//                    return;
//                }

                System.out.println("AUTHENTICATION ERROR: "+$$4.getString());
            }

            this.connection.send($$11, ($$2) -> {
                System.out.println("SETTING ENCRYPTION KEY");
                this.connection.setEncryptionKey($$8, $$9);
            });
        });
    }

    private Component authenticateServer(String $$0) {
        try {
            this.getMinecraftSessionService().joinServer(user.getGameProfile(), user.getAccessToken(), $$0);
            return null;
        } catch (AuthenticationUnavailableException var3) {
            return new TranslatableComponent("disconnect.loginFailedInfo", new TranslatableComponent("disconnect.loginFailedInfo.serversUnavailable"));
        } catch (InvalidCredentialsException var4) {
            return new TranslatableComponent("disconnect.loginFailedInfo", new TranslatableComponent("disconnect.loginFailedInfo.invalidSession"));
        } catch (InsufficientPrivilegesException var5) {
            return new TranslatableComponent("disconnect.loginFailedInfo", new TranslatableComponent("disconnect.loginFailedInfo.insufficientPrivileges"));
        } catch (AuthenticationException var6) {
            return new TranslatableComponent("disconnect.loginFailedInfo", var6.getMessage());
        }
    }

    private MinecraftSessionService getMinecraftSessionService() {
        return this.service;
    }

    public void handleGameProfile(ClientboundGameProfilePacket $$0) {
        GameProfile profile = $$0.getGameProfile();
        this.connection.setProtocol(ConnectionProtocol.PLAY);
        System.out.println("CLIENT PACKET LISTENER SET");
        this.connection.setListener(new CustomClientPacketHandler(connection));
    }

    public void onDisconnect(Component $$0) {
        System.out.println("DISCONNECT: "+$$0.getString());
    }

    public Connection getConnection() {
        return this.connection;
    }

    public void handleDisconnect(ClientboundLoginDisconnectPacket $$0) {
        System.out.println("DISCONNECTED: "+$$0.getReason().getString());
        this.connection.disconnect($$0.getReason());
    }

    public void handleCompression(ClientboundLoginCompressionPacket $$0) {
        System.out.println("COMPRESSION");
        if (!this.connection.isMemoryConnection()) {
            this.connection.setupCompression($$0.getCompressionThreshold(), false);
        }

    }

    public void handleCustomQuery(ClientboundCustomQueryPacket $$0) {
        System.out.println("CUSTOM QUERY");
        this.connection.send(new ServerboundCustomQueryPacket($$0.getTransactionId(), null));
    }
}