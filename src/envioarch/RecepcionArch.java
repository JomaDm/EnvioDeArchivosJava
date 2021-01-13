package envioarch;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class RecepcionArch {

  public static void main(String[] args) {
    try {
      int puerto = 5160;
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

        int tam_buffer = dis.readInt();
        System.out.println("Tamaño del buffer: " + tam_buffer);
        long tam_archivo = dis.readLong();
        System.out.println("Tamaño del archivo: " + tam_archivo);
        String nombre_archivo = dis.readUTF();
        //System.out.println("Nombre del archivo: " + nombre_archivo);

        byte[] b = new byte[tam_buffer];
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
        System.out.println("Archivo " + nombre_archivo + " Recibido");
        dos.close();
        dis.close();

        descomprimirArchivos(new File("temp.zip"), carpeta);
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public static void descomprimirArchivos(File zip, String carpeta) {
    String directorioZip = carpeta + "/";
    try {
      ZipInputStream zis = new ZipInputStream(new FileInputStream(directorioZip + zip.getName()));
      ZipEntry salida;
      while (null != (salida = zis.getNextEntry())) {
        System.out.println("Nombre del Archivo: " + salida.getName());
        FileOutputStream fos = new FileOutputStream(directorioZip + salida.getName());
        int leer;
        byte[] buffer = new byte[65536];
        while (0 < (leer = zis.read(buffer))) {
          fos.write(buffer, 0, leer);
        }
        fos.close();
        zis.closeEntry();
      }     
      zis.close(); 
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      File z  = new File(directorioZip + zip.getName());
      z.delete();
    }
  }
}
