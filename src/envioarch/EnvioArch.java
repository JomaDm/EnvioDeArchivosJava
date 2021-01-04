package envioarch;

import java.awt.GridLayout;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.net.Socket;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class EnvioArch {
    //public static String host_default = "201.124.122.167";
    public static String host_default = "127.0.0.1";
    public static int puerto_default = 8000; 
    
    public static void main(String[] args) {
        try {
            String host = host_default;
            int pto = puerto_default;
            
            String datos[] = obtenerDatos();
            int numBytes = Integer.parseInt(datos[0]);
            String nagle = datos[1];
            System.out.println("Bytes: "+numBytes);
            System.out.println("Nagle: "+nagle);
            Socket cl = new Socket(host, pto);
            

            JFileChooser jf = new JFileChooser();
            jf.setMultiSelectionEnabled(true);
            int r = jf.showOpenDialog(null);
            File[] files = jf.getSelectedFiles();
                        
            if (r == JFileChooser.APPROVE_OPTION) {
                int numArchivos = files.length;
                System.out.println("Numero de Archivos: "+numArchivos);   
                DataOutputStream dos = new DataOutputStream(cl.getOutputStream());
                //mandamos un mensaje con parametros
                dos.writeInt(numArchivos);
                dos.flush();
                dos.writeInt(numBytes);
                dos.flush();
                    
                //mandamos los archivos
                for(int i=0;i<files.length;i++){                 
                    String archivo = files[i].getAbsolutePath(); //Dirección
                    String nombre = files[i].getName().toString(); //Nombre
                    long tam = files[i].length();  //Tamaño
   
                    System.out.println("Name: "+nombre);
                    System.out.println("Path: "+archivo);
                    
                    dos.writeUTF(nombre);
                    dos.flush();
                    dos.writeLong(tam);
                    dos.flush();
                    
                    DataInputStream dis = new DataInputStream(new FileInputStream(archivo));
                    byte[] b = new byte[numBytes];
                    long enviados = 0;
                    int porcentaje, n;
                    while (enviados < tam) {
                        n = dis.read(b);
                        dos.write(b, 0, n);
                        dos.flush();
                        enviados = enviados + n;
                        porcentaje = (int) (enviados * 100 / tam);
                        System.out.print("Enviado: " + porcentaje + "%\r");
                    }
                    System.out.print("\nArchivo #"+(i+1)+" enviado\n\n");
                    dis.close();                    
                    //dos.flush();
                }
                dos.close();
                cl.close();    
            }
        } catch (Exception e) {
          e.printStackTrace();
        }
    }
    
    private static String[] obtenerDatos() {
        String s[]=new String[2];

        JTextField bytes = new JTextField(20);
        JCheckBox check = new JCheckBox("Nagle",true);
        bytes.setText("1024");
        
        JPanel myPanel = new JPanel();
        myPanel.setLayout(new GridLayout(2, 2));
        myPanel.add(new JLabel("Bytes en buffer:"));
        myPanel.add(bytes);
        myPanel.add(new JLabel("¿Usar Nagle?:"));
        myPanel.add(check);
        
        int result = JOptionPane.showConfirmDialog(null, myPanel, 
                 "Configuración de comunicación", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
                s[0] = bytes.getText();
                if(check.isSelected()) s[1] = "SI";
                else s[1] = "NO";
        }else System.exit(0);
        return s;
    }    
}