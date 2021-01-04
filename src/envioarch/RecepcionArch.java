package envioarch;
//189.216.151.20

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class RecepcionArch {

  public static void main(String[] args) {
    try {
      int puerto = 8000;
      //String ip = "localhost";
      ServerSocket ss = new ServerSocket(puerto);
      String carpeta = "./archivos_recibidos";
      File directorio = new File(carpeta);
      if (!directorio.exists()) {
        if (directorio.mkdirs()) {
          System.out.println("Directorio creado");
        } else {
          carpeta = "";
          System.out.println("Error al crear directorio");
        }
      }
      while (true) {
        Socket socket = ss.accept();
        DataInputStream dis = new DataInputStream(socket.getInputStream());
        int numero_archivos = dis.readInt();
        
        int tam_buffer = dis.readInt();
        byte[] b = new byte[tam_buffer];

        for (int i = 0; i < numero_archivos; i++) {
          String nombre_archivo = dis.readUTF();
          DataOutputStream dos = new DataOutputStream(new FileOutputStream(carpeta + "/" + nombre_archivo));
          long tam_archivo = dis.readLong();
          long recibidos = 0;
          int n, porcentaje;

          while (recibidos < tam_archivo) {
            n = dis.read(b);
            dos.write(b, 0, n);
            dos.flush();
            recibidos = recibidos + n;
            porcentaje = (int) (recibidos * 100 / tam_archivo);
            System.out.print("Recibido: " + porcentaje + "%\r");
            if(recibidos == tam_archivo){
              System.out.println("\nArchivo " + nombre_archivo + " recibido.");              
            }
          }//While
          dos.close();
          
        }
        
        dis.close();
        socket.close();
        
      }

    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
