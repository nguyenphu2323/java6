package poly.edu.service;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import poly.edu.entity.CartItemRequest;
import poly.edu.entity.GioHang;
import poly.edu.entity.GioHangChiTiet;
import poly.edu.entity.GioHangChiTietId;
import poly.edu.entity.HoaDon;
import poly.edu.entity.HoaDonChiTiet;
import poly.edu.entity.HoaDonChiTietId;
import poly.edu.entity.SanPham;
import poly.edu.repository.GioHangChiTietRepository;
import poly.edu.repository.GioHangRepository;
import poly.edu.repository.HoaDonChiTietRepository;
import poly.edu.repository.HoaDonRepository;
import poly.edu.repository.UsersRepository;

@Service
public class GioHangChiTietService {

    private final HoaDonChiTietRepository hoaDonChiTietRepository;
    private final HoaDonRepository hoaDonRepository;

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

    GioHangChiTietService(HoaDonRepository hoaDonRepository, HoaDonChiTietRepository hoaDonChiTietRepository) {
        this.hoaDonRepository = hoaDonRepository;
        this.hoaDonChiTietRepository = hoaDonChiTietRepository;
    }

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
///////thanh toán
    public void checkout(String userId, String address) {
        // Lấy giỏ hàng của người dùng
        List<GioHangChiTiet> cartItems = gioHangChiTietRepository.findByGioHang_Users_IdUser(userId);
        if (cartItems.isEmpty()) {
            throw new RuntimeException("Giỏ hàng trống!");
        }

        // Tạo hóa đơn mới
        HoaDon hoaDon = new HoaDon();
        hoaDon.setUsers(usersRepository.findById(userId).orElseThrow(() -> new RuntimeException("Người dùng không tồn tại")));
        hoaDon.setNgaytao(new Date());
        hoaDon.setTrangthai("Đã đặt hàng ");
        hoaDon.setDiachi(address); // Thay đổi địa chỉ giao hàng nếu cần
        hoaDon.setGiaohang("Chưa giao hàng");

        // Lưu hóa đơn vào cơ sở dữ liệu
        hoaDon = hoaDonRepository.save(hoaDon);

        // Lưu từng sản phẩm trong giỏ hàng vào hóa đơn chi tiết
        for (GioHangChiTiet item : cartItems) {
            HoaDonChiTiet hoaDonChiTiet = new HoaDonChiTiet();

            // Tạo composite key
            HoaDonChiTietId id = new HoaDonChiTietId();
            id.setIdHoadon(hoaDon.getIdHoadon()); // ID của hóa đơn vừa tạo
            id.setIdSanpham(item.getSanPham().getIdSanpham()); // ID của sản phẩm

            // Gán composite key vào HoaDonChiTiet
            hoaDonChiTiet.setId(id);

            // Gán các trường khác
            hoaDonChiTiet.setHoaDon(hoaDon);
            hoaDonChiTiet.setSanPham(item.getSanPham());
            hoaDonChiTiet.setSoluong(item.getSoluong());

            // Lưu HoaDonChiTiet
            hoaDonChiTietRepository.save(hoaDonChiTiet);
        }

        // Xóa giỏ hàng sau khi thanh toán thành công
        gioHangChiTietRepository.deleteAll(cartItems);
    }
}