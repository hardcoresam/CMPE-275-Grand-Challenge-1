package grpc.services;

import com.google.protobuf.ByteString;


import io.grpc.LoadBalancer;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;
import services.Message ;
import services.UserServiceGrpc;
public class GRPCClient {
    private static Message responseMessage;
    private static long responseTime;
    private static GRPCClient theClient;
    private long resTime;
    private int clientId ;
    private int serverPort;
    private UserServiceGrpc.UserServiceStub asyncStub;

    private UserServiceGrpc.UserServiceBlockingStub syncStub;

    LoaddBal theLoadB;


    public static Message getResponseMessage() {
        return responseMessage;
    }

    public static long getResponseTime() {
        return responseTime;
    }

    public synchronized static GRPCClient getNewInstance(int clientId){
        theClient = new GRPCClient();
        theClient.clientId = clientId;
        return theClient;
    }

    public synchronized static GRPCClient getInstance(){
        return theClient;
    }

    // To build message to send to server
    public Message constructMessage(int mId, String payload){
        Message.Builder builder = Message.newBuilder();
        builder.setId(mId);
        builder.setOrigin(this.clientId);

        byte[] payloadByteArr = payload.getBytes();
        builder.setPayload(ByteString.copyFrom(payloadByteArr)) ;

        return builder.build();
    }

    // To handle response from server
    public static StreamObserver<Message> getServerResponseObserver(){

        StreamObserver<Message> observer = new StreamObserver<Message> (){
            @Override
            public void onNext(Message msg) {
                String payload = new String(msg.getPayload().toByteArray());
                System.out.println("Server "+msg.getOrigin() +" response message: "+payload);
                responseMessage = msg;
            }
            @Override
            public void onError(Throwable t) {
                System.out.println("Error while reading response fromServer: " + t);
            }
            @Override
            public void onCompleted() {
                System.out.println("Server returned");
                responseTime = System.currentTimeMillis();
                //System.out.println("***************"+System.currentTimeMillis());
            }
        };
        return observer;
    }

    // Send Asynchrounous message
    public void sendRequest(int mId, String payload){
        var message = constructMessage(mId,payload);
        System.out.println("Sending request.. " + mId +" to server "+serverPort);
        this.asyncStub.request(message,getServerResponseObserver());
        //var response = this.syncStub.request(message);
    }

    public void sendRequest(Message message){
        System.out.println("Sending message "+message.getId()+"to "+serverPort);
        this.asyncStub.request(message,getServerResponseObserver());
    }

    // Build channel
    public void buildChannel(int serverPort){
        this.serverPort = serverPort;
        ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", serverPort).usePlaintext().build();
        this.asyncStub = UserServiceGrpc.newStub(channel);  // Non blocking stub
        //this.syncStub = UserServiceGrpc.newBlockingStub(channel);  // Blocking stub
    }

    public static void main(String[] args) throws InterruptedException {
        GRPCClient client = GRPCClient.getNewInstance(1);

        client.buildChannel(2345);

        long startTime = System.currentTimeMillis();
        //System.out.println("***************"+System.currentTimeMillis());
        for(int i=0;i<10;i++){
            client.sendRequest(i,"Hellooo...");
        }
        System.out.println("Request time: " + (System.currentTimeMillis() - startTime));
        Thread.sleep(10000);

    }
}
