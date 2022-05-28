package io.github.maliciousfiles;

import com.exaroton.api.APIException;
import com.exaroton.api.ExarotonClient;
import com.exaroton.api.server.PlayerInfo;
import com.exaroton.api.server.Server;
import com.exaroton.api.server.ServerStatus;
import com.exaroton.api.ws.subscriber.ServerStatusSubscriber;
import com.github.kklisura.cdt.launch.ChromeArguments;
import com.github.kklisura.cdt.launch.ChromeLauncher;
import com.github.kklisura.cdt.services.ChromeService;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService;
import fr.jcgay.notification.*;
import net.minecraft.CrashReport;
import net.minecraft.SharedConstants;
import net.minecraft.client.User;
import net.minecraft.network.Connection;
import net.minecraft.network.ConnectionProtocol;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.*;
import net.minecraft.network.protocol.handshake.ClientIntentionPacket;
import net.minecraft.network.protocol.login.*;
import net.minecraft.server.Bootstrap;
import net.minecraft.server.LoggedPrintStream;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.*;

public class ExarotonBot {
    public static final String MESSAGE = "This is a bot. If you would like to reach me for any purpose, please DM me in Discord (MaliciousFiles) or text me. Thank you! [P.S. this bot will automatically log off so as to preserve credits.]";
    public static final List<Class<? extends Packet<?>>> ALLOWED_PACKETS = List.of(
            ClientboundDisconnectPacket.class, ClientboundKeepAlivePacket.class,
            ClientboundLoginPacket.class, ClientboundPlayerPositionPacket.class,
            ClientboundPingPacket.class, ClientboundHelloPacket.class, ClientboundGameProfilePacket.class,
            ClientboundLoginDisconnectPacket.class, ClientboundLoginCompressionPacket.class,
            ClientboundCustomQueryPacket.class, ClientboundSetExperiencePacket.class,
            ClientboundSetHealthPacket.class, ClientboundPlayerInfoPacket.class,
            ClientboundChatPacket.class
    );

    // EXAROTON STUFF
    private static final String API_TOKEN = "l9rQYkymgqAB0jv4I1R4txMFWvtoQTQLEEupxFWe71A36TdNohz0TXArIID5fr1zlB6prKR5Eoc8nGivEestpHcMqVXMZ3dWl4SO";
    private static final String SERVER_ID = "Fcczg85in9GoALSn";
//    private static final int PLAYER_CHECK_INTERVAL = 1000 * 60 * 5 + 30;

    // AUTH STUFF
    public static final String NAME = "MaliciousFiles";
    public static final String UUID = "f0c7f7bb-7407-4dd7-9230-e074ed7c335d";
    private static final String XUID = "2535468957310460";
    private static final String CLIENT_ID = "N0MzMjNFNUZDRkFGNEFCODk5NjgwOTBGNzhCQ0NGMjc=";
    private static final User.Type USER_TYPE = User.Type.MSA;

    // NOTIFICATION STUFF
    private static final Icon ICON;

