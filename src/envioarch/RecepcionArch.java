package envioarch;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.net.Socket;

public class RecepcionArch {

  public static void main(String[] args) {
    try {
      int puerto = 8000;
      String ip = "189.216.151.20";
      String carpeta = "/archivos_recibidos";
      File directorio = new File(carpeta);
      if (!directorio.exists()) {
        if (directorio.mkdirs()) {
          System.out.println("Directorio creado");
        } else {
          carpeta = "";
          System.out.println("Error al crear directorio");
        }
      }
      Socket socket = new Socket(ip, puerto);
      DataInputStream dis = new DataInputStream(socket.getInputStream());

      int tam_buffer = dis.readInt();
      byte[] b = new byte[tam_buffer];

      int numero_archivos = dis.readInt();

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
        }//Whil
        System.out.print("\nArchivo " + nombre_archivo + " recibido.");
      }

    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
