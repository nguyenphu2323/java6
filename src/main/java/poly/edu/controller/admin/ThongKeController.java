package poly.edu.controller.admin;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import poly.edu.repository.ThongKeRepository;

@Controller
@RequestMapping("/admin")
public class ThongKeController {

    private final ThongKeRepository thongKeRepository;

    @Autowired
    public ThongKeController(ThongKeRepository thongKeRepository) {
        this.thongKeRepository = thongKeRepository;
    }
    @GetMapping("/thongke")
    public String thongKe(Model model) {
 
        return "/admin/thongke/thongke"; 
    }
    // Thống kê tồn kho
    @GetMapping("/thongkeTonKho")
    public String thongKeTonKho(Model model) {
        List<Object[]> thongKeTonKho = thongKeRepository.thongKeTonKho();
        model.addAttribute("thongKeTonKho", thongKeTonKho);
        return "/admin/thongke/thongkeTonKho"; 
    }

    // Thống kê doanh thu theo ngày
    @GetMapping("/thongkeDoanhThu")
    public String thongKeDoanhThu(Model model) {
        List<Object[]> thongKeDoanhThuTheoNgay = thongKeRepository.thongKeDoanhThuTheoNgay();
        model.addAttribute("thongKeDoanhThuTheoNgay", thongKeDoanhThuTheoNgay);
        return "/admin/thongke/thongkeDoanhThu"; 
    }

    // Thống kê sản phẩm bán chạy
    @GetMapping("/thongkeSanPhamBanChay")
    public String thongKeSanPhamBanChay(Model model) {
        List<Object[]> thongKeSanPhamBanChay = thongKeRepository.thongKeSanPhamBanChay();
        model.addAttribute("thongKeSanPhamBanChay", thongKeSanPhamBanChay);
        return "/admin/thongke/thongkeSanPhamBanChay"; 
    }
}
