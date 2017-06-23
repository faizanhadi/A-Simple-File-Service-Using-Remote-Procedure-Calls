/**
 * Created by faizan on 10/2/16.
 */

import com.sun.corba.se.impl.presentation.rmi.ExceptionHandlerImpl;
import org.apache.thrift.TException;
import sun.security.provider.MD5;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.io.*;
import java.util.logging.StreamHandler;

public class FileHandler implements FileStore.Iface {

    HashMap <String, ArrayList<RFile>> HMap = new HashMap<>();

    @Override
    public StatusReport writeFile(RFile rFile) throws TException {

        String folderName = rFile.getMeta().getOwner();
        File file = new File("FileStorage");
        if (!(file.exists()))
            file.mkdir();
        File UFolder = new File("FileStorage"+ File.separator + folderName);
        UFolder.mkdir();
        File Ufile = new File("FileStorage" + File.separator + folderName + File.separator + rFile.getMeta().getFilename());
        //FileStorage/UFolder/UFile

        try {
            String MDigest=null;
            try {
                MessageDigest md = MessageDigest.getInstance("MD5");
                byte md_hash[] = md.digest(rFile.getContent().getBytes());
                StringBuilder Sbulider = new StringBuilder();
                for (int i = 0; i < md_hash.length; i++) {
                    Sbulider.append(String.format("%02x", i & 0xff));
                }
                MDigest = Sbulider.toString();
            }
            catch (NoSuchAlgorithmException e){
                System.err.println("MD5 invalid error");
            }

            if (Ufile.exists()) {
                PrintWriter Pwriter = new PrintWriter("FileStorage"+ File.separator+folderName+File.separator+rFile.getMeta().getFilename());
                Pwriter.write(rFile.getContent());
                Pwriter.close();

                ArrayList<RFile> Wlist = new ArrayList<>();
                Wlist = HMap.get(rFile.getMeta().getOwner());

                for(int i=0; i<Wlist.size(); i++) {
                    if(Wlist.get(i).getMeta().getFilename().equals(rFile.getMeta().getFilename())) {
                        Wlist.get(i).getMeta().setUpdated(Ufile.lastModified());
                        Wlist.get(i).getMeta().setUpdatedIsSet(true);
                        int version = Wlist.get(i).getMeta().getVersion() + 1;
                        Wlist.get(i).getMeta().setVersion(version);
                        Wlist.get(i).getMeta().setContentLength(rFile.getContent().length());
                        Wlist.get(i).getMeta().setContentHash(MDigest);
                        Wlist.get(i).setContent(rFile.getContent());
                    }
                }
            }
            else {
                ArrayList<RFile> WList = new ArrayList<>();

                Ufile.createNewFile();
                PrintWriter PWriter = new PrintWriter(("FileStorage" + File.separator + folderName + File.separator + rFile.getMeta().getFilename()));
                PWriter.write(rFile.getContent());
                PWriter.close();
                String FContent = rFile.getContent();
                rFile.setContent(FContent);
                rFile.getMeta().setCreated(Ufile.lastModified());
                rFile.getMeta().setCreatedIsSet(true);
                rFile.getMeta().setUpdated(Ufile.lastModified());
                rFile.getMeta().setUpdatedIsSet(true);
                rFile.getMeta().setVersion(0);
                rFile.getMeta().setVersionIsSet(true);
                rFile.getMeta().setContentLength(rFile.getContent().length());
                rFile.getMeta().setContentLengthIsSet(true);
                rFile.getMeta().setContentHash(MDigest);
                rFile.getMeta().setContentHashIsSet(true);
                if (HMap.get(rFile.getMeta().getOwner()) == null ){
                    WList.add(rFile);
                    HMap.put(rFile.getMeta().getOwner(),WList);
                }
                else{
                    WList= HMap.get(rFile.getMeta().getOwner());
                    WList.add(rFile);
                    HMap.put(rFile.getMeta().getOwner(), WList);
                }
            }
        }

        catch (IOException e){
            StatusReport statusReport = new StatusReport(Status.FAILED);
            return statusReport;
        }


        StatusReport statusReport = new StatusReport(Status.SUCCESSFUL);
        return statusReport;
    }

    @Override
    public RFile readFile(String filename, String owner) throws SystemException, TException {


            ArrayList<RFile> listofFiles = new ArrayList<>();
            if(HMap.containsKey(owner)) {
                listofFiles = HMap.get(owner);
                for (int i = 0; i < listofFiles.size(); i++) {
                    if (listofFiles.get(i).getMeta().getFilename().equals(filename)) {

                        return listofFiles.get(i);
                    }

                }
                 {
                    SystemException e = new SystemException();
                    e.setMessage("File "+filename+" for User "+owner+" does not exist");
                    throw e;
                }
            }
            else {
                SystemException e = new SystemException();
                e.setMessage("User "+owner+" does not exist");
                throw e;
            }

    }

    @Override
    public List<RFileMetadata> listOwnedFiles(String user) throws SystemException, TException {
        ArrayList<RFile> ownerFile = new ArrayList<>();
        List<RFileMetadata>  fileList = new ArrayList<>();

        if(HMap.containsKey(user)) {
            ownerFile = HMap.get(user);




                for (int i = 0; i < ownerFile.size(); i++) {
                    fileList.add(ownerFile.get(i).getMeta());

                }

        }
            else {
                SystemException e = new SystemException();
                e.setMessage("User "+user+" does not exist");
                throw e;
            }


            return fileList;
        }








}
