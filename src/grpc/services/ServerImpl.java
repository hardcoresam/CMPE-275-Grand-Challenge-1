package grpc.services;

import com.google.protobuf.ByteString;
import io.grpc.stub.StreamObserver;
import services.Message;
import services.UserServiceGrpc;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class ServerImpl extends UserServiceGrpc.UserServiceImplBase {

    int serverPort;
    public ServerImpl(int serverPort){
        this.serverPort = serverPort;
    }

    @Override
    public void request(Message request, StreamObserver<Message> responseObserver) {
        System.out.println("Server port: "+serverPort+" Received req from Client "+request.getOrigin()+". Message ID: " +request.getId());

        Message.Builder builder = Message.newBuilder();
        builder.setId(request.getId());
        builder.setOrigin(this.serverPort);
        builder.setDestination(request.getOrigin());
        builder.setPayload(process(request)); // Get response payload after proceesing request

        Message response = builder.build();
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
