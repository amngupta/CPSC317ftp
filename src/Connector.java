//import sun.net.ftp.FtpClient;

import java.io.*;
import java.net.Socket;

/*
* Error Codes:
* 220: Connection Successful
* 530: Need to login
* 331: Need password
* 230: Login Successful
* 550: Failed to open file
* 250: Directory successfully changed
* 227: Entering Passive Mode
* https://kb.globalscape.com/KnowledgebaseArticle10142.aspx
* */

/**
 * This is the base class for the CSftp. We are calling it the connector
 */

public class Connector {

    Socket sock = null;
    BufferedReader br = null;
    PrintWriter out = null;
    BufferedReader userInputBR = null;
    String IPAddress = null;
    int PORT;

    /**
     * Connector constructor initialises the Socket, userInputBR,
     * br (the bufferedReader for server responses), and out (the PrintWriter for server)
     * @param IPAddress the user supplied IPAddress
     * @param Port the user supplied PORT
     */
    Connector(String IPAddress, int Port){

        try {
            this.IPAddress = IPAddress;
            this.PORT = Port;
            this.sock = new Socket(IPAddress, Port);
            this.br = new BufferedReader(new InputStreamReader(this.sock.getInputStream()));
            this.out = new PrintWriter(this.sock.getOutputStream(), true);
            this.userInputBR = new BufferedReader(new InputStreamReader(System.in));
        }catch(Exception e){
            System.out.println(e);
        }
    }

    private String readCommand(String[] args){
        if(args.length > 0) {
            String command = args[0];
            if(command.isEmpty() || command.startsWith("#"))
            {
                return "newline";
            }
            switch (command) {
                case "user": {
                    if (args.length != 2) {
                        return "0x002 Incorrect number of arguments.";
                    } else {
                        System.out.println("--> USER " + args[1]);
                        return "USER " + args[1];
                    }
                }
                case "pw": {
                    if (args.length != 2) {
                        return "0x002 Incorrect number of arguments.";
                    } else {
                        System.out.println("--> PASS "+ args[1]);
                        return "PASS " + args[1];
                    }
                }
                case "cd": {
                    if (args.length != 2) {
                        return "0x002 Incorrect number of arguments.";
                    } else {
                        System.out.println("--> CWD " + args[1]);
                        return "CWD " + args[1];
                    }
                }
                case "get": {
                    if (args.length != 2) {
                        return "0x002 Incorrect number of arguments.";
                    } else {
                        return "get";
                    }
                }
                case "quit": {
                    if (args.length != 1) {
                        return "0x002 Incorrect number of arguments.";
                    } else {
                        return "quit";
                    }
                }
                case "dir": {
                    if (args.length != 1) {
                        return "0x002 Incorrect number of arguments.";
                    } else {
                        return "dir";
                    }
                }
                case "features": {
                    if (args.length != 1) {
                        return "0x002 Incorrect number of arguments.";
                    } else {
                        return "features";
                    }
                }
                default:{
                    return "0x001 Invalid command.";
                }
            }
        }
        return "0x001 Invalid command.";
    }

    private int getResponseCode(String response){
        if (!response.contains("-")) {
            String[] split = response.split("\\s+");
            return Integer.parseInt(split[0]);
        } else {
            return Integer.parseInt(response.substring(0,3));
        }

    }

    /**
     *  This function reads the response from the server as String
     *  and then acts according to the response
     * @param response
     * @return String with error code and message for that error code
     */
    private String readResponse(String response){
        int responseCode = this.getResponseCode(response);
        if(responseCode == 426 || responseCode == 425){
            return "0xFFFC Control connection to "+this.IPAddress+ " on port "+ this.PORT+" failed to open.";
        }
        if(responseCode == 550){
            return "0x38E";
        }
        if (responseCode >=100 && responseCode < 400){
            return response;
        }
        else{
//                return response;
            return "0xFFFF Processing error. "+response;
        }
    }


