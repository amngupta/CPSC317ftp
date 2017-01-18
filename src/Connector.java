/**
 * Created by Aman Gupta on 1/17/17.
 */
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
            }
            System.out.println(IPAddress);
        }
    }
}
