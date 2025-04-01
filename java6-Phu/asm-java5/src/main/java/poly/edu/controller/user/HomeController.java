package poly.edu.controller.user;

import java.io.File;
import java.net.MalformedURLException;

import java.util.List;
import java.util.Optional;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.UrlResource;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import org.springframework.web.bind.annotation.RequestParam;

import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.SessionAttribute;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpSession;
import poly.edu.entity.SanPham;
import poly.edu.entity.Users;

import poly.edu.service.LoaiService;
import poly.edu.service.MailService;
import poly.edu.service.SanPhamService;
import poly.edu.service.UserService;
import poly.edu.utils.ImageUtils;

@Controller
public class HomeController {
	@Autowired
	UserService userService;
	@Autowired
	LoaiService loaiService;
	@Autowired
	SanPhamService sanPhamService;
	@Autowired
	private HttpSession session;
	@Autowired
    private MailService mailService;

	@GetMapping("/")
	public String home(
		@RequestParam(name = "page", defaultValue = "0") int page, // Thêm name = "page"
		@RequestParam(name = "size", defaultValue = "6") int size, // Thêm name = "size"
		Model model) {

		// Lấy danh sách sản phẩm phân trang
		Page<SanPham> sanPhamPage = sanPhamService.getAllSanPham(page, size);
		model.addAttribute("sanphams", sanPhamPage.getContent()); // Danh sách sản phẩm trên trang hiện tại
		model.addAttribute("currentPage", page); // Trang hiện tại
		model.addAttribute("pageSize", size); // Kích thước trang
		model.addAttribute("totalPages", sanPhamPage.getTotalPages()); // Tổng số trang

		// Các dữ liệu khác (nếu có)
		model.addAttribute("loais", loaiService.getAllLoai(0, 5));

		return "user/home";
	}

	@GetMapping("/signin")
	public String signin(@ModelAttribute("user") Users user, Model model) {
		Users currentUser = (Users) session.getAttribute("currentUser");
		if (currentUser != null) {
			return "redirect:/";
		}
		model.addAttribute("loais", loaiService.getAllLoai(0, 5));
		return "user/signin";
	}

	@PostMapping("/signin")
	public String login(@ModelAttribute("user") Users user, Model model) {
		try {
			userService.login(user); // Đăng nhập người dùng
			Users currentUser = (Users) session.getAttribute("currentUser");

			// Kiểm tra vai trò người dùng
			if (currentUser != null && currentUser.isVaitro()) {
				return "redirect:/admin"; // Nếu là admin, chuyển đến trang quản trị
			}

			return "redirect:/"; // Nếu là người dùng bình thường, chuyển đến trang chủ
		} catch (Exception e) {
			model.addAttribute("errorMessage", e.getMessage());
			return "user/signin"; // Nếu có lỗi, quay lại trang đăng nhập
		}
	}



	@GetMapping("/signup")
	public String signup(@ModelAttribute("user") Users user, Model model) {
		Users currentUser = (Users) session.getAttribute("currentUser");
		if (currentUser != null) {
			return "redirect:/";
		}
		model.addAttribute("loais", loaiService.getAllLoai(0, 5));
		return "user/signup";
	}

	@GetMapping("/signout")
	public String signout() {
		userService.logout();
		return "redirect:/";
	}

	@PostMapping("/signup")
	public String regiter(@ModelAttribute("user") Users user, Model model) {
		try {
			userService.register(user);
			model.addAttribute("successMessage", "Tạo tài khoản thành công");
		} catch (Exception e) {
			model.addAttribute("errorMessage", e.getMessage());
		}
		return "user/signup";
	}

