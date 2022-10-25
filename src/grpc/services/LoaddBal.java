package grpc.services;

import java.util.ArrayList;

public class LoaddBal {
    private ArrayList<GRPCClient> workerClients = new ArrayList<GRPCClient>();
    private int count=0;
    private static LoaddBal roundRobin = null;

    public void addWorker(GRPCClient client){
        workerClients.add(client);
        //System.out.println(workerClients.size());
    }
    public synchronized static LoaddBal getNewInstance(){
        roundRobin = new LoaddBal();
        return roundRobin;
    }

    public synchronized static LoaddBal getInstance(){
        if(roundRobin ==null)
            return getNewInstance();
        return roundRobin;
    }

    public GRPCClient getClient(){
        return workerClients.get((count++)%workerClients.size());
    }
}
