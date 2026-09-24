import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
class ProductRepository {
    static String amazon(String n){return "https://www.amazon.in/s?k="+enc(n);}
    static String flipkart(String n){return "https://www.flipkart.com/search?q="+enc(n);}
    static String blinkit(String n){return "https://blinkit.com/s/?q="+enc(n);}
    static String zepto(String n){return "https://www.zeptonow.com/search?query="+enc(n);}
    static String myntra(String n){return "https://www.myntra.com/"+enc(n).replace("+","-");}
    static String meesho(String n){return "https://www.meesho.com/search?q="+enc(n);}
    static String tata(String n){return "https://www.tataneu.com/search?searchTerm="+enc(n);}
    static String enc(String s){return URLEncoder.encode(s,StandardCharsets.UTF_8);}
    static Product p(String id,String n,String c,double r,String d){return new Product(id,n,c,r,d);}
    static List<Product> seed(){
        List<Product> x=new ArrayList<>();
        x.add(p("P01","iPhone 15","Mobiles",4.6,"Apple smartphone with A16 chip and OLED display")
            .price("Amazon",65000,amazon("iPhone 15")).price("Flipkart",64000,flipkart("iPhone 15")).price("Myntra",67000,myntra("iPhone 15")).price("Meesho",62999,meesho("iPhone 15")).price("Tata Neu",65500,tata("iPhone 15")));
        x.add(p("P02","Samsung Galaxy S24","Mobiles",4.5,"Compact flagship Android smartphone")
            .price("Amazon",72000,amazon("Samsung Galaxy S24")).price("Flipkart",70000,flipkart("Samsung Galaxy S24")).price("Myntra",71500,myntra("Samsung Galaxy S24")).price("Meesho",69999,meesho("Samsung Galaxy S24")));
        x.add(p("P03","OnePlus 13R","Mobiles",4.5,"Performance focused Android smartphone")
            .price("Amazon",42999,amazon("OnePlus 13R")).price("Flipkart",41999,flipkart("OnePlus 13R")).price("Meesho",40500,meesho("OnePlus 13R")).price("Tata Neu",43000,tata("OnePlus 13R")));
        x.add(p("P04","Google Pixel 9","Mobiles",4.4,"Google smartphone with computational photography")
            .price("Amazon",74999,amazon("Google Pixel 9")).price("Flipkart",73999,flipkart("Google Pixel 9")).price("Meesho",72800,meesho("Google Pixel 9")));
        x.add(p("P05","HP Pavilion 15","Laptops",4.3,"Everyday laptop for coding and productivity")
            .price("Amazon",64990,amazon("HP Pavilion 15")).price("Flipkart",63990,flipkart("HP Pavilion 15")).price("Meesho",62500,meesho("HP Pavilion 15")).price("Tata Neu",65200,tata("HP Pavilion 15")));
        x.add(p("P06","ASUS Vivobook 15","Laptops",4.4,"Slim productivity laptop")
            .price("Amazon",57990,amazon("ASUS Vivobook 15")).price("Flipkart",56990,flipkart("ASUS Vivobook 15")).price("Myntra",58990,myntra("ASUS Vivobook 15")));
        x.add(p("P07","Lenovo IdeaPad Slim 5","Laptops",4.5,"Thin laptop for study and development")
            .price("Amazon",67990,amazon("Lenovo IdeaPad Slim 5")).price("Flipkart",66990,flipkart("Lenovo IdeaPad Slim 5")).price("Tata Neu",67490,tata("Lenovo IdeaPad Slim 5")));
        x.add(p("P08","Sony WH-1000XM5","Headphones",4.7,"Premium wireless noise cancelling headphones")
            .price("Amazon",28990,amazon("Sony WH-1000XM5")).price("Flipkart",27990,flipkart("Sony WH-1000XM5")).price("Meesho",26800,meesho("Sony WH-1000XM5")));
        x.add(p("P09","JBL Tune 770NC","Headphones",4.5,"Wireless ANC headphones")
            .price("Amazon",5999,amazon("JBL Tune 770NC")).price("Flipkart",5799,flipkart("JBL Tune 770NC")).price("Meesho",5599,meesho("JBL Tune 770NC")));
        x.add(p("P10","Nike Air Max","Shoes",4.5,"Lifestyle running-inspired shoes")
            .price("Amazon",8495,amazon("Nike Air Max")).price("Flipkart",7999,flipkart("Nike Air Max")).price("Myntra",7499,myntra("Nike Air Max")).price("Meesho",6999,meesho("Nike Air Max")));
        x.add(p("P11","Adidas Grand Court","Shoes",4.4,"Classic casual sneakers")
            .price("Amazon",4999,amazon("Adidas Grand Court")).price("Flipkart",4699,flipkart("Adidas Grand Court")).price("Myntra",4499,myntra("Adidas Grand Court")).price("Meesho",4299,meesho("Adidas Grand Court")));
        x.add(p("P12","Puma Running Shoes","Shoes",4.3,"Lightweight sports running shoes")
            .price("Amazon",3999,amazon("Puma Running Shoes")).price("Flipkart",3799,flipkart("Puma Running Shoes")).price("Myntra",3599,myntra("Puma Running Shoes")));
        x.add(p("P13","Levi's 511 Jeans","Fashion",4.4,"Slim fit denim jeans")
            .price("Amazon",2999,amazon("Levis 511 Jeans")).price("Flipkart",2899,flipkart("Levis 511 Jeans")).price("Myntra",2499,myntra("Levis 511 Jeans")).price("Meesho",2399,meesho("Levis 511 Jeans")));
        x.add(p("P14","Roadster T-Shirt","Fashion",4.2,"Casual cotton t-shirt")
            .price("Amazon",699,amazon("Roadster T Shirt")).price("Flipkart",649,flipkart("Roadster T Shirt")).price("Myntra",599,myntra("Roadster T Shirt")).price("Meesho",499,meesho("Roadster T Shirt")));
        x.add(p("P15","Coca Cola 750ml","Groceries",4.6,"Chilled soft drink listing")
            .price("Amazon",45,amazon("Coca Cola 750ml")).price("Flipkart",44,flipkart("Coca Cola 750ml")).price("Blinkit",42,blinkit("Coca Cola 750ml")).price("Zepto",40,zepto("Coca Cola 750ml")));
        x.add(p("P16","Lay's Classic 50g","Groceries",4.5,"Potato chips snack")
            .price("Amazon",30,amazon("Lays Classic 50g")).price("Flipkart",30,flipkart("Lays Classic 50g")).price("Blinkit",28,blinkit("Lays Classic 50g")).price("Zepto",27,zepto("Lays Classic 50g")).price("Meesho",29,meesho("Lays Classic 50g")));
        x.add(p("P17","Tata Salt 1kg","Groceries",4.6,"Iodized salt")
            .price("Amazon",30,amazon("Tata Salt 1kg")).price("Flipkart",29,flipkart("Tata Salt 1kg")).price("Blinkit",28,blinkit("Tata Salt 1kg")).price("Zepto",27,zepto("Tata Salt 1kg")));
        x.add(p("P18","Aashirvaad Atta 5kg","Groceries",4.7,"Whole wheat flour")
            .price("Amazon",299,amazon("Aashirvaad Atta 5kg")).price("Flipkart",295,flipkart("Aashirvaad Atta 5kg")).price("Blinkit",289,blinkit("Aashirvaad Atta 5kg")).price("Zepto",285,zepto("Aashirvaad Atta 5kg")));
        x.add(p("P19","Dove Shampoo 650ml","Personal Care",4.5,"Daily care shampoo")
            .price("Amazon",499,amazon("Dove Shampoo 650ml")).price("Flipkart",489,flipkart("Dove Shampoo 650ml")).price("Blinkit",479,blinkit("Dove Shampoo 650ml")).price("Zepto",475,zepto("Dove Shampoo 650ml")).price("Meesho",469,meesho("Dove Shampoo 650ml")));
        x.add(p("P20","Nivea Men Face Wash","Personal Care",4.4,"Daily facial cleanser")
            .price("Amazon",249,amazon("Nivea Men Face Wash")).price("Flipkart",239,flipkart("Nivea Men Face Wash")).price("Blinkit",229,blinkit("Nivea Men Face Wash")).price("Zepto",225,zepto("Nivea Men Face Wash")));
        x.add(p("P21","Milton Thermosteel Bottle","Home",4.6,"Insulated stainless steel bottle")
            .price("Amazon",999,amazon("Milton Thermosteel Bottle")).price("Flipkart",949,flipkart("Milton Thermosteel Bottle")).price("Myntra",929,myntra("Milton Thermosteel Bottle")).price("Meesho",899,meesho("Milton Thermosteel Bottle")));
        x.add(p("P22","Prestige Electric Kettle","Home",4.4,"Electric kettle for quick boiling")
            .price("Amazon",1499,amazon("Prestige Electric Kettle")).price("Flipkart",1399,flipkart("Prestige Electric Kettle")).price("Meesho",1299,meesho("Prestige Electric Kettle")).price("Tata Neu",1450,tata("Prestige Electric Kettle")));
        x.add(p("P23","Logitech K380 Keyboard","Electronics",4.6,"Compact wireless keyboard")
            .price("Amazon",3295,amazon("Logitech K380 Keyboard")).price("Flipkart",3199,flipkart("Logitech K380 Keyboard")).price("Meesho",3099,meesho("Logitech K380 Keyboard")));
        x.add(p("P24","boAt Airdopes 141","Electronics",4.3,"True wireless earbuds")
            .price("Amazon",1299,amazon("boAt Airdopes 141")).price("Flipkart",1199,flipkart("boAt Airdopes 141")).price("Meesho",1099,meesho("boAt Airdopes 141")));
        return x;
    }
}
