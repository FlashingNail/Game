package com.example.a22493.game;

import android.widget.TextView;

import java.io.BufferedReader;

import java.io.IOException;

import java.io.InputStream;

import java.io.InputStreamReader;

import java.io.OutputStream;

import java.net.ServerSocket;

import java.net.Socket;

import java.util.ArrayList;

public class Server
{
    // 定义保存所有Socket的ArrayList
    public static ArrayList<Socket> socketList
            = new ArrayList<Socket>();
    public static void startServer()
            throws IOException
    {
        ServerSocket ss = new ServerSocket(6000);

        while(true)
        {
            // 此行代码会阻塞，将一直等待别人的连接
            Socket s = ss.accept();
            socketList.add(s);
            // 每当客户端连接后启动一条ServerThread线程为该客户端服务
            new Thread(new ServerThread(s)).start();
        }
    }

    static class ServerThread implements Runnable
    {
        // 定义当前线程所处理的Socket
        Socket s = null;
        // 该线程所处理的Socket所对应的输入流
        BufferedReader br = null;
        public ServerThread(Socket s)
                throws IOException
        {
            this.s = s;
            // 初始化该Socket对应的输入流
            br = new BufferedReader(new InputStreamReader(
                    s.getInputStream() , "utf-8"));
        }
        public void run()
        {
            try
            {
                String content = null;
                // 采用循环不断从Socket中读取客户端发送过来的数据
                while ((content = readFromClient()) != null)
                {
                    // 遍历socketList中的每个Socket，
                    // 将读到的内容向每个Socket发送一次
                    for (Socket s : Server.socketList)
                    {
                        OutputStream os = s.getOutputStream();
                        os.write((content + "\n").getBytes("utf-8"));
                    }
                }
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
        // 定义读取客户端数据的方法
        private String readFromClient()
        {
            try
            {
                return br.readLine();
            }
            // 如果捕捉到异常，表明该Socket对应的客户端已经关闭
            catch (IOException e)
            {
                // 删除该Socket。
                Server.socketList.remove(s);    //①
            }
            return null;
        }
    }
}
