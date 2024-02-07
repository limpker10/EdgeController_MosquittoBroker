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
        connOpts.setSocketFactory(SSLUtils.getSingleSocketFactory("mqttCA.crt"));
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
        connOpts.setSocketFactory(SSLUtils.getAWSIotSocketFactory("AmazonRootCA1.pem","Certificate.crt","private.key"));
        // Establecer callback y conectar
        client.setCallback(new AWSControllerCallback());
        System.out.println("Connecting to broker: " + broker);
        client.connect(connOpts);
        System.out.println("Connected to broker: " + broker);
        subscribeToTopic(client,"aws/sub",1);
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

}