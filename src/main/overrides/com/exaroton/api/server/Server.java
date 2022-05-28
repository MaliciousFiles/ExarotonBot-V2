package com.exaroton.api.server;

import com.exaroton.api.APIException;
import com.exaroton.api.ExarotonClient;
import com.exaroton.api.request.server.ExecuteCommandRequest;
import com.exaroton.api.request.server.GetPlayerListsRequest;
import com.exaroton.api.request.server.GetServerLogsRequest;
import com.exaroton.api.request.server.GetServerMOTDRequest;
import com.exaroton.api.request.server.GetServerRAMRequest;
import com.exaroton.api.request.server.GetServerRequest;
import com.exaroton.api.request.server.RestartServerRequest;
import com.exaroton.api.request.server.SetServerMOTDRequest;
import com.exaroton.api.request.server.SetServerRAMRequest;
import com.exaroton.api.request.server.ShareServerLogsRequest;
import com.exaroton.api.request.server.StartServerRequest;
import com.exaroton.api.request.server.StopServerRequest;
import com.exaroton.api.ws.WebSocketManager;
import com.exaroton.api.ws.subscriber.ConsoleSubscriber;
import com.exaroton.api.ws.subscriber.HeapSubscriber;
import com.exaroton.api.ws.subscriber.ServerStatusSubscriber;
import com.exaroton.api.ws.subscriber.StatsSubscriber;
import com.exaroton.api.ws.subscriber.TickSubscriber;

public class Server {
    private String id;
    private String name;
    private String address;
    private String motd;
    private int status;
    private PlayerInfo players;
    private String host;
    private int port;
    private ServerSoftware software;
    private boolean shared;
    private transient ExarotonClient client;
    private transient WebSocketManager webSocket;

    public Server(ExarotonClient client, String id) {
        if (client == null) {
            throw new IllegalArgumentException("Invalid client!");
        } else {
            this.client = client;
            if (id == null) {
                throw new IllegalArgumentException("Invalid server ID!");
            } else {
                this.id = id;
            }
        }
    }

    public String getId() {
        return this.id;
    }

    public String getName() {
        return this.name;
    }

    public String getAddress() {
        return this.address;
    }

    public int getStatus() {
        return this.status;
    }

    public boolean hasStatus(int... statusCodes) {
        if (statusCodes == null) {
            throw new IllegalArgumentException("Invalid status code array");
        } else {
            int[] var2 = statusCodes;
            int var3 = statusCodes.length;

            for(int var4 = 0; var4 < var3; ++var4) {
                int statusCode = var2[var4];
                if (this.status == statusCode) {
                    return true;
                }
            }

            return false;
        }
    }

    public String getMotd() {
        return this.motd;
    }

    public ServerMOTDInfo fetchMotd() throws APIException {
        GetServerMOTDRequest request = new GetServerMOTDRequest(this.client, this.id);
        return (ServerMOTDInfo)request.request().getData();
    }

    public ServerMOTDInfo setMotd(String motd) throws APIException {
        SetServerMOTDRequest request = new SetServerMOTDRequest(this.client, this.id, motd);
        return (ServerMOTDInfo)request.request().getData();
    }

    public PlayerInfo getPlayerInfo() {
        return this.players;
    }

    public String getHost() {
        return this.host;
    }

    public int getPort() {
        return this.port;
    }

    public ServerSoftware getSoftware() {
        return this.software;
    }

    public boolean isShared() {
        return this.shared;
    }

    public ExarotonClient getClient() {
        return this.client;
    }

    public Server get() throws APIException {
        GetServerRequest request = new GetServerRequest(this.client, this.id);
        this.setFromObject((Server)request.request().getData());
        return this;
    }

    public ServerLog getLog() throws APIException {
        GetServerLogsRequest request = new GetServerLogsRequest(this.client, this.id);
        return (ServerLog)request.request().getData();
    }

    public MclogsData shareLog() throws APIException {
        ShareServerLogsRequest request = new ShareServerLogsRequest(this.client, this.id);
        return (MclogsData)request.request().getData();
    }

    public ServerRAMInfo getRAM() throws APIException {
        GetServerRAMRequest request = new GetServerRAMRequest(this.client, this.id);
        return (ServerRAMInfo)request.request().getData();
    }

