const axios = require('axios');

const API_URL = 'http://localhost:5000/api';

const loginAdmin = async () => {
  try {
    const response = await axios.post(`${API_URL}/auth/login`, {
      email: 'admin@test.com',
      password: '123456'
    });

    console.log('Login successful, token:', response.data.token);
    return response.data.token;
  } catch (error) {
    console.error('Login failed:', error.response?.data || error.message);
    return null;
  }
};

const testAdminOrders = async (token) => {
  try {
    const response = await axios.get(`${API_URL}/admin/orders`, {
      headers: {
        'Authorization': `Bearer ${token}`
      }
    });

    console.log('Orders API Response:');
    console.log(`Found ${response.data.orders?.length || 0} orders`);
    if (response.data.orders?.length > 0) {
      response.data.orders.forEach((order, index) => {
        console.log(`${index + 1}. Order: ${order._id}`);
        console.log(`   User: ${order.user?.name} (${order.user?.email})`);
        console.log(`   Total: ${order.total}`);
        console.log(`   Status: ${order.status}`);
        console.log(`   Items: ${order.items?.length}`);
        console.log('---');
      });
    }
  } catch (error) {
    console.error('Error fetching orders:', error.response?.data || error.message);
  }
};

const run = async () => {
  const token = await loginAdmin();
  if (token) {
    await testAdminOrders(token);
  }
};

run();