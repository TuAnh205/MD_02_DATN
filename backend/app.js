require('dotenv').config();
const express = require('express');
const morgan = require('morgan');
const cors = require('cors');
const connectDB = require('./config/db');
const productRoutes = require('./routes/productRoutes');
const authRoutes = require('./routes/authRoutes');
const cartRoutes = require('./routes/cartRoutes');
const favoriteRoutes = require('./routes/favoriteRoutes');
const orderRoutes = require('./routes/orderRoutes');
const reviewRoutes = require('./routes/reviewRoutes');
const postRoutes = require('./routes/postRoutes');
const feedbackRoutes = require('./routes/feedbackRoutes');
const adminRoutes = require('./routes/adminRoutes');
const promotionRoutes = require('./routes/promotionRoutes');
const shopRoutes = require('./routes/shopRoutes');
const addressRoutes = require('./routes/addressRoutes');
const locationRoutes = require('./routes/locationRoutes');
const { seedDatabase } = require('./seeds/seed');

const app = express();
const autoSeedOnStart = String(process.env.AUTO_SEED_ON_START || 'true').toLowerCase() === 'true';

app.use(morgan('dev'));
app.use(cors());
app.use(express.json({ limit: '5mb' }));
app.use(express.urlencoded({ extended: true }));

app.use('/api/products', productRoutes);
app.use('/api/auth', authRoutes);
app.use('/api/cart', cartRoutes);
app.use('/api/favorites', favoriteRoutes);
app.use('/api/orders', orderRoutes);
app.use('/api/reviews', reviewRoutes);
app.use('/api/posts', postRoutes);
app.use('/api/feedback', feedbackRoutes);
app.use('/api/admin', adminRoutes);
app.use('/api/promotions', promotionRoutes);
app.use('/api/shop', shopRoutes);
app.use('/api/user/addresses', addressRoutes);
app.use('/api/locations', locationRoutes);

// Basic health
app.get('/health', (req, res) => res.json({ status: 'ok' }));

// Error handler
app.use((err, req, res, next) => {
    console.error(err);
    res.status(500).json({ message: 'Internal server error' });
});

const PORT = process.env.PORT || 5000;

const startServer = async () => {
    await connectDB();

    if (autoSeedOnStart) {
        await seedDatabase({ connect: false, exitOnFinish: false });
    }

    app.listen(PORT, () => console.log(`Server running on port ${PORT}`));
};

startServer();
