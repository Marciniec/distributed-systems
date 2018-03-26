package pl.edu.agh.ki.sr.map;

import org.jgroups.JChannel;
import org.jgroups.ReceiverAdapter;
import org.jgroups.protocols.*;
import org.jgroups.protocols.pbcast.GMS;
import org.jgroups.protocols.pbcast.NAKACK2;
import org.jgroups.protocols.pbcast.STABLE;
import org.jgroups.stack.ProtocolStack;

import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;

public class DistributedMap extends ReceiverAdapter implements SimpleStringMap {
    private final Map<String, String> map = new HashMap<>();
    private JChannel channel;

    DistributedMap(String channelName) {
        try {

            this.channel = new JChannel();
            Receiver receiver = new Receiver(channel,map);
            channel.setReceiver(receiver);
            channel.connect(channelName);
            channel.getState(null, 30000);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    DistributedMap(String channelName, String multicastAddress) {
        try {

            this.channel = new JChannel();
            channel.setReceiver(this);
            channel.connect(channelName);
            channel.getState(null, 30000);
            initProtocolStack(multicastAddress);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initProtocolStack(String multicastAddress) throws Exception {
        ProtocolStack stack = new ProtocolStack();
        channel.setProtocolStack(stack);
        stack.addProtocol(new UDP())
                .addProtocol(new UDP().setValue("mcast_group_addr", InetAddress.getByName(multicastAddress)))
                .addProtocol(new PING())
                .addProtocol(new MERGE3())
                .addProtocol(new FD_SOCK())
                .addProtocol(new FD_ALL().setValue("timeout", 12000).setValue("interval", 3000))
                .addProtocol(new VERIFY_SUSPECT())
                .addProtocol(new BARRIER())
                .addProtocol(new NAKACK2())
                .addProtocol(new UNICAST3())
                .addProtocol(new STABLE())
                .addProtocol(new GMS())
                .addProtocol(new UFC())
                .addProtocol(new MFC())
                .addProtocol(new FRAG2());

        stack.init();
    }

    public boolean containsKey(String key) {
        synchronized (map) {
            return map.containsKey(key);
        }
    }

    public String get(String key) {
        synchronized (map) {
            return map.get(key);
        }
    }

    public String put(String key, String value) {
        try {
            channel.send(null, "put: " + "key: " + key + " value: " + value);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return map.put(key, value);
    }

    public String remove(String key) {
        try {
            channel.send(null, "remove: " + "key: " + key);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return map.remove(key);
    }

    public void printState() {
        synchronized (map) {
            for (Map.Entry<String, String> entry :
                    map.entrySet()) {
                System.out.println(entry);
            }
        }
    }


    public void close() throws InterruptedException {
        Thread.sleep(20000);
        channel.close();
    }
}
