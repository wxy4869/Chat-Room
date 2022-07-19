import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class Server {
    static ServerSocket server;
    static File file = new File("RegisterInfo.json");
    static ArrayList<User> userList;                                    // 注册用户
    static ArrayList<Socket> socketList = new ArrayList<>();            // 连接客户端
    static Map<String, ServerThread> clientMap = new HashMap<>();       // 用户和客户端的映射
    static ArrayList<String> userTmpList = new ArrayList<>();           // 服务器聊天对象
    static int status = 0;                                              // 操作功能

    public static void main(String[] args) {
        setupServer();
    }

    public static void setupServer() {
        try {
            server = new ServerSocket(8888);
            System.out.println("服务器创建成功, 欢迎来到 wxyChat 聊天室");
            initRegisterInfo();
            helpServer(status);
            myServerReader m = new myServerReader();
            new Thread(m).start();
            while (true) {
                Socket client = server.accept();
                socketList.add(client);
                System.out.println("进入了一个客户机连接" + client.getRemoteSocketAddress());
                ServerThread t = new ServerThread(client);
                new Thread(t).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static void initRegisterInfo() {
        if (!file.exists()) {
            try {
                if (file.createNewFile()) {
                    MyUtil.write(file, "", "UTF-8");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        String fileContent =  MyUtil.read(file, "UTF-8");
        userList = (ArrayList<User>) JSONObject.parseArray(fileContent, User.class);
        if (userList == null) {
            userList = new ArrayList<>();
        }
    }

    public static void helpServer(int x) {
        if (x == 0) {
            String re =
                    "-------------------------------------------\n" +
                    "3. 退出\n" +
                    "4. 查看在线用户\n" +
                    "6. 服务器广播通知\n" +
                    "7. 服务器群聊用户\n" +
                    "8. 服务器私聊用户\n" +
                    "9. 查看当前连接客户端信息\n" +
                    "-------------------------------------------\n";
            MyUtil.colorPrint(re, "cyan");
        } else if (x == 6) {
            System.out.println("请输入想要广播的通知");
        } else if (x == 7) {
            System.out.println("请输入想要群聊的用户名, 用空格分隔");
        } else if (x == 8) {
            System.out.println("请输入想要私聊的用户名");
        } else if (x == 77 || x == 88) {
            System.out.println("请输入您要发送的消息");
        } else {
            System.out.println("您输入的命令不合法哦");
            helpServer(0);
        }
    }

    public static void onlineUserList() {
        int cnt = 0;
        for (User user : userList) {
            if (user.isActive()) {
                cnt++;
                MyUtil.colorPrint(user + "\n", "purple");
            }
        }
        MyUtil.colorPrint(String.format("当前在线人数: %d\n", cnt), "purple");
    }

    public static void socketList() {
        int size = socketList.size();
        for (Socket s : socketList) {
            MyUtil.colorPrint(String.format("客户端连接: %s\n", s.getRemoteSocketAddress()), "purple");
        }
        MyUtil.colorPrint(String.format("当前连接客户端数量: %d\n", size), "purple");
    }

    public static void close() {
        int size = socketList.size();
        if (size != 0) {
            System.out.println("还有客户端连接未断开");
            helpServer(0);
        } else {
            String jsonStr = JSON.toJSONString(Server.userList);
            MyUtil.write(Server.file, jsonStr, "UTF-8");
            System.exit(0);
        }
    }

    static class myServerReader implements Runnable {
        BufferedReader bRead = new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8));

        @Override
        public void run() {
            try {
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
                            case 3: close(); break;
                            case 4: onlineUserList(); break;
                            case 6: case 7: case 8:
                                helpServer(tmp);
                                status = tmp;
                                break;
                            case 9: socketList(); break;
                            default: helpServer(tmp); break;
                        }
                    } else if (status == 6) {                           // 广播
                        str = "[系统通知]" + str;
                        for (ServerThread st : clientMap.values()) {
                            st.sendMe(str);
                        }
                        System.out.println("广播结束");
                        status = 0;
                        helpServer(status);
                    } else if (status == 7) {                           // 群聊, 选择发送对象
                        String [] tmp = str.split(" ");
                        int ys = 1;
                        for (String s : tmp) {
                            int ysTmp = 0;
                            for (User user : userList) {
                                if (s.equals(user.getName())) {
                                    ysTmp = 1;
                                    break;
                                }
                            }
                            if (ysTmp == 0) {
                                ys = 0;
                                break;
                            }
                        }
                        if (ys == 0) {
                            System.out.print("有些用户不存在哦, ");
                            helpServer(status);
                        } else {
                            userTmpList.addAll(Arrays.asList(tmp).subList(0, tmp.length));
                            status = 77;
                            helpServer(status);
                        }
                    } else if (status == 8) {                           // 私聊, 选择发送对象
                        String [] tmp = str.split(" ");
                        if (tmp.length != 1) {
                            System.out.print("不能输入多个用户名哦, ");
                            helpServer(status);
                        } else {
                            int ys = 0;
                            for (User user : userList) {
                                if (tmp[0].equals(user.getName())) {
                                    ys = 1;
                                    break;
                                }
                            }
                            if (ys == 0) {
                                System.out.print("这个用户不存在哦, ");
                                helpServer(status);
                            } else {
                                userTmpList.add(tmp[0]);
                                status = 88;
                                helpServer(status);
                            }
                        }
                    } else if (status == 77) {                          // 群聊, 发送消息
                        str = "系统群发消息: " + str;
                        for (String user : userTmpList) {
                            ServerThread st = clientMap.get(user);
                            st.sendMe(str);
                        }
                        System.out.println("消息发送完毕");
                        userTmpList.clear();
                        status = 0;
                        helpServer(status);
                    } else if (status == 88) {                          // 私聊, 发送消息
                        str = "系统悄悄对您说: " + str;
                        ServerThread st = clientMap.get(userTmpList.get(0));
                        st.sendMe(str);
                        System.out.println("消息发送完毕");
                        userTmpList.clear();
                        status = 0;
                        helpServer(status);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}