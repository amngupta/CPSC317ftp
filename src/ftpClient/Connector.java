package ftpClient;

import java.io.*;
import java.net.Socket;
import java.net.SocketException;


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

    /**
     * This function is used to test if the user entered a valid command
     * @param args: the command that is supplied by the user
     * @return if Command is valid, it returns the PARSED command, else it returns error
     */
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
                        System.out.println("--> PASS " + args[1]);
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


    /**
     * This function is used to extract the integer value of the response code
     * @param response: The response from the server
     * @return Integer value of the response from server
     */
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
            return "0xFFFF Processing error. "+response;
        }
    }

    /**
     * This function is used to run another instance of the connector class
     * This helps in running the ftp in Passive mode.
     * Using this class, we are able to successfully use functions such as dir and get
     * @param args These are the commands supplied by the user. dir has only one command
     *             while get has the get command along with the filename
     */
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
            if(response.contains("0x")){
                System.out.println(response);
                this.sock.close();
                return;
            }
            else {
                int responseCode = this.getResponseCode(response);
                if (responseCode > 100 && responseCode < 400) {
                    String IPandPORT = response.substring(response.indexOf("(") + 1, response.indexOf(")"));
                    String[] numbers = IPandPORT.split(",");
                    String ipAddress = numbers[0] + "." + numbers[1] + "." + numbers[2] + "." + numbers[3];
                    int Port = Integer.parseInt(numbers[4]) * 256 + Integer.parseInt(numbers[5]);
                    if (responseCode == 227) {
                        //Entering Passive Mode
                        Connector pass = new Connector(ipAddress, Port);
                        if (command.equals("get")) {
                            System.out.println("--> RETR "+ argument);
                            this.out.println("RETR " + argument);
                            String responseBF = readResponse(this.br.readLine());
                            if (responseBF.contains("0x")) {
                                System.out.println("<-- " +responseBF);
                                this.sock.close();
                                return;
                            }
                            String line = pass.br.readLine();
                            String finalString = line;
                            while (line != null) {
                                if (line.contains("null")) {
                                    break;
                                }

                                line = pass.br.readLine();
                                finalString += line;
                            }

                            String responseEOF = readResponse(this.br.readLine());
                            if (responseEOF.contains("0x")) {
                                System.out.println("<-- "+responseEOF);
                                this.sock.close();
                                return;
                            }
                            // TODO
                            else {
                                System.out.println("<-- " + response);
                                try {
                                    String current_dir = System.getProperty("user.dir");
                                    byte[] myByteArray = finalString.getBytes();
                                    FileOutputStream fos = new FileOutputStream(current_dir+ "/" +argument);
                                    fos.write(myByteArray);
                                    fos.close();
                                } catch (Exception e) {
                                    System.out.println("0x38E Access to local file " + argument + " denied. ");
                                    return;
                                }
                            }

                        } else if (command.equals("dir")) {
                            System.out.println("--> LIST");
                            this.out.println("LIST");
                            System.out.println("<-- " + this.br.readLine());
                            String line = pass.br.readLine();
                            while (line != null) {
                                if (line.contains("null")) {
                                    break;
                                }
                                System.out.println(line);
                                line = pass.br.readLine();
                            }
                            System.out.println("<-- " + this.br.readLine());
                        }
                        return;
                    } else {
                        System.out.println("0x3A2 Data transfer connection to " + ipAddress + " on port " + Port + " yyy failed to open.");
                        this.sock.close();
                        return;
                    }
                }
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
     * This method runs an instance of client until user enters QUIT
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
                    System.out.println("--> FEAT");
                    this.out.println("FEAT");
                    String line = this.br.readLine();
                    while (!line.equals("")){
                        System.out.println("<-- " + line);
                        line = this.br.readLine();
                        if(line.contains("211")){
                            System.out.println("<--" + line);
                            break;
                        }
                    }
                    this.runClient();
                }
                else if("dir".contentEquals(command)){
                    this.runPassive(args);
                    if(!(this.sock.isClosed())) {

                        this.runClient();
                    }
                    return;
                }
                else if ("quit".contentEquals(command)) {
                    System.out.println("--> QUIT");
                    this.out.println("QUIT");
                    return;
                }
                else if (command.contains("0x001")) {
                    System.out.println(command);
                    this.runClient();
                }
                else if (command.contains("0x002")){
                    System.out.println(command);
                    this.runClient();
                }
                else if (command.contains("0x")){
                    //Error for invalid command
                    System.out.println(command);
                    if(command.contains("38E")){
                        System.out.println("0x38E Access to local file "+ args[0] + " denied. ");
                    }
                    if(!this.sock.isClosed())
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
                    System.out.println(response);
                    this.sock.close();
                    return;
                }
                else {
                    while(response.contains("-")) {
                        System.out.println(response);
                        response = readResponse(this.br.readLine());
                    }
                    System.out.println("<-- "+response);
                    if(!this.sock.isClosed())
                        this.runClient();
                    return;
                }
            }
            catch(SocketException se){
                return;
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
