package poly.edu.controller.user;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import jakarta.servlet.http.HttpSession;
import poly.edu.DTO.DonHangDTO;
import poly.edu.entity.CartItemRequest;
import poly.edu.entity.CartItemResponse;
import poly.edu.entity.HoaDon;
import poly.edu.entity.SanPham;
import poly.edu.entity.Users;
import poly.edu.service.GioHangChiTietService;
import poly.edu.service.HoaDonService;
import poly.edu.service.SanPhamService;
import poly.edu.service.UserService;

@Controller
public class CartController {
    @Autowired
    GioHangChiTietService gioHangChiTietService;
    @Autowired
    UserService userService;
    @Autowired
    SanPhamService sanPhamService;
    @Autowired
    HoaDonService hoaDonService;

    @GetMapping("/cart")
    public String cart(Model model) {
        return "user/cart";
    }

    @ResponseBody
    @PostMapping("/cartItem")
    public ResponseEntity<Object> cartItem(@RequestBody CartItemRequest cartItemRequest) {
        Map<String, String> response = new HashMap<>();
        try {
            gioHangChiTietService.add(cartItemRequest);
            response.put("message", "Đã thêm sản phẩm vào giỏ hàng thành công!");
            return ResponseEntity.status(200).body(response);
        } catch (Exception e) {
            response.put("message", "Đã có lỗi truy vấn!");
            return ResponseEntity.status(404).body(response);
        }
    }

