import createToast, { toastComponent } from "./toast.js";
import { setTotalCartItemsQuantity } from "./header.js";

// STATIC DATA
const currentUserIdMetaTag = document.querySelector("meta[name='currentUserId']");
console.log("User ID:", currentUserIdMetaTag?.content);

const deliveryMethodInputValues = {
  "1": {
    deliveryMethod: 1,
    deliveryPrice: 20000,
  },
  "2": {
    deliveryMethod: 2,
    deliveryPrice: 50000,
  },
};

// MESSAGES
const FAILED_OPERATION_MESSAGE = "Đã có lỗi truy vấn!";
const SUCCESS_DELETE_CART_ITEM_MESSAGE = (productName) => `Đã xóa sản phẩm ${productName} khỏi giỏ hàng thành công!`;
const SUCCESS_UPDATE_CART_ITEM_MESSAGE = (productName) => `Đã cập nhật số lượng của sản phẩm ${productName} thành công!`;
const SUCCESS_ADD_ORDER_MESSAGE = "Đặt hàng thành công!";

// ROOTS/ELEMENTS
const cartTableRootElement = document.querySelector("#cart-table");
const tempPriceRootElement = document.querySelector("#temp-price");
const deliveryPriceRootElement = document.querySelector("#delivery-price");
const totalPriceRootElement = document.querySelector("#total-price");
const checkoutBtnElement = document.querySelector("form button[type='submit']"); // Sửa để chọn nút submit
const deliveryMethodRadioElements = [...document.querySelectorAll("input[name='deliveryMethod']")];
const orderMessageElement = document.querySelector("#orderMessage");

// COMPONENTS
function cartItemRowComponent(props) {
  const {
    id,
    cartId,
    productId,
    productName,
    productPrice,
    productDiscount,
    productQuantity,
    productImageName,
    quantity,
  } = props;

  return `
    <tr>
      <td>
        <figure class="itemside">
          <div class="float-start me-3">
            <img
              src="${'/image/' + productImageName}"
              alt="${productName}"
              width="80"
              height="80"
            >
          </div>
          <figcaption class="info">
            <a href="${'/sanpham?id=' + productId}" class="title">${productName}</a>
          </figcaption>
        </figure>
      </td>
      <td>
        <div class="price-wrap">
          ${cartItemPriceComponent(productPrice, productDiscount)}
        </div>
      </td>
      <td>
        <input type="number" value="${quantity}" min="1" max="${productQuantity}" class="form-control" id="quantity-cart-item-${id}">
      </td>
      <td class="text-center text-nowrap">
        <button type="button" class="btn btn-light" id="update-cart-item-${id}"><i class="fa-solid fa-arrows-rotate"></i></button>
        <button type="button" class="btn btn-light ms-1" id="delete-cart-item-${id}"><i class="fas fa-trash-alt"></i></button>
      </td>
    </tr>
  `;
}

function cartItemPriceComponent(productPrice, productDiscount) {
  if (productDiscount === 0) {
    return `<span class="price">${_formatPrice(productPrice)}₫</span>`;
  }

  return `
    <div>
      <span class="price">${_formatPrice(productPrice * (100 - productDiscount) / 100)}₫</span>
      <span class="ms-2 badge bg-info">-${productDiscount}%</span>
    </div>
    <small class="text-muted text-decoration-line-through">${_formatPrice(productPrice)}₫</small>
  `;
}

function cartTableComponent(cartItemRowComponentsFragment) {
  if (state.cart.cartItems.length === 0) {
    return `
      <div class="d-flex justify-content-center p-5 font-monospace">
        Không có sản phẩm nào trong giỏ hàng
      </div>
    `;
  }

  return `
    <div class="table-responsive-xl">
      <table class="cart-table table table-borderless">
        <thead class="text-muted">
          <tr class="small text-uppercase">
            <th scope="col" style="min-width: 350px;">Sản phẩm</th>
            <th scope="col" style="min-width: 160px;">Giá</th>
            <th scope="col" style="min-width: 150px;">Số lượng</th>
            <th scope="col" style="min-width: 100px;"></th>
          </tr>
        </thead>
        <tbody>${cartItemRowComponentsFragment}</tbody>
      </table>
    </div>
  `;
}

