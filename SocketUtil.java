package cn.roger.socket;

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


@DesignerComponent(version = SocketUtil.VERSION,
    description = "made in fan hao jie \n QQ:535607581",
    category = ComponentCategory.EXTENSION,
    nonVisible = true,
    iconName = "images/extension.png")

@SimpleObject(external = true)

public class SocketUtil extends AndroidNonvisibleComponent {
    public static final int VERSION = 1;//控件版本号
    private static final String LOG_TAG = "SocketUtil";
    private ComponentContainer container;
    private Context context;
    private ServerSocket serverSocket = null;
    OutputStream ou = null;//系统输出流
    int KH = 0;
	
    String ip;//系统返回IP地址
    int port;//系统返回端口
    int con = 0;//控制信号
    byte[] bb = new byte[1000];//回复数据
    int[] i = new int[1000];//回复原始数据
    int k = 0;//回复数据的长度
    int DK = 0;//外部设置的端口
		
    public Handler handler = new Handler()
    {
        @Override
        public void handleMessage(Message msg){ GetMessage(msg.obj.toString()); }
    };
	
    public SocketUtil(ComponentContainer container) 
    {
        super(container.$form());
        this.container = container;
        context = (Context) container.$context();
    }
	
    public void getLocalIpAddress(ServerSocket serverSocket){
      try {
         for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();){
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses();    enumIpAddr.hasMoreElements();){
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    String mIP = inetAddress.getHostAddress().substring(0, 3);
                    if(mIP.equals("192")){
                        ip = inetAddress.getHostAddress();    //获取本地IP
                        port = serverSocket.getLocalPort();
                    }
                }
            }
      }catch (SocketException e) {e.printStackTrace();}
   }
    
    @SimpleFunction(description = "start")//软件向控件写回复信息
    public void sendMessage(String s)
    {
	 k = s.length()/3;
	 for(int j = 0; j<k ;j++){i[j] = Integer.parseInt(s.substring(j*3,(j+1)*3));}
	 for(int j = 0; j<k+1 ;j++){bb[j+1] = (byte)i[j];} 
	 //con=1;
	 new ServerThread2().start();
    }
    @SimpleFunction(description = "start")//断开客户端
    public void Clientclose(){con = 2;}
	
    @SimpleFunction(description = "start")//关闭服务器
    public void Serverclose(){try{serverSocket.close();}catch (IOException e) {}}
	
    @SimpleEvent//向软件输出信息
    public void GetMessage(String s){ EventDispatcher.dispatchEvent(this, "GetMessage", s); }
	
    @SimpleFunction(description = "start")//打开通信端口
    public void receiveData(int PORT){
	DK = PORT;
        Thread thread = new Thread(){//等待客户端连接的进程
            @Override
            public void run() {
                super.run();
		try {    
			serverSocket = new ServerSocket(DK);
			getLocalIpAddress(serverSocket);
			Message message_1 = handler.obtainMessage();
			message_1.obj = "IP:" + ip + " PORT: " + port;
			handler.sendMessage(message_1);
		}catch (IOException e) {}

                while (true)
		{
                    Socket socket = null;
                    try {
                        socket = serverSocket.accept(); 
                        Message message_2 = handler.obtainMessage();
                        message_2.obj = "连上了！"+socket.getInetAddress().getHostAddress();
                        handler.sendMessage(message_2);
                   	 } 
		    catch (IOException e) {}
		    con=0;
                    new ServerThread(socket).start();
                }
            }
        };
        thread.start();
 }
	class ServerThread2 extends Thread//输出回复信息的进程
	{ 
	    public ServerThread2(){}	
	    @Override
	    public void run(){ try{ou.write(bb , 1 , k);ou.flush();}catch (IOException e){} }
	}
	
	class ServerThread extends Thread//接收数据的进程
	{
	    Socket socket; 
	    Message message_2;
	    public ServerThread(Socket socket){this.socket = socket; }	
	    @Override
	    public void run()
	    {
                while(socket != null)
		{	
		    try {
                	int msy = 0;  byte[] b = new byte[255];
			msy = socket.getInputStream().read(b);
			if( msy >= 0)	
			{ 
			for(int j = 0; j<(b[5]+6) ; j++)
				{
				message_2 = handler.obtainMessage();
				message_2.obj = b[j]&0xff;
				handler.sendMessage(message_2);
				}
				ou = socket.getOutputStream();
			}
			if(con == 2){try{ou.close();socket.close();}catch (IOException e) {}}
			} catch (IOException e){}
                }
            }
	}
}
