package soundsenderinator5;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Inet4Address;
import java.net.InetAddress;
//import java.net.MulticastSocket;
import java.util.concurrent.ArrayBlockingQueue;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.TargetDataLine;

public class Soundd {
    // all "///" are for toggling speaker 

    static ArrayBlockingQueue<byte[]> a;
    //volatile static ArrayBlockingQueue<byte[]> spkr;///
    //static volatile boolean spkra11 = false;///
    static volatile boolean a11 = false;
    static TargetDataLine line = null;
    //static SourceDataLine line1=null;///
    static String IPstr;

    public static void sdard() {
        stop();
        a11 = true;
        a = new ArrayBlockingQueue<>(1);
        new Thread(new Runnable() {
            @Override
            public void run() {
                int i = 0;
                for (Mixer.Info p : AudioSystem.getMixerInfo()) {
                    System.out.println(i + "  " + p.toString() + " " + p.getDescription());
                    i++;
                }

                Mixer m = AudioSystem.getMixer(AudioSystem.getMixerInfo()[4]);
                AudioFormat format = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, 44100, 16, 1, 2, 44100, false);
                if (!m.isLineSupported(new DataLine.Info(TargetDataLine.class,
                        format))) {
                    System.out.println("not supported");
                }

                try {

                    line = (TargetDataLine) m.getLine(new DataLine.Info(TargetDataLine.class, format));
                    line.open(format, 3000);
                    int numBytesRead;
                    //byte[] data = new byte[3000];
                    //System.out.println(" " + data.length);
                    line.start();

                    while (a11) {
                        byte[] data = new byte[3001];
                        line.read(data, 0, 3000);
                        //System.out.println(""+data[5]);
                        try {
                            //                sendData(Arrays.copyOf(data, numBytesRead));
                            a.put(data);
                            //if(!(spkr==null))spkr.put(data);  ///

                        } catch (Exception ex) {
                            System.out.println("error in sound queue put()" + ex.toString());
                        }
                    }

                    line.flush();
                } catch (LineUnavailableException ex) {
                    System.out.println(ex.toString());
                }
            }
        }).start();

        new Thread(new Runnable() {
            @Override
            public void run() {
                byte d = -128;
                byte[] data = null;
                try {
                    //send data starts.
                    Inet4Address address = (Inet4Address) InetAddress.getByName(IPstr);
                    int port = 1234;
                    DatagramSocket ds = new DatagramSocket();
                    //ssend data ends
                    while (a11) {

                        data = a.take();
                        data[3000] = d;
//                        System.out.println(" " + data[3000-1]);
//                        sendData(data);//// 
//                        sendData(getData());
                        // send data continued
                        DatagramPacket sendData = new DatagramPacket(data, data.length, address, port);
                        ds.send(sendData);
                        //send data ends here
                        d++;
                    }
                } catch (Exception ex) {
                    System.out.println("error in sound queue take() and sendData()" + ex.toString());
                    //System.out.println(""+data.length);
                }
            }
        }).start();

    }

//    public static void sendData(byte[] b) throws Exception {
//        //Inet4Address address = (Inet4Address) InetAddress.getByAddress(new byte[]{((byte) 192), ((byte) 168), ((byte) 1), ((byte) 2)});
//        Inet4Address address = (Inet4Address) InetAddress.getByName(IPstr);
////        MulticastSocket ms  = new MulticastSocket();
//
//        int port = 1234;
//        DatagramSocket ds = new DatagramSocket();
//        DatagramPacket sendData = new DatagramPacket(b, b.length, address, port);
//        ds.send(sendData);
//    }
    ///
//    public static void spkrPlay(){///
//            
//            new Thread(new Runnable() {
//            @Override
//            public void run() {
//                spkr = new ArrayBlockingQueue<>(1);
//                Mixer m = AudioSystem.getMixer(AudioSystem.getMixerInfo()[2]);
//                AudioFormat format = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, 44100, 16, 1, 2, 44100, false);
//                if (!m.isLineSupported(new DataLine.Info(SourceDataLine.class,
//                        format))) {
//                    System.out.println("not supported");
//                }
//
//                try {
//                    line1 = (SourceDataLine) m.getLine(new DataLine.Info(SourceDataLine.class, format));
//                    line1.open(format, 3000);
//                    //int numBytesRead;
//                    line1.start();
//                    if(!(spkr==null)){
//                    while (spkra11) {
//                        
//                            line1.write(spkr.take(), 0, 3000);
//                    }}
//                    line1.flush();
//                    line1.close();
//                    line1 = null;
//                    spkr= null;
//                    m = null;
//                    format = null;
//                } catch (Exception ex) {
//                    System.out.println("eror in spkrPlay() "+ex.toString());
//                }
//            }
//        }).start();}

    public synchronized static void setIP(String a) {
        IPstr = a;
    }
    ///
//    public synchronized static void setSpkr(){
//    spkra11 = !spkra11;}

    public static boolean isset() {
        return !(IPstr == null);
    }

    public static void stop() {
        a11 = false;
        //spkra11 = false;///
        //spkr = null;///
        // line1= null;///
        if (!(a == null)) {
            a.clear();
        }
        a = null;
        if (!(line == null) && line.isOpen()) {
            line.close();
            line = null;
        }
    }
}
