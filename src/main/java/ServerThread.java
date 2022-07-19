import com.alibaba.fastjson.JSON;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class ServerThread implements Runnable {
    int uid = -1;
    Socket client;
    InputStream is;
    OutputStream os;
    BufferedReader bRead;
    BufferedWriter bWrite;

    String userTmp;
    String fileUrl;
    String fileName;
    int status;
    int bufSize;

    public ServerThread(Socket client) {
        this.client = client;
    }

    @Override
    public void run() {
        processSocket();
    }

    private void processSocket() {
        try {
            is = client.getInputStream();
            os = client.getOutputStream();
            bRead = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
            bWrite = new BufferedWriter(new OutputStreamWriter(os));

            String str;
            while (true) {
                if (status == 0) {
                    str = bRead.readLine();
                    if (str == null) {
                        break;
                    }
                    String[] tmp = str.split(" ");
                    if (tmp[0].equals("1")) {                           // 注册
                        int ys = 0;
                        for (User user : Server.userList) {
                            if (tmp[1].equals(user.getName())) {
                                ys = 1;
                                break;
                            }
                        }
                        if (ys == 1) {
                            sendMe("1 0 用户名已注册");
                        } else {
                            int size = Server.userList.size();
                            User user = new User(size, tmp[1], tmp[2], false);
                            Server.userList.add(user);
                            String jsonStr = JSON.toJSONString(Server.userList);
                            MyUtil.write(Server.file, jsonStr, "UTF-8");
                            sendMe("1 1 注册成功");
                        }
                    } else if (tmp[0].equals("2")) {                    // 登录
                        int ys = -1;
                        int size = Server.userList.size();
                        for (int i = 0; i < size; i++) {
                            if (tmp[1].equals(Server.userList.get(i).getName())
                                    && tmp[2].equals(Server.userList.get(i).getPwd())) {
                                ys = i;
                                break;
                            }
                        }
                        if (ys == -1) {
                            sendMe("2 0 用户名或密码错误");
                        } else {
                            if (Server.userList.get(ys).isActive()) {
                                sendMe("2 1 该用户已在其他设备登录");
                            } else {
                                Server.userList.get(ys).setActive(true);
                                Server.clientMap.put(Server.userList.get(ys).getName(), this);
                                uid = ys;
                                sendMe("2 2 登录成功 " + ys);
                                System.out.println("用户 `" + Server.userList.get(ys).getName() + "` 已上线");
                            }
                        }
                    } else if (tmp[0].equals("3")) {                    // 退出
                        if (uid != -1) {
                            Server.userList.get(uid).setActive(false);
                            Server.clientMap.remove(Server.userList.get(uid).getName());
                            System.out.println("用户 `" + Server.userList.get(uid).getName() + "` 已下线");
                        }
                        int size = Server.socketList.size();
                        for (int i = 0; i < size ; i++) {
                            Socket client1 = Server.socketList.get(i);
                            Socket client2 = client;
                            if (client1.getRemoteSocketAddress().equals(client2.getRemoteSocketAddress())) {
                                Server.socketList.remove(i);
                                break;
                            }
                        }
                        System.out.println("连接 `" + client.getRemoteSocketAddress() + "` 已断开");
                    } else if (tmp[0].equals("4")) {                    // 查看在线用户
                        int cnt = 0;
                        for (User user : Server.userList) {
                            if (user.isActive()) {
                                cnt++;
                                sendMe("4 " + user);
                            }
                        }
                        sendMe(String.format("4 当前在线人数: %d", cnt));
                    } else if (tmp[0].equals("5")) {                    // 私聊, 发送消息, 选择发送对象
                        int ys = searchUser(tmp[1]);
                        if (ys == -1) {
                            sendMe("5 0 这个用户不存在哦");
                        } else {
                            if (!Server.userList.get(ys).isActive()) {
                                sendMe("5 1 这个用户不在线哦, 先找别人吧");
                            } else {
                                userTmp = tmp[1];
                                sendMe("5 2");
                            }
                        }
                    } else if (tmp[0].equals("55")) {                   // 私聊, 发送消息
                        ServerThread st = Server.clientMap.get(userTmp);
                        st.sendMe("55 " + Server.userList.get(uid).getName() + "说: " + str.substring(3));
                    } else if (tmp[0].equals("10")) {                   // 私聊, 发送文件, 选择发送对象
                        int ys = searchUser(tmp[1]);
                        if (ys == -1) {
                            sendMe("10 0 这个用户不存在哦");
                        } else {
                            if (!Server.userList.get(ys).isActive()) {
                                sendMe("10 1 这个用户不在线哦, 先找别人吧");
                            } else {
                                userTmp = tmp[1];
                                sendMe("10 2");
                            }
                        }
                    } else if (tmp[0].equals("1001")) {                 // 私聊, 发送文件, 通知发送方发送, 接受方接受
                        bufSize = Integer.parseInt(tmp[1]);
                        fileName = tmp[2];
                        status = 1001;
                        sendMe("1001 0");  // 发送文件
                        ServerThread st = Server.clientMap.get(userTmp);
                        st.sendMe("1001 1");  // 接受文件
                    } else if (tmp[0].equals("1002")) {                 // 私聊, 发送文件, 接收方做好接受准备
                        fileUrl = tmp[1];
                        sendMe("1002 0 准备接收文件......");
                        status = 1002;
                    } else if (tmp[0].equals("11")) {                   // 私聊, 发送语音, 选择发送对象
                        int ys = searchUser(tmp[1]);
                        if (ys == -1) {
                            sendMe("11 0 这个用户不存在哦");
                        } else {
                            if (!Server.userList.get(ys).isActive()) {
                                sendMe("11 1 这个用户不在线哦, 先找别人吧");
                            } else {
                                userTmp = tmp[1];
                                sendMe("11 2");
                            }
                        }
                    } else if (tmp[0].equals("1101")) {                 // 私聊, 发送语音, 通知发送方发送, 接受方接受
                        bufSize = Integer.parseInt(tmp[1]);
                        fileName = tmp[2];
                        status = 1101;
                        sendMe("1101 0");  // 发送语音
                        ServerThread st = Server.clientMap.get(userTmp);
                        st.sendMe("1101 1");  // 接受语音
                    } else if (tmp[0].equals("1102")) {                 // 私聊, 发送语音, 接收方做好接收准备
                        sendMe("1102 0 准备接收语音......");
                        status = 1102;
                    }
                }
                else if (status == 1001) {                            // 私聊, 发送文件, 接受文件
                    ServerThread st = Server.clientMap.get(userTmp);
                    if (st.status == 1002) {
                        FileOutputStream fos = new FileOutputStream(st.fileUrl + "/" + fileName);
                        byte[] myByteArray = new byte[bufSize];
                        int bytesRead = is.read(myByteArray, 0, myByteArray.length);
                        MyUtil.colorPrint(String.format("服务器正在传输文件, 文件大小为 %d 字节......\n",
                                bytesRead), "purple");
                        fos.write(myByteArray, 0, bytesRead);
                        fos.write("\r\n".getBytes(StandardCharsets.UTF_8));
                        fos.flush();
                        fos.close();
                        sendMe("1001 2 发送完毕");
                        st.sendMe(String.format("1002 1 接收完毕, 文件已被保存到 %s/%s", st.fileUrl, fileName));
                        MyUtil.colorPrint("服务器传输文件完毕\n", "purple");
                        status = 0;
                        st.status = 0;
                    }
                } else if (status == 1101) {
                    ServerThread st = Server.clientMap.get(userTmp);
                    if (st.status == 1102) {
                        FileOutputStream fos = new FileOutputStream("dst/" + fileName);
                        byte[] myByteArray = new byte[bufSize];
                        int bytesRead = is.read(myByteArray, 0, myByteArray.length);
                        MyUtil.colorPrint("服务器正在转发语音......\n", "purple");
                        fos.write(myByteArray, 0, bytesRead);
                        fos.write("\r\n".getBytes(StandardCharsets.UTF_8));
                        fos.flush();
                        fos.close();
                        sendMe("1101 2 发送完毕");
                        st.sendMe("1102 1 接收完毕, 开始播放......");
                        MyUtil.colorPrint("服务器转发语音完毕\n", "purple");
                        status = 0;
                        st.status = 0;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sendMe(String str) {
        try {
            str += "\r\n";
            bWrite.write(str);
            bWrite.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public int searchUser(String name) {
        int ys = -1;
        int size = Server.userList.size();
        for (int i = 0; i < size; i++) {
            if (name.equals(Server.userList.get(i).getName())) {
                ys = i;
                break;
            }
        }
        return ys;
    }
}