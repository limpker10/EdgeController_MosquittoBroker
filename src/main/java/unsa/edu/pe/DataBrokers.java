package unsa.edu.pe;

import java.util.HashMap;
import java.util.Map;

public interface DataBrokers {
    // Definir un Map estático constante
    HashMap<String, String[]> MAP_BROKERS = new HashMap<String, String[]>() {{
        put("ssl://ajc0lzc2wmskx-ats.iot.us-east-2.amazonaws.com", new String[]{"AmazonRootCA1.pem","Certificate.crt", "private.key"});
        put("ssl://ajc0lzc2wmskx-ats.iot.us-east-2.amazonaws.com", new String[]{"AmazonRootCA1.pem","Certificate.crt", "private.key"});
    }};
}
