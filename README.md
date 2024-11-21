## CNT5106C - Computer Networks
## Project 

| Member | UFID |
| ------ | ------ |
| Dharmesh Adith Varma Penmetsa 
| Darshini Ram Mattaparthi 
| Jahnavi Reddy Bheemavarapu 

## Overview
The project is to create a peer-to-peer (P2P) file sharing program that is comparable to BitTorrent, a popular P2P protocol for file delivery. Java has been used to implement the project. The project's primary goal is to implement BitTorrent's essential choking-unchoking mechanism. The amount of connections that are active at once when uploading pieces to peers is controlled by this technique. Handshakes, message types (choke, unchoke, interested, not interested, have, bitfield, request, piece), and peer behaviour (peer connections, bitfields, and download tactics) are all covered in the project's protocol definition. The project needs to keep track of a file directory structure and provide logs for different peer operations. Configuration files are available for shared attributes and peer data.


## Result

The project successfully implements a peer-to-peer (P2P) file-sharing application, modeled after the BitTorrent protocol and implemented in Java. The primary emphasis is on integrating BitTorrent's critical choking-unchoking mechanism, which regulates the quantity of active connections while uploading file pieces to peers. The project comprehensively covers various aspects, including handshake procedures, message types (choke, unchoke, interested, not interested, have, bitfield, request, piece), and peer behavior, encompassing peer connections, bitfields, and download strategies.


Choking-Unchoking Mechanism:

Essential for managing the quantity of active connections during file piece uploads.
Message Types:

Choke, Unchoke, Interested, Not Interested, Have, Bitfield, Request, Piece.
Facilitating diverse communication between peers.
Peer Behavior:

Peer Connections: Establishing and overseeing connections between peers.
Bitfields: Monitoring the availability of pieces among peers.
Download Tactics: Strategies employed by peers during file downloads.
File Management and Logging:

The project adeptly handles a file directory structure and furnishes comprehensive logs for various peer operations, including:

TCP Connection Establishment
Changes in Preferred and Optimistically Unchoked Neighbors
Choking/Unchoking Events
File Download Completion
Configuration Files:

Configuration files play a pivotal role in the project, providing shared attributes and peer data. Noteworthy files such as Common.cfg and PeerInfo.cfg contain crucial details such as file information, size, choking and unchoking intervals, and the count of preferred neighbors.

## Execution Steps
> `make` 
The make command is a build automation tool that initiates the compilation process based on the provided Makefile. It compiles the source code, creating the executable and necessary files for the project.
> 
> `make StartRemotePeers.class` 
This command specifically compiles the StartRemotePeers class, which is likely a class responsible for initiating remote peers in the project. The .class extension indicates a compiled Java class file.
>
> `make StartRemotePeers` 
After compiling the StartRemotePeers class, this command executes the compiled program. It is likely that this class is responsible for starting remote peers in the servers or nodes specified in the project.
> 
> `rm -r *.class` 
#This is a shell command (likely for a Unix-based system like Linux) to remove all.class files in the current directory recursively (-r).

