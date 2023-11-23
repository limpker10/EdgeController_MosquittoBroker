package unsa.edu.pe;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import org.json.JSONObject;

import static java.time.LocalDateTime.*;

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
            // Convertir el string payload a un objeto JSON
            JSONObject jsonPayload = new JSONObject(payload);

            // Extraer los valores del objeto JSON
            double humidity = jsonPayload.getDouble("humidity");
            double temperature = jsonPayload.getDouble("temperature");
            String unitHumidity = jsonPayload.getString("UnitHumidity");
            String unitTemperature = jsonPayload.getString("UnitTemperature");
            String notes = jsonPayload.getString("Notes");

            System.out.println("Temperature: " + temperature + unitTemperature);
            System.out.println("Humidity: " + humidity + unitHumidity);

            // Guardar en la base de datos
            saveTemperatureData(temperature, humidity, unitHumidity, unitTemperature, notes);

            // Verificar rango de temperatura
            if (temperature < 10 || temperature > 25) {
                System.out.println("Temperatura fuera de rango normal: " + temperature + unitTemperature);
            }

        } catch (Exception e) {
            System.err.println("Error processing temperature data: " + e.getMessage());
        }
    }

    private void saveTemperatureData(double temperature, double humidity, String unitHumidity, String unitTemperature, String notes) {
        String url = "jdbc:mysql://localhost:3306/SensorData?allowPublicKeyRetrieval=true&useSSL=false&serverTimezone=UTC";
        String user = "root";
        String password = "mysqladmin";

        try {
            Connection conn = DriverManager.getConnection(url, user, password);
            // Modificar la consulta para incluir los nuevos campos
            String query = "INSERT INTO DHT11Data (temperature,unitTemperature, humidity, unitHumidity, notes, recordTime) VALUES (?, ?, ?, ?, ?, ?)";
            PreparedStatement preparedStmt = conn.prepareStatement(query);
            preparedStmt.setDouble(1, temperature);
            preparedStmt.setString(2, unitTemperature);
            preparedStmt.setDouble(3, humidity);
            preparedStmt.setString(4, unitHumidity);
            preparedStmt.setString(5, notes);
            preparedStmt.setString(6, now().toString());

            preparedStmt.execute();
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
