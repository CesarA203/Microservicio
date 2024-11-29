import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;

import java.io.OutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MicroServicios {

    public static void main(String[] args) throws IOException {
        // Crear el servidor HTTP en el puerto 8080
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);

        // Registrar el endpoint /api/panes
        server.createContext("/api/panes", new PanesHandler());

        // Iniciar el servidor
        server.setExecutor(null); // Usa un executor predeterminado
        System.out.println("Servidor iniciado en el puerto 8080");
        server.start();
    }

    // Clase que maneja las solicitudes al endpoint /api/panes
    static class PanesHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            // Comprobar que el método sea GET
            if ("GET".equals(exchange.getRequestMethod())) {
                // Crear una lista de panes
                List<Map<String, Object>> panes = new ArrayList<>();
                panes.add(crearPan("Baguette", 1.5, "https://example.com/baguette.jpg"));
                panes.add(crearPan("Ciabatta", 2.0, "https://example.com/ciabatta.jpg"));
                panes.add(crearPan("Croissant", 2.5, "https://example.com/croissant.jpg"));

                // Convertir la lista a JSON
                String jsonResponse = convertirAJson(panes);

                // Configurar cabeceras de la respuesta
                exchange.getResponseHeaders().add("Content-Type", "application/json");
                exchange.sendResponseHeaders(200, jsonResponse.getBytes().length);

                // Enviar la respuesta
                OutputStream os = exchange.getResponseBody();
                os.write(jsonResponse.getBytes());
                os.close();
            } else {
                // Si el método no es GET, devolver 405 Method Not Allowed
                exchange.sendResponseHeaders(405, -1);
            }
        }

        // Método para crear un objeto Pan
        private Map<String, Object> crearPan(String nombre, double precio, String imagen) {
            Map<String, Object> pan = new HashMap<>();
            pan.put("nombre", nombre);
            pan.put("precio", precio);
            pan.put("imagen", imagen);
            return pan;
        }

        // Método para convertir una lista de mapas a JSON
        private String convertirAJson(List<Map<String, Object>> lista) {
            StringBuilder json = new StringBuilder("[");
            for (int i = 0; i < lista.size(); i++) {
                Map<String, Object> item = lista.get(i);
                json.append("{");
                json.append("\"nombre\":\"").append(item.get("nombre")).append("\",");
                json.append("\"precio\":").append(item.get("precio")).append(",");
                json.append("\"imagen\":\"").append(item.get("imagen")).append("\"");
                json.append("}");
                if (i < lista.size() - 1) {
                    json.append(",");
                }
            }
            json.append("]");
            return json.toString();
        }
    }
}
