/**
 * Created by Aman Gupta on 1/17/17.
 */

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
* */
public class Connector {

    static void runClient(BufferedReader br, Socket socket, PrintWriter out){
        System.out.println("Running client");
        while(true) {
            try {
                BufferedReader userInputBR = new BufferedReader(new InputStreamReader(System.in));
                System.out.print("csftp> ");
                String userInput = userInputBR.readLine();
                out.println(userInput);

                System.out.println("server says:" + br.readLine());
                if ("quit".equalsIgnoreCase(userInput)) {
                    out.println("QUIT");
                    socket.close();
                    return;
                }
            } catch (Exception e) {
                System.out.println(e);
                break;
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
//            while (true) {
                try{
                    Socket socket = new Socket(IPAddress, Port);
                    BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

                    System.out.println(br.readLine());
//                    if((br.readLine()).contains("220")) {
                        runClient(br, socket, out);
//                        break;
//                    }
                }
                catch(Exception e){
                    System.out.println(e);
//                    break;
                }
//            }
            System.out.println(IPAddress + Port);
        }
    }
}
