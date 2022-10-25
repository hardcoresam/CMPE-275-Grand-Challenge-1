package gash.grpc.route.client;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;
import route.Route;
import route.RouteServiceGrpc;

import java.util.Random;

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

public class RouteClient {

    private static int noOfMessagesToSend = 1000;
    private static int noOfMessagesProcessed;
    private static final Random random = new Random();
    private static int noOfStringMessages = 0;
    private static int noOfIntMessages = 0;
    private static int noOfDoubleMessages = 0;


    private static long clientID = 501;
    private static int port = 2345;

    //This method creates variety of random data and sends it over the network
    private static final Route constructMessage(int mID) {
        Route.Builder bld = Route.newBuilder();
        bld.setId(mID);
        bld.setOrigin(RouteClient.clientID);
        bld.setMsgType("actual_work");
        if (mID % 2 == 0) {
            noOfStringMessages++;
            bld.setPayloadString(String.valueOf(getRandomPayload("string")));
            bld.setPayloadType("string");
        } else if (mID % 3 == 0) {
            noOfIntMessages++;
            bld.setPayloadInteger((Integer) getRandomPayload("int"));
            bld.setPayloadType("int");
        } else {
            noOfDoubleMessages++;
            bld.setPayloadDouble(((Number) getRandomPayload("double")).doubleValue());
            bld.setPayloadType("double");
        }
        return bld.build();
    }

    public static Object getRandomPayload(String payloadType) {
        Object payload;
        switch (payloadType) {
            case "double":
                payload = random.nextDouble(noOfMessagesToSend);
                break;
            case "int":
                payload = random.nextInt(noOfMessagesToSend);
                break;
            case "string":
            default:
                payload = generateRandomString();
                break;
        }
        return payload;
    }

    public static String generateRandomString() {
        int leftLimit = 97;
        int rightLimit = 122;
        int targetStringLength = 10;
        return random.ints(leftLimit, rightLimit + 1)
                .limit(targetStringLength)
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                .toString();
    }

    public static void main(String[] args) throws InterruptedException {
        ManagedChannel ch = ManagedChannelBuilder.forAddress("localhost", RouteClient.port).usePlaintext().build();
        RouteServiceGrpc.RouteServiceStub asyncstub = RouteServiceGrpc.newStub(ch);
        int noOfMessagesSent = 0;

        for (int i = 0; i < noOfMessagesToSend; i++) {
            var msg = RouteClient.constructMessage(i);

            // Non-blocking!
            asyncstub.request(msg, getServerResponseObserver());
            Thread.sleep(100);
            noOfMessagesSent++;
        }

        System.out.println("Client sent " + noOfMessagesSent + " messages successfully");
        System.out.println("Out of which, " + noOfStringMessages + " are messages of type String.");
        System.out.println(noOfIntMessages + " are messages of type Int.");
        System.out.println(noOfDoubleMessages + " are messages of type Double.");
        ch.shutdown();
    }

    public static StreamObserver<Route> getServerResponseObserver() {
        return new
                StreamObserver<>() {
                    @Override
                    public void onNext(Route route) {
                        noOfMessagesProcessed++;
                        //System.out.println("onNext...");
                    }

                    @Override
                    public void onError(Throwable t) {
                        System.out.println("onError...");
                    }

                    @Override
                    public void onCompleted() {
                        System.out.println("Total Messages Processed and received response are " + noOfMessagesProcessed);
                    }
                };
    }
}