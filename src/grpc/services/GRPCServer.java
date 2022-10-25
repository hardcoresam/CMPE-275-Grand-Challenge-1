package grpc.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.grpc.Server;
import io.grpc.ServerBuilder;

import java.io.IOException;

public class GRPCServer{
    public Logger logger = LoggerFactory.getLogger("server");

    private int serverId;
    private int serverPort;
    private int nextMsgId;
    private Server server;
    LoaddBal roundRobin;

    public GRPCServer(int serverId, int serverPort){
        this.serverId = serverId;
        if (serverPort <= 1024)
            throw new RuntimeException("server port must be above 1024");

        this.serverPort = serverPort;

        this.nextMsgId = 0;

        this.roundRobin = LoaddBal.getInstance();

    }
    public void startCommServer() throws IOException {
        this.server = ServerBuilder.forPort(this.serverPort).addService(new CommServerImpl(this)).build();
        System.out.println("-- starting Comm server "+this.serverPort);
        this.server.start();
    }

    public void startWorkerServer() throws IOException {
        this.server = ServerBuilder.forPort(this.serverPort).addService(new ServerImpl(this.serverPort)).build();
        GRPCClient client = GRPCClient.getNewInstance(2);
        client.buildChannel(this.serverPort) ;
        roundRobin.addWorker(client);
        System.out.println("-- starting Worker server "+this.serverPort);
        this.server.start();
    }

    public void stop(){
        this.server.shutdown();
    }

    public void blockUntilShutdown() throws Exception {
        /* TODO what clean up is required? */
        this.server.awaitTermination();
    }

    public int getServerPort() {
        return serverPort;
    }

    public int getServerId() {
        return serverId;
    }

    public int getNextMsgId() {
        return nextMsgId;
    }


}
