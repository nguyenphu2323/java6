package poly.edu.controller.user;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;


import poly.edu.entity.SanPham;
import poly.edu.service.LoaiService;
import poly.edu.service.SanPhamService;

@Controller
public class SearchController {
	@Autowired
	SanPhamService sanPhamService;
	@Autowired
	LoaiService loaiService;
	private static final int PRODUCTS_PER_PAGE = 12;
	@GetMapping("/search")
	public String search(Model model, 
						 @RequestParam(name = "q", required = false) String q,
						 @RequestParam(defaultValue = "0", name = "page") int page) {
		Optional<String> query = Optional.ofNullable(q).filter(s -> !s.trim().isEmpty());
	
		if (query.isPresent()) {
			String queryStr = query.get();
			
			// Đổi từ searchByName thành searchByMotangan để tìm theo mô tả ngắn
			Page<SanPham> sanPhamPage = sanPhamService.searchByMotangan(page, PRODUCTS_PER_PAGE, queryStr);
	
			model.addAttribute("sanphams", sanPhamPage.getContent()); // Danh sách sản phẩm
			model.addAttribute("totalProducts", sanPhamPage.getTotalElements()); // Tổng số sản phẩm
			model.addAttribute("currentPage", page); // Trang hiện tại
			model.addAttribute("totalPages", sanPhamPage.getTotalPages()); // Tổng số trang
			model.addAttribute("query", queryStr);
			model.addAttribute("loais", loaiService.getAllLoai(0, 5)); // Danh sách loại sản phẩm
			
			return "user/search"; // Trả về trang kết quả tìm kiếm
		} else {
			return "redirect:/"; // Nếu không có từ khóa, quay về trang chủ
		}
	}
	
}
