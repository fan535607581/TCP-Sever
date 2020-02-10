package cn.roger.socketclient;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.Socket;
import java.net.SocketException;

//import org.apache.commons.net.telnet.TelnetClient;

@DesignerComponent(version = TelnetClient.VERSION,
    description = "made in fan hao jie \n QQ:535607581",
    category = ComponentCategory.EXTENSION,
    nonVisible = true,
    iconName = "images/extension.png")

@SimpleObject(external = true)
public class TelnetClient extends AndroidNonvisibleComponent {
    public static final int VERSION = 1;//控件版本号
    private static final String LOG_TAG = "TelnetClient";
   /* private ComponentContainer container;
    private Context context;
	
    public TelnetClient(ComponentContainer container) 
    {
        super(container.$form());
        this.container = container;
        context = (Context) container.$context();
    }*/
    
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
