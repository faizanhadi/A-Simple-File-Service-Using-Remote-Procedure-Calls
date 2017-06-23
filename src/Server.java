/**
 * Created by faizan on 10/2/16.
 */
import org.apache.thrift.server.TServer;
import org.apache.thrift.server.TServer.Args;
import org.apache.thrift.server.TSimpleServer;
import org.apache.thrift.server.TThreadPoolServer;
import org.apache.thrift.transport.TSSLTransportFactory;
import org.apache.thrift.transport.TServerSocket;
import org.apache.thrift.transport.TServerTransport;
import org.apache.thrift.transport.TSSLTransportFactory.TSSLTransportParameters;

import java.io.File;
import java.util.*;


public class Server {

    public static FileHandler handler;
    public static FileStore.Processor processor;
    static int portNo = 0;

    public static void main(String[] args) {

        File directory = new File("FileStorage");
        if (directory.exists()){
            direcDelete(directory);
           // directory.delete();
        }

        if (args.length==0){
            System.out.println("Enter port number");
        }
        else {
            portNo = Integer.parseInt(args[0]);
            try {

                handler = new FileHandler();
                processor = new FileStore.Processor(handler);

                TServerTransport serverTransport = new TServerSocket(portNo);
                TServer server = new TThreadPoolServer(new TThreadPoolServer.Args(serverTransport).processor(processor));

                System.out.println("Server started on "+portNo);
                server.serve();


            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void direcDelete(File directory) {
        File[] filelist = directory.listFiles();
        if (null != filelist) {
            for (int i=0; i<filelist.length; i++) {
                if (filelist[i].isDirectory())
                    direcDelete(filelist[i]);
                else
                    filelist[i].delete();
            }
        }
        directory.delete();
    }


}
