package ModbusTCPServer;

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

import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.io.PrintStream;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import javax.net.*;
import java.util.*;

@DesignerComponent(version = SocketUtil.VERSION,
    description = "made in fan hao jie",
    category = ComponentCategory.EXTENSION,
    nonVisible = true,
    iconName = "images/extension.png")

@SimpleObject(external = true)

public class SocketUtil extends AndroidNonvisibleComponent 
{
    public static final int VERSION = 2;//控件版本号
    private static final String LOG_TAG = "SocketUtil";
    private ComponentContainer container;
    private Context context;
    private ServerSocket serverSocket = null;
    OutputStream ou = null;//系统输出流
	
    String ip;//系统返回IP地址
    int port;//系统返回端口
    int con = 0;//控制信号
    int DK = 0;//外部设置的端口
    int k = 0;//回复数据的长度
	private ArrayList<Socket> clients = new ArrayList<Socket>();
    	
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
	
    public void getLocalIpAddress(ServerSocket serverSocket)
	{
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

    @SimpleFunction(description="start")//获取客户端列表
	public ArrayList<String> IPList()
	{
		ArrayList<String> client = new ArrayList<String>();
		for (Socket socket : clients)
		try{socket.sendUrgentData(0xFF);}
			catch(Exception ex)
				{
				clients.remove(socket);
				try{
					if(ou != null)ou.close();
					socket.close();
					}catch (IOException e) {}	
				}
		for (Socket socket : clients) client.add(socket.getRemoteSocketAddress()+"");
		return	client;
	}

    @SimpleFunction(description = "start")//软件向控件写回复信息
    public void sendMessage(String s , int port)
    {
	 int[] i = new int[1000];//回复原始数据
	 byte[] bb = new byte[1000];//回复数据

	 k = s.length()/3;
	 for(int j = 0; j<k ;j++){i[j] = Integer.parseInt(s.substring(j*3,(j+1)*3));}
	 for(int j = 0; j<k+1 ;j++){bb[j+1] = (byte)i[j];} 
	 for (Socket socket : clients) if(port== socket.getPort()) new ServerThread2(bb,socket).start();	
    }

    @SimpleFunction(description = "start")//断开客户端
    public void Clientclose()
	{
		Message message_2;
		for (Socket socket : clients)
		try{
			if(ou != null)ou.close();
			socket.close();
			message_2 = handler.obtainMessage();
			message_2.obj ="客户端已断开:"+socket.getInetAddress().getHostAddress();
			handler.sendMessage(message_2);
			}catch (IOException e) {}
	}
	
    @SimpleFunction(description = "start")//关闭服务器
    public void Serverclose()
    {
	 Message message_2;
	 try{
	    if(serverSocket != null)
	    {
	    serverSocket.close();
	    message_2 = handler.obtainMessage();
	    message_2.obj = "服务器已关闭";
	    handler.sendMessage(message_2);
	    }
		}catch (IOException e) 
	    {
	    message_2 = handler.obtainMessage();
	    message_2.obj = "服务器关闭失败";
	    handler.sendMessage(message_2);        
	    }
    }
	
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
			message_1.obj = "服务器已开启:" + ip + ":" + port;
			handler.sendMessage(message_1);
			//new ServerThread3(DK).start();
		}catch (IOException e)
		{
			Message message_1 = handler.obtainMessage();
			message_1.obj = "服务器开启失败，端口被占用";
			handler.sendMessage(message_1);	
		}

                while (true)
				{
                    Socket socket = null;
                    try {
                        socket = serverSocket.accept();
						clients.add(socket);
                        new ServerThread(socket).start();  
			    
                        Message message_2 = handler.obtainMessage();
                        message_2.obj = "客户端连接："+socket.getInetAddress().getHostAddress();
                        handler.sendMessage(message_2);
                   	 } 
					catch (IOException e) {} 
                }
            }
        };
        thread.start();
 }
	class ServerThread3 extends Thread//清理客户端远程断开
	{
		int b = 0;
		Message message_2;
		public ServerThread3(int b){this.b = b;}
		@Override
	    public void run()
		{
			while (true)
			{
				for (Socket socket : clients)
					try{socket.sendUrgentData(0xFF);}
					catch(Exception ex)
					{
						try{
						if(ou != null)ou.close();
						socket.close();
						message_2 = handler.obtainMessage();
						message_2.obj ="远程断开:"+socket.getInetAddress().getHostAddress();
						handler.sendMessage(message_2);
						}catch (IOException e) {}	
					}
			}
		}
	}

	class ServerThread2 extends Thread//输出回复信息的进程
	{ 
		Socket socket = null;
	    byte[] bb = new byte[1000];
	    public ServerThread2(byte[] bb,Socket socket){this.bb = bb;this.socket = socket;}	
	    @Override
	    public void run()
			{
			try{ou = socket.getOutputStream();ou.write(bb , 1 , k);ou.flush();}catch (IOException e){} 
			}
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
					int mm =0;    String SC = "";//回复命令   
					msy = socket.getInputStream().read(b);
					if( msy >= 0)	
					{ 
						for(int j = 0; j<(b[5]+6) ; j++){mm = b[j]&0xff;SC = SC + mm + ",";}
						message_2 = handler.obtainMessage();
						message_2.obj = SC  + "," + socket.getPort();
						handler.sendMessage(message_2);
					}} catch (IOException e){}
                }
           }
	}
}
