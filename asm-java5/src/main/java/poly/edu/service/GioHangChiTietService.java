package poly.edu.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import poly.edu.entity.CartItemRequest;
import poly.edu.entity.GioHang;
import poly.edu.entity.GioHangChiTiet;
import poly.edu.entity.GioHangChiTietId;
import poly.edu.entity.SanPham;
import poly.edu.repository.GioHangChiTietRepository;
import poly.edu.repository.GioHangRepository;
import poly.edu.repository.UsersRepository;

@Service
public class GioHangChiTietService {
    @Autowired
    UsersRepository usersRepository;
    @Autowired
    SanPhamService sanPhamService;
    @Autowired
    CartService cartService;
    @Autowired
    GioHangRepository gioHangRepository;
    @Autowired
    GioHangChiTietRepository gioHangChiTietRepository;

    // 🛒 Thêm sản phẩm vào giỏ hàng với kiểm tra tồn kho
    public void add(CartItemRequest itemRequest) throws IllegalArgumentException {
        GioHang gioHang = gioHangRepository.findByUsers_IdUser(itemRequest.getUserId());
        SanPham sanPham = sanPhamService.getSanPhamById(itemRequest.getProductId());

        Optional<GioHangChiTiet> gioHangChiTietOptional = gioHangChiTietRepository
                .findById(new GioHangChiTietId(gioHang.getIdGiohang(), sanPham.getIdSanpham()));

        int currentQuantity = gioHangChiTietOptional.map(GioHangChiTiet::getSoluong).orElse(0);
        int newQuantity = currentQuantity + itemRequest.getQuantity();

        // 🔴 Kiểm tra nếu số lượng yêu cầu lớn hơn tồn kho
        if (newQuantity > sanPham.getSoluong()) {
            throw new IllegalArgumentException("Số lượng sản phẩm trong kho không đủ!");
        }

        if (gioHangChiTietOptional.isPresent()) {
            GioHangChiTiet gioHangChiTiet = gioHangChiTietOptional.get();
            gioHangChiTiet.setSoluong(newQuantity);
            gioHangChiTietRepository.save(gioHangChiTiet);
        } else {
            GioHangChiTiet gioHangChiTiet = new GioHangChiTiet(
                    new GioHangChiTietId(gioHang.getIdGiohang(), sanPham.getIdSanpham()), gioHang, sanPham,
                    itemRequest.getQuantity());
            gioHangChiTietRepository.save(gioHangChiTiet);
        }
    }

    // 🛒 Cập nhật số lượng sản phẩm trong giỏ hàng với kiểm tra tồn kho
    public void update(CartItemRequest itemRequest, Integer cartId) throws IllegalArgumentException {
        GioHang gioHang = cartService.getCartById(cartId);
        SanPham sanPham = sanPhamService.getSanPhamById(itemRequest.getProductId());

        GioHangChiTiet gioHangChiTiet = gioHangChiTietRepository
                .findById(new GioHangChiTietId(gioHang.getIdGiohang(), sanPham.getIdSanpham()))
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm trong giỏ hàng"));

        // 🔴 Kiểm tra nếu số lượng yêu cầu lớn hơn tồn kho
        if (itemRequest.getQuantity() > sanPham.getSoluong()) {
            throw new IllegalArgumentException("Số lượng sản phẩm trong kho không đủ!");
        }

        gioHangChiTiet.setSoluong(itemRequest.getQuantity());
        gioHangChiTietRepository.save(gioHangChiTiet);
    }

    // ❌ Xóa sản phẩm khỏi giỏ hàng
    public void delete(Integer cartId, Integer productId) throws IllegalArgumentException {
        GioHang gioHang = cartService.getCartById(cartId);
        SanPham sanPham = sanPhamService.getSanPhamById(productId);

        GioHangChiTiet gioHangChiTiet = gioHangChiTietRepository
                .findById(new GioHangChiTietId(gioHang.getIdGiohang(), sanPham.getIdSanpham()))
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm trong giỏ hàng"));

        gioHangChiTietRepository.delete(gioHangChiTiet);
    }

    // 🔎 Lấy danh sách sản phẩm trong giỏ hàng của user
    public List<GioHangChiTiet> getAllByIdUser(String id) {
        return gioHangChiTietRepository.findByGioHang_Users_IdUser(id);
    }
}
