import java.io.*;
import java.math.*;
import java.text.*;
import java.util.*;
import java.util.regex.*;


class Match{
    private String symbol;
    private Order buy;
    private Order sell;
    
    public Match(String symbol, Order buy, Order sell){
        this.symbol = symbol;
        this.buy = buy;
        this.sell = sell;     
    }
    
    public String generate(){
        return "";
    }
    
    
    

}

class Order implements Comparable<Order>{
    
    @Override
    public int compareTo(Order o){
        return this.symbol.compareTo(o.getSymbol());
    }

    public int getOrderID() {
        return orderID;
    }

    public int getTimestamp() {
        return timestamp;
    }

    public String getSymbol() {
        return symbol;
    }

    public OrderType getOrderType() {
        return orderType;
    }

    public Side getSide() {
        return side;
    }

    public float getPrice() {
        return price;
    }

    public int getQuantity() {
        return quantity;
    }
    
    public void setPrice(float price){
        this.price = price;
    }
    
    public void setQuantity(int quantity){
        this.quantity = quantity;
    }

    public enum OrderType {
        M, L, I
    }

    public enum Side {
        B, S
    }

    static int lastTimestamp = 0;
    public static List<String> responses = new ArrayList<>();
    public static List<Order> orders = new ArrayList<>();
    private final int orderID;
    private int timestamp;
    private String symbol;
    private OrderType orderType;
    private Side side;
    private float price;
    private int quantity;

    public Order(int orderID, int timestamp, String symbol, OrderType orderType, Side side, Float price, int quantity) {
        this.orderID = orderID;
        this.timestamp = timestamp;
        this.symbol = symbol;
        this.orderType = orderType;
        this.side = side;
        this.price = price;
        this.quantity = quantity;

    }
    
    //returns index of seeked order
    //returns -1 if doesn't exist
   private static int findOrder(int orderID){
       
       for(Order order : orders){
           if(orderID == order.getOrderID()){
               return orders.indexOf(order);
           }
       }
       return -1;
       
   }
   
    
    private static void MatchMany(String symbol){
        List<Order> toBuyMatch = new ArrayList();
        List<Order> toSellMatch = new ArrayList();
            for (Order order : orders){
                if(symbol.equals(null) || order.getSymbol().equals(symbol)){
                    if(order.side == Side.B){
                    toBuyMatch.add(order);
                    orders.remove(order);
                    }else{
                    toSellMatch.add(order);
                    orders.remove(order); 
                    }
                }
            }
        
        for(Order buy : toBuyMatch){
            for(Order sell : toSellMatch){
                
            }
        }
        
        
        
            
        
                
    }

