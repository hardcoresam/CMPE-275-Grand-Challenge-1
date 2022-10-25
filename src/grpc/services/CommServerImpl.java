package grpc.services;

import com.google.protobuf.ByteString;

import io.grpc.stub.StreamObserver;
import services.Message;
import services.UserServiceGrpc;

import java.util.ArrayList;

public class CommServerImpl extends  UserServiceGrpc.UserServiceImplBase {
    int serverPort;
    private GRPCClient client;
    GRPCServer grpcServer;

    LoaddBal roundRobin;

    public CommServerImpl(GRPCServer gserver){
        grpcServer = gserver;
        serverPort = gserver.getServerPort();
        roundRobin = LoaddBal.getInstance();
    }


    public void request(Message request, StreamObserver<Message> responseObserver) throws InterruptedException {
        System.out.println("Comm Server port: "+serverPort+" Received req from Client "+request.getOrigin()+". Message ID: " +request.getId());

        roundRobin.getClient().sendRequest(request);
        Thread.sleep(5000);
        Message response = client.getResponseMessage();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    private ByteString process(Message msg) {
        String content = new String(msg.getPayload().toByteArray());
        System.out.println("Processing Msg "+ msg.getId() +" Payload: " + content);

        // TODO complete processing
        final String blank = "Hello from Server";
        byte[] raw = blank.getBytes();

        return ByteString.copyFrom(raw);
    }
}