    @ResponseBody
    @GetMapping("/cartItem")
    public ResponseEntity<Object> cartItem(@RequestParam(name = "userId") String id) {
        try {
            userService.getUserById(id);
            AtomicInteger counter = new AtomicInteger(1);
            Map<String, List<CartItemResponse>> response = new HashMap<>();
            List<CartItemResponse> cartItemResponses = gioHangChiTietService.getAllByIdUser(id).stream()
                    .filter(item -> {
                        SanPham sanPham = sanPhamService.getSanPhamById(item.getSanPham().getIdSanpham());
                        if (sanPham.getSoluong() > 0) {
                            return true;
                        } else {
                            gioHangChiTietService.delete(item.getGioHang().getIdGiohang(), sanPham.getIdSanpham());
                            return false;
                        }
                    })
                    .map(item -> new CartItemResponse(counter.getAndIncrement(), item.getGioHang().getIdGiohang(),
                            item.getSanPham().getIdSanpham(), item.getSanPham().getTenSanpham(),
                            item.getSanPham().getGia(), item.getSanPham().getGiamgia(), item.getSanPham().getSoluong(),
                            item.getSanPham().getHinh(), item.getSoluong()))
                    .collect(Collectors.toList());
            response.put("cartItems", cartItemResponses);
            return ResponseEntity.status(200).body(response);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "Đã có lỗi truy vấn!");
            return ResponseEntity.status(404).body(response);
        }
    }

    @ResponseBody
    @PutMapping("/cartItem")
    public ResponseEntity<Object> cartItem(@RequestBody CartItemRequest cartItemRequest,
            @RequestParam(name = "cartItemId") Integer cartItemId) {
        Map<String, String> response = new HashMap<>();
        try {
            gioHangChiTietService.update(cartItemRequest, cartItemId);
            response.put("message", "Đã cập nhật số lượng của sản phẩm thành công!");
            return ResponseEntity.status(200).body(response);
        } catch (Exception e) {
            e.printStackTrace();
            response.put("message", "Đã có lỗi truy vấn!");
            return ResponseEntity.status(404).body(response);
        }
    }

    @ResponseBody
    @DeleteMapping("/cartItem")
    public ResponseEntity<Object> cartItem(@RequestParam(name = "cartItemId") Integer cartItemId,
            @RequestParam(name = "productId") Integer productId) {
        Map<String, String> response = new HashMap<>();
        try {
            gioHangChiTietService.delete(cartItemId, productId);
            response.put("message", "Đã cập nhật số lượng của sản phẩm thành công!");
            return ResponseEntity.status(200).body(response);
        } catch (Exception e) {
            response.put("message", "Đã có lỗi truy vấn!");
            return ResponseEntity.status(404).body(response);
        }
    }

    @PostMapping("/checkout")
    public ResponseEntity<?> checkout(
            @RequestParam(name = "userId") String userId,
            @RequestParam(name = "address") String address,
            @RequestParam(name = "deliveryMethod") Integer deliveryMethod) {
        try {
            if (deliveryMethod != 1 && deliveryMethod != 2) {
                Map<String, String> response = new HashMap<>();
                response.put("message", "Hình thức giao hàng không hợp lệ!");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }

            System.out.println("User ID: " + userId);
            System.out.println("Address: " + address);
            System.out.println("Delivery Method: " + deliveryMethod);
            gioHangChiTietService.checkout(userId, address, deliveryMethod);

            Map<String, String> response = new HashMap<>();
            response.put("message", "Đặt hàng thành công!");
            return ResponseEntity.status(HttpStatus.FOUND)
                    .header("Location", "/cart")
                    .body(response);
        } catch (RuntimeException e) {
            System.out.println("RuntimeException: " + e.getMessage());
            Map<String, String> response = new HashMap<>();
            response.put("message", "Đặt hàng thất bại: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        } catch (Exception e) {
            System.out.println("Exception: " + e.getMessage());
            Map<String, String> response = new HashMap<>();
            response.put("message", "Đã có lỗi truy vấn! Chi tiết: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @ResponseBody
    @GetMapping("/orders")
    public ResponseEntity<Object> getOrders(
            @RequestParam(name = "userId") String userId,
            @RequestParam(name = "trangthai", required = false) String trangthai,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size) {
        try {
            System.out.println("Fetching orders for userId: " + userId + ", trangthai: " + trangthai);
            List<DonHangDTO> allOrders = hoaDonService.getDonHangByUserId(userId);

            // Lọc theo trạng thái nếu có
            if (trangthai != null && !trangthai.isEmpty()) {
                allOrders = allOrders.stream()
                        .filter(order -> trangthai.equals(order.getTrangThai()))
                        .collect(Collectors.toList());
            }

            // Phân trang thủ công
            Pageable pageable = PageRequest.of(page, size);
            int start = (int) pageable.getOffset();
            int end = Math.min((start + pageable.getPageSize()), allOrders.size());
            List<DonHangDTO> pagedOrders = start < allOrders.size() ? allOrders.subList(start, end) : new ArrayList<>();
            Page<DonHangDTO> orderPage = new PageImpl<>(pagedOrders, pageable, allOrders.size());

            Map<String, Object> response = new HashMap<>();
            response.put("orders", orderPage.getContent());
            response.put("currentPage", orderPage.getNumber());
            response.put("totalPages", orderPage.getTotalPages());
            response.put("totalItems", orderPage.getTotalElements());

            System.out.println("Orders found: " + orderPage.getTotalElements());
            return ResponseEntity.status(200).body(response);
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, String> response = new HashMap<>();
            response.put("message", "Đã có lỗi truy vấn! Chi tiết: " + e.getMessage());
            return ResponseEntity.status(404).body(response);
        }
    }

    @GetMapping("/user/orders")
    public String orders(Model model, HttpSession session) {
        Users currentUser = (Users) session.getAttribute("currentUser");
        if (currentUser == null) {
            return "redirect:/signin";
        }
        model.addAttribute("userId", currentUser.getIdUser());
        return "user/orders";
    }

    @ResponseBody
    @GetMapping("/order-details")
    public ResponseEntity<Object> getOrderDetails(@RequestParam(name = "orderId") Integer orderId) {
        try {
            HoaDon hoaDon = hoaDonService.getHoaDonWithDetailsById(orderId);
            Map<String, Object> response = new HashMap<>();
            response.put("order", hoaDon);
            response.put("orderDetails", hoaDon.getHoaDonChiTiets());
            return ResponseEntity.status(200).body(response);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "Đã có lỗi truy vấn! Chi tiết: " + e.getMessage());
            return ResponseEntity.status(404).body(response);
        }
    }

    @GetMapping("/user/order-details")
    public String orderDetails(Model model) {
        return "user/order-details";
    }
}