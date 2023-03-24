# FTP-Sliding-Windows
An FTP Implementation using Sliding Window Packet Transmission

To run, ensure you have Maven installed

- First run the server with `mvn exec:java -Dexec.mainClass="org.tftp.server.Server" -Dexec.args="<1> <2> <3>"`

| Key           | Value                                                                                    |
| ------------- |:----------------------------------------------------------------------------------------:|
| <1>           | The desired IP address you want to run the server on                                     |
| <2>           | The desired port you want to run the server on                                           |
| <3>           | 'no-drop' to not simulate 1% of packet loss or 'drop' to simulate 1% of packet loss      |

After each interaction with a client, the server prints out the throughput of the transfer to the console.

- Run the client with `mvn exec:java -Dexec.mainClass="org.tftp.client.Client" -Dexec.args="<1> <2> <3>"`

| Key           | Value         |
| ------------- |:-------------:|
| <1>           | The IP address the server is on |
| <2>           | The port the server is on     |
| <3>           | URL of an image you wish to view      |

The client will display the image the URL points to in a separate window upon successful transmission. The server caches previously
requested images so repeat requests of the same URL are inherently faster than a request of a brand new URL. 
