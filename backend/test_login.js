const axios = require('axios');

const API_URL = 'http://localhost:5000/api';

const testLogin = async (email, password) => {
  try {
    console.log(`Testing login for ${email}...`);
    const response = await axios.post(`${API_URL}/auth/login`, {
      email,
      password
    });

    console.log('Login successful!');
    console.log('Token:', response.data.token);
    console.log('User:', response.data.user);
    return response.data.token;
  } catch (error) {
    console.error(`Login failed for ${email}:`, error.response?.data || error.message);
    return null;
  }
};

const testAdminOrders = async (token) => {
  try {
    console.log('Testing admin orders API...');
    const response = await axios.get(`${API_URL}/admin/orders`, {
      headers: {
        'Authorization': `Bearer ${token}`
      }
    });

    console.log(`Found ${response.data.orders?.length || 0} orders`);
    return response.data;
  } catch (error) {
    console.error('Admin orders API failed:', error.response?.data || error.message);
    return null;
  }
};

const run = async () => {
  // Test admin login
  const adminToken = await testLogin('admin@test.com', '123456');
  if (adminToken) {
    await testAdminOrders(adminToken);
  }

  // Test user login
  const userToken = await testLogin('user@test.com', '123456');
  if (userToken) {
    console.log('User can login successfully');
  }
};

run();