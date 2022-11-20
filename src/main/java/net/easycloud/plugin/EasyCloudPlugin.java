package net.easycloud.plugin;

import net.easycloud.packet.factory.PacketManagerFactory;
import net.easycloud.packet.list.HelloPacket;
import net.easycloud.packet.list.PingPacket;
import net.easycloud.plugin.socket.EasyCloudSocket;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

public class EasyCloudPlugin extends JavaPlugin {

    private EasyCloudSocket socket;

    @Override
    public void onEnable() {

        try {
            socket = new EasyCloudSocket("127.0.0.1", 8869, PacketManagerFactory.create());
            new Thread(socket).start();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        AtomicInteger id = new AtomicInteger();

        Bukkit.getScheduler().scheduleAsyncRepeatingTask(this, () -> {
            id.getAndIncrement();
            try {
                socket.sendPacket(new PingPacket(System.currentTimeMillis()));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

        }, 20, 20);

    }
}
