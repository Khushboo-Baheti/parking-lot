package org.uw.adc.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Date;

public class ParkingClient extends Thread {
    public void run() {
        try{
            for(int i = 1; i <= 2; ++i){
                final int threadId = i;
                new Thread(() -> test(threadId)).start();
            }

        } catch(Exception ex){
            System.out.println(ex);
        }
    }

    private void test(final int threadId) {
        try {
            while (true) {
            		String dateStr = new Date().toString();
                // get all free spots
                String freeSpots = getFreeSpots();
                System.out.println("Client Log: " + dateStr + " Thread-" + threadId + "#Empty spots: " + freeSpots);

                if(freeSpots == null || freeSpots.isEmpty()){
                    
                    System.out.println("Client Log: " + dateStr + " Thread-" + threadId + "#No free parking spots,waiting for the next free spot");
                    Thread.sleep(100);
                    continue;
                }

                // request to assign first free parking spot
                int spotId = Integer.parseInt(freeSpots.split(",")[0]);
                boolean assignSuccess = requestToAssignParkingSpot(spotId);
                dateStr = new Date().toString();

                if (assignSuccess) {
                    System.out.println("Client Log: " + dateStr + " Thread-" + threadId + "#SUCCESS. Car assigned to spotId(" + spotId + ")");

                    Thread.sleep(100);

                    releaseParkingSpot(spotId);
                    dateStr = new Date().toString();
                    System.out.println("Client Log: " + dateStr + " Thread-" + threadId + "#RELEASED. spotId(" + spotId + ") now free" );

                    break;
                } else {
                    System.out.println("Client Log: " + dateStr + " Thread-" + threadId + "#Failed to assign spotId(" + spotId + ")");
                }
            }
        }catch(Exception ex){
            System.out.println("Client Log: Thread-"+threadId+"#ERROR : "+ex);
        }
    }

    private String getFreeSpots() throws  IOException{
    		// Contacting the server on port 9090
        Socket s = new Socket("127.0.0.1", 9090);

        // request all free parking spots
        PrintWriter out =
                new PrintWriter(s.getOutputStream(), true);
        out.println("1");
        BufferedReader input =
                new BufferedReader(new InputStreamReader(s.getInputStream()));

        String emptySpots = input.readLine();

        return emptySpots;
    }

    // request to assign parking spot
    private boolean requestToAssignParkingSpot(int spotId) throws Exception{
		// Contacting the server on port 9090
        Socket socket = new Socket("127.0.0.1", 9090);
        PrintWriter out2 =
                new PrintWriter(socket.getOutputStream(), true);
        out2.println("2," + spotId);

        BufferedReader input =
                new BufferedReader(new InputStreamReader(socket.getInputStream()));

        String result = input.readLine();

        return "YES".equals(result) ? true : false;

    }

    // request to release parking spot
    private boolean releaseParkingSpot(int spotId) throws IOException {
		// Contacting the server on port 9090
        Socket socket = new Socket("127.0.0.1", 9090);
        PrintWriter out2 =
                new PrintWriter(socket.getOutputStream(), true);
        out2.println("3," + spotId);

        BufferedReader input =
                new BufferedReader(new InputStreamReader(socket.getInputStream()));

        String result = input.readLine();

        return "YES".equals(result) ? true : false;
    }
}
