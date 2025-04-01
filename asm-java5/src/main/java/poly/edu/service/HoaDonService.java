package poly.edu.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import poly.edu.entity.HoaDon;
import poly.edu.entity.HoaDonChiTiet;
import poly.edu.entity.HoaDonChiTietId;
import poly.edu.entity.Users;
import poly.edu.repository.HoaDonRepository;
import poly.edu.repository.HoaDonChiTietRepository;
import poly.edu.repository.UsersRepository;

import java.util.List;

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
}

