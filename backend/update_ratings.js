const mongoose = require('mongoose');
const Review = require('./models/Review');
const Product = require('./models/Product');
require('dotenv').config();

async function updateAllProductRatings() {
    try {
        await mongoose.connect(process.env.MONGODB_URI || 'mongodb://localhost:27017/md02_datn');

        const products = await Product.find({});
        console.log(`Found ${products.length} products`);

        for (const product of products) {
            const reviews = await Review.find({ product: product._id });
            const count = reviews.length;
            const average = count > 0 ? reviews.reduce((sum, r) => sum + r.rating, 0) / count : 0;

            await Product.findByIdAndUpdate(product._id, {
                'ratings.average': average,
                'ratings.count': count
            });

            console.log(`Updated ${product.name}: ${average.toFixed(1)} (${count} reviews)`);
        }

        console.log('All product ratings updated successfully');
    } catch (err) {
        console.error('Error updating ratings:', err);
    } finally {
        await mongoose.disconnect();
    }
}

updateAllProductRatings();