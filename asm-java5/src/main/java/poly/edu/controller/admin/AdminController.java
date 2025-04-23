package poly.edu.controller.admin;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpSession;
import poly.edu.entity.Users;
import poly.edu.repository.ThongKeRepository;
import poly.edu.service.HoaDonService;
import poly.edu.service.UserService;

@Controller
public class AdminController {
	@Autowired
	UserService userService;
	@Autowired
	private HttpSession session;
	@Autowired
	private HoaDonService hoaDonService;
	private final ThongKeRepository thongKeRepository;

	@Autowired
	public AdminController(ThongKeRepository thongKeRepository) {
		this.thongKeRepository = thongKeRepository;

	}

	@GetMapping("/admin") // test
	public String home(Model model) {
		Users currentUser = (Users) session.getAttribute("currentUser");
		if (currentUser == null || !currentUser.isVaitro()) {
			return "redirect:/";
		}

		// Thống kê tồn kho
		List<Object[]> thongKeTonKho = thongKeRepository.thongKeTonKho();
		model.addAttribute("thongKeTonKho", thongKeTonKho);

		// Thống kê doanh thu theo ngày
		List<Object[]> thongKeDoanhThuTheoNgay = thongKeRepository.thongKeDoanhThuTheoNgay();
		model.addAttribute("thongKeDoanhThuTheoNgay", thongKeDoanhThuTheoNgay);

		// Thống kê sản phẩm bán chạy
		List<Object[]> thongKeSanPhamBanChay = thongKeRepository.thongKeSanPhamBanChay();
		model.addAttribute("thongKeSanPhamBanChay", thongKeSanPhamBanChay);

		return "admin/home";
	}

	@GetMapping("/admin/user")
	public String userManager(Model model, @RequestParam(defaultValue = "0", name = "page") int page) {
		Users currentUser = (Users) session.getAttribute("currentUser");
		if (currentUser == null || !currentUser.isVaitro()) {
			return "redirect:/";
		}

		Page<Users> userPage = userService.getAllUsers(page, 8);

		model.addAttribute("users", userPage.getContent()); // Danh sách user
		model.addAttribute("currentPage", page); // Trang hiện tại
		model.addAttribute("totalPages", userPage.getTotalPages());
		return "admin/user/userManager";
	}

	@GetMapping("/admin/user/create")
	public String userCreate(Model model, @ModelAttribute("user") Users user) {
		Users currentUser = (Users) session.getAttribute("currentUser");
		if (currentUser == null || !currentUser.isVaitro()) {
			return "redirect:/";
		}
		return "admin/user/createUser";
	}

	@PostMapping("/admin/user/create")
	public String userInsert(Model model, @ModelAttribute("user") Users user) {

		try {
			Users currentUser = (Users) session.getAttribute("currentUser");
			if (currentUser == null || !currentUser.isVaitro()) {
				return "redirect:/";
			}

			userService.register(user);
			model.addAttribute("successMessage", "Tạo tài khoản thành công");
		} catch (Exception e) {
			model.addAttribute("errorMessage", e.getMessage());
		}
		return "admin/user/createUser";
	}

	@GetMapping("/admin/user/edit/{id}")
	public String showUpdateForm(@PathVariable("id") String id, Model model) {
		Users currentUser = (Users) session.getAttribute("currentUser");
		if (currentUser == null || !currentUser.isVaitro()) {
			return "redirect:/";
		}
		try {
			Users user = userService.getUserById(id);
			model.addAttribute("user", user);
		} catch (Exception e) {
			model.addAttribute("errorMessage", e.getMessage());
		}
		return "admin/user/updateUser";
	}

	@PostMapping("/admin/user/update/{id}")
	public String updateUser(Model model, @PathVariable("id") String id, @ModelAttribute("user") Users updatedUser) {
		try {
			Users currentUser = (Users) session.getAttribute("currentUser");
			if (currentUser == null || !currentUser.isVaitro()) {
				return "redirect:/";
			}
			userService.updateUser(id, updatedUser);
			model.addAttribute("successMessage", "Cập nhật tài khoản thành công");
		} catch (Exception e) {
			model.addAttribute("errorMessage", e.getMessage());
		}
		return "admin/user/updateUser";
	}

	@GetMapping("/admin/user/delete/{id}")
	public String deleteUser(Model model, @PathVariable("id") String id, RedirectAttributes redirectAttributes) {
		try {
			userService.deleteUser(id);
			redirectAttributes.addFlashAttribute("successMessage", "Xóa tài khoản thành công");
		} catch (Exception e) {
			redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
		}
		return "redirect:/admin/user";
	}

	@GetMapping("/admin/orderManager")
	public String danhsachhoadon(Model model) {
		model.addAttribute("hoadons", hoaDonService.getAllHoaDons());
		return "/admin/orderManager";
	}

	@PostMapping("/admin/orderManager/update")
	public String updateTrangThaiVaGiaohang(
			@RequestParam("idHoadon") int idHoadon,
			@RequestParam("giaohang") String giaohang,
			@RequestParam("trangthai") String trangthai,
			RedirectAttributes redirectAttributes) {

		try {

			System.out.println("ID Hóa Đơn: " + idHoadon);
			System.out.println("Trạng Thái: " + trangthai);
			System.out.println("Giao Hàng: " + giaohang);

			hoaDonService.updateTrangThaiVaGiaohang(idHoadon, giaohang, trangthai);
			redirectAttributes.addFlashAttribute("successMessage", "Cập nhật đơn hàng thành công!");
		} catch (Exception e) {
			redirectAttributes.addFlashAttribute("errorMessage", "Lỗi: " + e.getMessage());
		}

		return "redirect:/admin/orderManager";
	}

	// Thống kê tồn kho
	@GetMapping("/admin/thongkeTonKho")
	public String thongKeTonKho(Model model) {
		List<Object[]> thongKeTonKho = thongKeRepository.thongKeTonKho();
		model.addAttribute("thongKeTonKho", thongKeTonKho);
		return "/admin/thongke/thongkeTonKho";
	}

	// Thống kê doanh thu theo ngày
	@GetMapping("/admin/thongkeDoanhThu")
	public String thongKeDoanhThu(Model model) {
		List<Object[]> thongKeDoanhThuTheoNgay = thongKeRepository.thongKeDoanhThuTheoNgay();
		model.addAttribute("thongKeDoanhThuTheoNgay", thongKeDoanhThuTheoNgay);
		return "/admin/thongke/thongkeDoanhThu";
	}

	// Thống kê sản phẩm bán chạy
	@GetMapping("/admin/thongkeSanPhamBanChay")
	public String thongKeSanPhamBanChay(Model model) {
		List<Object[]> thongKeSanPhamBanChay = thongKeRepository.thongKeSanPhamBanChay();
		model.addAttribute("thongKeSanPhamBanChay", thongKeSanPhamBanChay);
		return "/admin/thongke/thongkeSanPhamBanChay";
	}

}
// test update