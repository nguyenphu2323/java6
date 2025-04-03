package poly.edu.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import poly.edu.entity.HoaDon;
import poly.edu.entity.HoaDonChiTiet;
import poly.edu.entity.HoaDonChiTietId;
import poly.edu.entity.Users;
import poly.edu.repository.HoaDonChiTietRepository;
import poly.edu.repository.HoaDonRepository;
import poly.edu.repository.UsersRepository;

@Service
public class HoaDonService {

    @Autowired
    private HoaDonRepository hoaDonRepository;

    @Autowired
    private HoaDonChiTietRepository hoaDonChiTietRepository;

    @Autowired
    private UsersRepository usersRepository;

    // Phương thức đặt hàng
    @Transactional
    public HoaDon placeOrder(String userId, HoaDon hoaDon, List<HoaDonChiTiet> hoaDonChiTiets) {
        // Kiểm tra user có tồn tại không
        Users user = usersRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Người dùng không tồn tại"));

        // Kiểm tra giỏ hàng có sản phẩm không
        if (hoaDonChiTiets == null || hoaDonChiTiets.isEmpty()) {
            throw new RuntimeException("Giỏ hàng trống, không thể đặt hàng");
        }

        // Gán người dùng cho hóa đơn
        hoaDon.setUsers(user);

        // Lưu hóa đơn
        HoaDon savedHoaDon = hoaDonRepository.save(hoaDon);

        // Lưu chi tiết hóa đơn
        for (HoaDonChiTiet hoaDonChiTiet : hoaDonChiTiets) {
            HoaDonChiTietId id = new HoaDonChiTietId(savedHoaDon.getIdHoadon(),
                    hoaDonChiTiet.getSanPham().getIdSanpham());
            hoaDonChiTiet.setId(id);
            hoaDonChiTiet.setHoaDon(savedHoaDon);
            hoaDonChiTietRepository.save(hoaDonChiTiet);
        }

        return savedHoaDon;
    }
    // Phương thức lấy danh sách tất cả hóa đơn
 public List<HoaDon> getAllHoaDons() {
        return hoaDonRepository.findAll();
    }

    // Phương thức lấy danh sách hóa đơn theo trang
    public Page<HoaDon> getHoaDonsByPage(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return hoaDonRepository.findAll(pageable);
    }

    // Phương thức xóa hóa đơn theo id
    @Transactional
    public void deleteHoaDonById(Integer id) {
        hoaDonRepository.deleteById(id);
    }

    // Phương thức cập nhật trạng thái giao hàng
    @Transactional
    public void updateGiaoHangByIdHoadon(String giaohang, Integer idHoadon) {
        hoaDonRepository.updateGiaoHangByIdHoadon(giaohang, idHoadon);
    }
}

