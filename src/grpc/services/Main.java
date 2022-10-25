package grpc.services;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class Main {
    public static Properties getConfiguration(final File path) throws IOException {
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

    public static void main(String args[]) throws Exception{
/*
        String confPath = args[0];
        try{
            Properties conf = getConfiguration(new File(confPath)) ;
            // Similar to the socket, waiting for a connection

        } catch (IOException e) {
            e.printStackTrace();
        }
*/


        GRPCServer commServer = new GRPCServer(1, 2345);
        GRPCServer workerServer1 = new GRPCServer(2, 3456);
        GRPCServer workerServer2 = new GRPCServer(3, 3457);
        GRPCServer workerServer3 = new GRPCServer(4, 3458);
        commServer.startCommServer();
        workerServer1.startWorkerServer();
        workerServer2.startWorkerServer();
        workerServer3.startWorkerServer();
        workerServer3.blockUntilShutdown();
        workerServer2.blockUntilShutdown();
        workerServer1.blockUntilShutdown();
        commServer.blockUntilShutdown();

    }
}
