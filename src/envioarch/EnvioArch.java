package envioarch;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.net.Socket;
import javax.swing.JFileChooser;

public class EnvioArch {
    public static void main(String[] args) {
        try {
            //String host = "201.124.122.167";
            String host = "localhost";
            int pto = 8000;
            Socket cl = new Socket(host, pto);
            DataOutputStream dos = new DataOutputStream(cl.getOutputStream());

            JFileChooser jf = new JFileChooser();
            jf.setMultiSelectionEnabled(true);
            int r = jf.showOpenDialog(null);
            File[] files = jf.getSelectedFiles();
                        
            if (r == JFileChooser.APPROVE_OPTION) {
                int numArchivos = files.length;
                int numBytes = 1024;
                System.out.println("Numero de Archivos: "+numArchivos);   
                
                //mandamos un mensaje con parametros
                dos.writeInt(numArchivos);
                dos.flush();
                dos.writeInt(numBytes);
                dos.flush();
                    
                //mandamos los archivos
                for(int i=0;i<files.length;i++){                 
                    String archivo = files[i].getAbsolutePath(); //Dirección
                    String nombre = files[i].getName(); //Nombre
                    long tam = files[i].length();  //Tamaño
   
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
                    System.out.print("\n\nArchivo #"+(i+1)+" enviado\n");
                    dis.close();
                }
                dos.close();
                cl.close();    
            }
        } catch (Exception e) {
          e.printStackTrace();
        }
    } 
}