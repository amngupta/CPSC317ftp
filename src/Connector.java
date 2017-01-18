/**
 * Created by Aman Gupta on 1/17/17.
 */

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;


public class Connector {

    public static void main(String args[]){
        if(args.length < 1){
            System.out.println("Error, at least two commands required");
        }
        else{
            String IPAddress = args[0];
            int Port = 21;
            if(args.length == 2){
                Port = Integer.parseInt(args[1]);
                while (true) {
                    try{
                        Socket socket = new Socket(IPAddress, Port);
                        BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                        PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

                        System.out.println("server says:" + br.readLine());

                        BufferedReader userInputBR = new BufferedReader(new InputStreamReader(System.in));

                        String userInput = userInputBR.readLine();
//                        System.out.println("csftp> ");
                        out.println(userInput);

                        System.out.println("server says:" + br.readLine());

                        if ("quit".equalsIgnoreCase(userInput)) {
                            socket.close();
                            break;
                        }
                    }
                    catch(Exception e){
                        System.out.println(e);
                        break;
                    }
                }
            }
            System.out.println(IPAddress + Port);
        }
    }
}
