
package terremotos;

import java.awt.BorderLayout;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException; 
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.*;
import org.xml.sax.InputSource;

public class ConsultaTerremotos extends JFrame {
    private JTable tabla; // Declara una tabla para mostrar los datos de los terremotos
    private JButton button_actualizar; // Declara un botón para actualizar los datos
    private DefaultTableModel datostabla; // Declara el modelo de datos para la tabla
    private JPanel p=new JPanel();

    // Constructor de la clase EarthquakeViewer
    public ConsultaTerremotos() {
        setTitle("Consulta de Terremotos"); // Establece el título de la ventana
        setSize(800, 600); // Establece el tamaño de la ventana
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // Establece la operación de cierre
        setLayout(new BorderLayout()); // Establece el diseño de la ventana
        setLocationRelativeTo(null); // Establece el JFrame en el centro de la pantalla
        add(p);

        // Crea el modelo de la tabla con las columnas necesarias
        datostabla = new DefaultTableModel(new String[]{"Descripción", "Magnitud", "Tiempo", "Longitud", "Latitud", "Profundidad", "Fases Usadas", "Error Estándar", "Gap Azimutal", "Distancia Mínima", "Incertidumbre"}, 0);
        tabla = new JTable(datostabla); // Crea la tabla con el modelo de datos
        add(new JScrollPane(tabla), BorderLayout.CENTER); // Añade la tabla a la ventana dentro de un JScrollPane

        // Crea el botón para actualizar los datos
        button_actualizar = new JButton("Actualizar Datos");
        button_actualizar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ObtenerDatos(); // Llama al método fetchData cuando se presiona el botón
            }
        });
        add(button_actualizar, BorderLayout.SOUTH); // Añade el botón a la parte inferior de la ventana
    }

    // Método para obtener los datos del XML desde la URL
    private void ObtenerDatos() {
        try {
            // Define la URL del servicio de datos de terremotos
            URL url = new URL("https://earthquake.usgs.gov/fdsnws/event/1/query?format=xml&starttime=2014-01-01&endtime=2014-01-02&minmagnitude=5");
            HttpURLConnection con = (HttpURLConnection) url.openConnection(); // Abre una conexión a la URL
            con.setRequestMethod("GET"); // Configura la solicitud HTTP como GET
            con.connect(); // Establece la conexión

            int respuesta = con.getResponseCode(); // Obtiene el código de respuesta HTTP
            if (respuesta != 200) { // Verifica si la respuesta es exitosa (código 200)
                throw new RuntimeException("HttpResponseCode: " + respuesta); // Lanza una excepción si la respuesta no es exitosa
            } else {
                Scanner sc = new Scanner(url.openStream()); // Lee los datos de la URL
                StringBuilder inline = new StringBuilder(); // Almacena los datos leídos
                while (sc.hasNext()) {
                    inline.append(sc.nextLine()); // Lee línea por línea y las añade a inline
                }
                sc.close(); // Cierra el escáner

                // Línea de depuración para imprimir los datos XML obtenidos
                System.out.println("XML Data: " + inline.toString());

                // Parsear el XML
                DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance(); // Crea una fábrica de constructores de documentos
                DocumentBuilder builder = factory.newDocumentBuilder(); // Crea un constructor de documentos
                Document doc = builder.parse(new InputSource(new StringReader(inline.toString()))); // Analiza el XML
                doc.getDocumentElement().normalize(); // Normaliza el documento XML

                // Obtener la lista de eventos
                NodeList nodeList = doc.getElementsByTagName("event"); // Obtiene la lista de eventos
                // Línea de depuración para imprimir el número total de eventos encontrados
                System.out.println("Total de eventos encontrados: " + nodeList.getLength());
                datostabla.setRowCount(0); // Limpia los datos existentes en la tabla

                // Recorrer cada evento y extraer los datos necesarios
                for (int i = 0; i < nodeList.getLength(); i++) {
                    Node node = nodeList.item(i); // Obtiene el nodo del evento actual
                    if (node.getNodeType() == Node.ELEMENT_NODE) { // Verifica si el nodo es un elemento
                        Element element = (Element) node; // Convierte el nodo a un elemento
                        // Extrae los valores de las etiquetas XML
                        String description = getTagValue("text", (Element) element.getElementsByTagName("description").item(0));
                        String magnitud = getTagValue("value", (Element) element.getElementsByTagName("magnitude").item(0));
                        String tiempo = getTagValue("value", (Element) element.getElementsByTagName("time").item(0));
                        String longitud = getTagValue("value", (Element) element.getElementsByTagName("longitude").item(0));
                        String latitud = getTagValue("value", (Element) element.getElementsByTagName("latitude").item(0));
                        String profundidad = getTagValue("value", (Element) element.getElementsByTagName("depth").item(0));
                        String fases = getTagValue("usedPhaseCount", (Element) element.getElementsByTagName("quality").item(0));
                        String errores = getTagValue("standardError", (Element) element.getElementsByTagName("quality").item(0));
                        String azimutal = getTagValue("azimuthalGap", (Element) element.getElementsByTagName("quality").item(0));
                        String distancia_minima = getTagValue("minimumDistance", (Element) element.getElementsByTagName("quality").item(0));
                        String incertidumbre = getTagValue("uncertainty", (Element) element.getElementsByTagName("depth").item(0));

                        // Añade los datos a la tabla
                        datostabla.addRow(new Object[]{description, magnitud, tiempo, longitud, latitud, profundidad, fases, errores, azimutal, distancia_minima, incertidumbre});
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace(); // Imprime la traza de la excepción en caso de error
        }
    }

    // Método para obtener el valor de una etiqueta XML
    private String getTagValue(String tag, Element element) {
        if (element == null) { // Verifica si el elemento es nulo
            return "N/A"; // Devuelve "N/A" si el elemento es nulo
        }
        NodeList nodeList = element.getElementsByTagName(tag).item(0).getChildNodes(); // Obtiene la lista de nodos hijos de la etiqueta especificada
        Node node = (Node) nodeList.item(0); // Obtiene el primer nodo hijo
        return node != null ? node.getNodeValue() : "N/A"; // Devuelve el valor del nodo o "N/A" si el nodo es nulo
    }

    // Método principal para ejecutar la aplicación
    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new ConsultaTerremotos().setVisible(true); // Crea y muestra la ventana principal
            }
        });
    }
}
