
package com.example.affablebeanui.controller;

import com.example.affablebeanui.entity.Product;
import com.example.affablebeanui.model.CartItem;
import com.example.affablebeanui.service.CartService;
import com.example.affablebeanui.service.ProductService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.http.HttpRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
@RequestMapping("/ui")
public class ProductController {
    private final ProductService productService;
    private final CartService cartService;

    @QueryMapping
    public  List<CartItem> cartItems(){
        return cartService.getAllProducts()
                .stream()
                .map(p -> new CartItem(p.getId(),
                        p.getName()
                        ,p.getPrice()
                        ,p.getDescription()
                        ,p.getQuantity()
                         ,p.getLastUpdate())).collect(Collectors.toList());
    }


      @GetMapping("/transfer")
    public  String checkoutTransfer(@ModelAttribute("total") double total, RedirectAttributes redirectAttributes){
       ResponseEntity responseEntity=
               productService.transfer("poe@gmail.com","thae@gmail.com",total);
        if (responseEntity.getStatusCode().is2xxSuccessful()){
            redirectAttributes.addFlashAttribute("transfer",true);
            return "redirect:/";
        }
        redirectAttributes.addFlashAttribute("transfer-error",true);
        return  "redirect:/ui/checkout-view";
    }
    @GetMapping("/transport")
   public  String transport(){
        ResponseEntity responseEntity=productService
                .saveCartItem();
        if (responseEntity.getStatusCode().is2xxSuccessful()){
            return "redirect:/";
        }
        return "redirect:/ui/checkout-view";
   }
    @GetMapping("/products/{id}")
    @ResponseBody
    public List<Product> showAll(@PathVariable int id){
        return productService.findProductByCategory(id);
    }
    @GetMapping("/product/category")
    public  String showProdut(@RequestParam("id") int id, Model model){
        model.addAttribute("products",productService.findProductByCategory(id));
        return "products";
    }
  @GetMapping("/product/purchase")
    public  String addtoCard(@RequestParam("id")int id){
      Product product =   productService.purchaseProduct(id);
        return "redirect:/ui/product/category?id=" + product.getCategory().getId();
    }
    @GetMapping ("/")
    public  String home(Model model, HttpServletRequest request){
        boolean transfer = Boolean.valueOf(request.getParameter("transfer"));
        model.addAttribute("transfer",transfer);
        return "home";
    }

    @GetMapping("/product/cartView")
    public  String viewCart(Model model){
        model.addAttribute("cartItems",cartService.getAllProducts());
        model.addAttribute("product",new Product());
        return "cartView";
    }
    @PostMapping("/checkout")
    public  String checkout(Product product){
        System.out.println("++++++++++++++++++++" + product.getQuantityList());
        int i=0;
        for (Product cartItem:cartService.getAllProducts()){
            cartItem.setQuantity(product.getQuantityList().get(i));
            i++;
        }
        cartService.getAllProducts().forEach(
                System.out::println
        );
        return  "redirect:/ui/checkout-view";
    }



    @GetMapping("/checkout-view")
    public  String toCheckOutView(Model model){
          model.addAttribute("transfererror",
                  model.containsAttribute("transfererror"));
        return "checkoutView";
    }
    @ModelAttribute("total")
    public  double toalAmount(){
        return  cartService.getAllProducts()
                .stream()
                .map(p-> p.getQuantity() * p.getPrice())
                .mapToDouble(i ->i)
                .sum();
    }

  @ModelAttribute("cartSize")
    public int cartSize(){
        return  cartService.cartSize();
    }
    @GetMapping("/clear")
    public  String  clearcart(){
        cartService.clearCart();
        return  "redirect:/ui/";
    }


}
