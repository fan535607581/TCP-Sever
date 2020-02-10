package com.core;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.SocketException;

import org.apache.commons.net.telnet.TelnetClient;

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
    
    @SimpleFunction(description = "start")//软件向控件写回复信息
    public void sendMessage(String s)
    {
	 k = s.length()/3;
	 for(int j = 0; j<k ;j++){i[j] = Integer.parseInt(s.substring(j*3,(j+1)*3));}
	 for(int j = 0; j<k+1 ;j++){bb[j+1] = (byte)i[j];} 
	 //con=1;
	 //new ServerThread2().start();
    }
    @SimpleFunction(description = "start")//断开客户端
    public void Clientclose(){con = 2;}
	
    @SimpleFunction(description = "start")//关闭服务器
    public void Serverclose(){try{serverSocket.close();}catch (IOException e) {}}
	
    @SimpleEvent//向软件输出信息
    public void GetMessage(String s){ EventDispatcher.dispatchEvent(this, "GetMessage", s); }
	
    @SimpleFunction(description = "start")//打开通信端口
    public void receiveData(int PORT){
        
        Thread thread = new Thread()//等待客户端连接的进程
        {
            @Override
            public void run() { }
        };
        thread.start();
 }
    
public class Main {

    public static void main(String[] args) {
        try {
            TelnetClient telnetClient = new TelnetClient("vt200");  //指明Telnet终端类型，否则会返回来的数据中文会乱码
            telnetClient.setDefaultTimeout(5000); //socket延迟时间：5000ms
            telnetClient.connect("127.0.0.1",23);  //建立一个连接,默认端口是23
            InputStream inputStream = telnetClient.getInputStream(); //读取命令的流
            PrintStream pStream = new PrintStream(telnetClient.getOutputStream());  //写命令的流
            byte[] b = new byte[1024];
            int size;
            StringBuffer sBuffer = new StringBuffer(300);
            while(true) {     //读取Server返回来的数据，直到读到登陆标识，这个时候认为可以输入用户名
                size = inputStream.read(b);
                if(-1 != size) {
                    sBuffer.append(new String(b,0,size));
                    if(sBuffer.toString().trim().endsWith("login:")) {
                        break;
                    }
                }
            }
            System.out.println(sBuffer.toString());
            pStream.println("exit"); //写命令
            pStream.flush(); //将命令发送到telnet Server
            if(null != pStream) {
                pStream.close();
            }
            telnetClient.disconnect();
        } catch (SocketException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
