# parking-lot
Summary:
The project has two classes - one for server and one for client.
Classes:
1. ParkingServer.java - This class opens the server socket and listens on 9090 port for client connections. Server has 3 functionalities which are invoked via TCP connection:
It looks for first parameter from client (separated by “,”) and allowed values are (1,2,3), which mean following:
Value - 1: getEmptySpots - It returns list of parking spots available currently.
Value - 2: assignParkingSpot - Client request for a parking spot by providing spotId and server returns YES/NO if the request was server successfully or not.
Value - 3: makeParkingSpotAvailable - Client sends a parking spotId which it wants to leave, server releases the spot and makes the spot available.

2. ParkingClient.java - This is the client side code which tries to open a connection with server on 9090 port and sends different requests to server. It also spawns multiple threads to send concurrent requests to server, currently configured to 8.
Client sends multiple requests to server for getting all parking spots, it requests for a parking spot and also frees it up after sometime.

3. EntryPoint.java – This is an additional file to emulate client and server on same machine. See note at
the bottom.
# currently it's not available on cloud 
Cloud Information:
ParkingServer is also running on an AWS EC2 machine and it’s public IP is - 54.89.251.71. ParkingClient is currently configured to contact this machine.
Execution details:
The client can be configured to contact any server by modifying serverIP variable in ParkingClient class. It can be triggered using this command:
java org.uw.adc.client.ParkingClient
The server is running on cloud 54.89.251.71 and can be started on any machine with the following steps:
java -jar parking.jar

Note:
EntryPoint.java lets you emulate server and client on same machine and makes it easy to run/debug the code.
