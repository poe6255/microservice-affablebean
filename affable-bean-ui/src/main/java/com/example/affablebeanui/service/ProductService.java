package com.example.affablebeanui.service;

import com.example.affablebeanui.entity.Product;
import com.example.affablebeanui.entity.Products;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProductService {
    public static final int DELIVERY_CHARGE = 3;
    /*  @Value("${backend.url}")*/
   /* private String baseUrl;*/
    private List<Product> products;

    private  final   CartService cartService;

    record  TransferData(String from_email,String to_email,double amount){}


    public ResponseEntity transfer(String from_email,String to_email,double amount){
        var data =new TransferData(from_email, to_email, amount + DELIVERY_CHARGE);
       return restTemplate.postForEntity("http://localhost:8090/account/transfer",data, String.class);
    }

    public  ResponseEntity  saveCartItem(){
        return  restTemplate.getForEntity("http://localhost:9000/transport/cart/save", String.class);
    }
    private RestTemplate restTemplate=new RestTemplate();
    public ProductService( final  CartService cartService){
        this.cartService=cartService;
        var productsResponseEntity=restTemplate
                .getForEntity("http://localhost:8095/backend/products"
                        ,Products.class);
        if(productsResponseEntity.getStatusCode().is2xxSuccessful()){
            products=productsResponseEntity.getBody().getProducts();
            return;
        }
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
    }
    public List<Product> showAllProducts(){
        return products;
    }
  public  Product  purchaseProduct(int id){
      Product product=findProduct(id);

        cartService.addToCart(product);
        return product;
  }
   private Product findProduct(int id){
        return  products.stream().filter(product ->  product.getId()==id)
                .findAny()
                .orElseThrow(()-> new ResponseStatusException(HttpStatus.NOT_FOUND));
   }


    public  List<Product> findProductByCategory(int categoryId){
        return products.stream()
        .filter(p -> p.getCategory().getId() ==categoryId)
                .collect(Collectors.toList());
    }

}
