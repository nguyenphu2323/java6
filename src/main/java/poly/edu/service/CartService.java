package poly.edu.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import poly.edu.entity.GioHang;
import poly.edu.repository.GioHangRepository;

@Service
public class CartService {
	@Autowired
	GioHangRepository gioHangRepository;

	public GioHang getCartById(Integer id) {
		return gioHangRepository.findById(id)
				.orElseThrow(() -> new IllegalArgumentException("Giỏ hàng không tồn tại!"));
	}
	
}