function loadingComponent() {
  return `
    <div class="d-flex justify-content-center p-5">
      <div class="spinner-border text-primary" role="status">
        <span class="visually-hidden">Đang tải...</span>
      </div>
    </div>
  `;
}

// UTILS
async function _fetchGetCart() {
  const response = await fetch(`/cartItem?userId=${currentUserIdMetaTag.content}`, {
    method: "GET",
    headers: {
      "Accept": "application/json",
      "Content-Type": "application/json",
    },
  });
  return [response.status, await response.json()];
}

async function _fetchDeleteCartItem(cartItemId, productId) {
  const response = await fetch(`/cartItem?cartItemId=${cartItemId}&productId=${productId}`, {
    method: "DELETE",
    headers: {
      "Accept": "application/json",
      "Content-Type": "application/json",
    },
  });
  return [response.status, await response.json()];
}

async function _fetchUpdateCartItem(cartItemId, quantity, productId) {
  const cartItemRequest = {
    productId: productId,
    quantity: quantity,
  };

  const response = await fetch(`/cartItem?cartItemId=${cartItemId}`, {
    method: "PUT",
    headers: {
      "Accept": "application/json",
      "Content-Type": "application/json",
    },
    body: JSON.stringify(cartItemRequest),
  });

  return [response.status, await response.json()];
}

async function _fetchPostAddOrder() {
  const response = await fetch(`/checkout?userId=${currentUserIdMetaTag.content}`, {
    method: "POST",
    headers: {
      "Accept": "application/json",
      "Content-Type": "application/json",
    },
  });

  return [response.status, await response.json()];
}

function _formatPrice(price) {
  return new Intl.NumberFormat('vi-VN').format(price.toFixed());
}

// STATE
const initialCart = {
  id: 0,
  userId: 0,
  cartItems: [],
};

const initialOrder = {
  deliveryMethod: 1,
  deliveryPrice: 20000,
};

const state = {
  cart: { ...initialCart },
  order: { ...initialOrder },
  initState: async () => {
    if (!currentUserIdMetaTag || !currentUserIdMetaTag.content) {
      cartTableRootElement.innerHTML = '<p>Vui lòng đăng nhập để xem giỏ hàng.</p>';
      if (checkoutBtnElement) checkoutBtnElement.disabled = true;
      return;
    }

    const [status, data] = await _fetchGetCart();
    if (status === 200) {
      state.cart = {
        id: data.cartId || 0,
        userId: currentUserIdMetaTag.content,
        cartItems: data.cartItems || data
      };
      render();
      attachEventHandlersForNoneRerenderElements();
    } else if (status === 404) {
      createToast(toastComponent(FAILED_OPERATION_MESSAGE, "danger"));
    }
  },
  deleteCartItem: async (currentCartItem) => {
    if (confirm("Bạn có muốn xóa?")) {
      const [status] = await _fetchDeleteCartItem(currentCartItem.cartId, currentCartItem.productId);
      if (status === 200) {
        state.cart.cartItems = state.cart.cartItems.filter((cartItem) => cartItem.id !== currentCartItem.id);
        render();
        createToast(toastComponent(SUCCESS_DELETE_CART_ITEM_MESSAGE(currentCartItem.productName), "success"));
        setTotalCartItemsQuantity(state.cart);
      } else if (status === 404) {
        createToast(toastComponent(FAILED_OPERATION_MESSAGE, "danger"));
      }
    }
  },
  updateCartItem: async (currentCartItem, quantity) => {
    if (currentCartItem.quantity !== quantity) {
      if (quantity <= 0) {
        createToast(toastComponent("Vui lòng nhập số lượng hợp lệ", "danger"));
        return;
      }
      const [status] = await _fetchUpdateCartItem(currentCartItem.cartId, quantity, currentCartItem.productId);
      if (status === 200) {
        state.cart.cartItems = state.cart.cartItems.map((cartItem) => {
          return (cartItem.id === currentCartItem.id) ? { ...cartItem, quantity: quantity } : cartItem;
        });
        render();
        createToast(toastComponent(SUCCESS_UPDATE_CART_ITEM_MESSAGE(currentCartItem.productName), "success"));
        setTotalCartItemsQuantity(state.cart);
      } else if (status === 404) {
        createToast(toastComponent(FAILED_OPERATION_MESSAGE, "danger"));
      }
    }
  },
  checkoutCart: async () => {
    if (state.cart.cartItems.length === 0) {
      createToast(toastComponent("Giỏ hàng của bạn đang trống!", "danger"));
      return;
    }

    if (confirm("Bạn có muốn đặt hàng?")) {
      const [status, response] = await _fetchPostAddOrder();
      if (status === 200) {
        createToast(toastComponent(SUCCESS_ADD_ORDER_MESSAGE, "success"));
        state.cart = { ...initialCart };
        render();
        setTotalCartItemsQuantity(state.cart);
        cartTableRootElement.style.display = "none";
        orderMessageElement.style.display = "block";
      } else {
        createToast(toastComponent(response.message || FAILED_OPERATION_MESSAGE, "danger"));
      }
    }
  },
  changeDeliveryMethod: (deliveryMethodValue) => {
    state.order.deliveryMethod = deliveryMethodInputValues[deliveryMethodValue].deliveryMethod;
    state.order.deliveryPrice = deliveryMethodInputValues[deliveryMethodValue].deliveryPrice;
    console.log("Updated delivery price:", state.order.deliveryPrice); // Debug
    render();
  },
  getTempPrice: () => {
    return state.cart.cartItems
      .map((cartItem) => {
        if (cartItem.productDiscount === 0) {
          return cartItem.productPrice * cartItem.quantity;
        }
        return (cartItem.productPrice * (100 - cartItem.productDiscount) / 100).toFixed() * cartItem.quantity;
      })
      .reduce((partialSum, productTotalPrice) => partialSum + productTotalPrice, 0);
  },
  getDeliveryPrice: () => state.order.deliveryPrice,
  getTotalPrice: () => state.getTempPrice() + state.getDeliveryPrice(),
};

