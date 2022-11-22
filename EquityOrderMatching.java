import java.io.*;
import java.math.*;
import java.text.*;
import java.util.*;
import java.util.regex.*;


class Match implements Comparable<Match>{
    private String symbol;
    private Order buy;
    private Order sell;
    
     @Override
    public int compareTo(Match o){
        return this.symbol.compareTo(o.getSymbol());
    }
    
    public String getSymbol(){
        return this.symbol;
    }
    
    public void setBuy(Order buy){
        this.buy = buy;
    }
    
    public void setSell(Order sell){
        this.sell = sell;
    }
    
    public Match(String symbol, Order buy, Order sell){
        this.symbol = symbol;
        this.buy = buy; 
        this.sell = sell;     
    }
    
    
     public String generate(){
        StringBuilder gen = new StringBuilder();
        gen.append(symbol);
        gen.append("|");
        if(buy != null)
        gen.append(buy.getOrderID()+ "," + buy.getOrderType().name() + "," + buy.getQuantity() + "," + buy.getPrice());
        gen.append("|");
        if(sell != null)
        gen.append(sell.getPrice() + "," + sell.getQuantity() + "," + sell.getOrderType().name() + "," + sell.getOrderID());
        return gen.toString();
        
    }
    
    
    

}

class Order implements Comparable<Order>{
    
    @Override
    public int compareTo(Order o){
        if(this.symbol.equals(o.getSymbol())){
            float thisValue = this.price * this.getQuantity();
            float oValue = o.getPrice() * o.getQuantity();
            float x = thisValue = oValue;
            if(x > 0)
                return 1;
            else if (x < 0)
                return -1;
            else if (x == 0)
            {
                return 0;
            }
            
        }
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
    
    public Order(Order o) {
        this.orderID = o.getOrderID();
        this.timestamp = o.getTimestamp();
        this.symbol = o.getSymbol();
        this.orderType = o.getOrderType();
        this.side = o.getSide();
        this.price = o.getPrice();
        this.quantity = o.getQuantity();
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
   
    
    private static void Match(String symbol, int timestamp){
        List<Order> toBuyMatch = new ArrayList();
        List<Order> toSellMatch = new ArrayList();
            for (Order order : orders){
                if(order.getTimestamp() <= timestamp && (symbol == null || symbol.equals(order.getSymbol()))){
                    if(order.side == Side.B){
                    toBuyMatch.add(order);
                    }else{
                    toSellMatch.add(order);
                    }
                }
            }
        orders.removeAll(toBuyMatch);
        orders.removeAll(toSellMatch);
        
        Collections.sort(toBuyMatch, Collections.reverseOrder());
        Collections.sort(toSellMatch, Collections.reverseOrder());
        
        List<Order> matched = new ArrayList();
        
        List<Match> toReturn = new ArrayList();
        
        for(Order buy : toBuyMatch){
            List<Order> resolved = new ArrayList();
            for(Order sell : toSellMatch){
                if(buy.getSymbol().equals(sell.getSymbol())){
                    if(((buy.getOrderType() == OrderType.L || buy.getOrderType() == OrderType.I) && 
                       buy.getPrice() >= sell.getPrice()) || buy.orderType == OrderType.M) {                      
                        Match match;
                        if(buy.getQuantity() > sell.getQuantity()){
                            Order order = new Order(buy);
                            order.setQuantity(sell.getQuantity());
                            buy.setQuantity(buy.getQuantity()-sell.getQuantity());
                            order.setPrice(sell.getPrice());
                            match = new Match(buy.getSymbol(), order, sell);
                            matched.add(sell);
                            toReturn.add(match);
                            resolved.add(sell);
                        }else if(buy.getQuantity() < sell.getQuantity()){
                            Order order = new Order(sell);
                            order.setQuantity(buy.getQuantity());
                            sell.setQuantity(sell.getQuantity() - buy.getQuantity());
                            buy.setPrice(sell.getPrice());
                            match = new Match(buy.getSymbol(), buy, order);
                            matched.add(buy);
                            toReturn.add(match);
                            break;
                        }else{
                            if(buy.getPrice() != sell.getPrice())
                            buy.setPrice(sell.getPrice());
                            
                            matched.add(buy);
                            matched.add(sell);
                            match = new Match(buy.getSymbol(), buy, sell);
                            toReturn.add(match);
                            resolved.add(sell);
                            break;
                        }    
                }
                
                
            }
        }
        toSellMatch.removeAll(resolved);
    }
        
        for(Order buy : toBuyMatch){
            if(buy.getOrderType() == OrderType.I)
                matched.add(buy);
        }
        
        for(Order sell : toSellMatch){
            if(sell.getOrderType() == OrderType.I)
                matched.add(sell);
        }
        
                
        toBuyMatch.removeAll(matched);
        toSellMatch.removeAll(matched);
        orders.addAll(toBuyMatch);
        orders.addAll(toSellMatch);
        Collections.sort(toReturn);
        
        
        for(Match match : toReturn){
            responses.add(match.generate());
        }
        
        
        
        
    }
    
    public static void Query(int timestamp, String symbol){
        List<Order> toBuyMatch = new ArrayList();
        List<Order> toSellMatch = new ArrayList();
        for (Order order : orders){
                if((timestamp < 0 || order.getTimestamp() <= timestamp) && 
                   (symbol == null || symbol.equals(order.getSymbol()))){
                    if(order.side == Side.B){
                    toBuyMatch.add(order);
                    }else{
                    toSellMatch.add(order);
                    }
                }
        }
        List<Match> matches = new ArrayList();
         for(Order buy : toBuyMatch){
             Match match = new Match(buy.symbol, buy, null);
            for(Order sell : toSellMatch){
                if(buy.getSymbol().equals(sell.getSymbol()) && buy.getOrderType() == sell.getOrderType()){
                    match.setSell(sell);
                    toSellMatch.remove(sell);
                    break;
                }
            }
             matches.add(match);             
         }
        for(Order sell : toSellMatch){
            matches.add(new Match(sell.symbol, null, sell));           
        }
        
        Collections.sort(matches);
        
        for(int i = 0; i < 5; i++){
            if(i < matches.size()){
                responses.add(matches.get(i).generate());
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
                String symbol = null;
                if(data.length > 2)
                symbol = data[2];
                    
                Match(symbol, timeStamp);
                   
                }catch(Exception e){}            
            }else if(data[0].equals("Q")){
                String symbol = null;
                int timeStamp = -1;
                if(data.length> 1){
                    if(data[1].matches("[a-zA-Z]+"))
                        symbol = data[1];
                    else if(data[1].matches("[0-9]+")){
                        String removedZeroes = data[1].replaceFirst("^0+(?!$)", "");
                        timeStamp = Integer.parseInt(removedZeroes);
                    }
                    
                    if(data.length>2){
                         if(data[2].matches("[a-zA-Z]+") && symbol.equals(null))
                        symbol = data[2];
                        else if(data[2].matches("[0-9]+") && timeStamp == -1){
                           String removedZeroes = data[2].replaceFirst("^0+(?!$)", "");
                            timeStamp = Integer.parseInt(removedZeroes); 
                        }
                    } 
                    
                    
                
                }
                Query(timeStamp, symbol);
                
                
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
