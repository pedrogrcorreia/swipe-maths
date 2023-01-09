# Swipe Maths
## Overview
A mathematical game where the player has to swipe the column or row with the highest operation result. 

Can be played alone or with friends!

## Online functionality

To implement the communication between devices is used a TCP Multithreaded Server. 
One device is used as a server while the clients connect to its socket.

All the communication is made using JSON messages, where a <i>Request</i> field tells the entity what is asked.

## User registration/login

Registration and login are implemented using Authentication from Firebase.

## Top scores

The highest games are stored in a Cloud Firestore NoSQL database.
