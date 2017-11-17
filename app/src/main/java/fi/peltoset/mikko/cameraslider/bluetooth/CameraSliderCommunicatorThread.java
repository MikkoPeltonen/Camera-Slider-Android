package fi.peltoset.mikko.cameraslider.bluetooth;

import android.bluetooth.BluetoothSocket;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.Console;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Arrays;

import fi.peltoset.mikko.cameraslider.miscellaneous.Constants;

public class CameraSliderCommunicatorThread extends Thread {
  private BluetoothSocket socket;
  private SocketListener listener;
  private InputStream inputStream;
  private OutputStream outputStream;

  interface SocketListener {
    void onConnect(String deviceAddress);
    void onDisconnect();
    void onConnectionFailed();
    void onDeviceDetectionFail();
    void onNewMessage(byte[] message);
  }

  private boolean verified = false;

  public CameraSliderCommunicatorThread(BluetoothSocket socket, final SocketListener listener) {
    this.socket = socket;
    this.listener = listener;

    try {
      inputStream = socket.getInputStream();
      outputStream = socket.getOutputStream();
    } catch (IOException e) {
      e.printStackTrace();
    }

    byte[] handshakeMessage = ConnectionConstants.HANDSHAKE_RESPONSE.getBytes();
    byte[] msg = new byte[handshakeMessage.length + 3];
    msg[0] = ConnectionConstants.FLAG_START;
    msg[1] = ConnectionConstants.SEND_HANDSHAKE_GREETING;
    System.arraycopy(handshakeMessage, 0, msg, 1, handshakeMessage.length);
    msg[handshakeMessage.length + 2] = ConnectionConstants.FLAG_STOP;

    write(msg);
  }

  public void write(byte[] message) {
    try {
      outputStream.write(message);
      outputStream.flush();
    } catch (IOException e) {
      listener.onDisconnect();
      e.printStackTrace();
      cancel();
    }
  }

  /**
   * Start listening to incoming lines
   */
  public void run() {
    try {
      ByteArrayOutputStream buffer = new ByteArrayOutputStream();
      int inByte;
      while((inByte = inputStream.read()) != -1) {
        if (inByte == 0x01) {
          buffer.reset();
        }

        buffer.write(inByte);

        if (inByte == 0x00) {
          if (verified) {
            listener.onNewMessage(buffer.toByteArray());
          } else {
            byte[] rawData = buffer.toByteArray();
            byte[] data = Arrays.copyOfRange(rawData, 2, rawData.length - 1);
            StringBuilder str = new StringBuilder();
            for (byte b : data) {
              str.append(String.format("%02X ", b));
            }
            Log.d(Constants.TAG, "Not yet verified");
          }
          Log.d(Constants.TAG, str.toString());
        }
      }
    } catch (IOException e) {
      listener.onDisconnect();
      e.printStackTrace();
      cancel();
    }
  }

  /**
   * Closes the socket and terminates the thread
   */
  public void cancel() {
    try {
      outputStream.close();
      inputStream.close();
      socket.close();
    } catch (IOException e) {
      e.printStackTrace();
    }

    Thread.currentThread().interrupt();
  }
}
