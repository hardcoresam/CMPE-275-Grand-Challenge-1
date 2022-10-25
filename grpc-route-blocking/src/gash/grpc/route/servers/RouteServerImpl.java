package gash.grpc.route.servers;

import com.google.protobuf.ByteString;
import gash.grpc.route.comm.CommHelper;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;
import route.RouteServiceGrpc.RouteServiceImplBase;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Properties;

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
public class RouteServerImpl extends RouteServiceImplBase {

    private Server svr;
    private int noOfMessagesProcessed = 0;


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
            Properties conf = RouteServerImpl.getConfiguration(new File(path));
            RouteServer.configure(conf);

            /* Similar to the socket, waiting for a connection */
            final RouteServerImpl impl = new RouteServerImpl();
            impl.start();
            impl.blockUntilShutdown();

        } catch (IOException e) {
            // TODO better error message
            e.printStackTrace();
        }
    }

    private void start() throws Exception {
        svr = ServerBuilder.forPort(RouteServer.getInstance().getServerPort()).addService(new RouteServerImpl())
                .build();

        System.out.println("-- starting server");
        svr.start();

        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                RouteServerImpl.this.stop();
            }
        });
    }

    protected void stop() {
        svr.shutdown();
    }

    private void blockUntilShutdown() throws Exception {
        /* TODO what clean up is required? */
        svr.awaitTermination();
    }

    /**
     * server received a message!
     */
    @Override
    public void request(route.Route request, StreamObserver<route.Route> responseObserver) {
        List<route.Work> listOfWork = request.getListOfWorkList();
        for (route.Work w : listOfWork) {
            System.out.println("Server with Id - " + RouteServer.getInstance().getServerID()
                    + " processed message with id - " + w.getId() + " and type - "
                    + w.getPayloadType() + " and payload - " + w.getPayload());
            noOfMessagesProcessed++;
        }

        route.Route.Builder builder = route.Route.newBuilder();
        builder.setPayload(ByteString.copyFrom("Empty Response".getBytes()));
        builder.setMsgType("response_after_work_processed");
        builder.setOrigin(CommHelper.getInstance().getServerID());
        route.Route rtn = builder.build();
        responseObserver.onNext(rtn);
        responseObserver.onCompleted();
    }
}
