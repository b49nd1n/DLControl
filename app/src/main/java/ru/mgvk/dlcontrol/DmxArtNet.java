package ru.mgvk.dlcontrol;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import java.util.List;

import artnet4j.ArtNet;
import artnet4j.ArtNetNode;
import artnet4j.events.ArtNetDiscoveryListener;
import artnet4j.packets.ArtDmxPacket;


/**
 * Класс, отвечающий за работу протокола Art-Net
 *
 * Created by mihail on 09.11.15.
 */

public class DmxArtNet implements ArtNetDiscoveryListener {

    boolean works=false;

    Context context;

    MainActivity MAct;
    DmxControl dmxControl;
    DmxArtNet localcontext;

    private ArtNetNode netLynx;

    private int sequenceID;

    DmxArtNet(Context parentcontext){
        context = parentcontext;
        MAct = (MainActivity) parentcontext;
        dmxControl = MAct.dmxControl;
        localcontext = this;
    }


    @Override
    public void discoveredNewNode(ArtNetNode node) {
        if (netLynx == null) {
            netLynx = node;
            Log.d("ArtNetNode ","IP: "+node.getIPAddress());
        }
    }

    @Override
    public void discoveredNodeDisconnected(ArtNetNode node) {
        Log.d("ArtNetNode","Disconnected " + node);
        if (node == netLynx) {
            netLynx = null;
        }
    }

    @Override
    public void discoveryCompleted(List<ArtNetNode> nodes) {
        Log.d("ArtNetNode",""+nodes.size() + " nodes found:");
        for (ArtNetNode n : nodes) {
            Log.d("ArtNetNode", "" + n);
        }
    }

    @Override
    public void discoveryFailed(Throwable t) {
        Log.d("ArtNetNode","Discovery failed");
    }


    /**
     * COPYPASTED FROM GITHUB
     */

//TODO Разобрать алгоритм!!!
    public class artNet_Thread implements Runnable{


        @Override
        public void run() {
            ArtNet artnet = new ArtNet();

            try {
                works = true;
                artnet.start();
                artnet.getNodeDiscovery().addListener(localcontext);
                artnet.startNodeDiscovery();
                ArtDmxPacket dmx;
                while(netLynx==null){
                    Thread.sleep(1500);
                }
                while (works) {
                        dmx = new ArtDmxPacket();
                        dmx.setUniverse(netLynx.getSubNet(),
                                netLynx.getDmxOuts()[0]);
                        dmx.setSequenceID(sequenceID % 255);

                        dmx.setDMX(MAct.dmxmas, MAct.dmxmas.length);

                        artnet.unicastPacket(dmx, netLynx.getIPAddress());

                        dmx.setUniverse(netLynx.getSubNet(),
                                netLynx.getDmxOuts()[1]);
                        artnet.unicastPacket(dmx, netLynx.getIPAddress());
                        sequenceID++;

                    Thread.sleep(30);
                }
            } catch (Exception e) {
                works = false;
                Log.e("ArtNetThread",String.valueOf(e));
                MAct.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(context,MAct.getString(R.string.artneterr),Toast.LENGTH_SHORT).show();
                    }
                });
                e.printStackTrace();

            }
        }
    }


}
