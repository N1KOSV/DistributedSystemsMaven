package org.example;

public class reducer {
    private ReducerServer reducerServer;
    public reducer() {}
    public reducer(int port) {
        reducerServer = new ReducerServer(port);
    }
    public void start() {
        reducerServer.openServer();
    }

    public static void main(String[] args) {
        reducer reducerServer = new reducer(6000);
        reducerServer.start();


    }
}
