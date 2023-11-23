package unsa.edu.pe;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

public class MqttSslClient {
    static MqttClient setupClient(String broker) throws Exception {
        // Generar un ID de cliente y crear un nuevo cliente MQTT
        String clientId = MqttClient.generateClientId();
        MemoryPersistence persistence = new MemoryPersistence();
        return new MqttClient(broker, clientId, persistence);
    }

    static void connectToBroker(MqttClient client, String broker) throws Exception {
        // Configurar opciones de conexión
        MqttConnectOptions connOpts = new MqttConnectOptions();
        connOpts.setHttpsHostnameVerificationEnabled(false);
        //connOpts.setSocketFactory(SSLUtils.getSingleSocketFactory("mqttCA.crt"));
        connOpts.setSocketFactory(SSLUtils.getAWSIotSocketFactory("Certificate.crt","Certi",""));

        // Establecer callback y conectar
        client.setCallback(new DataControllerCallback());
        System.out.println("Connecting to broker: " + broker);
        client.connect(connOpts);
        System.out.println("Connected to broker: " + broker);
    }

    static void subscribeToTopic(MqttClient client, String topic, int qos) throws Exception {
        // Suscribirse al topico
        client.subscribe(topic, qos);
        System.out.println("Subscribed to topic: " + topic);
    }

    private static void disconnectFromBroker(MqttClient client, String broker) throws Exception {
        // Desconectar del broker
        System.out.println("Disconnecting from broker: " + broker);
        client.disconnect();
        System.out.println("Disconnected from broker: " + broker);
    }

    public static void main(String[] args) {
        String topic = "esp8266/pub";
        int qos = 1;
        String broker = "ssl://192.168.1.13:8883";

        try {
            MqttClient client = MqttSslClient.setupClient(broker);
            MqttSslClient.connectToBroker(client, broker);
            MqttSslClient.subscribeToTopic(client, topic, qos);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}