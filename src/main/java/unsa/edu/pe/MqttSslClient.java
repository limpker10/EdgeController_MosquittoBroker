package unsa.edu.pe;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

public class MqttSslClient {
    public static void main(String[] args) {
        String topic = "test/topic";
        String content = "Hello World";
        int qos = 1;
        String broker = "ssl://192.168.1.13:8883";
        String clientId = MqttClient.generateClientId();
        // persistence
        MemoryPersistence persistence = new MemoryPersistence();
        // MQTT connect options
        MqttConnectOptions connOpts = new MqttConnectOptions();
        connOpts.setHttpsHostnameVerificationEnabled(false);
        // CA certificate
        try {

            connOpts.setSocketFactory(SSLUtils.getSingleSocketFactory("mqttCA.crt"));
            MqttClient client = new MqttClient(broker, clientId, persistence);
            // callback : The callback class used when connecting over TCP Port.
            client.setCallback(new CallbackTCP());
            // Connect
            System.out.println("Connecting to broker: " + broker);
            client.connect(connOpts);
            System.out.println("Connected to broker: " + broker);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}