    static {
        try {
            ICON = Icon.create(new URL("https://exaroton.com/panel/img/exaroton-icon.png"), "ExarotonBot");
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    private static final Notifier NOTIFIER = new SendNotification()
            .setApplication(Application.builder()
                    .name("ExarotonBot")
                    .id("ExarotonBot")
                    .icon(ICON)
                    .build())
            .setChosenNotifier("notificationcenter")
            .initNotifier();

    private static MinecraftSessionService sessionService;
    private static Connection connection;
    private static String access_token;
    private static Server server;
    private static boolean connected = false;
    private static PlayerInfo info = null;

    public static final Map<UUID, String> PLAYER_NAMES = new HashMap<>();

    public static void main(String[] args) {
        SharedConstants.tryDetectVersion();

        CrashReport.preload();
        bootStrap();

        sessionService = new YggdrasilAuthenticationService(Proxy.NO_PROXY).createMinecraftSessionService();

        setAccessToken();

//        connect("localhost", 25565);

        try {
            ExarotonClient client = new ExarotonClient(API_TOKEN);
            server = client.getServer(SERVER_ID).get();

            server.subscribe();
            server.addStatusSubscriber(new ServerStatusSubscriber() {
                @Override
                public void statusUpdate(Server oldServer, Server newServer) {
                    if (newServer.hasStatus(ServerStatus.ONLINE)) {
                        checkPlayerCount();
                    }
                }
            });

            if (server.hasStatus(ServerStatus.ONLINE)) {
                checkPlayerCount();
            }

            /*while (true) {
                try {
                    if (server.get().hasStatus(ServerStatus.ONLINE)) {
                        checkPlayerCount();
                    }
                } catch (APIException e) {
                    e.printStackTrace();
                }
                Thread.sleep(PLAYER_CHECK_INTERVAL);
            }*/
            while (true);
        } catch (APIException e) {
            e.printStackTrace();
        }

//        connect("127.0.0.1", 25565);
//        while (connection.isConnected() || connection.isConnecting());

        System.out.println("Exiting - error!!");
        notify("Exiting", "An error occurred. Check console!");
    }

    public static void notify(String title, String message) {
        NOTIFIER.send(Notification.builder()
                .title("")
                .subtitle(title)
                .message(message)
                .icon(ICON)
                .level(Notification.Level.INFO).build());
    }

    private static void setAccessToken() {
        Gson gson = new Gson();

        String client_id = "00000000402b5328";
        String token_redirect = "https://login.live.com/oauth20_desktop.srf";
        String prompt = "select_account";

        int width = 500;
        int height = 650;

        String os = System.getProperty("os.name");
        Runtime runtime = Runtime.getRuntime();

        String start = null;
        switch (os) {
            case "Windows_NT": {
                String[] pathsW = new String[]{"HKEY_LOCAL_MACHINE", "HKEY_CURRENT_USER"};
                String[] compatibleW = new String[]{"chrome.exe", "vivaldi.exe", "brave.exe", "blisk.exe", "msedge.exe"};
                WE:
                {
                    for (String file : compatibleW) {
                        for (String path : pathsW) {
                            try {
                                String locW = path + "\\SOFTWARE\\Microsoft\\Windows\\CurrentVersion\\App Paths\\";
                                String out = new String(runtime.exec("\"C:\\Windows\\System32\\reg.exe\" query \"" + locW + file + "\"").getInputStream().readAllBytes());
                                if (!out.startsWith("ERROR")) {
                                    out = out.substring(out.indexOf("REG_SZ") + "REG_SZ".length()).trim();
                                    if (out.indexOf("\n") > 0) {
                                        out = out.substring(0, out.indexOf("\n") - 1);
                                    }
                                    if (new File(out).exists()) {
                                        start = out;
                                        break WE;
                                    }
                                }
                            } catch (IOException ignored) {}
                        }
                    }
                }

                break;
            }
            case "Mac OS X": {
                String locD = "/Applications/FILE.app/Contents/MacOS/FILE";
                String[] compatibleD = new String[]{"Google\\ Chrome", "Google Chrome", "Microsoft\\ Edge", "Microsoft Edge", "Vivaldi", "Blisk", "Brave\\ Browser", "Brave Browser", "Yandex"};
                for (String file : compatibleD) {
                    String s = locD.replaceAll("FILE", file);
                    if (new File(s).exists()) {
                        start = s;
                        break;
                    }
                }
                if (start != null) {
                    break;
                }
            }
            case "Linux":
                throw new RuntimeException("Linux is not supported!");
            default: {
                throw new RuntimeException("Cannot find valid browser!");
            }
        }

        String redirect = "https://login.live.com/oauth20_authorize.srf" + "?client_id=" + client_id + "&response_type=code" + "&redirect_uri=" + URLEncoder.encode(token_redirect, StandardCharsets.UTF_8) + "&scope=XboxLive.signin%20offline_access&prompt=" + prompt;

        ChromeLauncher launcher = new ChromeLauncher();

        ChromeService service = launcher.launch(ChromeArguments.builder()
                .noFirstRun()
                .disableExtensions()
                .remoteDebuggingPort(0)
                .noDefaultBrowserCheck()
                .userDataDir(Paths.get(System.getProperty("java.io.tmpdir"), "ExarotonBot").toString())
                .noDefaultBrowserCheck()
                .additionalArguments(Map.of(
                        "disable-restore-session-state", true,
                        "disable-first-run-ui", true,
                        "disable-component-extensions-with-background-pages", true,
                        "window-size", width+","+height,
                        "force-app-mode", true,
                        "app", redirect
                )).build());

        try {
            String loc;
            while (!(loc = service.getTabs().get(0).getUrl()).startsWith(token_redirect));

            launcher.close();

            String code = getQueryMap(new URL(loc).getQuery()).get("code");
            String body = "client_id=" + client_id + "&code=" + code + "&grant_type=authorization_code&redirect_uri=" + token_redirect;

            JsonObject ms = fetch("https://login.live.com/oauth20_token.srf", body, Map.entry("Content-Type", "application/x-www-form-urlencoded"));

            JsonObject body2 = new JsonObject();
            JsonObject properties = new JsonObject();
            properties.addProperty("AuthMethod", "RPS");
            properties.addProperty("SiteName", "user.auth.xboxlive.com");
            properties.addProperty("RpsTicket", "d="+ms.get("access_token"));
            body2.add("Properties", properties);
            body2.addProperty("RelyingParty", "http://auth.xboxlive.com");
            body2.addProperty("TokenType", "JWT");

            JsonObject token1 = fetch("https://user.auth.xboxlive.com/user/authenticate", gson.toJson(body2), Map.entry("Content-Type", "application/json"), Map.entry("Accept", "application/json"));
            String XBLToken = token1.get("Token").getAsString();
            String UserHash = token1.get("DisplayClaims").getAsJsonObject().get("xui").getAsJsonArray().get(0).getAsJsonObject().get("uhs").getAsString();

            JsonObject body3 = new JsonObject();
            JsonObject properties2 = new JsonObject();
            properties2.addProperty("SandboxId", "RETAIL");
            JsonArray userTokens = new JsonArray();
            userTokens.add(XBLToken);
            properties2.add("UserTokens", userTokens);
            body3.add("Properties", properties2);
            body3.addProperty("RelyingParty", "rp://api.minecraftservices.com/");
            body3.addProperty("TokenType", "JWT");

            JsonObject xsts = fetch("https://xsts.auth.xboxlive.com/xsts/authorize", gson.toJson(body3), Map.entry("Content-Type", "application/json"), Map.entry("Accept", "application/json"));

            JsonObject body4 = new JsonObject();
            body4.addProperty("identityToken", "XBL3.0 x="+UserHash+";"+xsts.get("Token").getAsString());

            JsonObject mcauth = fetch("https://api.minecraftservices.com/authentication/login_with_xbox", gson.toJson(body4), Map.entry("Content-Type", "application/json"), Map.entry("Accept", "application/json"));

            access_token = mcauth.get("access_token").getAsString();

            System.out.println("GOT IT! "+access_token);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @SafeVarargs
    private static JsonObject fetch(String url, String body, Map.Entry<String, String>... headers) {
        try {
            HttpClient httpclient = HttpClients.createDefault();
            HttpPost httppost = new HttpPost(url);

            httppost.setEntity(new StringEntity(body));
            for (Map.Entry<String, String> header : headers) {
                httppost.setHeader(header.getKey(), header.getValue());
            }

            HttpResponse response = httpclient.execute(httppost);
            HttpEntity entity = response.getEntity();

            Scanner scan = new Scanner(entity.getContent());
            scan.useDelimiter("\\Z");

            JsonObject json = new Gson().fromJson(scan.next(), JsonObject.class);
            scan.close();

            return json;
        } catch (IOException e) {
            e.printStackTrace();

            return null;
        }
    }

    private static Map<String, String> getQueryMap(String query) {
        String[] params = query.split("&");
        Map<String, String> map = new HashMap<>();

        for (String param : params) {
            String name = param.split("=")[0];
            String value = param.split("=")[1];
            map.put(name, value);
        }
        return map;
    }

    private static void bootStrap() {
        try {
            Field isBootstrapped = Bootstrap.class.getDeclaredField("isBootstrapped");
            isBootstrapped.setAccessible(true);
            if (!((Boolean) isBootstrapped.get(null))) {
                isBootstrapped.set(null, true);
                System.setErr(new LoggedPrintStream("STDERR", System.err));
                System.setOut(new LoggedPrintStream("STDOUT", Bootstrap.STDOUT));
            }
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    private static void connect(String ip, int port) {
        if (connected || Arrays.stream(info.getList()).toList().contains(NAME)) {
            System.out.println("Trying to connect while already connected!");
            return;
        }

        try {
            InetSocketAddress address = new InetSocketAddress(InetAddress.getByName(ip), port);

            User user = new User(NAME, UUID.replace("-",""), access_token, Optional.of(XUID), Optional.of(CLIENT_ID), USER_TYPE);

            connection = Connection.connectToServer(address, false);
            connection.setListener(new CustomClientHandshakePacketListener(connection, user, sessionService));

            connection.send(new ClientIntentionPacket(address.getHostName(), address.getPort(), ConnectionProtocol.LOGIN));
            connection.send(new ServerboundHelloPacket(user.getGameProfile()));

            connected = true;
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }

    private static void checkPlayerCount() {
        info = server.getPlayerInfo();
        int players = info.getCount();

        System.out.println("checking player count: "+ players);
        if (players == 1 && connected) {
            System.out.println("Only player online.");
            disconnect("Only player online.");
        } else if (/*players > 0 && */!connected) {
            System.out.println("Connecting");

            StringBuilder message = new StringBuilder(players + " players online: ");
            for (String player : info.getList()) message.append(player).append(", ");

            notify("Connecting", message.substring(0, message.length()-2));
            connect(server.getAddress(), server.getPort());
        }
    }

    /*public static void addPlayer() {
        System.out.println("Player joined.");
        players++;
        checkPlayerCount();
    }

    public static void removePlayer() {
        System.out.println("Player left.");
        players--;
        checkPlayerCount();;
    }*/

    public static void disconnect(String reason) {
        System.out.println("Disconnecting!! "+reason);
        notify("Disconnecting", reason);
        connection.disconnect(new TranslatableComponent(reason));
        connected = false;
    }
}
