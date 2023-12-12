package org.example;

import java.util.*;
import org.eclipse.paho.client.mqttv3.*;
public class App
{
    public static Scanner scanner = new Scanner(System.in);
    
    public static void main( String[] args ) throws Exception{



         ArrayList<Bracelet> bracelets = new ArrayList<Bracelet>();
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

}