    public static void Parse(String[] data) {
        if(data[0].equals("N") || data[0].equals("A")){
                try{
                    int orderID = Integer.parseInt(data[1]);
                    String removedZeroes = data[2].replaceFirst("^0+(?!$)", "");
                    int timeStamp = Integer.parseInt(removedZeroes);
                    String symbol = data[3];
                    float price = Float.parseFloat(data[6]);
                    
                    OrderType orderType;
                            switch (data[4]) {
                                case "M":
                                    orderType = OrderType.M;
                                    break;
                                case "L":
                                    orderType = OrderType.L;
                                    break;
                                case "I":
                                    orderType = OrderType.I;
                                    break;
                                default:
                                    orderType = null;
                                    break;
                            }
                    
                    Side side;
                                switch (data[5]) {
                                    case "B":
                                        side = Side.B;
                                        break;
                                    case "S":
                                        side = Side.S;
                                        break;
                                    default:
                                        side = null;
                                        break;
                                }
                    
                    int quantity = Integer.parseInt(data[7]);
                    if(timeStamp >= Order.lastTimestamp && symbol.matches("[a-zA-Z]+") && orderType != null && !(orderType == 
                        OrderType.M && price >= 0) && price >= 0 && side != null && quantity >= 1 &&
                       quantity <= Math.pow(2, 63) - 1 && orderID >= 1 && orderID <= Math.pow(2, 63) - 1 && quantity >= 0){
                    if(data[0].equals("N")){
                        //Command N
                        if(findOrder(orderID) == -1){
                        orders.add(new Order(orderID, timeStamp, symbol, orderType, side, price, quantity));
                        responses.add(orderID + " - Accept");
                        }else{ responses.add(data[1] + " - Reject - 303 - Invalid order details");}
                    }else if (data[0].equals("A")){
                        //Command A
                        int orderIndex = findOrder(orderID);
                        if(orderIndex >= 0){
                            Order toAmmend = orders.get(orderIndex);
                            if(toAmmend.getSymbol().equals(symbol) &&
                               toAmmend.getSide() == side && toAmmend.getOrderType() == orderType){
                                
                                toAmmend.setPrice(price);
                                toAmmend.setQuantity(quantity);
                                responses.add(data[1] + " - AmendAccept");
                                
                            }else{
                            responses.add(data[1] + " - AmendReject - 101 - Invalid amendment details");
                            }
                        }else{
                            responses.add(data[1] + " - AmendReject - 404 - Order does not exist");
                            
                        }
                        
                    }                  
                                    } else {
                        if(data[0].equals("N"))
                        responses.add(data[1] + " - Reject - 303 - Invalid order details");
                        else if(data[0].equals("A"))
                        responses.add(data[1] + " - AmendReject - 101 - Invalid amendment details");
                                    }
        }catch(Exception e){
                        if(data[0].equals("N"))
                        responses.add(data[1] + " - Reject - 303 - Invalid order details");
                        else if(data[0].equals("A"))
                        responses.add(data[1] + " - AmendReject - 101 - Invalid amendment details");
                }
        }else{
            if(data[0].equals("X")){
                //Command X
                try{
                    int orderID = Integer.parseInt(data[1]);
                    String removedZeroes = data[2].replaceFirst("^0+(?!$)", "");
                    int timeStamp = Integer.parseInt(removedZeroes);
                    int toCancel = findOrder(orderID);
                    if(toCancel >= 0 && timeStamp >= Order.lastTimestamp){
                        responses.add(orderID + " - CancelAccept");
                        orders.remove(orders.get(toCancel));
                    }else{
                        responses.add(orderID + " - CancelReject - 404 - Order does not exist");
                    }
                    
                }catch(Exception e){
                    responses.add(data[1] + " - CancelReject - 404 - Order does not exist");
                }
            }else if(data[0].equals("M")){
                //Command M
                try{
                String removedZeroes = data[1].replaceFirst("^0+(?!$)", "");
                int timeStamp = Integer.parseInt(removedZeroes);
                if(timeStamp >= Order.lastTimestamp){
                String symbol = null;
                if(data.length > 2)
                symbol = data[2];
                }
                
                
                
                    
                }catch(Exception e){}            
            }
            
            
        }


        }
}


public class Solution {
    
    


    /*
     * Complete the function below.
     */
    static String[] processQueries(String[] queries) {
        for (String query : queries) {
            String[] split = query.split(",");
            Order.Parse(split);
        }

        String[] toReturn = new String[Order.responses.size()];
        for (int i = 0; i < toReturn.length; i++) {
            toReturn[i] = Order.responses.get(i);
        }
        return toReturn;
      


    }




    private static final Scanner scan = new Scanner(System.in);

    public static void main(String[] args) throws IOException {
        BufferedWriter bw = new BufferedWriter(new FileWriter(System.getenv("OUTPUT_PATH")));
        if (bw == null) {
            bw = new BufferedWriter(new OutputStreamWriter(System.out));
        }

        int queriesSize = Integer.parseInt(scan.nextLine().trim());

        String[] queries = new String[queriesSize];

        for (int queriesItr = 0; queriesItr < queriesSize; queriesItr++) {
            String queriesItem = scan.nextLine();
            queries[queriesItr] = queriesItem;

        }

        String[] response = processQueries(queries);

        for (int responseItr = 0; responseItr < response.length; responseItr++) {
            bw.write(response[responseItr]);

            if (responseItr != response.length - 1) {
                bw.write("\n");
            }
        }

        bw.newLine();

        bw.close();
    }
}
