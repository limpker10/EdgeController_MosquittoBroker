package unsa.edu.pe;

import org.eclipse.paho.client.mqttv3.*;

public class AWSControllerCallback  implements MqttCallback  {

    private final MqttClient mosquittoClient = App.mosquitoClient;
    @Override
    public void connectionLost(Throwable throwable) {
        System.out.println("Conexión perdida con el broker: " + throwable.getMessage());
    }

    @Override
    public void messageArrived(String topic, MqttMessage message) throws Exception {
        System.out.println("Mensaje recibido:");
        System.out.println("\tTópico: " + topic);
        System.out.println("\tMensaje: " + new String(message.getPayload()));
        publishMosquittoTopic(mosquittoClient,new String(message.getPayload()));
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {
        System.out.println("Entrega del mensaje completada");
    }
    private void publishMosquittoTopic(MqttClient client, String payload) throws Exception {
        MqttMessage message = new MqttMessage(payload.getBytes());
        message.setQos(1); // Configurar la Calidad de Servicio
        client.publish("aws/sub", message); // Publicar el mensaje
        System.out.println("Message published: " + payload);
    }
}
