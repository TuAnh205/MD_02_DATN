const express = require('express');
const axios = require('axios');
const router = express.Router();

let cachedLocations = null;

const fetchLocationsFromRemote = async () => {
  const response = await axios.get('https://provinces.open-api.vn/api/?depth=3');
  if (!Array.isArray(response.data)) {
    throw new Error('Dữ liệu địa lý từ nguồn ngoài không hợp lệ');
  }

  return response.data.map((province) => ({
    name: province.name,
    code: province.code,
    districts: Array.isArray(province.districts)
      ? province.districts.map((district) => ({
          name: district.name,
          code: district.code,
          wards: Array.isArray(district.wards)
            ? district.wards.map((ward) => (typeof ward === 'string' ? ward : ward.name))
            : []
        }))
      : []
  }));
};

const getLocations = async () => {
  if (cachedLocations) return cachedLocations;
  const remoteLocations = await fetchLocationsFromRemote();
  cachedLocations = remoteLocations;
  return cachedLocations;
};

router.get('/', async (req, res) => {
  try {
    const locations = await getLocations();
    res.json({ locations });
  } catch (err) {
    res.status(500).json({ message: err.message || 'Không tải được dữ liệu địa lý' });
  }
});

module.exports = router;
