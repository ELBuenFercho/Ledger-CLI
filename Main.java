import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;


public class Main {
    public static final Map<String, Runnable> COMMANDS;
    public static final Map<String, String> OPTIONS;
    public static HashMap<String,Double> currencies;

    static {
        COMMANDS = new HashMap<>();
        COMMANDS.put("balance", Main::printBalance);
        COMMANDS.put("bal", Main::printBalance);
        COMMANDS.put("register", Main::printRegister);
        COMMANDS.put("reg", Main::printRegister);
        COMMANDS.put("print", Main::printPrint);
        OPTIONS = new HashMap<>();
    }
    public static class Transaction{
        public String date="";
        public String description="";
        public String fromMainAccount="";
        public String fromSubAccount="";
        public String toMainAccount="";
        public String toSubAccount="";
        public Double fromAmount=0.0;
        public Double toAmount=0.0;
        }
    public static class transInfo{
        public static ArrayList<Transaction> read(File file){
            ArrayList<Transaction> t=new ArrayList<Transaction>();
            Scanner sc = null;

            try {
                sc = new Scanner(new FileReader(file));
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            }
            while (true) {
                String line = sc.nextLine();
                if (line.isEmpty()) {
                    continue;
                }
                Transaction trans=new Transaction();
                //Split line of text like ["Asset:Savings",123.00]
                String[] splitLine = line.trim().split("[ \\t]+");
                trans.date=splitLine[0];
                trans.description=splitLine[1];

                line=sc.nextLine();
                splitLine = line.trim().split("[ \\t]+");
                trans.fromMainAccount=splitLine[0].split(":")[0];
                trans.fromSubAccount=splitLine[0].split(":")[1];
                if(splitLine.length==1){trans.fromAmount=0.0;}
                else{trans.fromAmount=Double.parseDouble(splitLine[1]);}

                line=sc.nextLine();
                splitLine = line.trim().split("[ \\t]+");
                trans.toMainAccount=splitLine[0].split(":")[0];
                trans.toSubAccount=splitLine[0].split(":")[1];
                if(splitLine.length==1){trans.toAmount=0.0;}
                else{trans.toAmount=Double.parseDouble(splitLine[1]);}

                t.add(trans);

                if(!sc.hasNextLine()){
                    break;
                }

            }
            return t;
        }
    }
    public static class currency{
        public static HashMap<String,Double> money (File file){
            HashMap<String,Double> curr=new HashMap<>();
            Scanner sc = null;
            try {
                sc = new Scanner(new FileReader(file));
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            }
            while(true) {
                String line = sc.nextLine();
                if (line.isEmpty()) {
                    continue;
                }
                String[] splitLine = line.trim().split("[ \\t]+");
                curr.put(splitLine[2],Double.parseDouble(splitLine[3].split("\\$")[1]));
                if(!sc.hasNextLine()){
                    break;
                }
            }
            return curr;
        }
    }
    public static String Path="";
    public static String dbPath="prices_db.pricedb";
    public static String coin;
    public static Boolean sort=false;
    public static void main(String[] args) {
        // Parse options and arguments from command line
        Scanner sc = new Scanner(System.in);
        System.out.print("$ LedgerCLI ");
        String userInput = sc.nextLine();
        String[] inputs=userInput.split(" ");

        String command = null;
        for (String arg : inputs) {
            if (arg.startsWith("--")) {
                // Option
                int equalsIndex = arg.indexOf('=');
                if (equalsIndex == -1) {
                    // Flag option
                    OPTIONS.put(arg, null);
                } else {
                    // Value option
                    String option = arg.substring(0, equalsIndex);
                    String value = arg.substring(equalsIndex + 1);
                    OPTIONS.put(option, value);
                }
            } else {
                // Command or argument
                if (command == null) {
                    command = arg;
                } else {
                    System.err.println("Unrecognized argument: " + arg);
                    System.exit(1);
                }
            }
        }

        if (command == null) {
            // No command specified
            System.err.println("No command specified");
            System.exit(1);
        }

        // Get ledger file
        Path = OPTIONS.get("--file");


        // Get price database file
        coin = OPTIONS.get("--price-db");
        if (coin!=null){
            File db=new File(dbPath);
            currencies=currency.money(db);
        }


        // Get sort option
        sort = OPTIONS.containsKey("--sort");

        // Execute command
        Runnable commandImpl = COMMANDS.get(command);
        if (commandImpl == null) {
            // Unrecognized command
            System.err.println("Unrecognized command: " + command);
            System.exit(1);
        }
        // Execute command
        commandImpl.run();
    }
    static void printPrint () {
        //TODO: Implement printBalance method
        if(Path==null){Path= "example.ledger";}
        File file = new File(Path);
        Scanner sc = null;
        try {
            sc = new Scanner(new FileReader(file));
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
        while (sc.hasNextLine()) {
            String line = sc.nextLine();
            System.out.println(line);
        }
    }

    static void printRegister () {
        //TODO: Implement printRegister method
        File file = new File("example.ledger");
        ArrayList<Transaction> trans=transInfo.read(file);
        HashMap<String,Double> accounts=new HashMap<>();
        Double total=0.0;
        if(coin!=null){
            for(int i=0;i<trans.size();++i){
                trans.get(i).fromAmount=trans.get(i).fromAmount/currencies.get(coin);
                trans.get(i).toAmount=trans.get(i).toAmount/currencies.get(coin);
            }
        }
        for (int i = 0; i < trans.size(); i++) {
            total=0.0;
            Transaction t = trans.get(i);
            System.out.printf("%10s \t", t.date);
            System.out.printf("%10s \n", t.description);
            System.out.printf("\t\t%25s %10.5f\n", t.fromMainAccount + ":" + t.fromSubAccount, t.fromAmount);
            System.out.printf("\t\t%25s %10.5f\n", t.toMainAccount + ":" + t.toSubAccount, t.toAmount);
            if(t.fromMainAccount.equals("Assets")){total+=t.fromAmount;}
            if (t.toMainAccount.equals("Assets")) {total+=t.toAmount;}
            System.out.printf("\t\t%25s %10.5f\n","Total in Checking:",total);
            //Main Account: Assets, Expenses etc
        }
    }

    static void printBalance () {
        //TODO: Implement printTransactions method
        if(Path==null){Path= "example.ledger";}
        File file = new File(Path);
        ArrayList<Transaction> t=transInfo.read(file);
        HashMap<String,Double> accounts=new HashMap<>();

        if(coin!=null){
            for(int i=0;i<t.size();++i){
                t.get(i).fromAmount=t.get(i).fromAmount/currencies.get(coin);
                t.get(i).toAmount=t.get(i).toAmount/currencies.get(coin);
            }
        }
        for (int i = 0; i < t.size(); i++) {
            Transaction transaction=t.get(i);
            //Main Account: Assets, Expenses etc
            if(!accounts.containsKey(transaction.fromMainAccount)){
                accounts.put(transaction.fromMainAccount,0.0);
            }
            //Sub account: Car, Groceries etc
            if(!accounts.containsKey(transaction.fromSubAccount)){
                accounts.put(transaction.fromSubAccount,0.0);
            }
            accounts.put(transaction.fromMainAccount,accounts.get(transaction.fromMainAccount)+transaction.fromAmount);
            accounts.put(transaction.fromSubAccount,accounts.get(transaction.fromSubAccount)+transaction.fromAmount);

            //Where the money is going
            if(!accounts.containsKey(transaction.toMainAccount)){
                accounts.put(transaction.toMainAccount,0.0);
            }
            //Sub account: Car, Groceries etc
            if(!accounts.containsKey(transaction.toSubAccount)){
                accounts.put(transaction.toSubAccount,0.0);
            }
            accounts.put(transaction.toMainAccount,accounts.get(transaction.toMainAccount)+transaction.toAmount);
            accounts.put(transaction.toSubAccount,accounts.get(transaction.toSubAccount)+transaction.toAmount);

        }
        System.out.println("Balance for each Account");
        for (String key : accounts.keySet()) {
            Double value = accounts.get(key);
            System.out.printf("%-15s %15.5f%n",key,value);
        }
    }
}


