package fi.peltoset.mikko.cameraslider.bluetooth;

public class ConnectionConstants {
    public static byte FLAG_START = 0x01;
    public static byte FLAG_STOP = 0x00;
    public static byte SEND_VERIFICATION = 0x09;
    public static byte MOVE_MOTORS = 0x05;
    public static byte SEND_HANDSHAKE_GREETING = 0x08;
    public static String HANDSHAKE_GREETING = "r6lrj37e2nkaavgz";
    public static String HANDSHAKE_RESPONSE = "77fpm1lyw612jhlr";
    public static int VERIFICATION_INTERVAL = 5000;
}
