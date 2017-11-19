package fi.peltoset.mikko.cameraslider.bluetooth;

import android.bluetooth.BluetoothSocket;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;

import fi.peltoset.mikko.cameraslider.miscellaneous.Constants;

public class ConnectedThread extends Thread {
  private BluetoothSocket socket;
  private ConnectedThreadListener listener;
  private InputStream inputStream;
  private OutputStream outputStream;

  // Interface used to send information to CameraSliderCommunicator
  interface ConnectedThreadListener {
    void onConnect();
    void onDisconnect();
    void onVerificationFail();
    void onNewMessage(byte[] message);
  }

  private boolean verified = false;

  public ConnectedThread(BluetoothSocket socket, final ConnectedThreadListener listener) {
    this.socket = socket;
    this.listener = listener;

    // Open input and output streams to receive and write data. If an error occurs, call
    // onDisconnect() to notify the listener of an unsuccessful connection and cancel the thread.
    try {
      inputStream = socket.getInputStream();
      outputStream = socket.getOutputStream();
    } catch (IOException e) {
      e.printStackTrace();
      listener.onDisconnect();
      return;
    }

    // Send a greeting message to verify the remote device
    write(ConnectionConstants.SEND_HANDSHAKE_GREETING, ConnectionConstants.HANDSHAKE_RESPONSE.getBytes());
  }

  /**
   * Start listening to incoming lines
   */
  public void run() {
    try {
      ByteArrayOutputStream buffer = new ByteArrayOutputStream();
      int inByte;

      // Read incoming bytes. If -1 is returned, the socket has closed.
      while((inByte = inputStream.read()) != -1) {
        // If a correct start byte is received, reset the buffer and start buffering the input.
        if (inByte == ConnectionConstants.FLAG_START) {
          buffer.reset();
        }

        buffer.write(inByte);

        byte[] message = buffer.toByteArray();

        // If a correct stop byte is received we process the message. However, this only happens
        // if the first byte of the buffer is FLAG_START.
        if (inByte == ConnectionConstants.FLAG_STOP) {
          if (message[0] != ConnectionConstants.FLAG_START) {
            return;
          }

          StringBuilder str = new StringBuilder();
          for (byte b : message) {
            str.append(String.format("%02X ", b));
          }

          Log.d(Constants.TAG, "received message: " + str.toString());

          // If the remote device has been already verified to be a Camera Slider, send the message
          // to the listener (BluetoothService). Otherwise check if the received message is a
          // correct response to the initial greeting message. If so, accept the device by setting
          // the boolean flag and notify Bluetooth about a successful connection. If the device
          // wasn't verified successfully, notify the listener about unsuccessful connection and
          // stop the thread.
          if (verified) {
            listener.onNewMessage(message);
          } else {
            verified = verifyDevice(message);

            if (verified) {
              listener.onConnect();
            } else {
              listener.onVerificationFail();
              cancel();
            }
          }
        }

        // To prevent the buffer from expanding indefinitely we check that the first byte indeed
        // was a FLAG_START byte. Otherwise we clear the buffer.
        if (message[0] != ConnectionConstants.FLAG_START) {
          buffer.reset();
        }
      }
    } catch (IOException e) {
      listener.onDisconnect();
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
  }


  /**
   * Send a message (byte array) to the Bluetooth device.
   *
   * @param message
   */
  private void write(byte[] message) {
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
   * Given a message, see if the payload equals the expected handshake greeting message.
   *
   * @param message
   * @return
   */
  private boolean verifyDevice(byte[] message) {
    return Arrays.equals(getPayload(message), ConnectionConstants.HANDSHAKE_GREETING.getBytes());
  }

  /**
   * Send a specific command message to the Camera Slider. Will automatically construct the message
   * by adding required start and stop bytes.
   *
   * @param command Command byte
   * @param payload Data payload, max 61 bytes
   */
  public void write(byte command, byte[] payload) {
    // The complete message must be less than or equal to 64 bytes in length, including the
    // three bytes required for the start, command and stop bytes.
    if (payload.length > 61) {
      throw new RuntimeException("Payload length must be less than or equal to 61 bytes!");
    }

    // Construct the message. First byte is the start byte, second is the command byte. Data bytes
    // take a maximum of 61 bytes. The last byte is
    ByteArrayOutputStream message = new ByteArrayOutputStream();
    message.write(ConnectionConstants.FLAG_START);
    message.write(command);
    message.write(payload, 0, payload.length);
    message.write(ConnectionConstants.FLAG_STOP);

    StringBuilder str = new StringBuilder();
    for (byte b : message.toByteArray()) {
      str.append(String.format("%02X ", b));
    }

    Log.d(Constants.TAG, "sending message: " + str.toString());

    // Send the message
    write(message.toByteArray());
  }

  /**
   * Strip start, command and stop bytes from the message.
   *
   * @param message Raw message
   * @return Payload
   */
  public byte[] getPayload(byte[] message) {
    return Arrays.copyOfRange(message, 2, message.length - 1);
  }
}
