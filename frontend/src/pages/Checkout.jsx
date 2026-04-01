import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { cartService } from '../services/cartService';
import { orderService } from '../services/orderService';
import { promotionService } from '../services/promotionService';

export default function Checkout() {
  const vietnamProvinces = [
    'An Giang',
    'Bà Rịa - Vũng Tàu',
    'Bắc Giang',
    'Bắc Kạn',
    'Bạc Liêu',
    'Bắc Ninh',
    'Bến Tre',
    'Bình Định',
    'Bình Dương',
    'Bình Phước',
    'Bình Thuận',
    'Cà Mau',
    'Cần Thơ',
    'Cao Bằng',
    'Đà Nẵng',
    'Đắk Lắk',
    'Đắk Nông',
    'Điện Biên',
    'Đồng Nai',
    'Đồng Tháp',
    'Gia Lai',
    'Hà Giang',
    'Hà Nam',
    'Hà Nội',
    'Hà Tĩnh',
    'Hải Dương',
    'Hải Phòng',
    'Hậu Giang',
    'Hòa Bình',
    'Hưng Yên',
    'Khánh Hòa',
    'Kiên Giang',
    'Kon Tum',
    'Lai Châu',
    'Lâm Đồng',
    'Lạng Sơn',
    'Lào Cai',
    'Long An',
    'Nam Định',
    'Nghệ An',
    'Ninh Bình',
    'Ninh Thuận',
    'Phú Thọ',
    'Phú Yên',
    'Quảng Bình',
    'Quảng Nam',
    'Quảng Ngãi',
    'Quảng Ninh',
    'Quảng Trị',
    'Sóc Trăng',
    'Sơn La',
    'Tây Ninh',
    'Thái Bình',
    'Thái Nguyên',
    'Thanh Hóa',
    'Thừa Thiên Huế',
    'Tiền Giang',
    'Thành phố Hồ Chí Minh',
    'Trà Vinh',
    'Tuyên Quang',
    'Vĩnh Long',
    'Vĩnh Phúc',
    'Yên Bái'
  ];

  const navigate = useNavigate();
  const [provinceOptions, setProvinceOptions] = useState(
    vietnamProvinces.map((name) => ({ name, code: null }))
  );
  const [districtOptions, setDistrictOptions] = useState([]);
  const [loadingDistricts, setLoadingDistricts] = useState(false);
  const [wardOptions, setWardOptions] = useState([]);
  const [loadingWards, setLoadingWards] = useState(false);
  const [cartData, setCartData] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [fieldErrors, setFieldErrors] = useState({});
  const [discountCode, setDiscountCode] = useState('');
  const [appliedDiscount, setAppliedDiscount] = useState(null);
  const [applyingDiscount, setApplyingDiscount] = useState(false);

  const [formData, setFormData] = useState({
    name: '',
    phone: '',
    address: '',
    city: '',
    district: '',
    ward: '',
    paymentMethod: 'cod',
    cardNumber: '',
    cardholderName: '',
    expiryDate: '',
    cvv: '',
    bankName: '',
    accountNumber: '',
    accountHolder: ''
  });

  React.useEffect(() => {
    fetchCart();
  }, []);

  React.useEffect(() => {
    const fetchProvinces = async () => {
      try {
        const res = await fetch('https://provinces.open-api.vn/api/p/');
        if (!res.ok) return;
        const data = await res.json();
        if (!Array.isArray(data) || data.length === 0) return;

        const mapped = data
          .map((p) => ({
            name: p.name,
            code: p.code
          }))
          .sort((a, b) => a.name.localeCompare(b.name, 'vi'));

        setProvinceOptions(mapped);
      } catch (err) {
        // Keep fallback province list if API is unavailable
      }
    };

    fetchProvinces();
  }, []);

  React.useEffect(() => {
    const selectedProvince = provinceOptions.find((p) => p.name === formData.city);

    if (!selectedProvince?.code) {
      setDistrictOptions([]);
      setWardOptions([]);
      return;
    }

    const fetchDistricts = async () => {
      try {
        setLoadingDistricts(true);
        const res = await fetch(`https://provinces.open-api.vn/api/p/${selectedProvince.code}?depth=2`);
        if (!res.ok) {
          setDistrictOptions([]);
          return;
        }

        const data = await res.json();
        const districts = Array.isArray(data.districts)
          ? data.districts
            .map((d) => ({ name: d.name, code: d.code }))
            .sort((a, b) => a.name.localeCompare(b.name, 'vi'))
          : [];
        setDistrictOptions(districts);
        setWardOptions([]);
      } catch (err) {
        setDistrictOptions([]);
        setWardOptions([]);
      } finally {
        setLoadingDistricts(false);
      }
    };

    fetchDistricts();
  }, [formData.city, provinceOptions]);

  React.useEffect(() => {
    const selectedDistrict = districtOptions.find((d) => d.name === formData.district);

    if (!selectedDistrict?.code) {
      setWardOptions([]);
      return;
    }

    const fetchWards = async () => {
      try {
        setLoadingWards(true);
        const res = await fetch(`https://provinces.open-api.vn/api/d/${selectedDistrict.code}?depth=2`);
        if (!res.ok) {
          setWardOptions([]);
          return;
        }

        const data = await res.json();
        const wards = Array.isArray(data.wards)
          ? data.wards.map((w) => w.name).sort((a, b) => a.localeCompare(b, 'vi'))
          : [];
        setWardOptions(wards);
      } catch (err) {
        setWardOptions([]);
      } finally {
        setLoadingWards(false);
      }
    };

    fetchWards();
  }, [formData.district, districtOptions]);

  const fetchCart = async () => {
    try {
      const cart = await cartService.getCart();
      if (!cart.items || cart.items.length === 0) {
        setError('Giỏ hàng trống, vui lòng thêm sản phẩm');
        return;
      }
      setCartData(cart);
    } catch (err) {
      setError('Không thể tải giỏ hàng');
    } finally {
      setLoading(false);
    }
  };

  const handleInputChange = (e) => {
    const { name, value } = e.target;
    setError(null);

    if (name === 'paymentMethod') {
      setFieldErrors(prev => {
        const next = { ...prev };
        delete next.cardholderName;
        delete next.cardNumber;
        delete next.expiryDate;
        delete next.cvv;
        delete next.bankName;
        delete next.accountNumber;
        delete next.accountHolder;
        return next;
      });
    } else {
      setFieldErrors(prev => {
        if (!prev[name]) return prev;
        const next = { ...prev };
        delete next[name];
        return next;
      });
    }

    if (name === 'city') {
      setFormData(prev => ({
        ...prev,
        city: value,
        district: '',
        ward: ''
      }));

      setFieldErrors(prev => {
        const next = { ...prev };
        delete next.city;
        delete next.district;
        delete next.ward;
        return next;
      });
      return;
    }

    if (name === 'district') {
      setFormData(prev => ({
        ...prev,
        district: value,
        ward: ''
      }));

      setFieldErrors(prev => {
        const next = { ...prev };
        delete next.district;
        delete next.ward;
        return next;
      });
      return;
    }

    if (name === 'name') {
      const sanitizedName = value.replace(/\d/g, '');
      setFormData(prev => ({
        ...prev,
        [name]: sanitizedName
      }));
      return;
    }

    setFormData(prev => ({
      ...prev,
      [name]: value
    }));
  };

  const validateShippingInfo = () => {
    const normalizedName = formData.name.trim().replace(/\s+/g, ' ');
    const normalizedPhone = formData.phone.trim();
    const normalizedAddress = formData.address.trim();
    const normalizedCity = formData.city.trim().replace(/\s+/g, ' ');
    const normalizedDistrict = formData.district.trim().replace(/\s+/g, ' ');
    const normalizedWard = formData.ward.trim().replace(/\s+/g, ' ');
    const shippingErrors = {};

    if (!normalizedName) {
      shippingErrors.name = 'Vui lòng nhập họ và tên người nhận';
    } else if (/\d/.test(normalizedName)) {
      shippingErrors.name = 'Họ và tên người nhận không được chứa số';
    } else if (normalizedName.length < 2) {
      shippingErrors.name = 'Họ và tên người nhận phải có ít nhất 2 ký tự';
    }

    if (!normalizedPhone) {
      shippingErrors.phone = 'Vui lòng nhập số điện thoại';
    } else if (!/^0\d{9,10}$/.test(normalizedPhone)) {
      shippingErrors.phone = 'Số điện thoại không hợp lệ (ví dụ: 0912345678)';
    }

    if (!normalizedAddress) {
      shippingErrors.address = 'Vui lòng nhập địa chỉ giao hàng';
    } else if (normalizedAddress.length < 5) {
      shippingErrors.address = 'Địa chỉ giao hàng quá ngắn, vui lòng nhập chi tiết hơn';
    }

    if (!normalizedCity) {
      shippingErrors.city = 'Vui lòng nhập Tỉnh/Thành phố';
    } else if (/\d/.test(normalizedCity)) {
      shippingErrors.city = 'Tỉnh/Thành phố không được chứa số';
    } else if (normalizedCity.length < 2) {
      shippingErrors.city = 'Tỉnh/Thành phố không hợp lệ';
    }

    if (!normalizedDistrict) {
      shippingErrors.district = 'Vui lòng chọn Quận/Huyện';
    } else if (districtOptions.length > 0 && !districtOptions.some((d) => d.name === normalizedDistrict)) {
      shippingErrors.district = 'Quận/Huyện không thuộc Tỉnh/Thành phố đã chọn';
    }

    if (!normalizedWard) {
      shippingErrors.ward = 'Vui lòng chọn Phường/Xã';
    } else if (wardOptions.length > 0 && !wardOptions.includes(normalizedWard)) {
      shippingErrors.ward = 'Phường/Xã không thuộc Quận/Huyện đã chọn';
    }

    if (Object.keys(shippingErrors).length > 0) {
      setFieldErrors(prev => {
        const next = { ...prev };
        delete next.name;
        delete next.phone;
        delete next.address;
        delete next.city;
        delete next.district;
        delete next.ward;
        return { ...next, ...shippingErrors };
      });
      setError('Vui lòng kiểm tra lại thông tin giao hàng');
      return null;
    }

    return {
      name: normalizedName,
      phone: normalizedPhone,
      address: normalizedAddress,
      city: normalizedCity,
      district: normalizedDistrict,
      ward: normalizedWard
    };
  };

  const validatePaymentInfo = () => {
    const paymentErrors = {};

    if (formData.paymentMethod === 'card') {
      const cardNumber = formData.cardNumber.replace(/\s/g, '').trim();

      if (!formData.cardholderName.trim()) {
        paymentErrors.cardholderName = 'Vui lòng nhập tên trên thẻ';
      }
      if (!cardNumber) {
        paymentErrors.cardNumber = 'Vui lòng nhập số thẻ';
      } else if (!/^\d{16}$/.test(cardNumber)) {
        paymentErrors.cardNumber = 'Số thẻ phải có đúng 16 chữ số';
      } else if (cardNumber !== '4242424242424242') {
        paymentErrors.cardNumber = 'Thẻ test hợp lệ là 4242 4242 4242 4242';
      }
      if (!formData.expiryDate.trim()) {
        paymentErrors.expiryDate = 'Vui lòng nhập ngày hết hạn';
      }
      if (!formData.cvv.trim()) {
        paymentErrors.cvv = 'Vui lòng nhập mã CVV';
      } else if (!/^\d{3}$/.test(formData.cvv.trim())) {
        paymentErrors.cvv = 'CVV phải có 3 chữ số';
      }
    }

    if (formData.paymentMethod === 'bank') {
      if (!formData.bankName.trim()) {
        paymentErrors.bankName = 'Vui lòng nhập tên ngân hàng';
      }
      if (!formData.accountNumber.trim()) {
        paymentErrors.accountNumber = 'Vui lòng nhập số tài khoản';
      } else if (!/^\d{6,20}$/.test(formData.accountNumber.trim())) {
        paymentErrors.accountNumber = 'Số tài khoản chỉ gồm chữ số (6-20 ký tự)';
      }
      if (!formData.accountHolder.trim()) {
        paymentErrors.accountHolder = 'Vui lòng nhập chủ tài khoản';
      }
    }

    if (Object.keys(paymentErrors).length > 0) {
      setFieldErrors(prev => ({ ...prev, ...paymentErrors }));
      setError('Vui lòng kiểm tra lại thông tin thanh toán');
      return false;
    }

    return true;
  };

  const applyDiscountCode = async () => {
    if (!discountCode.trim()) return;

    try {
      setApplyingDiscount(true);
      setError(null);

      const cartItems = cartData.items.map(item => ({
        productId: item.product._id,
        quantity: item.qty,
        price: item.price
      }));

      const discountResult = await promotionService.applyDiscountCode(
        discountCode.trim(),
        cartItems,
        null // userId if logged in
      );

      setAppliedDiscount(discountResult);
    } catch (err) {
      setError(err.response?.data?.message || 'Mã giảm giá không hợp lệ');
      setAppliedDiscount(null);
    } finally {
      setApplyingDiscount(false);
    }
  };

  const removeDiscount = () => {
    setAppliedDiscount(null);
    setDiscountCode('');
  };

  const calculateTotal = () => {
    if (!cartData?.items) return 0;
    const subtotal = cartData.items.reduce((total, item) => total + (item.price * item.qty), 0);
    const discount = appliedDiscount?.discountAmount || 0;
    return Math.max(0, subtotal - discount);
  };

  const handleSubmitOrder = async (e) => {
    e.preventDefault();
    setError(null);

    const shippingInfo = validateShippingInfo();
    if (!shippingInfo) {
      return;
    }

    if (!validatePaymentInfo()) {
      return;
    }

    try {
      setLoading(true);

      const orderData = {
        items: cartData.items.map(item => {
          // Extract product ID whether it's an object or string
          const productId = typeof item.product === 'object' ? item.product._id : item.product;
          return {
            product: productId,
            name: item.name,
            price: item.price,
            qty: item.qty,
            image: item.image,
            sku: item.sku || ''
          };
        }),
        subtotal: cartData.items.reduce((total, item) => total + (item.price * item.qty), 0),
        discount: appliedDiscount?.discountAmount || 0,
        discountCode: appliedDiscount ? discountCode : null,
        total: calculateTotal(),
        payment: {
          method: formData.paymentMethod || 'cod',
          status: 'pending',
          // Only store safe payment info
          ...(formData.paymentMethod === 'card' && {
            cardholderName: formData.cardholderName,
            // NOTE: In production, never send card digits to backend
            // Use Stripe/Payment gateway tokenization instead
            cardLastFour: formData.cardNumber.slice(-4)
          }),
          ...(formData.paymentMethod === 'bank' && {
            bankName: formData.bankName,
            accountNumber: formData.accountNumber,
            accountHolder: formData.accountHolder
          })
        },
        shipping: {
          address: {
            name: shippingInfo.name,
            phone: shippingInfo.phone,
            address: shippingInfo.address,
            city: shippingInfo.city,
            district: shippingInfo.district,
            ward: shippingInfo.ward
          },
          method: 'standard',
          fee: 0
        }
      };

      const order = await orderService.createOrder(orderData);
      
      // Process payment if card payment
      if (formData.paymentMethod === 'card') {
        try {
          await orderService.processCardPayment(order._id, {
            cardNumber: formData.cardNumber.replace(/\s/g, '').trim(),
            cardholderName: formData.cardholderName,
            expiryDate: formData.expiryDate,
            cvv: formData.cvv
          });
        } catch (paymentErr) {
          setError('❌ Xử lý thanh toán thất bại: ' + (paymentErr.response?.data?.error || paymentErr.message));
          return;
        }
      }
      
      // Clear cart after successful order
      await cartService.clearCart();

      // Navigate to success page
      navigate(`/order-success/${order._id}`, { 
        state: { order } 
      });
    } catch (err) {
      const errorMsg = err.response?.data?.error || err.response?.data?.message || err.message;
      const errorDetails = err.response?.data?.details || [];
      setError(`Không thể tạo đơn hàng: ${errorMsg}${errorDetails.length > 0 ? '\n' + errorDetails.join('\n') : ''}`);
    } finally {
      setLoading(false);
    }
  };

  if (loading && !cartData) {
    return (
      <div className="flex justify-center items-center h-screen">
        <div className="text-gray-500">Đang tải...</div>
      </div>
    );
  }

  if (error && !cartData) {
    return (
      <div className="min-h-screen bg-gray-50 flex items-center justify-center">
        <div className="text-center">
          <p className="text-2xl font-bold text-red-600 mb-4">{error}</p>
          <button
            onClick={() => navigate('/')}
            className="bg-primary text-white px-6 py-2 rounded hover:bg-primary/90"
          >
            Quay lại trang chủ
          </button>
        </div>
      </div>
    );
  }

  const total = calculateTotal();

  return (
    <div className="min-h-screen bg-gray-50 py-12 px-4">
      <div className="max-w-6xl mx-auto">
        <div className="mb-8">
          <h1 className="text-3xl font-bold text-dark mb-2">Thanh Toán</h1>
          <button
            onClick={() => navigate('/cart')}
            className="text-blue-600 hover:underline text-sm"
          >
            ← Quay lại giỏ hàng
          </button>
        </div>

        {error && (
          <div className="mb-4 p-4 bg-red-100 text-red-700 rounded">
            {error}
          </div>
        )}

        <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
          {/* Checkout Form */}
          <div className="lg:col-span-2">
            <form onSubmit={handleSubmitOrder}>
              {/* Shipping Information */}
              <div className="bg-white rounded-lg shadow p-6 mb-6">
                <h2 className="text-xl font-bold text-dark mb-6">Thông tin giao hàng</h2>

                <div className="grid grid-cols-1 md:grid-cols-2 gap-4 mb-4">
                  <div>
                    <label className="block text-sm font-semibold mb-2">Họ và tên *</label>
                    <input
                      type="text"
                      name="name"
                      value={formData.name}
                      onChange={handleInputChange}
                      required
                      maxLength={60}
                      pattern="[^0-9]*"
                      title="Họ và tên không được chứa số"
                      className="w-full border rounded px-3 py-2 focus:outline-none focus:border-primary"
                      placeholder="Nguyễn Văn A"
                    />
                    {fieldErrors.name && <p className="mt-1 text-xs text-red-600">{fieldErrors.name}</p>}
                  </div>
                  <div>
                    <label className="block text-sm font-semibold mb-2">Số điện thoại *</label>
                    <input
                      type="tel"
                      name="phone"
                      value={formData.phone}
                      onChange={handleInputChange}
                      required
                      pattern="0[0-9]{9,10}"
                      title="Số điện thoại bắt đầu bằng 0 và có 10-11 số"
                      className="w-full border rounded px-3 py-2 focus:outline-none focus:border-primary"
                      placeholder="0123456789"
                    />
                    {fieldErrors.phone && <p className="mt-1 text-xs text-red-600">{fieldErrors.phone}</p>}
                  </div>
                </div>

                <div className="mb-4">
                  <label className="block text-sm font-semibold mb-2">Địa chỉ *</label>
                  <input
                    type="text"
                    name="address"
                    value={formData.address}
                    onChange={handleInputChange}
                    required
                    className="w-full border rounded px-3 py-2 focus:outline-none focus:border-primary"
                    placeholder="123 Đường ABC, ..."
                  />
                  {fieldErrors.address && <p className="mt-1 text-xs text-red-600">{fieldErrors.address}</p>}
                </div>

                <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
                  <div>
                    <label className="block text-sm font-semibold mb-2">Tỉnh/Thành phố *</label>
                    <select
                      name="city"
                      value={formData.city}
                      onChange={handleInputChange}
                      required
                      className="w-full border rounded px-3 py-2 focus:outline-none focus:border-primary bg-white"
                    >
                      <option value="">Chọn Tỉnh/Thành phố</option>
                      {provinceOptions.map((province) => (
                        <option key={province.name} value={province.name}>
                          {province.name}
                        </option>
                      ))}
                    </select>
                    {fieldErrors.city && <p className="mt-1 text-xs text-red-600">{fieldErrors.city}</p>}
                  </div>
                  <div>
                    <label className="block text-sm font-semibold mb-2">Quận/Huyện *</label>
                    <select
                      name="district"
                      value={formData.district}
                      onChange={handleInputChange}
                      required
                      disabled={!formData.city || loadingDistricts || districtOptions.length === 0}
                      className="w-full border rounded px-3 py-2 focus:outline-none focus:border-primary bg-white disabled:bg-gray-100 disabled:text-gray-500"
                    >
                      {!formData.city && <option value="">Chọn Tỉnh/Thành phố trước</option>}
                      {formData.city && loadingDistricts && <option value="">Đang tải danh sách Quận/Huyện...</option>}
                      {formData.city && !loadingDistricts && districtOptions.length === 0 && (
                        <option value="">Không tải được danh sách Quận/Huyện</option>
                      )}
                      {formData.city && !loadingDistricts && districtOptions.length > 0 && (
                        <option value="">Chọn Quận/Huyện</option>
                      )}
                      {districtOptions.map((district) => (
                        <option key={district.code || district.name} value={district.name}>
                          {district.name}
                        </option>
                      ))}
                    </select>
                    {fieldErrors.district && <p className="mt-1 text-xs text-red-600">{fieldErrors.district}</p>}
                  </div>
                  <div>
                    <label className="block text-sm font-semibold mb-2">Phường/Xã *</label>
                    <select
                      name="ward"
                      value={formData.ward}
                      onChange={handleInputChange}
                      required
                      disabled={!formData.district || loadingWards || wardOptions.length === 0}
                      className="w-full border rounded px-3 py-2 focus:outline-none focus:border-primary bg-white disabled:bg-gray-100 disabled:text-gray-500"
                    >
                      {!formData.district && <option value="">Chọn Quận/Huyện trước</option>}
                      {formData.district && loadingWards && <option value="">Đang tải danh sách Phường/Xã...</option>}
                      {formData.district && !loadingWards && wardOptions.length === 0 && (
                        <option value="">Không tải được danh sách Phường/Xã</option>
                      )}
                      {formData.district && !loadingWards && wardOptions.length > 0 && (
                        <option value="">Chọn Phường/Xã</option>
                      )}
                      {wardOptions.map((ward) => (
                        <option key={ward} value={ward}>
                          {ward}
                        </option>
                      ))}
                    </select>
                    {fieldErrors.ward && <p className="mt-1 text-xs text-red-600">{fieldErrors.ward}</p>}
                  </div>
                </div>
              </div>

              {/* Payment Method */}
              <div className="bg-white rounded-lg shadow p-6 mb-6">
                <h2 className="text-xl font-bold text-dark mb-6">Phương thức thanh toán</h2>

                <div className="space-y-3">
                  <label className="flex items-center border rounded p-3 cursor-pointer hover:bg-gray-50">
                    <input
                      type="radio"
                      name="paymentMethod"
                      value="cod"
                      checked={formData.paymentMethod === 'cod'}
                      onChange={handleInputChange}
                      className="mr-3"
                    />
                    <span className="font-semibold">Thanh toán khi nhận hàng (COD)</span>
                  </label>

                  <label className="flex items-center border rounded p-3 cursor-pointer hover:bg-gray-50">
                    <input
                      type="radio"
                      name="paymentMethod"
                      value="card"
                      checked={formData.paymentMethod === 'card'}
                      onChange={handleInputChange}
                      className="mr-3"
                    />
                    <span className="font-semibold">Thẻ Tín Dụng/Ghi Nợ (Visa, Mastercard, ...)</span>
                  </label>

                  <label className="flex items-center border rounded p-3 cursor-pointer hover:bg-gray-50">
                    <input
                      type="radio"
                      name="paymentMethod"
                      value="bank"
                      checked={formData.paymentMethod === 'bank'}
                      onChange={handleInputChange}
                      className="mr-3"
                    />
                    <span className="font-semibold">Chuyển Khoản Ngân Hàng</span>
                  </label>

                  <label className="flex items-center border rounded p-3 cursor-pointer hover:bg-gray-50 opacity-50">
                    <input
                      type="radio"
                      name="paymentMethod"
                      value="momo"
                      disabled
                      onChange={handleInputChange}
                      className="mr-3"
                    />
                    <span className="font-semibold">MoMo (Sắp có)</span>
                  </label>

                  <label className="flex items-center border rounded p-3 cursor-pointer hover:bg-gray-50 opacity-50">
                    <input
                      type="radio"
                      name="paymentMethod"
                      value="vnpay"
                      disabled
                      onChange={handleInputChange}
                      className="mr-3"
                    />
                    <span className="font-semibold">VNPay (Sắp có)</span>
                  </label>
                </div>

                {/* Card Payment Form */}
                {formData.paymentMethod === 'card' && (
                  <div className="mt-6 border-t pt-6">
                    <h3 className="text-lg font-semibold text-dark mb-2">Thông tin thẻ tín dụng</h3>
                    <p className="text-sm text-blue-600 bg-blue-50 rounded p-3 mb-4">
                      💡 Để test: Nhập số thẻ <strong>4242 4242 4242 4242</strong>, tên bất kỳ, ngày hết hạn và CVV bất kỳ
                    </p>
                    <div className="space-y-4">
                      <div>
                        <label className="block text-sm font-semibold mb-1">Tên trên thẻ</label>
                        <input
                          type="text"
                          name="cardholderName"
                          value={formData.cardholderName}
                          onChange={handleInputChange}
                          className="w-full border rounded px-3 py-2 focus:outline-none focus:border-primary"
                          placeholder="VD: Trần Văn A"
                        />
                        {fieldErrors.cardholderName && <p className="mt-1 text-xs text-red-600">{fieldErrors.cardholderName}</p>}
                      </div>
                      <div>
                        <label className="block text-sm font-semibold mb-1">Số thẻ</label>
                        <input
                          type="text"
                          name="cardNumber"
                          value={formData.cardNumber}
                          onChange={handleInputChange}
                          className="w-full border rounded px-3 py-2 focus:outline-none focus:border-primary"
                          placeholder="4242 4242 4242 4242"
                        />
                        {fieldErrors.cardNumber && <p className="mt-1 text-xs text-red-600">{fieldErrors.cardNumber}</p>}
                      </div>
                      <div className="grid grid-cols-2 gap-4">
                        <div>
                          <label className="block text-sm font-semibold mb-1">Ngày hết hạn</label>
                          <input
                            type="text"
                            name="expiryDate"
                            value={formData.expiryDate}
                            onChange={handleInputChange}
                            className="w-full border rounded px-3 py-2 focus:outline-none focus:border-primary"
                            placeholder="MM/YY (vd: 12/25)"
                          />
                          {fieldErrors.expiryDate && <p className="mt-1 text-xs text-red-600">{fieldErrors.expiryDate}</p>}
                        </div>
                        <div>
                          <label className="block text-sm font-semibold mb-1">CVV</label>
                          <input
                            type="text"
                            name="cvv"
                            value={formData.cvv}
                            onChange={handleInputChange}
                            className="w-full border rounded px-3 py-2 focus:outline-none focus:border-primary"
                            placeholder="123 (bất kỳ)"
                          />
                          {fieldErrors.cvv && <p className="mt-1 text-xs text-red-600">{fieldErrors.cvv}</p>}
                        </div>
                      </div>
                    </div>
                  </div>
                )}

                {/* Bank Transfer Form */}
                {formData.paymentMethod === 'bank' && (
                  <div className="mt-6 border-t pt-6">
                    <h3 className="text-lg font-semibold text-dark mb-4">Thông tin tài khoản ngân hàng</h3>
                    <div className="space-y-4">
                      <div>
                        <label className="block text-sm font-semibold mb-1">Tên ngân hàng</label>
                        <input
                          type="text"
                          name="bankName"
                          value={formData.bankName}
                          onChange={handleInputChange}
                          className="w-full border rounded px-3 py-2 focus:outline-none focus:border-primary"
                          placeholder="VD: VietcomBank, BIDV, ..."
                        />
                        {fieldErrors.bankName && <p className="mt-1 text-xs text-red-600">{fieldErrors.bankName}</p>}
                      </div>
                      <div>
                        <label className="block text-sm font-semibold mb-1">Số tài khoản</label>
                        <input
                          type="text"
                          name="accountNumber"
                          value={formData.accountNumber}
                          onChange={handleInputChange}
                          className="w-full border rounded px-3 py-2 focus:outline-none focus:border-primary"
                          placeholder="VD: 1234567890"
                        />
                        {fieldErrors.accountNumber && <p className="mt-1 text-xs text-red-600">{fieldErrors.accountNumber}</p>}
                      </div>
                      <div>
                        <label className="block text-sm font-semibold mb-1">Chủ tài khoản</label>
                        <input
                          type="text"
                          name="accountHolder"
                          value={formData.accountHolder}
                          onChange={handleInputChange}
                          className="w-full border rounded px-3 py-2 focus:outline-none focus:border-primary"
                          placeholder="VD: Trần Văn A"
                        />
                        {fieldErrors.accountHolder && <p className="mt-1 text-xs text-red-600">{fieldErrors.accountHolder}</p>}
                      </div>
                    </div>
                  </div>
                )}
              </div>

              {/* Order Items */}
              <div className="bg-white rounded-lg shadow overflow-hidden">
                <div className="p-6 border-b">
                  <h2 className="text-xl font-bold text-dark">Chi tiết đơn hàng</h2>
                </div>
                <table className="w-full">
                  <thead className="bg-gray-100">
                    <tr>
                      <th className="text-left px-6 py-3">Sản phẩm</th>
                      <th className="text-center px-6 py-3">Giá</th>
                      <th className="text-center px-6 py-3">SL</th>
                      <th className="text-right px-6 py-3">Thành tiền</th>
                    </tr>
                  </thead>
                  <tbody>
                    {cartData.items.map((item) => (
                      <tr key={item._id} className="border-b">
                        <td className="px-6 py-4">
                          <p className="font-semibold text-dark">{item.name}</p>
                        </td>
                        <td className="px-6 py-4 text-center">
                          ₫{item.price?.toLocaleString('vi-VN')}
                        </td>
                        <td className="px-6 py-4 text-center">{item.qty}</td>
                        <td className="px-6 py-4 text-right font-semibold">
                          ₫{(item.price * item.qty)?.toLocaleString('vi-VN')}
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            </form>
          </div>

          {/* Order Summary */}
          <div className="lg:col-span-1">
            <div className="bg-white rounded-lg shadow p-6 sticky top-4">
              <h2 className="text-xl font-bold text-dark mb-6">Tóm tắt đơn hàng</h2>

              {/* Discount Code */}
              <div className="mb-6">
                <label className="block text-sm font-semibold mb-2">Mã giảm giá</label>
                <div className="flex gap-2">
                  <input
                    type="text"
                    value={discountCode}
                    onChange={(e) => setDiscountCode(e.target.value.toUpperCase())}
                    placeholder="Nhập mã giảm giá"
                    className="flex-1 border rounded px-3 py-2 focus:outline-none focus:border-primary"
                    disabled={appliedDiscount !== null}
                  />
                  {!appliedDiscount ? (
                    <button
                      type="button"
                      onClick={applyDiscountCode}
                      disabled={applyingDiscount || !discountCode.trim()}
                      className="bg-primary text-white px-4 py-2 rounded hover:bg-primary/90 disabled:bg-gray-400"
                    >
                      {applyingDiscount ? '...' : 'Áp dụng'}
                    </button>
                  ) : (
                    <button
                      type="button"
                      onClick={removeDiscount}
                      className="bg-red-500 text-white px-4 py-2 rounded hover:bg-red-600"
                    >
                      Xóa
                    </button>
                  )}
                </div>
                {appliedDiscount && (
                  <div className="mt-2 text-green-600 text-sm">
                    ✅ {appliedDiscount.promotion.name}: -₫{appliedDiscount.discountAmount.toLocaleString('vi-VN')}
                  </div>
                )}
              </div>

              <div className="space-y-4 mb-6">
                <div className="flex justify-between">
                  <span className="text-gray-600">Tạm tính:</span>
                  <span className="font-semibold">₫{cartData?.items?.reduce((total, item) => total + (item.price * item.qty), 0)?.toLocaleString('vi-VN')}</span>
                </div>
                {appliedDiscount && (
                  <div className="flex justify-between text-green-600">
                    <span>Giảm giá ({appliedDiscount.promotion.name}):</span>
                    <span>-₫{appliedDiscount.discountAmount.toLocaleString('vi-VN')}</span>
                  </div>
                )}
                <div className="flex justify-between">
                  <span className="text-gray-600">Giao hàng:</span>
                  <span className="font-semibold text-green-600">Miễn phí</span>
                </div>
                <div className="border-t pt-4 flex justify-between">
                  <span className="text-lg font-bold">Tổng cộng:</span>
                  <span className="text-lg font-bold text-primary">
                    ₫{calculateTotal()?.toLocaleString('vi-VN')}
                  </span>
                </div>
              </div>

              <button
                onClick={handleSubmitOrder}
                disabled={loading}
                className="w-full bg-primary text-white py-3 rounded font-semibold hover:bg-primary/90 disabled:bg-gray-400"
              >
                {loading ? 'Đang xử lý...' : 'Đặt hàng'}
              </button>
              <button
                onClick={() => navigate('/cart')}
                className="w-full border border-primary text-primary py-3 rounded font-semibold hover:bg-gray-50 mt-2"
              >
                Quay lại giỏ hàng
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
