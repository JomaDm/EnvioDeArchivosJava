package envioarch;

import java.awt.GridLayout;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class EnvioArch {
    //public static String host_default = "201.124.138.119";
    public static String host_default = "127.0.0.1";
    public static int puerto_default = 8005;

    public static void main(String[] args) {
    try {
      String host = host_default;
      int pto = puerto_default;

      String datos[] = obtenerDatos();
      int numBytes = Integer.parseInt(datos[0]);
      String nagle = datos[1];
      System.out.println("Bytes: " + numBytes);
      System.out.println("Nagle: " + nagle);
      Socket cl = new Socket(host, pto);
      if(nagle.equals("NO")) cl.setTcpNoDelay(true);
      
      JFileChooser jf = new JFileChooser();
      jf.setMultiSelectionEnabled(true);
      int r = jf.showOpenDialog(null);
      File[] files = jf.getSelectedFiles();

      if (r == JFileChooser.APPROVE_OPTION) {
        int numArchivos = files.length;
        System.out.println("Numero de Archivos: " + numArchivos);
        DataOutputStream dos = new DataOutputStream(cl.getOutputStream());

        File zip = comprimirArchivos(files);
        dos.writeInt(numBytes); //No bytes
        dos.flush();       
        long tam = zip.length();  //Tamaño
        dos.writeLong(tam);
        dos.flush();
        dos.writeUTF(zip.getName()); //Nombre
        dos.flush();
        
        DataInputStream dis = new DataInputStream(new FileInputStream(zip));
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
        System.out.print("\nArchivos enviado\n\n");
        dis.close();
        dos.close();
        cl.close();
        
        zip.deleteOnExit();
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private static File comprimirArchivos(File[] archivos) {
    String zipFile = archivos[0].getPath().replace(archivos[0].getName(), "") + "temp.zip";
    try {
        byte[] buffer = new byte[65536];
        FileOutputStream fos = new FileOutputStream(zipFile);
        ZipOutputStream zos = new ZipOutputStream(fos);

        for (int i = 0; i < archivos.length; i++) {
          //File srcFile = new File(srcFiles[i]);
          FileInputStream fis = new FileInputStream(archivos[i]);
          // begin writing a new ZIP entry, positions the stream to the start of the entry data
          zos.putNextEntry(new ZipEntry(archivos[i].getName()));
          int length;
          while ((length = fis.read(buffer)) > 0) {
            zos.write(buffer, 0, length);
          }
          zos.closeEntry();
          fis.close();
        }
        zos.close();

    } catch (IOException ioe) {
      System.out.println("Error creating zip file: " + ioe);
    }
    return new File(zipFile);
  }

  private static String[] obtenerDatos() {
    String s[] = new String[2];

    JTextField bytes = new JTextField(20);
    JCheckBox check = new JCheckBox("Nagle", true);
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
        if (check.isSelected())   s[1] = "SI";
        else                      s[1] = "NO";
    } else System.exit(0);
    return s;
  }
}