package poly.edu.controller.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.ui.Model;
import poly.edu.entity.HoaDon;
import poly.edu.entity.HoaDonChiTiet;
import poly.edu.entity.Users;
import poly.edu.service.HoaDonService;
import poly.edu.repository.UsersRepository;

import java.util.Date;
import java.util.List;

@Controller
public class HoaDonController {

    @Autowired
    private HoaDonService hoaDonService;

    @Autowired
    private UsersRepository usersRepository;  // Thêm UsersRepository để truy vấn người dùng

    @PostMapping("/placeOrder")
    public String placeOrder(@RequestParam("userId") String userId,
                             @RequestParam("address") String address,
                             @RequestParam("products") List<HoaDonChiTiet> hoaDonChiTiets, Model model) {
        // Lấy người dùng từ cơ sở dữ liệu theo userId
        Users user = usersRepository.findById(userId).orElseThrow(() -> new RuntimeException("Người dùng không tồn tại"));

        // Tạo Hóa Đơn mới
        HoaDon hoaDon = new HoaDon();
        hoaDon.setUsers(user);  // Gán người dùng từ cơ sở dữ liệu vào hóa đơn
        hoaDon.setDiachi(address);
        hoaDon.setNgaytao(new Date());  // Ngày tạo là ngày hiện tại
        hoaDon.setTrangthai("Chờ xử lý"); // Trạng thái đơn hàng

        // Đặt hàng và lưu vào cơ sở dữ liệu
        HoaDon savedHoaDon = hoaDonService.placeOrder(userId, hoaDon, hoaDonChiTiets);

        // Thêm thông báo vào model để hiển thị thông báo thành công
        model.addAttribute("message", "Đặt hàng thành công!");

        // Chuyển hướng đến trang xác nhận đơn hàng
        return "orderConfirmation";
    }
}
