package unsa.edu.pe;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.util.HashMap;

public class MqttSslClient {

    public MqttClient setupClient(String broker) throws Exception {
        // Generar un ID de cliente y crear un nuevo cliente MQTT
        String clientId = MqttClient.generateClientId();
        MemoryPersistence persistence = new MemoryPersistence();
        return new MqttClient(broker, clientId, persistence);
    }

    public void connectToBrokerMosquitto(MqttClient client, String broker) throws Exception {
        // Configurar opciones de conexión
        MqttConnectOptions connOpts = new MqttConnectOptions();
        connOpts.setHttpsHostnameVerificationEnabled(false);
//        if (!isBrokerInMap(broker)) {
//            throw new Exception("No existe el broker enviado");
//        }
        connOpts.setSocketFactory(SSLUtils.getSingleSocketFactory("mqttCA.crt"));
        //connOpts.setSocketFactory(SSLUtils.getAWSIotSocketFactory("AmazonRootCA1.pem","Certificate.crt","private.key"));
        // Establecer callback y conectar

        client.setCallback(new DataControllerCallback());
        System.out.println("Connecting to broker: " + broker);
        client.connect(connOpts);
        System.out.println("Connected to broker: " + broker);
    }
    public void connectToBrokerAWS(MqttClient client, String broker) throws Exception {
        // Configurar opciones de conexión
        MqttConnectOptions connOpts = new MqttConnectOptions();
        connOpts.setHttpsHostnameVerificationEnabled(false);
//        if (!isBrokerInMap(broker)) {
//            throw new Exception("No existe el broker enviado");
//        }
        //connOpts.setSocketFactory(SSLUtils.getSingleSocketFactory("mqttCA.crt"));
        connOpts.setSocketFactory(SSLUtils.getAWSIotSocketFactory("AmazonRootCA1.pem","Certificate.crt","private.key"));
        // Establecer callback y conectar
        client.setCallback(new AWSControllerCallback());
        System.out.println("Connecting to broker: " + broker);
        client.connect(connOpts);
        System.out.println("Connected to broker: " + broker);
    }

    private boolean isBrokerInMap(String brokerUrl) {
        return DataBrokers.MAP_BROKERS.containsKey(brokerUrl);
    }


    public void subscribeToTopic(MqttClient client, String topic, int qos) throws Exception {
        // Suscribirse al topico
        client.subscribe(topic, qos);
        System.out.println("Subscribed to topic: " + topic);
    }

    public void publishToTopic(MqttClient client,String topic, String payload, int qos) throws Exception {
        System.out.println("Publishing message to topic: " + topic);
        MqttMessage message = new MqttMessage(payload.getBytes());
        message.setQos(qos); // Configurar la Calidad de Servicio
        client.publish(topic, message); // Publicar el mensaje
        System.out.println("Message published: " + payload);

    }

    private static void disconnectFromBroker(MqttClient client, String broker) throws Exception {
        // Desconectar del broker
        System.out.println("Disconnecting from broker: " + broker);
        client.disconnect();
        System.out.println("Disconnected from broker: " + broker);
    }

    public static void main(String[] args) {
        String topicMosquitto = "esp8266/mosquitto";
        String topicAWS = "esp8266/aws";
        MqttSslClient mosquittoSslClient = new MqttSslClient();
        MqttSslClient AWSSslClient = new MqttSslClient();
        int qos = 1;
        String brokerMosquitto = "ssl://192.168.1.13:8883";
        String brokerAWS = "ssl://ajc0lzc2wmskx-ats.iot.us-east-2.amazonaws.com";

        try {
            MqttClient clientMosquitto = mosquittoSslClient.setupClient(brokerMosquitto);
            mosquittoSslClient.connectToBrokerAWS(clientMosquitto, brokerMosquitto);
            mosquittoSslClient.connectToBrokerMosquitto(clientMosquitto, brokerMosquitto);
            mosquittoSslClient.subscribeToTopic(clientMosquitto, topicMosquitto, qos);

            MqttClient clientAws = AWSSslClient.setupClient(brokerAWS);
            mosquittoSslClient.connectToBrokerAWS(clientAws, brokerAWS);
            mosquittoSslClient.connectToBrokerMosquitto(clientAws, brokerAWS);
            mosquittoSslClient.subscribeToTopic(clientAws, topicAWS, qos);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}