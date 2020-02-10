package cn.roger.socketclient;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.Socket;
import java.net.SocketException;
import java.io.OutputStream;
import com.google.appinventor.components.annotations.*;
import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.runtime.*;
import com.google.appinventor.components.runtime.util.*;
import com.google.appinventor.components.runtime.errors.YailRuntimeError;
import android.graphics.drawable.GradientDrawable;
import android.graphics.Color;
import android.content.res.ColorStateList;
import android.view.View;
import android.graphics.drawable.RippleDrawable;
import android.graphics.drawable.Drawable;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.app.Activity;
import android.content.Context;
import android.view.Menu;
import android.widget.TextView;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.SocketException;
import java.util.Enumeration;
import java.net.TelnetClient;

import org.apache.commons.net.telnet.TelnetClient;

@SimpleObject(external = true)
public class SocketUtil extends AndroidNonvisibleComponent {
  
    @SimpleFunction(description = "start")//打开通信端口
    public void receiveData(int PORT)
	{     
        Thread thread = new Thread()//等待客户端连接的进程
        {
            @Override
            public void run() { }
        };
        thread.start();
 	}
}


    class ServerThread extends Thread
    {
    	Socket socket; 
        public ServerThread(Socket socket){this.socket = socket; }	
	    @Override
	    public void run()
	    {
        try {
            TelnetClient telnetClient = new TelnetClient("vt200");  //指明Telnet终端类型，否则会返回来的数据中文会乱码
            telnetClient.setDefaultTimeout(5000); //socket延迟时间：5000ms
            telnetClient.connect("127.0.0.1",23);  //建立一个连接,默认端口是23
            InputStream inputStream = telnetClient.getInputStream(); //读取命令的流
            PrintStream pStream = new PrintStream(telnetClient.getOutputStream());  //写命令的流
            byte[] b = new byte[1024];
            int size;
            StringBuffer sBuffer = new StringBuffer(300);
            while(true) //读取Server返回来的数据，直到读到登陆标识，这个时候认为可以输入用户名
	    {    
                size = inputStream.read(b);
                if(-1 != size) 
		{
                    sBuffer.append(new String(b,0,size));
                    if(sBuffer.toString().trim().endsWith("login:")) {break;}
                }
            }
		
            /*System.out.println(sBuffer.toString());
            pStream.println("exit"); //写命令
            pStream.flush(); //将命令发送到telnet Server */
		
            if(null != pStream) {pStream.close();}telnetClient.disconnect();
        } catch (IOException e) {}
	}
    }
