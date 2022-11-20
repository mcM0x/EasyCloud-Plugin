package net.easycloud.plugin.socket;

import net.easycloud.packet.Packet;
import net.easycloud.packet.PacketManager;
import net.easycloud.packet.list.HelloPacket;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class EasyCloudSocket implements Runnable {

    private Socket socket;

    private Queue<Packet> packetQueue = new ConcurrentLinkedQueue<>();

    private DataInputStream inputStream;
    private DataOutputStream outputStream;

    private PacketManager packetManager;

    public EasyCloudSocket(String host, int port, PacketManager packetManager) throws IOException {
        this.packetManager = packetManager;
        this.socket = new Socket(host, port);
        this.inputStream = new DataInputStream(socket.getInputStream());
        this.outputStream = new DataOutputStream(socket.getOutputStream());

        this.sendPacket(new HelloPacket("Hello from Plugin"));

    }

    public void sendPacket(Packet packet) throws IOException {
        this.packetQueue.add(packet);
    }

    @Override
    public void run() {

        while (this.socket.isBound()) {
            try {
                if (!packetQueue.isEmpty()) {
                    Packet poll = packetQueue.poll();

                    int packetId = packetManager.getPacketId(poll.getClass());
                    System.out.println("sending packet with id: " + packetId);
                    this.outputStream.writeInt(packetId);
                    this.outputStream.flush();


                    poll.write(this.outputStream);

                    this.outputStream.flush();

                }

                if (this.inputStream.available() > 0) {
                    int packetId = this.inputStream.readInt();
                    if (this.packetManager.existsPacketById(packetId)) {
                        Packet packet = this.packetManager.getPacketById(packetId).newInstance();
                        packet.read(this.inputStream);
                        packetManager.processPacket(packet);

                    }
                }
            } catch (IOException | InstantiationException | IllegalAccessException e) {
                throw new RuntimeException(e);
            }
            try {
                Thread.sleep(5);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

        }

    }
}
