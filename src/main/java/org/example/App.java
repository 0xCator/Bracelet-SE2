package org.example;

import java.util.*;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import org.json.JSONObject;

import java.io.OutputStream;
import java.io.InputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.io.FileInputStream;
import java.io.ObjectInputStream;


public class App
{
    public static Scanner scanner = new Scanner(System.in);
    public static ArrayList<Bracelet> bracelets = new ArrayList<Bracelet>();

    public static void main( String[] args ) throws Exception{

        //server 
        HttpServer server = HttpServer.create(new InetSocketAddress(1337), 0);
        server.createContext("/api/unpair", new braceletHandler());

        server.setExecutor(null);
        server.start();


        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            serializeObject(bracelets, "bracelets.ser");
        }));


        // bracelet side
        bracelets = deSerializeObject("bracelets.ser");
        if(bracelets.size() > 0){
            for (Bracelet bracelet : bracelets) {
                new Thread(bracelet).start();
            }
        }
        Bracelet bracelet;
        System.out.print("\033[H\033[2J");
        System.out.println("===Bracelet manager===");
        System.out.println("1. Create new bracelet");
        System.out.println("2. Show all bracelets");
        System.out.println("3. Exit");
        System.out.println("Enter your choice: ");
        int choice = scanner.nextInt();

        while (true) {
            switch (choice) {
                case 1:
                    System.out.print("\033[H\033[2J");
                    System.out.println("Enter patient token");
                    System.out.println("Type 'exit' to exit");
                    String name = scanner.next();
                    if(name.equals("exit"))
                        break;
                    bracelet = new Bracelet(name);
                    bracelets.add(bracelet);
                    new Thread(bracelet).start();
                    break;
                case 2:
                    braceletsMenu(bracelets);
                    break;
                case 3:
                    serializeObject(bracelets, "bracelets.ser");
                    System.exit(0);
                default:
                    System.out.println("Invalid choice");
                    break;
            }
            System.out.print("\033[H\033[2J");
            System.out.println("===Bracelet manager===");
            System.out.println("1. Create new bracelet");
            System.out.println("2. Show all bracelets");
            System.out.println("3. Exit");
            choice = scanner.nextInt();
        }

    }
    public static void serializeObject(ArrayList<Bracelet> bracelets, String path){
        try(FileOutputStream fileOut = new FileOutputStream(path); ObjectOutputStream out = new ObjectOutputStream(fileOut)){
            out.writeObject(bracelets);
        }catch(Exception i){
            i.printStackTrace();
        }
    }
    public static ArrayList<Bracelet> deSerializeObject(String path){
        try(FileInputStream fileIn = new FileInputStream(path); ObjectInputStream in = new ObjectInputStream(fileIn)){
            if(fileIn.available() == 0)
                return new ArrayList<Bracelet>();
            return (ArrayList<Bracelet>) in.readObject();
        }catch(Exception i){
            i.printStackTrace();
        }
        return new ArrayList<Bracelet>();
    }

    public static void braceletsMenu(ArrayList<Bracelet> bracelets){
        while(true){
            System.out.print("\033[H\033[2J");
            System.out.println("===Bracelets===");
            for (int i = 0; i <= bracelets.size(); i++) {
                if(i == bracelets.size())
                    System.out.println((i+1) + ". Exit");
                else
                    System.out.println((i+1) + ". " + bracelets.get(i).getPatientToken());
            }
            System.out.println("Enter your choice: ");
            int choice = scanner.nextInt();
            scanner.nextLine();

            if(choice -1 == bracelets.size())
                break;
            else if(choice - 1 < bracelets.size()){
                braceletMenu(bracelets.get(choice -1));
            }
        }

    }

    public static void braceletMenu(Bracelet bracelet){
        while(true){
            System.out.print("\033[H\033[2J");
            System.out.println("===Bracelet===");
            System.out.println("1. Show bracelet info");
            System.out.println("2. Change bracelet state");
            System.out.println("3. Exit");
            System.out.println("Enter your choice: ");
            int choice = scanner.nextInt();
            scanner.nextLine();
            switch (choice) {
                case 1:
                    System.out.print("\033[H\033[2J");
                    System.out.println("Token: " + bracelet.getPatientToken());
                    System.out.println("Age: " + bracelet.getAge());
                    System.out.println("State: " + bracelet.getState());
                    System.out.println("Press any key to continue");
                    scanner.nextLine();
                    break;
                case 2:
                    stateMenu(bracelet);
                    break;
                case 3:
                    return;
                default:
                    System.out.println("Invalid choice");
                    break;
            }
        }
    }

    public static void stateMenu(Bracelet bracelet){
        while(true){
            System.out.print("\033[H\033[2J");
            System.out.println("===State===");
            System.out.println("1. Normal");
            System.out.println("2. Warning");
            System.out.println("3. Critical");
            System.out.println("4. Exit");
            System.out.println("Enter new state: ");
            int state = scanner.nextInt();
            scanner.nextLine();
            switch (state) {
                case 1:
                    bracelet.setState(State.NORMAL);
                    break;
                case 2:
                    bracelet.setState(State.WARNING);
                    break;
                case 3:
                    bracelet.setState(State.CRITICAL);
                    break;
                case 4:
                    return;
                default:
                    System.out.println("Invalid choice");
                    break;
            }
        }
    }

static class braceletHandler implements HttpHandler {
    @Override
    public void handle(HttpExchange t) throws IOException {
        String reqestMethod = t.getRequestMethod();
        if(reqestMethod.equals("POST")){
            InputStream requestBody = t.getRequestBody();
            byte[] bytes = requestBody.readAllBytes();
            JSONObject body;
            try{
                body = new JSONObject(new String(bytes));
                String braceletId = body.getString("braceletId");
                for (Bracelet bracelet : bracelets) {
                    if(bracelet.getBraceletId().equals(braceletId)){
                        bracelet.setPatientToken(null);
                        bracelets.remove(bracelet);
                        break;
                    }
                }
            }catch(Exception e){
                t.sendResponseHeaders(400, 0);
                t.getResponseBody().close();
            }
            t.sendResponseHeaders(200,0);
            t.getResponseBody().close();
        }else{
            t.sendResponseHeaders(405, 0);
            t.getResponseBody().close();
        }
    }
}

}
