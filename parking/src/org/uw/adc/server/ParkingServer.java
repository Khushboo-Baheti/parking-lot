package org.uw.adc.server;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;

import net.openhft.affinity.AffinityLock;


public class ParkingServer extends Thread {

    HashMap<Integer, Boolean> parkingLot;

    public ParkingServer() {
        this.parkingLot = BuildEmptyParkingLot();
    }
    
    public static void main(String[] args) {
    		new ParkingServer().start();
        System.out.println("Started server....");
        try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
    }

    public void run(){
        try{
            ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(4);

            // Creating a server socket on 9090 port
        		int port = 9090;
            ServerSocket listener = new ServerSocket(port);
            System.out.println("Server listening on port: " + port);

            try {
                while (true) {
                    Socket socket = listener.accept();
                    // Starting new thread for each client request.
                    executor.execute(new Thread(() -> {
	                    	AffinityLock.acquireCore(true);
	                    	serveRequest(socket);
                    	}));
                }
            }
            finally {
                listener.close();
                executor.shutdown();
            }
        }catch(Exception ex){
            System.out.println(ex);
        }
    }

    private void serveRequest(Socket socket){

        String dateStr= new Date().toString();

        System.out.println("Server Log: "+ dateStr +": Got connection from client.");

        try {

            // read request type
            BufferedReader input =
                    new BufferedReader(new InputStreamReader(socket.getInputStream()));
            String request = input.readLine();
            String[] parameters = request.split(",");
            int requestType = Integer.parseInt(parameters[0]);


            switch(requestType){
                case 1 : 
                			dateStr= new Date().toString();
                			System.out.println("Server Log: "+ dateStr +": Client requesting for availble spots.");
                			getEmptySpots(socket);
                         break;

                case 2 : 
                			dateStr= new Date().toString();
                			int requestSpotId = Integer.parseInt(parameters[1]);
                			System.out.println("Server Log: "+ dateStr +": Client requesting for parking spot: " + requestSpotId);
                         assignParkingSpot(socket, requestSpotId);
                         break;

                case 3 : 
                			dateStr= new Date().toString();
                			int freeSpotId = Integer.parseInt(parameters[1]);
                			System.out.println("Server Log: "+ dateStr +": Client releasing parking spot: " + freeSpotId);
                			makeParkingSpotAvailable(socket, freeSpotId);
                			break;

                default: System.out.println("RequestType '"+requestType+"' not supported.");
            }
        }catch(Exception ex){
            System.out.println(ex);
        }
    }

    private void makeParkingSpotAvailable(Socket socket, int spotId){
    		// Using mutex becuase multiple threads can modify parkingLot variable
        synchronized (this.parkingLot){
            this.parkingLot.put(spotId, true);
        }

        try {
            try {
                PrintWriter out =
                        new PrintWriter(socket.getOutputStream(), true);
                String dateStr= new Date().toString();
    				System.out.println("Server Log: "+ dateStr +": Parking spot " + spotId + " released");
                out.println("YES");
            } catch (Exception ex) {
                System.out.println(ex);
            } finally {
                socket.close();
            }
        }catch(Exception ex){
            System.out.println(ex);
        }
    }

    private void assignParkingSpot(Socket socket, int spotId){
        boolean result = false;

		// Using mutex becuase multiple threads can modify parkingLot variable
        synchronized (this.parkingLot){

            // get current status
            boolean isSpotFree = this.parkingLot.get(spotId);

            // spot taken
            if(isSpotFree == false) result = false;
            else{
                this.parkingLot.put(spotId, false);
                result = true;
                String dateStr= new Date().toString();
    				System.out.println("Server Log: "+ dateStr +": Parking spot " + spotId + " assigned");
            }
        }

        try {
            try {
                PrintWriter out =
                        new PrintWriter(socket.getOutputStream(), true);
                out.println(result == true ? "YES" : "NO");
            } catch (Exception ex) {
                System.out.println(ex);
            } finally {
                socket.close();
            }
        }catch(Exception ex){
            System.out.println(ex);
        }
    }

    private void getEmptySpots(Socket socket){

        List<Integer> emptySpots = new LinkedList<>();

		// Using mutex becuase multiple threads can modify parkingLot variable
        synchronized (this.parkingLot) {
            Set<Integer> keys = this.parkingLot.keySet();
            for (int spotId : keys) {

                // if free
                if (this.parkingLot.get(spotId)) {
                    emptySpots.add(spotId);
                }
            }
        }

        String emptySpotsStr = emptySpots.stream().map(java.lang.Object::toString)
                .collect(Collectors.joining(", "));

        try {
            try {
                PrintWriter out =
                        new PrintWriter(socket.getOutputStream(), true);
                out.println(emptySpotsStr);
                String dateStr= new Date().toString();
    				System.out.println("Server Log: "+ dateStr +": Available spots returned to client " + emptySpotsStr);
            } catch (Exception ex) {
                System.out.println(ex);
            } finally {
                socket.close();
            }
        }catch(Exception ex){
            System.out.println(ex);
        }
    }

    private HashMap<Integer, Boolean> BuildEmptyParkingLot(){
        HashMap<Integer, Boolean> parking = new HashMap<>();
        for(int i = 0; i < 4; ++i){
            parking.put(i + 1, true);
        }

        return parking;
    }
}
