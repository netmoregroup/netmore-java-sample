package netmoregroup.mqtt;

import org.eclipse.paho.mqttv5.client.IMqttToken;
import org.eclipse.paho.mqttv5.client.MqttCallback;
import org.eclipse.paho.mqttv5.client.MqttClient;
import org.eclipse.paho.mqttv5.client.MqttConnectionOptions;
import org.eclipse.paho.mqttv5.client.MqttDisconnectResponse;
import org.eclipse.paho.mqttv5.client.persist.MemoryPersistence;
import org.eclipse.paho.mqttv5.common.MqttException;
import org.eclipse.paho.mqttv5.common.MqttMessage;
import org.eclipse.paho.mqttv5.common.MqttSubscription;
import org.eclipse.paho.mqttv5.common.packet.MqttProperties;

import java.io.FileInputStream;
import java.security.KeyStore;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

import javax.net.ssl.*;

public class NetmoreMqttClient {  

  public static void main(String[] args) {
    
    if( args.length != 1 ) {
      System.out.println("Missing argument <customerId>");
      System.out.println("Customer ID is stored in the certificate as common name.");
      System.exit(1);
    }
    String broker = "ssl://mqtt.netmoregroup.com:8883";
    String customerId = args[0]; 
    String topic = "client/" + customerId + "/#";

    // To get this file from client.key and client.crt use
    // openssl pkcs12 -export -out client.p12 -inkey client.key -in client.crt
    String rootCAPath = "./certs/" + customerId + "/ca.crt";
    String keystorePath = "./certs/" + customerId + "/client.p12";
    String keystorePassword = ""; 
    
    try {
      KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
      trustStore.load(null,null);
      trustStore.setCertificateEntry("Custom CA", (X509Certificate) CertificateFactory.getInstance("X509").generateCertificate(new FileInputStream(rootCAPath)));

      TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
      tmf.init(trustStore);
      TrustManager[] trustManagers = tmf.getTrustManagers();

      // Load the client certificate from the keystore
      KeyStore keystore = KeyStore.getInstance("PKCS12");
      keystore.load(new FileInputStream(keystorePath), keystorePassword.toCharArray());
      
      // Create a KeyManagerFactory with the client certificate
      KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
      keyManagerFactory.init(keystore, keystorePassword.toCharArray());

      // Create an SSLContext with the KeyManagerFactory
      SSLContext sslContext = SSLContext.getInstance("TLS");
      sslContext.init(keyManagerFactory.getKeyManagers(), trustManagers, null);

      // Create an MQTT client
      String clientId = ""; // Leave empty to generate a random client ID
      MqttClient client = new MqttClient(broker, clientId, new MemoryPersistence());

      // Set the SSL socket factory on the client
      SSLSocketFactory socketFactory = sslContext.getSocketFactory();
      MqttConnectionOptions options = new MqttConnectionOptions();
      options.setSocketFactory(socketFactory);      

      // Connect to the MQTT server
      client.connect(options);

      // Subscribe to a topic
      System.out.println("Subscribing to: " + topic);
		  MqttSubscription subscription = new MqttSubscription(topic);
      client.subscribe(new MqttSubscription[] { subscription });

      // Create a callback for receiving MQTT messages
      MqttCallback callback = new MqttCallback() {
        @Override
        public void messageArrived(String topic, MqttMessage message) throws Exception {
          System.out.println("Received message on topic: " + topic);
          System.out.println("Message: " + new String(message.getPayload()));
        }

        @Override
        public void authPacketArrived(int arg0, MqttProperties arg1) {
          // TODO Auto-generated method stub
          throw new UnsupportedOperationException("Unimplemented method 'authPacketArrived'");
        }

        @Override
        public void connectComplete(boolean arg0, String arg1) {
          System.out.println("Connected to the MQTT server");
        }

        @Override
        public void deliveryComplete(IMqttToken arg0) {          
        }

        @Override
        public void disconnected(MqttDisconnectResponse arg0) {
          System.out.println("Disconnected from the MQTT server");
        }

        @Override
        public void mqttErrorOccurred(MqttException arg0) {
          System.out.println("An error occurred: " + arg0.getMessage());
        }
      };

      // Set the callback on the MQTT client
      client.setCallback(callback);
      // Disconnect from the MQTT server      
      client.publish("client/" + customerId + "/test", "Hello World".getBytes(), 0, false);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