// RENDER
function render() {
  const cartItemRowComponentsFragment = state.cart.cartItems.map(cartItemRowComponent).join("");
  cartTableRootElement.innerHTML = cartTableComponent(cartItemRowComponentsFragment);

  tempPriceRootElement.innerHTML = _formatPrice(state.getTempPrice());
  deliveryPriceRootElement.innerHTML = _formatPrice(state.getDeliveryPrice());
  totalPriceRootElement.innerHTML = _formatPrice(state.getTotalPrice());

  const isCartItemsEmpty = state.cart.cartItems.length === 0;
  if (checkoutBtnElement) {
    checkoutBtnElement.disabled = isCartItemsEmpty;
  }
  deliveryMethodRadioElements.forEach((radio) => {
    radio.disabled = isCartItemsEmpty;
    radio.checked = radio.value === String(state.order.deliveryMethod);
  });

  state.cart.cartItems.forEach((cartItem) => {
    const deleteCartItemBtnElement = document.querySelector(`#delete-cart-item-${cartItem.id}`);
    if (deleteCartItemBtnElement) {
      deleteCartItemBtnElement.addEventListener("click", () => state.deleteCartItem(cartItem));
    }
  });

  state.cart.cartItems.forEach((cartItem) => {
    const updateCartItemBtnElement = document.querySelector(`#update-cart-item-${cartItem.id}`);
    if (updateCartItemBtnElement) {
      updateCartItemBtnElement.addEventListener("click", () => {
        const quantityCartItemInputElement = document.querySelector(`#quantity-cart-item-${cartItem.id}`);
        void state.updateCartItem(cartItem, Number(quantityCartItemInputElement.value));
      });
    }
  });
}

function attachEventHandlersForNoneRerenderElements() {
  deliveryMethodRadioElements.forEach((radio) => {
    radio.addEventListener("change", () => {
      console.log("Delivery method changed to:", radio.value); // Debug
      state.changeDeliveryMethod(radio.value);
    });
  });

  // Không cần gắn sự kiện click cho checkoutBtnElement vì nó là nút submit form
}

// MAIN
if (currentUserIdMetaTag) {
  cartTableRootElement.innerHTML = loadingComponent();
  void state.initState();
}