    public ServerRAMInfo setRAM(int ram) throws APIException {
        SetServerRAMRequest request = new SetServerRAMRequest(this.client, this.id, ram);
        return (ServerRAMInfo)request.request().getData();
    }

    public void start() throws APIException {
        StartServerRequest request = new StartServerRequest(this.client, this.id);
        request.request();
    }

    public void stop() throws APIException {
        StopServerRequest request = new StopServerRequest(this.client, this.id);
        request.request();
    }

    public void restart() throws APIException {
        RestartServerRequest request = new RestartServerRequest(this.client, this.id);
        request.request();
    }

    public void executeCommand(String command) throws APIException {
        if (this.webSocket == null || !this.webSocket.executeCommand(command)) {
            ExecuteCommandRequest request = new ExecuteCommandRequest(this.client, this.id, command);
            request.request();
        }

    }

    public String[] getPlayerLists() throws APIException {
        GetPlayerListsRequest request = new GetPlayerListsRequest(this.client, this.id);
        return (String[])request.request().getData();
    }

    public PlayerList getPlayerList(String name) {
        return new PlayerList(name, this.id, this.client);
    }

    public Server setFromObject(Server server) {
        this.id = server.getId();
        this.name = server.getName();
        this.address = server.getAddress();
        this.motd = server.getMotd();
        this.status = server.getStatus();
        this.players = server.getPlayerInfo();
        this.host = server.getHost();
        this.port = server.getPort();
        this.software = server.getSoftware();
        this.shared = server.isShared();
        return this;
    }

    public void setClient(ExarotonClient client) {
        if (client == null) {
            throw new IllegalArgumentException("No client provided");
        } else {
            this.client = client;
        }
    }

    public void subscribe() {
        String protocol = this.client.getProtocol().equals("https") ? "wss" : "ws";
        String uri = protocol + "://" + this.client.getHost() + this.client.getBasePath() + "servers/" + this.id + "/websocket";
        this.webSocket = new WebSocketManager(uri, this.client.getApiToken(), this);
    }

    public void subscribe(String stream) {
        if (this.webSocket == null) {
            this.subscribe();
        }

        this.webSocket.subscribe(stream);
    }

    public void subscribe(String[] streams) {
        String[] var2 = streams;
        int var3 = streams.length;

        for(int var4 = 0; var4 < var3; ++var4) {
            String stream = var2[var4];
            this.subscribe(stream);
        }

    }

    public void unsubscribe() {
        if (this.webSocket == null) {
            throw new RuntimeException("No websocket connection active.");
        } else {
            this.webSocket.close();
            this.webSocket = null;
        }
    }

    public void unsubscribe(String stream) {
        if (this.webSocket == null) {
            throw new RuntimeException("No websocket connection active.");
        } else {
            this.webSocket.unsubscribe(stream);
        }
    }

    public void unsubscribe(String[] streams) {
        String[] var2 = streams;
        int var3 = streams.length;

        for(int var4 = 0; var4 < var3; ++var4) {
            String stream = var2[var4];
            this.unsubscribe(stream);
        }

    }

    public void addStatusSubscriber(ServerStatusSubscriber subscriber) {
        if (this.webSocket == null) {
            throw new RuntimeException("No websocket connection active.");
        } else {
            this.webSocket.addServerStatusSubscriber(subscriber);
        }
    }

    public void addConsoleSubscriber(ConsoleSubscriber subscriber) {
        if (this.webSocket == null) {
            throw new RuntimeException("No websocket connection active.");
        } else {
            this.webSocket.addConsoleSubscriber(subscriber);
        }
    }

    public void addHeapSubscriber(HeapSubscriber subscriber) {
        if (this.webSocket == null) {
            throw new RuntimeException("No websocket connection active.");
        } else {
            this.webSocket.addHeapSubscriber(subscriber);
        }
    }

    public void addStatsSubscriber(StatsSubscriber subscriber) {
        if (this.webSocket == null) {
            throw new RuntimeException("No websocket connection active.");
        } else {
            this.webSocket.addStatsSubscriber(subscriber);
        }
    }

    public void addTickSubscriber(TickSubscriber subscriber) {
        if (this.webSocket == null) {
            throw new RuntimeException("No websocket connection active.");
        } else {
            this.webSocket.addTickSubscriber(subscriber);
        }
    }

    public WebSocketManager getWebSocket() {
        return this.webSocket;
    }
}