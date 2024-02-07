package unsa.edu.pe;

import org.eclipse.paho.client.mqttv3.*;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import org.json.JSONObject;

import static java.time.LocalDateTime.*;

public class DataControllerCallback implements MqttCallback {

    private final MqttClient client = App.AWSClient();
    private double lastPublishedTemperature = Double.MIN_VALUE;
    @Override
    public void connectionLost(Throwable cause) {
        System.out.println("connection lost .... : " + cause.getMessage());
    }
    @Override
    public void messageArrived(String topic, MqttMessage message) {
        // Acciones a realizar cuando llega un mensaje del sensor DHT11
        String payload = new String(message.getPayload());
        System.out.println("Received message: \n  topic：" + topic + "\n  Qos：" + message.getQos() + "\n  payload：" + payload);

        // Procesar los datos de temperatura recibidos del sensor DHT11
        processTemperatureData(payload);
    }
    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {
        System.out.println("deliveryComplete");
    }

    public void processTemperatureData(String payload) {

        try {
            // Convertir el string payload a un objeto JSON
            JSONObject jsonPayload = new JSONObject(payload);

            // Extraer los valores del objeto JSON
            double humidity = jsonPayload.getDouble("humidity");
            double temperature = jsonPayload.getDouble("temperature");
            String unitHumidity = jsonPayload.getString("UnitHumidity");
            String unitTemperature = jsonPayload.getString("UnitTemperature");
            String notes = jsonPayload.getString("Notes");
            String timestamp = jsonPayload.getString("timestamp");

            System.out.println("Temperature: " + temperature + unitTemperature);
            System.out.println("Humidity: " + humidity + unitHumidity);

            // Verificar rango de temperatura y si es una temperatura repetida en un rango de +/- 5
            if ((temperature < 10.05 || temperature > 25.89) &&
                    (lastPublishedTemperature == Double.MIN_VALUE || Math.abs(temperature - lastPublishedTemperature) > 0.5)) {

                // Actualiza la última temperatura publicada
                lastPublishedTemperature = temperature;

                // Aquí puedes llamar a saveTemperatureData si es necesario
                // saveTemperatureData(temperature, humidity, unitHumidity, unitTemperature, notes, timestamp);

                // Publica el mensaje
                publishToTopic("mosquitto/aws", payload, 1);
            } else {
                System.out.println("Temperatura dentro del rango aceptable o repetida en un rango de +/- 5 grados. No se publica.");
            }
        } catch (Exception e) {
            System.err.println("Error processing temperature data: " + e.getMessage());
        }

    }

    private void saveTemperatureData(double temperature, double humidity, String unitHumidity, String unitTemperature, String notes ,String timestamp) {
        String url = "jdbc:mysql://localhost:3306/SensorData?allowPublicKeyRetrieval=true&useSSL=false&serverTimezone=UTC";
        String user = "root";
        String password = "mysqladmin";

        try {
            Connection conn = DriverManager.getConnection(url, user, password);
            // Modificar la consulta para incluir los nuevos campos
            String query = "INSERT INTO DHT11Data (temperature,unitTemperature, humidity, unitHumidity, notes, timestamp) VALUES (?, ?, ?, ?, ?, ?)";
            PreparedStatement preparedStmt = conn.prepareStatement(query);
            preparedStmt.setDouble(1, temperature);
            preparedStmt.setString(2, unitTemperature);
            preparedStmt.setDouble(3, humidity);
            preparedStmt.setString(4, unitHumidity);
            preparedStmt.setString(5, notes);
            preparedStmt.setString(6, timestamp);

            preparedStmt.execute();
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    private void publishToTopic(String topic, String payload, int qos) throws Exception {
        try {
            System.out.println("Publishing message to topic: " + topic);
            MqttMessage message = new MqttMessage(payload.getBytes());
            message.setQos(qos); // Configurar la Calidad de Servicio
            client.publish(topic, message); // Publicar el mensaje
            System.out.println("Message published: " + payload);
        } catch (MqttException e) {
            e.printStackTrace();
            throw new Exception("Error al publicar el mensaje en el tópico: " + e.getMessage());
        }
    }
}
