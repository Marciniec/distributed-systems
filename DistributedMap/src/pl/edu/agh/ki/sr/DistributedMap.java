package pl.edu.agh.ki.sr;

import org.jgroups.*;
import org.jgroups.protocols.*;
import org.jgroups.protocols.pbcast.GMS;
import org.jgroups.protocols.pbcast.NAKACK2;
import org.jgroups.protocols.pbcast.STABLE;
import org.jgroups.stack.ProtocolStack;
import org.jgroups.util.Util;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DistributedMap extends ReceiverAdapter implements SimpleStringMap {
    private final Map<String, String> map = new HashMap<>();
    private JChannel channel;
    private ProtocolStack stack;

    DistributedMap(String channelName) {
        try {
            this.channel = new JChannel();
            channel.setReceiver(this);
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
        stack = new ProtocolStack();
        channel.setProtocolStack(stack);
        stack.addProtocol(new UDP().setValue("mcast_group_addr", InetAddress.getByName(multicastAddress)))
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

    public void getState(OutputStream output) throws Exception {
        synchronized (map) {
            Util.objectToStream(map, new DataOutputStream(output));
        }

    }

    public void viewAccepted(View new_view) {
        handleView(channel, new_view);
    }

    private static void handleView(JChannel ch, View new_view) {
        if (new_view instanceof MergeView) {
            ViewHandler handler = new ViewHandler(ch, (MergeView) new_view);
            // requires separate thread as we don't want to block JGroups
            handler.start();
        }
    }

    private static class ViewHandler extends Thread {
        JChannel ch;
        MergeView view;

        private ViewHandler(JChannel ch, MergeView view) {
            this.ch = ch;
            this.view = view;
        }

        public void run() {
            List<View> subgroups = view.getSubgroups();
            View tmp_view = subgroups.get(0); // picks the first
            Address local_addr = ch.getAddress();
            if (!tmp_view.getMembers().contains(local_addr)) {
                System.out.println("Not member of the new primary partition ("
                        + tmp_view + "), will re-acquire the state");
                try {
                    ch.getState(null, 30000);
                } catch (Exception ignored) {
                }
            } else {
                System.out.println("Not member of the new primary partition ("
                        + tmp_view + "), will do nothing");
            }
        }
    }

    public void printState() {
        synchronized (map) {
            for (Map.Entry<String, String> entry :
                    map.entrySet()) {
                System.out.println(entry);
            }
        }
    }

    @Override
    public void receive(Message msg) {
        System.out.println(msg.getObject());
        if (msg.getSrc().equals(channel.getAddress())) {
            return;
        }
        handleReceivedMessage((String) msg.getObject());
    }

    private void handleReceivedMessage(String message) {
        String operation = message.split(":")[0];
        String key = message.split("key: ")[1].split(" value: ")[0];
        synchronized (map) {
            switch (operation) {
                case "remove":
                    map.remove(key);
                    break;
                case "put":
                    String value = message.split("value: ")[1];
                    map.put(key, value);
                    break;
                default:
                    break;
            }
        }
    }

    public void setState(InputStream input) throws Exception {
        Map<String, String> tmpMap;
        tmpMap = (HashMap<String, String>) Util.objectFromStream(new DataInputStream(input));
        synchronized (map) {
            map.clear();
            map.putAll(tmpMap);
        }

    }
    public void close() throws InterruptedException {
        Thread.sleep(20000);
        channel.close();
    }
}