	@ResponseBody
	@GetMapping("/image/{filename:.+}")
	public ResponseEntity<Object> downloadFile(@PathVariable(name = "filename") String filename) {
		File file = new File("c:/var/java5/images/" + filename);
		if (!file.exists()) {
			throw new RuntimeException("File không tồn tại!");
		}

		UrlResource resource;
		try {
			resource = new UrlResource(file.toURI());
		} catch (MalformedURLException ex) {
			throw new RuntimeException("File không tồn tại!");
		}

		return ResponseEntity.ok()
				.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getName() + "\"")
				.body(resource);
	}

	@GetMapping("/sanpham")
	public String sanpham(@RequestParam(name = "id") String id, Model model) {
		try {
			SanPham sanPham = sanPhamService.getSanPhamById(Integer.valueOf(id));
			model.addAttribute("sanpham", sanPham);
			model.addAttribute("sanphams", sanPhamService.getSanPhamByIdLoai(sanPham.getLoai().getIdLoai(), 0, 4)
					.filter(item -> item.getIdSanpham() != sanPham.getIdSanpham()));
			model.addAttribute("loais", loaiService.getAllLoai(0, 5));
			model.addAttribute("luotMua", sanPhamService.getLuotMuaById(sanPham.getIdSanpham()));
			return "user/productDetail";
		} catch (Exception e) {
			return "redirect:/";
		}
	}

	// Chuyển hướng sang trang tài khoản của người dùng
    @GetMapping("/account")
	public String userAccountPage(Model model, @SessionAttribute(name = "currentUser", required = false) Users currentUser) {
		if (currentUser == null) {
			return "redirect:/login";
		}
		model.addAttribute("account", currentUser); // Gán `currentUser` vào biến `account`
		return "user/account";
	}
	@PostMapping("/account/update")
public String updateAccount(
        @RequestParam("idUser") String idUser,
        @RequestParam("hoten") String hoten,
        @RequestParam("sdt") String sdt,
        @RequestParam("matkhau") String matkhau,
        @RequestParam(value = "hinh", required = false) MultipartFile hinh,
        @SessionAttribute(name = "currentUser", required = false) Users currentUser,
        Model model) {

    if (currentUser == null) {
        return "redirect:/signin"; // Nếu chưa đăng nhập, quay về trang đăng nhập
    }

    try {
        // Cập nhật thông tin người dùng
        currentUser.setHoten(hoten);
        currentUser.setSdt(sdt);
        currentUser.setMatkhau(matkhau);

        // Kiểm tra nếu có ảnh mới được tải lên
        if (hinh != null && !hinh.isEmpty()) {
            Optional<String> imageName = ImageUtils.upload(hinh);

            if (imageName.isPresent()) {
                currentUser.setHinh(imageName.get());
            } else {
                model.addAttribute("errorMessage", "Lỗi khi tải ảnh lên!");
                return "user/account";
            }
        }

        // Lưu thông tin người dùng vào database
        userService.updateUser(currentUser.getIdUser(), currentUser);

        model.addAttribute("successMessage", "Cập nhật thông tin thành công!");
    } catch (Exception e) {
        model.addAttribute("errorMessage", "Lỗi cập nhật: " + e.getMessage());
    }

    model.addAttribute("account", currentUser);
    return "user/account"; // Quay lại trang tài khoản sau khi cập nhật
}

	@GetMapping("/sanpham-theo-loai")
	public String sanPhamTheoLoai(@RequestParam("idLoai") int idLoai, Model model) {
		List<SanPham> sanphams = sanPhamService.getSanPhamByLoai(idLoai);
		model.addAttribute("sanphams", sanphams);
		return "user/sanpham-theo-loai"; // Trả về trang hiển thị sản phẩm theo danh mục
	}

	@PostMapping("/subscribe")
	public String subscribe(@RequestParam("email") String email, RedirectAttributes redirectAttributes) {
		try {
			String subject = "Đăng ký nhận tin thành công!";
			String message = "Chúc mừng bạn đã đăng ký nhận tin thành công!";
			mailService.sendEmail(email, subject, message);
			redirectAttributes.addFlashAttribute("successMessage", "Đã đăng ký nhận tin thành công!");

		} catch (Exception e) {
			redirectAttributes.addFlashAttribute("errorMessage", "Lỗi khi gửi email!");
		}
		return "redirect:/";
	}
	
	

	
}

