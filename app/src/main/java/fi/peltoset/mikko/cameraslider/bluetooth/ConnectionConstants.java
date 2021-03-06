package fi.peltoset.mikko.cameraslider.bluetooth;

public class ConnectionConstants {
    public static byte FLAG_START = (byte) 0b11110000;
    public static byte FLAG_STOP = 0b00001111;
    public static byte SEND_VERIFICATION = 11;
    public static byte MOVE_MOTORS = 32;
    public static byte GO_HOME = 31;
    public static final byte HOMING_DONE = 22;
    public static byte SET_HOME = 30;
    public static byte SEND_HANDSHAKE_GREETING = 10;
    public static byte SAVE_INSTRUCTIONS = 33;
    public static final byte DATA_RECEIVED = 24;
    public static final byte BEGIN_DATA_DOWNLOAD = 35;
    public static final byte SEND_DATA_CHECKSUM = 36;
    public static final byte SAVE_SETTINGS = 34;
    public static String HANDSHAKE_GREETING = "r6lrj37e2nkaavgz";
    public static String HANDSHAKE_RESPONSE = "77fpm1lyw612jhlr";
    public static int VERIFICATION_INTERVAL = 5000;
}
