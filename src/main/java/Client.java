import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Client {
    static int uid = -1;
    static Socket client;
    static InputStream is;
    static OutputStream os;
    static int status = 0;
    static byte[] myByteArray;

    public static void main(String[] args) {
        setupClient();
    }

    public static void setupClient() {
        try {
            client = new Socket("localhost", 8888);
            System.out.println("客户端连接成功, 欢迎来到 wxyChat 聊天室");
            helpClient(status);
            is = client.getInputStream();
            os = client.getOutputStream();

            ExecutorService exec = Executors.newCachedThreadPool();
            exec.execute(new ListenerServer());

            BufferedReader bRead = new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8));
            BufferedWriter bWrite = new BufferedWriter(new OutputStreamWriter(os));
            while (true) {
                String str = bRead.readLine();
                if (status == 0) {                                  // 初始
                    int tmp;
                    try {
                        tmp = Integer.parseInt(str);
                    } catch (Exception e) {
                        tmp = -1;
                    }
                    switch (tmp) {
                        case 1:
                        case 2:
                            if (uid != -1) {
                                System.out.println("您已登录");
                                helpClient(status);
                            } else {
                                helpClient(tmp);
                                status = tmp;
                            }
                            break;
                        case 3:
                            close(bWrite);
                            break;
                        case 4:
                            if (uid == -1) {
                                System.out.println("您还没有登录哦");
                                helpClient(status);
                            } else {
                                bWrite.write("4\n");
                                bWrite.flush();
                            }
                            break;
                        case 5:
                        case 10:
                        case 11:
                            if (uid == -1) {
                                System.out.println("您还没有登录哦");
                                helpClient(status);
                            } else {
                                helpClient(tmp);
                                status = tmp;
                            }
                            break;
                        default:
                            helpClient(tmp);
                            break;
                    }
                } else if (status == 1) {                           // 注册
                    String[] tmp = str.split(" ");
                    if (tmp.length != 2) {
                        System.out.print("您的输入不合法哦, ");
                        helpClient(status);
                    } else {
                        bWrite.write("1 " + str + "\n");
                        bWrite.flush();
                    }
                } else if (status == 2) {                           // 登录
                    if (str.equals("q")) {
                        status = 0;
                        helpClient(status);
                    } else {
                        String[] tmp = str.split(" ");
                        if (tmp.length != 2) {
                            System.out.print("您的输入不合法哦, ");
                            helpClient(status);
                        } else {
                            bWrite.write("2 " + str + "\n");
                            bWrite.flush();
                        }
                    }
                } else if (status == 5) {                           // 私聊, 发送消息, 选择发送对象
                    if (str.equals("q")) {
                        status = 0;
                        helpClient(status);
                    } else {
                        String[] tmp = str.split(" ");
                        if (tmp.length != 1) {
                            System.out.print("不能输入多个用户名哦, ");
                            helpClient(status);
                        } else {
                            bWrite.write("5 " + str + "\n");
                            bWrite.flush();
                        }
                    }
                } else if (status == 55) {                          // 私聊, 发送消息
                    if (!str.equals("q")) {
                        bWrite.write("55 " + str + "\n");
                        bWrite.flush();
                    }
                    status = 0;
                    helpClient(status);
                } else if (status == 10) {                          // 私聊, 发送文件, 选择发送对象
                    if (str.equals("q")) {
                        status = 0;
                        helpClient(status);
                    } else {
                        String[] tmp = str.split(" ");
                        if (tmp.length != 1) {
                            System.out.print("不能输入多个用户名哦, ");
                            helpClient(status);
                        } else {
                            bWrite.write("10 " + str + "\n");
                            bWrite.flush();
                        }
                    }
                } else if (status == 1001) {                        // 私聊, 发送文件, 发送文件
                    File myFile = new File(str);
                    FileInputStream fis = new FileInputStream(myFile);
                    BufferedInputStream bis = new BufferedInputStream(fis);
                    int size = (int) myFile.length();
                    myByteArray = new byte[size];
                    bis.read(myByteArray, 0, myByteArray.length);
                    bWrite.write(String.format("1001 %d %s\n", size, myFile.getName()));
                    bWrite.flush();
                } else if (status == 1002) {                        // 私聊, 发送文件, 接受文件
                    bWrite.write("1002 " + str + "\n");
                    bWrite.flush();
                } else if (status == 11) {                          // 私聊, 发送语音, 选择发送对象
                    if (str.equals("q")) {
                        status = 0;
                        helpClient(status);
                    } else {
                        String[] tmp = str.split(" ");
                        if (tmp.length != 1) {
                            System.out.print("不能输入多个用户名哦, ");
                            helpClient(status);
                        } else {
                            bWrite.write("11 " + str + "\n");
                            bWrite.flush();
                        }
                    }
                } else if (status == 1101) {
                    String[] tmp = str.split(" ");
                    AudioRecorder audioRecorder = new AudioRecorder("test.wav");
                    if (tmp.length != 1 || (!tmp[0].equals("1") && !tmp[0].equals("2"))) {
                        System.out.print("您的输入不合法哦, ");
                        helpClient(status);
                    } else if (tmp[0].equals("1")) {
                        audioRecorder.start();
                    } else {
                        audioRecorder.stopRecording();
                        File myFile = new File("test.wav");
                        FileInputStream fis = new FileInputStream(myFile);
                        BufferedInputStream bis = new BufferedInputStream(fis);
                        int size = (int) myFile.length();
                        myByteArray = new byte[size];
                        bis.read(myByteArray, 0, myByteArray.length);
                        bWrite.write(String.format("1101 %d %s\n", size, myFile.getName()));
                        bWrite.flush();
                    }
                } else if (status == 1102) {
                    if (!str.equals("1")) {
                        System.out.println("您只能按 1 捏");
                    } else {
                        bWrite.write("1102 " + str + "\n");
                        bWrite.flush();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void helpClient(int x) {
        if (x == 0) {
            String re =
                    "-------------------------------------------\n" +
                    "1. 注册\n" +
                    "2. 登录\n" +
                    "3. 退出\n" +
                    "4. 查看在线用户\n" +
                    "5. 发起私聊\n" +
                    "10. 传输文件\n" +
                    "11. 发送语音\n" +
                    "-------------------------------------------\n";
            MyUtil.colorPrint(re, "cyan");
        } else if (x == 1 || x == 2) {
            System.out.println("请输入用户名和密码, 用空格分隔, 按 q 返回主页面");
        } else if (x == 5) {
            System.out.println("请输入想要私聊的用户名, 按 q 返回主页面");
        } else if (x == 55) {
            System.out.println("请输入您要发送的消息, 按 q 返回主页面");
        } else if (x == 10) {
            System.out.println("请输入想要传输文件的用户名, 按 q 返回主页面");
        } else if (x == 1001) {
            System.out.println("请输入想要传输的文件路径");
        } else if (x == 1002) {
            System.out.println("您收到一个文件, 请输入文件保存的路径");
        } else if (x == 11) {
            System.out.println("请输入想要发送语音的用户名, 按 q 返回主页面");
        } else if (x == 1101) {
            System.out.println("请按 1 开始录音, 随后按 2 结束录音");
        } else if (x == 1102) {
            System.out.println("您收到一条语音, 请按 1 准备接收");
        } else {
            System.out.println("您输入的命令不合法哦");
            helpClient(0);
        }
    }

    public static void close(BufferedWriter bWrite) {
        try {
            bWrite.write("3\n");
            bWrite.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("您已下线并退出程序");
        System.exit(0);
    }

    static class ListenerServer implements Runnable {
        @Override
        public void run() {
            try {
                BufferedReader bRead = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
                String str;
                while ((str = bRead.readLine()) != null) {
                    String[] tmp = str.split(" ");
                    if (tmp[0].equals("1")) {                       // 注册
                        if (tmp[1].equals("0")) {
                            System.out.print(tmp[2] + ", ");
                            helpClient(Integer.parseInt(tmp[0]));
                        } else if (tmp[1].equals("1")) {
                            System.out.println(tmp[2]);
                            status = 0;
                            helpClient(status);
                        }
                    } else if (tmp[0].equals("2")) {                // 登录
                        if (tmp[1].equals("0")) {
                            System.out.print(tmp[2] + ", ");
                            helpClient(Integer.parseInt(tmp[0]));
                        } else if (tmp[1].equals("1")) {
                            System.out.print(tmp[2] + ", ");
                            helpClient(Integer.parseInt(tmp[0]));
                        } else if (tmp[1].equals("2")) {
                            System.out.println(tmp[2]);
                            uid = Integer.parseInt(tmp[3]);
                            status = 0;
                            helpClient(status);
                        }
                    } else if (tmp[0].equals("4")) {                // 查看在线用户
                        MyUtil.colorPrint(str.substring(2) + "\n", "purple");
                    } else if (tmp[0].equals("5")) {                // 私聊, 发送消息, 选择发送对象
                        if (tmp[1].equals("0")) {
                            System.out.print(tmp[2] + ", ");
                            helpClient(Integer.parseInt(tmp[0]));
                        } else if (tmp[1].equals("1")) {
                            System.out.println(str.substring(4));
                            status = 0;
                            helpClient(status);
                        } else if (tmp[1].equals("2")) {
                            status = 55;
                            helpClient(status);
                        }
                    } else if (tmp[0].equals("55")) {               // 私聊, 发送消息
                        MyUtil.colorPrint(str.substring(3) + "\n", "yellow");
                        status = 0;
                    } else if (tmp[0].equals("10")) {               // 私聊, 发送文件, 选择发送对象
                        if (tmp[1].equals("0")) {
                            System.out.print(tmp[2] + ", ");
                            helpClient(Integer.parseInt(tmp[0]));
                        } else if (tmp[1].equals("1")) {
                            System.out.println(str.substring(5));
                            status = 0;
                            helpClient(status);
                        } else if (tmp[1].equals("2")) {
                            status = 1001;
                            helpClient(status);
                        }
                    } else if (tmp[0].equals("1001")) {             // 私聊, 发送文件, 发送文件
                        if (tmp[1].equals("0")) {
                            MyUtil.colorPrint("开始发送文件......\n", "purple");
                            os.write(myByteArray, 0, myByteArray.length);
                            os.flush();
                        } else if (tmp[1].equals("1")) {
                            status = 1002;
                            helpClient(status);
                        } else if (tmp[1].equals("2")) {
                            MyUtil.colorPrint(tmp[2] + "\n", "purple");
                            status = 0;
                            helpClient(status);
                        }
                    } else if (tmp[0].equals("1002")) {             // 私聊, 发送文件, 接受文件
                        if (tmp[1].equals("0")) {
                            MyUtil.colorPrint(tmp[2] + "\n", "purple");
                        } else if (tmp[1].equals("1")) {
                            MyUtil.colorPrint(str.substring(7) + "\n", "purple");
                            status = 0;
                            helpClient(status);
                        }
                    } else if (tmp[0].equals("11")) {               // 私聊, 发送语音, 选择发送对象
                        if (tmp[1].equals("0")) {
                            System.out.print(tmp[2] + ", ");
                            helpClient(Integer.parseInt(tmp[0]));
                        } else if (tmp[1].equals("1")) {
                            System.out.println(str.substring(5));
                            status = 0;
                            helpClient(status);
                        } else if (tmp[1].equals("2")) {
                            status = 1101;
                            helpClient(status);
                        }
                    } else if (tmp[0].equals("1101")) {
                        if (tmp[1].equals("0")) {
                            MyUtil.colorPrint("开始发送语音......\n", "purple");
                            os.write(myByteArray, 0, myByteArray.length);
                            os.flush();
                        } else if (tmp[1].equals("1")) {
                            status = 1102;
                            helpClient(status);
                        } else if (tmp[1].equals("2")) {
                            MyUtil.colorPrint(tmp[2] + "\n", "purple");
                            status = 0;
                            helpClient(status);
                        }
                    } else if (tmp[0].equals("1102")) {
                        if (tmp[1].equals("0")) {
                            MyUtil.colorPrint(tmp[2] + "\n", "purple");
                        } else if (tmp[1].equals("1")) {
                            MyUtil.colorPrint(str.substring(7) + "\n", "purple");
                            AudioRecorder.play("dst/test.wav");
                            MyUtil.colorPrint("播放完毕\n", "purple");
                            status = 0;
                            helpClient(status);
                        }
                    } else {
                        MyUtil.colorPrint(str + "\n", "yellow");
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
