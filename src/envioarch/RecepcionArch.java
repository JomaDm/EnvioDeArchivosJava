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
        System.out.println("Esperando a recibir archivos...");
        Socket socket = ss.accept();
        System.out.println("Conexión establecida desde" + socket.getInetAddress() + ":" + socket.getPort());
        DataInputStream dis = new DataInputStream(socket.getInputStream());

        int numero_archivos = dis.readInt();
        System.out.println("Numero de archivos: " + numero_archivos);
        int tam_buffer = dis.readInt();
        System.out.println("Tamaño del buffer: " + tam_buffer);

        byte[] b = new byte[tam_buffer];

        for (int i = 0; i < numero_archivos; i++) {
          String nombre_archivo = dis.readUTF();
          System.out.println("Nombre del archivo " + (i + 1) + ": " + nombre_archivo);

          long tam_archivo = dis.readLong();
          System.out.println("Tamaño del archivo: " + tam_archivo);
          long recibidos = 0;
          int n, porcentaje;
          DataOutputStream dos = new DataOutputStream(new FileOutputStream(carpeta + "/" + nombre_archivo));
          while (recibidos < tam_archivo) {
            n = dis.read(b);
            dos.write(b, 0, n);
            dos.flush();
            recibidos = recibidos + n;
            porcentaje = (int) (recibidos * 100 / tam_archivo);
            System.out.print("Recibido: " + porcentaje + "%\r");
          }//While
          //dis.read();
          System.out.println("Archivo " + nombre_archivo + " Recibido");
          dos.close();
        }

      }

    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
