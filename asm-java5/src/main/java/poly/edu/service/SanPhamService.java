package poly.edu.service;

import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import org.springframework.data.domain.Pageable;


import poly.edu.entity.SanPham;
import poly.edu.repository.SanPhamRepository;
import poly.edu.utils.ImageUtils;

@Service
public class SanPhamService {
	@Autowired
	SanPhamRepository sanPhamRepository;
	public Page<SanPham> getSanPhamPhanTrang(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return sanPhamRepository.findAll(pageable);
    }
	public SanPham create(SanPham sanPham, MultipartFile image) {
		if (sanPham.getSoluong() < 0) {
			throw new RuntimeException("Vui lòng nhập số lượng >= 0!");
		}
		if (sanPham.getGia() <= 0) {
			throw new RuntimeException("Vui lòng nhập giá > 0!");
		}
		if (sanPham.getGiamgia() < 0) {
			throw new RuntimeException("Vui lòng nhập giảm giá >= 0!");
		}

		String fileName = ImageUtils.upload(image)
				.orElseThrow(() -> new RuntimeException("Có lỗi xảy ra trong quá trình tải ảnh"));
		sanPham.setHinh(fileName);
		sanPham.setNgaytao(new Date());
		sanPhamRepository.save(sanPham);
		return sanPham;
	}

	public SanPham updateSanPham(Integer id, SanPham updatedSanPham, MultipartFile image) {
		if (updatedSanPham.getSoluong() < 0) {
			throw new RuntimeException("Vui lòng nhập số lượng >= 0!");
		}
		if (updatedSanPham.getGia() <= 0) {
			throw new RuntimeException("Vui lòng nhập giá > 0!");
		}
		if (updatedSanPham.getGiamgia() < 0) {
			throw new RuntimeException("Vui lòng nhập giảm giá >= 0!");
		}

		Optional<SanPham> optionalSanPham = sanPhamRepository.findById(id);
		if (optionalSanPham.isPresent()) {
			SanPham sanPham = optionalSanPham.get();
			if (image.getSize() > 0) {
				if (Objects.nonNull(sanPham.getHinh()) || sanPham.getHinh() == "") {
					ImageUtils.delete(sanPham.getHinh());
				}
				sanPham.setHinh(ImageUtils.upload(image)
						.orElseThrow(() -> new RuntimeException("Có lỗi xảy ra trong quá trình tải ảnh")));
			}
			sanPham.setTenSanpham(updatedSanPham.getTenSanpham());
			sanPham.setLoai(updatedSanPham.getLoai());
			sanPham.setMota(updatedSanPham.getMota());
			sanPham.setMotangan(updatedSanPham.getMotangan());
			sanPham.setGiamgia(updatedSanPham.getGiamgia());
			sanPham.setGia(updatedSanPham.getGia());
			sanPham.setSoluong(updatedSanPham.getSoluong());
			return sanPhamRepository.save(sanPham);
		} else {
			throw new RuntimeException("Không tìm thấy mã sản phẩm!");
		}
	}

	public Page<SanPham> getSanPhams(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("idSanpham").descending());
        return sanPhamRepository.findAll(pageable);
    }
	public Page<SanPham> getAllSanPham(int page, int size) {
		Pageable pageable = PageRequest.of(page, size);
		return sanPhamRepository.findAll(pageable);
	}
	public int getTotalPages(int size) {
		long totalItems = sanPhamRepository.count();
		return (int) Math.ceil((double) totalItems / size);
	}

	public Page<SanPham> searchByMotangan(int pageNumber, int limit, String keyword) {
		PageRequest pageable = PageRequest.of(pageNumber, limit);
		return sanPhamRepository.findByMotanganContainingIgnoreCase(keyword, pageable);
	}
	

	public SanPham getSanPhamById(Integer id) {
		return sanPhamRepository.findById(id)
				.orElseThrow(() -> new IllegalArgumentException("Mã sản phẩm không tồn tại!"));
	}

	public Page<SanPham> getSanPhamByIdLoai(Integer id, int pageNumber, int limit) {
		PageRequest pageable = PageRequest.of(pageNumber, limit);
		return sanPhamRepository.findByLoai_IdLoai(id, pageable);
	}

	public Integer getLuotMuaById(Integer id) {
		return sanPhamRepository.tongSoLuongSanPhamDaBanTheoId(id);
	}

	public void deleteSanPham(Integer id) {
		try {
			SanPham sanPham = sanPhamRepository.findById(id)
					.orElseThrow(() -> new RuntimeException("Mã sản phẩm không tồn tại!"));
			sanPhamRepository.deleteById(id);
			if (Objects.nonNull(sanPham.getHinh()) || sanPham.getHinh() == "") {
				ImageUtils.delete(sanPham.getHinh());
			}
		} catch (Exception e) {
			throw new RuntimeException("Sản phẩm này đã tồn tại trên hóa đơn không thể xóa!");
		}
	}
	public List<SanPham> getSanPhamByLoai(int idLoai) {
		return sanPhamRepository.findByLoai(idLoai);
	}
	
	
}
