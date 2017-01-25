//import sun.net.ftp.FtpClient;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
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
            System.out.println(this.br.readLine());
        }catch(Exception e){
            System.out.println(e);
        }
    }

    /**
     *  This function reads the response from the server as String
     *  and then acts according to the response
     * @param response
     * @return String with error code and message for that error code
     */
    private String readResponse(String response){
        if(response.contains("426") || response.contains("425")){
            return "0xFFFC Control connection to "+this.IPAddress+ " on port "+ this.PORT+" failed to open.";
        }
        if(response.contains("550")){
            return "0x38E";
        }
        else{
                return response;
//            return "0xFFFF Processing error. "+response;
        }
    }

    /**
     * This method runs indefinitely until user enters QUIT
     */
    private void runClient(){
        System.out.println("Running client");
        while(true) {
            try {
                System.out.print("csftp> ");
                String userInput = this.userInputBR.readLine();
                if ("quit".contentEquals(userInput)) {
                    this.out.println("QUIT");
                    this.sock.close();
                    return;
                }
                this.out.println(userInput);
                String response = readResponse(this.br.readLine());
                if (response.contains("0x")) {
                    System.out.println(response);
                    break;
                }
                else {
                    System.out.println(response);
                }
            } catch (Exception e) {
                System.out.println(e);
                return;
            }
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
//                    Probably cannot use this library... but lets check
//                    FtpClient test = FtpClient.create();
//                    test.enablePassiveMode(true);
            conn.runClient();
            System.out.println(IPAddress + Port);
        }
    }
}
