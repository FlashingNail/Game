package com.example.a22493.game;

import android.app.Activity;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketTimeoutException;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 定义界面上的控件
        final EditText input;
        final TextView show;
        Button openServer;
        Button send;
        final TextView ServerState;
        Handler handler;
        // 定义与服务器通信的子线程
        final ClientThread clientThread;

        input = (EditText) findViewById(R.id.input);
        show = (TextView) findViewById(R.id.show);
        openServer=(Button)findViewById(R.id.openServer);
        send = (Button) findViewById(R.id.send);
        ServerState=(TextView)findViewById(R.id.ServerState);
        handler = new Handler()
        {
            @Override
            public void handleMessage(Message msg)
            {
                if (msg.what == 0x123)
                {
                    // 若消息来自子线程，将读取的内容显示在文本框中
                    show.append("\n" + msg.obj.toString());
                }
            }
        };
        openServer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 当用户按下按钮后启动服务器
                try {
                ServerState.setText("服务器已开启");
                Server.startServer();
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        });
        clientThread = new ClientThread(handler);
        // 客户端启动ClientThread线程创建网络连接、读取来自服务器的数据
        new Thread(clientThread).start();
        send.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                try
                {
                    // 当用户按下发送按钮后，将用户输入的数据封装成Message，
                    // 并然后发送给子线程的Handler
                    Message msg = new Message();
                    msg.what = 0x345;
                    msg.obj = input.getText().toString();
                    clientThread.revHandler.sendMessage(msg);
                    // 清空input文本框
                    input.setText("");
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        });
    }

               //客户端线程

    class ClientThread implements Runnable
    {
        private Socket socket;
        // 定义向UI线程发送消息的Handler对象
        private Handler handler;
        // 定义接收UI线程的消息的Handler对象
        public Handler revHandler;
        // 该线程所处理的Socket所对应的输入流
        BufferedReader br = null;
        OutputStream os = null;

        public ClientThread(Handler handler)
        {
            this.handler = handler;
        }
        public void run()
        {
            try
            {
                socket = new Socket("192.168.31.23",6000);
                br = new BufferedReader(new InputStreamReader(
                        socket.getInputStream()));
                os = socket.getOutputStream();
                // 启动一条子线程来读取服务器响应的数据
                new Thread()
                    {
                        @Override
                        public void run()
                        {
                            String content = null;
                            // 不断读取Socket输入流中的内容。
                            try
                            {
                                while ((content = br.readLine()) != null)
                                {
                                    // 每当读到来自服务器的数据之后，发送消息通知程序界面显示该数据
                                    Message msg = new Message();
                                    msg.what = 0x123;
                                    msg.obj = content;
                                    handler.sendMessage(msg);
                                }
                            }
                            catch (IOException e)
                            {
                                e.printStackTrace();
                            }
                        }
                    }.start();
                // 为当前线程初始化Looper
                Looper.prepare();
                // 创建revHandler对象
                revHandler = new Handler()
                    {
                        @Override
                        public void handleMessage(Message msg)
                        {
                            // 接收到UI线程中用户输入的数据
                            if (msg.what == 0x345)
                            {
                                // 将用户在文本框内输入的内容写入网络
                                try
                                {
                                    os.write((msg.obj.toString() + "\r\n")
                                            .getBytes("utf-8"));
                                }
                                catch (Exception e)
                                {
                                    e.printStackTrace();
                                }
                            }
                        }
                    };
                // 启动Looper
                Looper.loop();
            }
            catch (SocketTimeoutException e1)
            {
                System.out.println("网络连接超时！！");
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }
}

