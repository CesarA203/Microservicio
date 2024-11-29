import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;

import java.io.OutputStream;
import java.io.InputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PanesMicroservicio {

    private static final List<Map<String, Object>> panes = new ArrayList<>();

    public static void main(String[] args) throws IOException {
        // Inicializar con algunos panes
        panes.add(crearPan(1, "Baguette", 1.5, "https://example.com/baguette.jpg"));
        panes.add(crearPan(2, "Ciabatta", 2.0, "https://example.com/ciabatta.jpg"));
        panes.add(crearPan(3, "Croissant", 2.5, "https://example.com/croissant.jpg"));

        // Crear servidor HTTP
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
        server.createContext("/api/panes", new PanesHandler());
        server.setExecutor(null); // Usa el executor predeterminado
        System.out.println("Servidor iniciado en el puerto 8080");
        server.start();
    }

    static class PanesHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String method = exchange.getRequestMethod();

            // Permitir solicitudes CORS
            exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");

            switch (method) {
                case "GET":
                    handleGet(exchange);
                    break;
                case "POST":
                    handlePost(exchange);
                    break;
                default:
                    exchange.sendResponseHeaders(405, -1); // Método no permitido
            }
        }

        private void handleGet(HttpExchange exchange) throws IOException {
            // Convertir lista de panes a JSON
            String jsonResponse = convertirAJson(panes);

            // Enviar respuesta
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, jsonResponse.getBytes().length);

            OutputStream os = exchange.getResponseBody();
            os.write(jsonResponse.getBytes());
            os.close();
        }

        private void handlePost(HttpExchange exchange) throws IOException {
            // Leer cuerpo de la solicitud
            InputStream is = exchange.getRequestBody();
            byte[] bytes = is.readAllBytes();
            String body = new String(bytes, StandardCharsets.UTF_8);

            // Parsear datos del JSON recibido
            Map<String, Object> nuevoPan = parsearJson(body);

            // Agregar ID único y guardar en la lista
            int nuevoId = panes.size() + 1;
            nuevoPan.put("id", nuevoId);
            panes.add(nuevoPan);

            // Responder con confirmación
            String response = "Pan agregado con ID: " + nuevoId;
            exchange.sendResponseHeaders(201, response.getBytes().length);

            OutputStream os = exchange.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }

        private Map<String, Object> parsearJson(String json) {
            Map<String, Object> map = new HashMap<>();
            String[] pares = json.replace("{", "").replace("}", "").split(",");
            for (String par : pares) {
                String[] claveValor = par.split(":");
                String clave = claveValor[0].replace("\"", "").trim();
                String valor = claveValor[1].replace("\"", "").trim();
                try {
                    if (valor.matches("\\d+\\.\\d+")) {
                        map.put(clave, Double.parseDouble(valor));
                    } else if (valor.matches("\\d+")) {
                        map.put(clave, Integer.parseInt(valor));
                    } else {
                        map.put(clave, valor);
                    }
                } catch (NumberFormatException e) {
                    map.put(clave, valor); // Si no es un número, se guarda como texto
                }
            }
            return map;
        }

        private String convertirAJson(List<Map<String, Object>> lista) {
            StringBuilder json = new StringBuilder("[");
            for (int i = 0; i < lista.size(); i++) {
                Map<String, Object> item = lista.get(i);
                json.append("{");
                for (String key : item.keySet()) {
                    json.append("\"").append(key).append("\":");
                    Object value = item.get(key);
                    if (value instanceof String) {
                        json.append("\"").append(value).append("\"");
                    } else {
                        json.append(value);
                    }
                    json.append(",");
                }
                json.setLength(json.length() - 1); // Eliminar última coma
                json.append("}");
                if (i < lista.size() - 1) {
                    json.append(",");
                }
            }
            json.append("]");
            return json.toString();
        }

        private Map<String, Object> crearPan(int id, String nombre, double precio, String imagen) {
            Map<String, Object> pan = new HashMap<>();
            pan.put("id", id);
            pan.put("nombre", nombre);
            pan.put("precio", precio);
            pan.put("imagen", imagen);
            return pan;
        }
    }
}
