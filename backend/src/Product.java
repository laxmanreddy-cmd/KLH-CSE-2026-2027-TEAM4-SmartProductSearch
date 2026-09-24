import java.util.*;
import java.util.stream.Collectors;
class Product {
    String id, name, category, description;
    double rating;
    List<PlatformPrice> prices = new ArrayList<>();
    Product(String id,String name,String category,double rating,String description){
        this.id=id;this.name=name;this.category=category;this.rating=rating;this.description=description;
    }
    Product price(String platform,int amount,String url){ prices.add(new PlatformPrice(platform,amount,url)); return this; }
    boolean supportedPlatform(String platform){ return "Amazon".equals(platform) || "Flipkart".equals(platform); }
    int minPrice(){ return prices.stream().filter(x->supportedPlatform(x.platform)).mapToInt(x->x.price).min().orElse(Integer.MAX_VALUE); }
    String toJson(){
        String ps=prices.stream().filter(x->supportedPlatform(x.platform)).map(x->"{\"platform\":"+Main.quote(x.platform)+",\"price\":"+x.price+",\"url\":"+Main.quote(x.url)+"}")
                .collect(Collectors.joining(","));
        String links="{\"Amazon\":"+Main.quote(ProductRepository.amazon(name))+
            ",\"Flipkart\":"+Main.quote(ProductRepository.flipkart(name))+
            "}";
        return "{\"id\":"+Main.quote(id)+",\"name\":"+Main.quote(name)+",\"category\":"+Main.quote(category)+",\"rating\":"+rating+
               ",\"description\":"+Main.quote(description)+",\"minPrice\":"+minPrice()+",\"prices\":["+ps+"],\"links\":"+links+"}";
    }
}
class PlatformPrice {
    String platform,url; int price;
    PlatformPrice(String platform,int price,String url){this.platform=platform;this.price=price;this.url=url;}
}
