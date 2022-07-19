import javax.sound.sampled.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class AudioRecorder extends Thread {
    static TargetDataLine mic;
    String audioName;

    public AudioRecorder(String audioName) {
        this.audioName = audioName;
    }

    @Override
    public  void run() {
        initRecording();
        statRecording();
    }

    private void initRecording() {
        MyUtil.colorPrint("开始录音......\n", "purple");
        try {
            AudioFormat audioFormat = new AudioFormat(
                    8000, 16, 1, true, false);
            DataLine.Info info = new DataLine.Info(TargetDataLine.class, audioFormat);
            mic = (TargetDataLine) AudioSystem.getLine(info);
            mic.open();
            MyUtil.colorPrint("录音中......\n", "purple");
        } catch (LineUnavailableException e) {
            e.printStackTrace();
        }
    }

    private void statRecording() {
        try {
            mic.start();
            AudioInputStream audioInputStream = new AudioInputStream(mic);
            File f = new File(audioName);
            AudioSystem.write(audioInputStream, AudioFileFormat.Type.WAVE, f);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public void stopRecording() {
        mic.stop();
        mic.close();
        MyUtil.colorPrint("录音结束\n", "purple");
    }

    public static void play(String file){
        try {
            FileInputStream fis = new FileInputStream(file);
            AudioFormat audioFormat = new AudioFormat(
                    8000, 16, 1, true, false);
            SourceDataLine srcLine;
            DataLine.Info info = new DataLine.Info(SourceDataLine.class, audioFormat);

            try {
                srcLine = (SourceDataLine) AudioSystem.getLine(info);
                srcLine.open(audioFormat);
            } catch (Exception e) {
                e.printStackTrace();
                return;
            }
            srcLine.start();
            byte[] b = new byte[256];
            try {
                while(fis.read(b) > 0) {
                    srcLine.write(b, 0, b.length);
                }
                srcLine.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
