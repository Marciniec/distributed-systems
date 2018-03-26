package pl.edu.agh.ki.sr.map;

import org.jgroups.*;
import org.jgroups.util.Util;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Receiver extends ReceiverAdapter {
    private JChannel channel;
    private Map<String, String> map;

    public Receiver(JChannel channel, Map<String, String> map) {
        this.channel = channel;
        this.map = map;
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

}
