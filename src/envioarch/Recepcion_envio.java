package envioarch;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class Recepcion_envio {
    //recibir
    public static int puerto_recibir = 8000;
    //enviar
    public static int puerto_enviar  = 5160;
    public static String host_enviar = "localhost";
    //public static String host_enviar = "127.0.0.1";
    
    public static void main(String[] args) throws IOException {
        try {
            int puerto = puerto_recibir;
            ServerSocket ss = new ServerSocket(puerto);
            String carpeta = "./archivos_recibidos_medio";
            File directorio = new File(carpeta);
            if (!directorio.exists()) {
              if (directorio.mkdirs()) {
                System.out.println("Directorio creado");
              } else {
                carpeta = "";
                System.out.println("Error al crear directorio");
              }
            }
            byte[] b;
            while (true) {
                //Codigo para recibir el archivo
                System.out.println("Esperando a recibir archivos...");
                Socket socket = ss.accept();
                System.out.println("Conexión establecida desde" + socket.getInetAddress() + ":" + socket.getPort());
                DataInputStream dis = new DataInputStream(socket.getInputStream());

                int tam_buffer = dis.readInt();
                System.out.println("Tamaño del buffer: " + tam_buffer);
                long tam_archivo = dis.readLong();
                System.out.println("Tamaño del archivo: " + tam_archivo);
                String nombre_archivo = dis.readUTF();
                System.out.println("Nombre del archivo: " + nombre_archivo);

                b = new byte[tam_buffer];
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
                } 
                System.out.println("Archivo " + nombre_archivo + " Recibido");

                //Código para enviar el archivo     
                String ruta_zip_enviar = carpeta+"/"+nombre_archivo;
                File f = new File(ruta_zip_enviar);
                String ruta_archivo = f.getAbsolutePath(); //Dirección
                
                Socket socket2 = new Socket(host_enviar, puerto_enviar);
                DataOutputStream dos2 = new DataOutputStream(socket2.getOutputStream());
                DataInputStream dis2 = new DataInputStream(new FileInputStream(ruta_archivo)); 

                dos2.writeInt(tam_buffer); //No bytes
                dos2.flush();       
                dos2.writeLong(tam_archivo); //tamaño zip
                dos2.flush();
                dos2.writeUTF(nombre_archivo); //Nombre
                dos2.flush();
                
                b = new byte[tam_buffer];
                long enviados = 0;
                int porcentaje2, n2;
                while (enviados < tam_archivo) {
                  n2 = dis2.read(b);
                  dos2.write(b, 0, n2);
                  dos2.flush();
                  enviados = enviados + n2;
                  porcentaje2 = (int) (enviados * 100 / tam_archivo);
                  System.out.print("Enviado: " + porcentaje2 + "%\r");          
                }
                System.out.print("\nArchivo "+nombre_archivo+" reenviado\n\n");
                //Cerrar Streams
                dos.close();
                dis.close();
                dos2.close();
                dis2.close();
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