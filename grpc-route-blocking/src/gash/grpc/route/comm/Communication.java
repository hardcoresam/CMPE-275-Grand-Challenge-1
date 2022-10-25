package gash.grpc.route.comm;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Properties;
import java.util.concurrent.LinkedBlockingDeque;

import com.google.protobuf.ByteString;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;
import route.Route;
import route.RouteServiceGrpc;
import route.RouteServiceGrpc.RouteServiceImplBase;

/**
 * copyright 2021, gash
 * <p>
 * Gash licenses this file to you under the Apache License, version 2.0 (the
 * "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at:
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
public class Communication extends RouteServiceImplBase {
    private Server svr;
    private LinkedBlockingDeque<route.Work> workQueue = new LinkedBlockingDeque<>();
    private LinkedBlockingDeque<ServerInfo> serverInfos = new LinkedBlockingDeque<>();
    private LinkedBlockingDeque<String> freeServers;
    private WorkDivider workDivider;

    public static class ServerInfo {
        private int serverId;
        private String serverName;
        private int serverPort;
        private RouteServiceGrpc.RouteServiceStub asyncStub;
        private int capacity;

        public ServerInfo(int serverId, String serverName, int serverPort, int capacity) {
            this.serverId = serverId;
            this.serverName = serverName;
            this.serverPort = serverPort;
            this.capacity = capacity;
            ManagedChannel ch = ManagedChannelBuilder.forAddress("localhost", this.serverPort).usePlaintext().build();
            this.asyncStub = RouteServiceGrpc.newStub(ch);
        }

        public int getServerId() {
            return serverId;
        }

        public String getServerName() {
            return serverName;
        }

        public int getServerPort() {
            return serverPort;
        }

        public RouteServiceGrpc.RouteServiceStub getAsyncStub() {
            return asyncStub;
        }

        public int getCapacity() {
            return capacity;
        }
    }

    public void init() {
        ServerInfo serverInfo1 = new ServerInfo(1111, "server1", 2346, 50);
        ServerInfo serverInfo2 = new ServerInfo(2222, "server2", 2347, 50);
        ServerInfo serverInfo3 = new ServerInfo(3333, "server3", 2348, 50);
        ServerInfo serverInfo4 = new ServerInfo(4444, "server4", 2349, 50);
        serverInfos.add(serverInfo1);
        serverInfos.add(serverInfo2);
        serverInfos.add(serverInfo3);
        serverInfos.add(serverInfo4);

        this.workQueue = new LinkedBlockingDeque<>();
        freeServers = new LinkedBlockingDeque<>();
        for (ServerInfo serverInfo : serverInfos) {
            freeServers.add(serverInfo.getServerName());
        }
        workDivider = new WorkDivider();
    }

    /**
     * Configuration of the server's identity, port, and role
     */
    private static Properties getConfiguration(final File path) throws IOException {
        if (!path.exists())
            throw new IOException("missing file");

        Properties rtn = new Properties();
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(path);
            rtn.load(fis);
        } finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e) {
                    // ignore
                }
            }
        }
        return rtn;
    }

    public static void main(String[] args) throws Exception {
        String path = args[0];
        try {
            Properties conf = Communication.getConfiguration(new File(path));
            CommHelper.configure(conf);

            /* Similar to the socket, waiting for a connection */
            final Communication impl = new Communication();
            impl.init();
            impl.start();
            impl.blockUntilShutdown();

        } catch (IOException e) {
            // TODO better error message
            e.printStackTrace();
        }
    }

    private void start() throws Exception {
        svr = ServerBuilder.forPort(CommHelper.getInstance().getServerPort()).addService(new Communication())
                .build();

        System.out.println("-- starting server");
        workDivider.start();
        svr.start();

        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                Communication.this.stop();
            }
        });
    }

    protected void stop() {
        svr.shutdown();
    }

    private void blockUntilShutdown() throws Exception {
        svr.awaitTermination();
    }

    /**
     * Once server receives a message, we store them in the queue and then the WorkDivider
     * Thread takes care of diving and handling the work between different servers.
     */
    @Override
    public void request(route.Route request, StreamObserver<route.Route> responseObserver) {
        route.Route.Builder builder = route.Route.newBuilder();

        Object payload = request.getPayloadType().equals("string") ? request.getPayloadString() :
                request.getPayloadType().equals("int") ? request.getPayloadInteger() :
                        request.getPayloadDouble();

        route.Work work = route.Work.newBuilder().setId(request.getId())
                .setOrigin(request.getOrigin()).setMsgType(request.getMsgType())
                .setPayloadType(request.getPayloadType())
                .setPayload(String.valueOf(payload)).build();

        workQueue.add(work);

        System.out.println("Received message - " + request.getId() + " from client - " + request.getOrigin() + " with MessageType - " + request.getPayloadType() + " and Message - " + payload);

        //Sending empty response back to the client as an acknowledgement.
        builder.setPayload(ByteString.copyFrom("Empty Response".getBytes()));
        route.Route rtn = builder.build();
        responseObserver.onNext(rtn);
        responseObserver.onCompleted();
    }

    public class WorkDivider extends Thread {

        /*
         * This thread will check for items in the queue and then divides the work between
         * multiple servers and then sends over the divided data using grpc network calls
         */
        @Override
        public void run() {
            while (true) {
                try {
                    // Take work from server queue and divide them to other servers to process them.
                    if (!workQueue.isEmpty() && !freeServers.isEmpty()) {
                        int noOfItemsToBeSent = (Integer) workQueue.size() / freeServers.size();
                        RouteServiceGrpc.RouteServiceStub asyncStub = getStubForSpecificServer(freeServers.pollFirst());
                        if (asyncStub == null) {
                            throw new RuntimeException("Exception occurred. Please check again.");
                        }
                        ArrayList<route.Work> workList = new ArrayList<>();
                        while (noOfItemsToBeSent > 0) {
                            workList.add(workQueue.pollFirst());
                            noOfItemsToBeSent--;
                        }
                        Route routeToSend = constructMessage(workList);
                        // Non-blocking!
                        asyncStub.request(routeToSend, getServerResponseObserver());
                    }
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        public RouteServiceGrpc.RouteServiceStub getStubForSpecificServer(String serverName) {
            for (ServerInfo serverInfo : serverInfos) {
                if (serverInfo.getServerName().equalsIgnoreCase(serverName)) {
                    return serverInfo.getAsyncStub();
                }
            }
            return null;
        }

        public Route constructMessage(ArrayList<route.Work> workList) {
            Route.Builder bld = Route.newBuilder();
            bld.setOrigin(CommHelper.getInstance().getServerID());
            bld.setMsgType("actual_work");
            bld.addAllListOfWork(workList);
            return bld.build();
        }

        public static StreamObserver<Route> getServerResponseObserver() {
            return new
                    StreamObserver<>() {
                        @Override
                        public void onNext(Route route) {
                            long receivedServerId = route.getOrigin();
                            updateServerStatus(receivedServerId);
                        }

                        @Override
                        public void onError(Throwable t) {
                            //System.out.println("onError...");
                        }

                        @Override
                        public void onCompleted() {
                            //System.out.println("Completed...");
                        }
                    };
        }

        public void updateServerStatus(long serverId) {
            String serverName = getServerName(serverId);
            freeServers.add(serverName);
        }

        public String getServerName(long serverId) {
            for (ServerInfo serverInfo : serverInfos) {
                if (serverInfo.serverId == serverId) {
                    return serverInfo.getServerName();
                }
            }
            return null;
        }
    }
}