    private void runPassive(String[] args){
        String command = args[0];
        String argument = null;
        if(args.length > 1) {
            argument = args[1];
        }
        String response = null;
        this.out.println("PASV");
        try {
            response = readResponse(this.br.readLine());
            System.out.println("<-- " + response);
            int responseCode = this.getResponseCode(response);
            if(responseCode > 100 && responseCode <400){
                String IPandPORT = response.substring(response.indexOf("(")+1,response.indexOf(")"));
                String[] numbers = IPandPORT.split(",");
                String ipAddress = numbers[0] +"."+numbers[1] +"."+numbers[2] +"."+numbers[3];
                int Port = Integer.parseInt(numbers[4])*256 + Integer.parseInt(numbers[5]);
                if(responseCode == 227){
                    //Entering Passive Mode
                    Connector pass = new Connector(ipAddress, Port);
                    if(command.equals("get")){
                        this.out.println("RETR "+argument);
                        System.out.println("--> RETR " + argument);
                        System.out.println(this.br.readLine());
                        String line = pass.br.readLine();
                        String finalString = line;
                        while (line != null){
                            if (line.contains("null")) {
                                break;
                            }
                            System.out.println(line);
                            line = pass.br.readLine();
                            finalString +=line;
                        }

                        String responseEOF = readResponse(this.br.readLine());
                        if(responseEOF.contains("0x")){
                            System.out.println(responseEOF);
                            this.sock.close();
                            return;
                        }
                        // TO DO
                        else{
                            System.out.println(response);
                            try {
                                byte[] myByteArray = finalString.getBytes();
                                FileOutputStream fos = new FileOutputStream(".");
                                fos.write(myByteArray);
                                fos.close();
                                //PrintWriter out = new PrintWriter("C:/Users/Trevor/Desktop/filename.xml");
                                //out.print(finalString);
                                //out.close();
                            }
                            catch(Exception e){
                                System.out.println("0x38E Access to local file "+ argument + " denied. ");
                                return;
                            }
                        }

                    }
                    else if (command.equals("dir")){
                        this.out.println("LIST");
                        System.out.println("--> LIST");
                        System.out.println("<-- " + this.br.readLine());
                        String line = pass.br.readLine();
                        while (line != null){
                            if (line.contains("null")){
                                break;
                            }
                            System.out.println("<-- "+ line);
                            line = pass.br.readLine();
                        }
                        System.out.println("<-- " + this.br.readLine());
                    }
                    return;
                }
                else{
                    System.out.println("0x3A2 Data transfer connection to " + ipAddress+" on port "+Port+" yyy failed to open.");
                    this.sock.close();
                    return;
                }
            }
            else{
                System.out.println("0xFFFF Processing error. yyyy. " + response);
                this.sock.close();
                return;
            }
        }
        catch(IOException e){
            System.out.println("0xFFFE Input error while reading commands, terminating.");
        }
        catch(Exception e){
            System.out.println("0xFFFF Processing error." + e);
            return;
        }
    }

    /**
     * This method runs an instance of client until user enters QUIT or an error occurs
     * causing the client to stop
     */
    private void runClient(){
            try {
                System.out.print("csftp> ");
                String userInput = this.userInputBR.readLine();
                String[] args = userInput.split("\\s+");
                String command = this.readCommand(args);
                if("get".contentEquals(command)){
                    this.runPassive(args);
                    if(!this.sock.isClosed()) {
                        this.runClient();
                    }
                    return;
                }
                else if ("features".contentEquals(command)) {
                    this.out.println("FEAT");
                    System.out.println("---> FEAT");
                    String line = this.br.readLine();
                    while (!line.equals("")){
                        System.out.println("<-- " + line);
                        line = this.br.readLine();
                        if(line.contains("211")){
                            System.out.println("<-- " + line);
                            break;
                        }
                    }
                    this.runClient();
                }
                else if("dir".contentEquals(command)){
                    this.runPassive(args);
                    if(!this.sock.isClosed()) {
                        this.runClient();
                    }
                    return;
                }
                else if ("quit".contentEquals(command)) {
                    this.out.println("QUIT");
                    System.out.println("<-- " + this.br.readLine());
                    this.sock.close();
                    return;
                }
                else if (command.contains("0x")){
                    //Error for invalid command
                    System.out.println(command);
                    if(command.contains("38E")){
                        System.out.println("0x38E Access to local file "+ args[0] + " denied. ");
                    }
                    this.sock.close();
                    return;
                }
                else if("newline".contentEquals(command)){
                    this.runClient();
                    return;
                }
                else{
                    this.out.println(command);
                }
                String response = readResponse(this.br.readLine());
                if (response.contains("0x")) {
                    System.out.println("<-- " + response);
                    this.sock.close();
                    return;
                }
                else {

                    while(response.contains("-")) {
                        System.out.println("<-- " + response);
                        response = readResponse(this.br.readLine());
                    }
                    System.out.println("<-- " + response);
                    this.runClient();
                    return;
                }
            }
            catch(IOException e){
                System.out.println("0xFFFE Input error while reading commands, terminating.");
            }
            catch(Exception e){
                System.out.println("0xFFFF Processing error." + e);
                return;
            }
    }

    public static void main(String args[]){
        if(args.length < 1){
            System.out.println("Error, at least two commands required");
        }
        else{
            String IPAddress = args[0];
            int Port = 21;
            if(args.length == 2){
                Port = Integer.parseInt(args[1]);
            }
            Connector conn = new Connector(IPAddress, Port);
            try{
                System.out.println(conn.br.readLine());
            }catch (Exception e){}
            conn.runClient();

            System.out.println(IPAddress + Port);
        }
    }
}
