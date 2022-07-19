import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

public class MyUtil {

    public static String read(File file, String charset) {
        try (FileInputStream fstream = new FileInputStream(file)) {
            int fileSize = (int) file.length();
            if (fileSize > 1024 * 512)
                throw new Exception("File too large to read! size=" + fileSize);

            byte[] buffer = new byte[fileSize];
            int n = fstream.read(buffer);

            if (fileSize != 0) {
                int off = 0;
                int len = n;
                if (buffer[0] == (byte) 0xef && buffer[1] == (byte) 0xbb && buffer[2] == (byte) 0xbf) {
                    off += 3;
                    len -= 3;
                }
                return new String(buffer, off, len, charset);
            }
            return null;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void write(File file, String text, String charset)
    {
        try (FileOutputStream fstream = new FileOutputStream(file)) {
            fstream.write(text.getBytes(charset));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    static public void colorPrint(String str, String color) {
        switch (color) {
            case "red": System.out.print("\033[" + 31 + ";1m" + str + "\033[0m"); break;
            case "green": System.out.print("\033[" + 32 + ";1m" + str + "\033[0m"); break;
            case "yellow": System.out.print("\033[" + 33 + ";1m" + str + "\033[0m"); break;
            case "blue": System.out.print("\033[" + 34 + ";1m" + str + "\033[0m"); break;
            case "purple": System.out.print("\033[" + 35 + ";1m" + str + "\033[0m"); break;
            case "cyan": System.out.print("\033[" + 36 + ";1m" + str + "\033[0m"); break;
            default: System.out.print(str); break;
        }
    }
}
