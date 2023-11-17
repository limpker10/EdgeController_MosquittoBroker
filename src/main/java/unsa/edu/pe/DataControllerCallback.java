package unsa.edu.pe;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class DataControllerCallback implements MqttCallback {

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

    private void processTemperatureData(String payload) {
        try {
            // Supongamos que el payload es un string con temperatura y humedad separados por coma
            String[] values = payload.split(",");
            float temperature = Float.parseFloat(values[0]);
            float humidity = Float.parseFloat(values[1]);
            // Agrega aquí la lógica para obtener otros valores del payload si es necesario

            System.out.println("Temperature from DHT11 sensor: " + temperature + "°C");
            System.out.println("Humidity from DHT11 sensor: " + humidity + "%");

            // Guardar en la base de datos independientemente del rango
            saveTemperatureData(temperature, humidity);

            // Verificar rango de temperatura
            if (temperature < 10 || temperature > 24) {
                System.out.println("Temperatura fuera de rango normal: " + temperature + "°C");
            }

        } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
            System.err.println("Error processing temperature data: " + e.getMessage());
        }
    }

    private void saveTemperatureData(float temperature, float humidity) {
        String url = "jdbc:mysql://localhost:3306/SensorData?allowPublicKeyRetrieval=true&useSSL=false&serverTimezone=UTC";
        String user = "root";
        String password = "mysqladmin";

        try {
            Connection conn = DriverManager.getConnection(url, user, password);
            String query = "INSERT INTO DHT11Data (temperature, humidity, timestamp) VALUES (?, ?, CURRENT_TIMESTAMP)";
            PreparedStatement preparedStmt = conn.prepareStatement(query);
            preparedStmt.setFloat(1, temperature);
            preparedStmt.setFloat(2, humidity);
            preparedStmt.execute();
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
