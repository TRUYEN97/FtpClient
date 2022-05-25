/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */
package ftpclient;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.IntStream;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;

/**
 *
 * @author Administrator
 */
public class FtpClient {

    private final FTPClient ftpClient;
    private final String server;
    private final int port;
    private final String user;
    private final String password;

    public FtpClient(String host, int port, String user, String passWord) {
        this.ftpClient = new FTPClient();
        this.server = host;
        this.port = port;
        this.user = user;
        this.password = passWord;
    }

    public boolean connect() {
        try {
            if (isConnect() && !disConnect()) {
                return false;
            }
            this.ftpClient.connect(server, port);
            int reply = this.ftpClient.getReplyCode();
            if (!FTPReply.isPositiveCompletion(reply)) {
                this.ftpClient.disconnect();
                System.err.println("Exception in connecting to FTP Server. Reply: " + reply);
                return false;
            }
            boolean sucess = this.ftpClient.login(user, password);
            this.ftpClient.enterLocalPassiveMode();
            this.ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
            return sucess;
        } catch (IOException ex) {
            ex.printStackTrace();
            return false;
        }
    }

    public boolean isConnect() {
        return this.ftpClient != null && this.ftpClient.isConnected();
    }

    public boolean appendFtpFile(String data, String ftpFile) {
        if (!isConnect()) {
            return false;
        }
        File file = new File(ftpFile);
        if (!checkFtpDirectoryExists(file.getParent())) {
            makeFtpDirectory(file.getParent());
        }
        try ( OutputStream stream = this.ftpClient.appendFileStream(ftpFile)) {
            stream.write(data.getBytes());
            stream.flush();
            return true;
        } catch (IOException ex) {
            ex.printStackTrace();
            return false;
        } finally {
            resetConnect();
        }

    }

    public boolean uploadFile(String localFile, String hostDir, String newFileName) {
        if (newFileName == null || hostDir == null || hostDir.isBlank()) {
            return false;
        }
        return uploadFile(localFile, String.format("%s/%s", hostDir, newFileName));
    }

    public boolean uploadFile(String localFile, String newFtpFile) {
        if (!isConnect()) {
            return false;
        }
        File file = new File(newFtpFile);
        if (!checkFtpDirectoryExists(file.getParent())) {
            makeFtpDirectory(file.getParent());
        }
        try ( InputStream input = new FileInputStream(new File(localFile))) {
            return this.ftpClient.storeFile(newFtpFile, input);
        } catch (IOException ex) {
            ex.printStackTrace();
            return false;
        }
    }

    public boolean downloadFile(String FtpFile, String localFile) {
        if (!isConnect()) {
            return false;
        }
        if (!checkFileFtpExists(FtpFile)) {
            return false;
        }
        File file = new File(localFile);
        if (!isParentExists(file) && !makeParentFile(file)) {
            return false;
        }
        OutputStream outputStream = null;
        try {
            outputStream = new BufferedOutputStream(new FileOutputStream(localFile));
            return this.ftpClient.retrieveFile(FtpFile, outputStream);
        } catch (IOException ex) {
            ex.printStackTrace();
            return false;
        } finally {
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    public boolean renameFtpFile(String oldName, String newName) {
        try {
            if (!isConnect() && connect()) {
                return false;
            }
            return this.ftpClient.rename(oldName, newName);
        } catch (IOException ex) {
            ex.printStackTrace();
            return false;
        }
    }

    public boolean disConnect() {
        try {
            if (this.ftpClient != null && this.ftpClient.isConnected() && this.ftpClient.logout()) {
                this.ftpClient.disconnect();
                return true;
            }
            return false;
        } catch (IOException ex) {
            ex.printStackTrace();
            return false;
        }
    }

    private boolean makeFtpDirectory(String dir) {
        try {
            return this.ftpClient.makeDirectory(dir);
        } catch (IOException ex) {
            ex.printStackTrace();
            return false;
        }
    }

    private boolean checkFileFtpExists(String filePath) {
        if (!isConnect()) {
            return false;
        }
        InputStream inputStream = null;
        try {
            inputStream = ftpClient.retrieveFileStream(filePath);
            return !(inputStream == null || ftpClient.getReplyCode() == 550);
        } catch (IOException ex) {
            ex.printStackTrace();
            return false;
        } finally {
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            resetConnect();
        }
    }

    private void resetConnect() {
        disConnect();
        connect();
    }

    private boolean checkFtpDirectoryExists(String dirPath) {
        try {
            ftpClient.changeWorkingDirectory(dirPath);
            if (ftpClient.getReplyCode() == 550) {
                return false;
            }
            return true;
        } catch (IOException ex) {
            ex.printStackTrace();
            return false;
        } finally {
            resetConnect();
        }
    }

    private boolean makeParentFile(File file) {
        return file.getParentFile().mkdirs();
    }

    private boolean isParentExists(File file) {
        return file.exists() || file.getParentFile().exists();
    }

    public static void main(String[] args) {
        FtpClient ftpClient1 = new FtpClient("10.90.1.168", 21, "oper", "mfg-oper");
        FtpClient ftpClient2 = new FtpClient("10.90.1.168", 21, "oper", "mfg-oper");
        System.out.println(ftpClient1.connect());
        System.out.println(ftpClient2.connect());
        System.out.println(ftpClient1.uploadFile("test1.txt", "temp/TE PRO/SQT/sqt1/a123/346/test14.txt"));
        System.out.println(ftpClient2.uploadFile("test1.txt", "temp/TE PRO/SQT/sqt1/a123/346/test1.txt"));
        System.out.println(ftpClient1.appendFtpFile("ggg/test15.txt", "temp/TE PRO/SQT/sqt1/a123/346/test14.txt"));
        System.out.println(ftpClient2.appendFtpFile("ggg/test1.txt", "temp/TE PRO/SQT/sqt1/a123/346/test1.txt"));
        System.out.println(ftpClient1.renameFtpFile("temp/TE PRO/SQT/sqt1/a123/346/test14.txt", "temp/TE PRO/SQT/sqt1/a123/346/test15.txt"));
        System.out.println(ftpClient2.renameFtpFile("temp/TE PRO/SQT/sqt1/a123/346/test1.txt", "temp/TE PRO/SQT/sqt1/a123/346/test2.txt"));
        System.out.println(ftpClient1.downloadFile("temp/TE PRO/SQT/sqt1/a123/346/test15.txt", "ggg/test15.txt"));
        System.out.println(ftpClient2.downloadFile("temp/TE PRO/SQT/sqt1/a123/346/test2.txt", "ggg/test2.txt"));
        System.out.println(ftpClient1.disConnect());
        System.out.println(ftpClient2.disConnect());
        
    }

}
