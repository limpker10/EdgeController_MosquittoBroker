package unsa.edu.pe;


import org.eclipse.paho.client.mqttv3.MqttClient;

public class App {

    static MqttClient AWSClient(){

        String topicAWS = "mosquitto/aws";
        MqttSslClient AWSSslClient = new MqttSslClient();
        MqttClient clientAws;
        int qosAWS = 1;
        String brokerAWS = "ssl://ajc0lzc2wmskx-ats.iot.us-east-2.amazonaws.com";
        try {
            clientAws = AWSSslClient.setupClient(brokerAWS);
            AWSSslClient.connectToBrokerAWS(clientAws, brokerAWS);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
       return  clientAws;
    }
    public static void main(String[] args) {
        String topicMosquitto = "esp8266/mosquitto";
        //String topicAWS = "mosquitto/aws";
        MqttSslClient mosquittoSslClient = new MqttSslClient();
        //MqttSslClient AWSSslClient = new MqttSslClient();
        int qos = 1;
        int qosAWS = 1;
        String brokerMosquitto = "ssl://192.168.1.13:8883";
        String brokerAWS = "ssl://ajc0lzc2wmskx-ats.iot.us-east-2.amazonaws.com";

        try {
            MqttClient clientMosquitto = mosquittoSslClient.setupClient(brokerMosquitto);
            mosquittoSslClient.connectToBrokerMosquitto(clientMosquitto, brokerMosquitto);
            //mosquittoSslClient.connectToBrokerMosquitto(clientMosquitto, brokerMosquitto);
            mosquittoSslClient.subscribeToTopic(clientMosquitto, topicMosquitto, qos);

            //MqttClient clientAws = AWSSslClient.setupClient(brokerAWS);
            //mosquittoSslClient.connectToBrokerAWS(clientAws, brokerAWS);
            //mosquittoSslClient.connectToBrokerMosquitto(clientAws, brokerAWS);
            //mosquittoSslClient.publishToTopic(clientAws, topicAWS,qosAWS);



        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
