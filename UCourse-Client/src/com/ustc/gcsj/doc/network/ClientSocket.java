package com.ustc.gcsj.doc.network;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.List;

import com.ustc.gcsj.doc.CONSTANT;

public class ClientSocket {

    private String ip = CONSTANT.IP;

    private int port = CONSTANT.PORT;

    private Socket socket = null;

    public boolean isConnected = true;

    ObjectOutputStream output = null;
    ObjectInputStream input = null;

    /**
     * 创建socket连接
     */
    public ClientSocket() {
        try {
            socket = new Socket(ip, port);
            output = new ObjectOutputStream(socket.getOutputStream());
            input = new ObjectInputStream(socket.getInputStream());
        } catch (Exception e) {
            e.printStackTrace();
            isConnected = false;
        }
    }

    public boolean sendFile(String filePath, String course) {
        // 获取本地文件
        File file = new File(filePath);
        DataInputStream fis = null;
        DataOutputStream dos = null;
        try {
            fis = new DataInputStream(new FileInputStream(file));
            dos = new DataOutputStream(output);
        } catch (FileNotFoundException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
            return false;
        }

        try {
            dos.writeUTF("sendFile");
            dos.writeUTF(file.getName());
            dos.writeUTF(course);
            dos.flush();

            int len = 0;
            byte[] buf = new byte[10240];
            while (true) {
                int read = fis.read(buf);
                if (read == -1)
                    break;
                len += read;
                dos.write(buf, 0, read);
                dos.flush();
            }
            fis.close();
            System.out.println("ClientSocket --> send: " + len);
            dos.close();
            return true;
        } catch (IOException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
            return false;
        }

    }

    /**
     * 接收服务器传过来的文件
     */
    public boolean getFile(String filePath) {
        DataInputStream dis = null;
        DataOutputStream dos = null;
        try {
            dis = new DataInputStream(input);
            dos = new DataOutputStream(output);
        } catch (Exception e) {
            System.out.print("接收文件出错！");
            return false;
        }

        try {
            dos.writeUTF("getFile");
            dos.writeUTF(filePath);
            dos.flush();

            // 文件存储路径
            String savePath = CONSTANT.SAVE_PATH + "/"
                    + filePath.substring(filePath.lastIndexOf("\\") + 1);
            System.out.println("ClientSocket --> " + savePath);

            byte[] buf = new byte[1024];

            DataOutputStream fos = new DataOutputStream(new FileOutputStream(
                    savePath));

            long len = 0;
            while (true) {
                int read = dis.read(buf);
                if (read == -1)
                    break;
                len += read;
                fos.write(buf, 0, read);
                fos.flush();
            }
            System.out.println("ClientSocket -->read: " + len);
            fos.close();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("出错了：" + e.toString());
            return false;
        }
    }

    @SuppressWarnings("rawtypes")
    public List getList() {
        List list = null;
        try {
            if (output != null) {
                output.writeUTF("getList");
                output.flush();
            }
            System.out.println("ClientSocket --> getList");
            try {
                list = (List) input.readObject();
            } catch (ClassNotFoundException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return list;
    }
}
