# CMPE - 275 - Grand Challenge 1 

## Objective
The objective of this grand challenge is to construct a software tool that connects processes together to manage the three Vs. (Volume, Velocity, and variety) and the volatility of the process participation lifecycle.

## Team Name: ACHIEVERS

## Project Members:
| Project Members              | Student ID | 
|------------------------------|------------|
| Thirumala Sai Krishna Kaaja  | 016033758  |
| Sushmitha Dhummi Thrilochana | 016043755  |
| Santhosh Bodla           | 016002454  |
| Sai Sri Harshini Kosuri     | 016005912  |
| Sai Subhash Reddy Gangireddygari             | 016003403  |
| Abhiram Yenugadhati             | 015962128  |


## Approach/Design:

* We will be having a client written in Java which will be pushing continuous data into the Communication Server.
* The Communication server is a service written in Java and will act as a channel for the data flow across the network
* The Communication server will store the received data in a LinkedBlockingQueue.
* Then, we will have WorkDivider Thread which will continuously check for any items in the queue.
* If it finds any items, it will take the items and divide them between 4 different servers in order to be processed.
* We also have 4 different servers out of which 3 are written in Java and one is written in Python for achieving the variety.
* These 4 different servers will receive the data from the Communication server and process them and returns the response back to the Communication server.
* So, this is how we are achieving the data communication in the network through distributed multi client server architecture.

## Technologies and Frameworks:
* gRpc
* ProtoBuf
* Java
* Python