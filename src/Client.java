/**
 * Created by faizan on 10/4/16.
 */

import org.apache.thrift.TException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.protocol.TJSONProtocol;
import org.apache.thrift.server.TServer;
import org.apache.thrift.server.TServer.Args;
import org.apache.thrift.server.TSimpleServer;
import org.apache.thrift.server.TThreadPoolServer;
import org.apache.thrift.transport.*;
import org.apache.thrift.transport.TSSLTransportFactory.TSSLTransportParameters;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

public class Client {
    public static void main (String[] args){

        String host = null;
        int portNo=0;
        String operation = null;
        String filename = null;
        String user = null;
        if ((args.length == 6) || (args.length ==8)){
            if (args[0] == null) {
                System.err.println("Host name can not be null");
                System.exit(0);
            }
            else
                host=args[0];

            if (args[1] == null)
                System.err.println("Port number can not be null");
            else
                portNo = Integer.parseInt(args[1]);

            if (args.length==6){
                for(int i =0; i<args.length;i++){
                    if(args[i].equals("--operation")){
                        String tempOper = args[i+1];
                        operation = tempOper.toLowerCase();
                        if(!(operation.equals("list"))) {
                            System.err.println("Invalid number of arguments");
                            System.exit(0);
                        }
                    }
                    else if (args[i].equals("--user")){
                        String tempUser = args[i+1];
                        user = tempUser.toLowerCase();
                    }
                }
            }

            else if (args.length == 8){
                for(int i =0; i<args.length;i++) {
                    if (args[i].equals("--operation")){
                        String tempOper = args[i+1];
                        operation = tempOper.toLowerCase();
                        if( (operation.equals("read")) || (operation.equals("write")) )  {
                        }
                        else {
                            System.err.println("Invalid type of arguments");
                            System.exit(0);
                        }
                    }
                    else if (args[i].equals("--filename")){
                        String tempfilename = args[i+1];
                        filename = tempfilename.toLowerCase();
                    }
                    else if (args[i].equals("--user")){
                        String tempUser = args[i+1];
                        user = tempUser.toLowerCase();
                    }

                }
                if(operation.equals("list")) {
                    System.err.println("Invalid arguments for List operation");
                    System.exit(0);
                }

            }
            else {
                System.err.println("Argument fault");
                System.exit(0);
            }
        }
        else{
            System.err.println("Invalid arguments");
            System.exit(0);
        }
        //System.out.println(host+" " + portNo +" "+ operation +" "+ filename +" "+ user);
        TTransport transport = new TSocket(host,portNo);
        try {
            transport.open();
            TProtocol protocol = new TBinaryProtocol(transport);
            FileStore.Client client = new FileStore.Client(protocol);
            perform(client, operation, filename, user);
            transport.close();
        }
        catch (TException e){
            e.printStackTrace();
        }
    }


    private static void perform(FileStore.Client client, String operation, String filename, String username) {


        String readContent;
        StringBuilder fileContent = null;
        FileReader fileReader=null;
        RFile readFile = new RFile();
        RFileMetadata Mdata = new RFileMetadata();
        StatusReport statusR= null;
        TIOStreamTransport tios = new TIOStreamTransport(System.out);
        TProtocol tProtocol = new TJSONProtocol.Factory().getProtocol(tios);
        String workingdir = System.getProperty("user.dir")+File.separator+filename;
        File fileN = new File(workingdir);

        try {
            if (operation.equals("write")) {

                fileReader = new FileReader(fileN);
                BufferedReader bufReader = new BufferedReader(fileReader);
                fileContent = new StringBuilder();

                while((readContent =bufReader.readLine()) != null){
                    fileContent.append(readContent);
                    fileContent.append(System.getProperty("line.separator"));
                }

                if (fileContent.toString().isEmpty()){}

                else
                    fileContent.deleteCharAt(fileContent.length()-1);
                //System.out.println("cont  "+fileContent);
                Mdata.setFilename(filename);
                Mdata.setOwner(username);
                Mdata.setFilenameIsSet(true);
                Mdata.setOwnerIsSet(true);

                readFile.setMeta(Mdata);
                readFile.setContent(fileContent.toString());
                readFile.setMetaIsSet(true);
                readFile.setContentIsSet(true);



                statusR =client.writeFile(readFile);
                statusR.write(tProtocol);
                System.out.println();

            }

            else if (operation.equals("read")) {
                try {
                    readFile = client.readFile(filename, username);
                    readFile.write(tProtocol);
                    System.out.println();


                }
                catch (SystemException e){
                    e.write(tProtocol);
                    System.out.println();

                }

            }

            else if (operation.equals("list")) {
                try {

                    String content = null;
                    List<RFileMetadata> listfiles = new ArrayList<>();
                    listfiles = client.listOwnedFiles(username);
                    for (int i = 0; i < listfiles.size(); i++) {

                        listfiles.get(i).write(tProtocol);

                    }System.out.println();
                }
                catch (SystemException e){
                    e.write(tProtocol);
                    System.out.println();
                }
            }
        }
        catch (Exception e){
            System.err.println(e.getMessage());
            System.exit(0);
        }

    }
}
