package hudson.plugins.growl;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

import java.net.SocketException;
import java.nio.ByteBuffer;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import java.util.Arrays;

public class MacGrowler {
	private String host = "localhost";
    private int port = 9887;

    private final byte PROTOCOL_VERSION = 1;
    private final byte TYPE_REGISTRATION = 0;
    private final byte TYPE_NOTIFICATION = 1;
    private String NOTIFICATION_NAME = "MacGrowler";
    
    private MacGrowler( String host, int port ) {
      this.host = host;
      this.port = port;
    }
   
    private byte[] stringEnc( String str ) {
      try {
          return str.getBytes( "UTF8");
      } catch (UnsupportedEncodingException e) {
          e.printStackTrace();
      }
     
      return null;
    }

    private byte[] md5( byte[] bytes, String password ) {
      MessageDigest md;
      try {
          md = MessageDigest.getInstance( "MD5" );
          md.update(bytes);
         
          if( password != null && password != "") {
            md.update(stringEnc( password ));
          }               
          return md.digest();
      } catch (NoSuchAlgorithmException e) {
          e.printStackTrace();
      }
      return null;
    }

    private ByteBuffer registrationPacket( String appName, String password ) {
      byte[] name = stringEnc( appName );
      byte[] notName = stringEnc(NOTIFICATION_NAME);
     
      int len = 6 + name.length + 2 + notName.length + 1 + 16;

      ByteBuffer bb = ByteBuffer.allocate( len );
      bb.put(PROTOCOL_VERSION);
      bb.put(TYPE_REGISTRATION);
      bb.putShort((short)name.length);
      bb.put((byte) 1);  //nall
      bb.put((byte) 1);  //ndef
      bb.put(name);
      bb.putShort((short)notName.length);
      bb.put(notName);
      bb.put((byte) 0);  //defaults
      bb.put(md5( Arrays.copyOf( bb.array(), len - 16 ), password ));
     
      return bb;

    }
    
    private ByteBuffer notificationPacket( String appName, String title, String message, String password ) {
      byte[] uappName = stringEnc(appName);
      byte[] unotif = stringEnc(NOTIFICATION_NAME);
      byte[] utitle = stringEnc(title);
      byte[] umessage = stringEnc(message);
     
      int  len = 12 + unotif.length + utitle.length + umessage.length + uappName.length + 16;
      ByteBuffer bb = ByteBuffer.allocate( len );
     
        bb.put(PROTOCOL_VERSION);
        bb.put(TYPE_NOTIFICATION);
        bb.putShort((short) 1);              // Not sure what the flag value is for...
        bb.putShort((short) unotif.length);
        bb.putShort((short) utitle.length);
        bb.putShort((short) umessage.length);
        bb.putShort((short) uappName.length);
        bb.put(unotif);
        bb.put(utitle);
        bb.put(umessage);
        bb.put(uappName);
        bb.put(md5( Arrays.copyOf( bb.array(), len - 16 ), password ));
       
        return bb;
    }

    private void sendPacket( byte [] bytes ) {
      try {

          DatagramSocket sct = new DatagramSocket();
          sct.connect(InetAddress.getByName( host ), port );

          DatagramPacket pkt = new DatagramPacket( bytes, bytes.length);
         
          sct.send(pkt);
          sct.close();
      } catch (SocketException e) {
          e.printStackTrace();
      } catch (IOException e) {
          e.printStackTrace();
      }

    }

    private void doRegistration( String appName, String password ) {
      sendPacket( registrationPacket( appName, password ).array() );
    }
   
    public void notify ( String appName, String title, String message, String password) {
      sendPacket( notificationPacket( appName, title, message, password ).array() );
    }
   
    public static MacGrowler register( String appName, String password) {
        return register( appName, password, "localhost", 9887);
    }
   
    public static MacGrowler register( String appName, String password, String host) {
        return register( appName, password, host, 9887);
    }
   
    public static MacGrowler register( String appName, String password, String host, int port) {
      MacGrowler g = new MacGrowler( host, port );
      g.doRegistration( appName, password );
      return g;
    }
}
