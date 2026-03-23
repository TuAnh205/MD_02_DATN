const mongoose = require('mongoose');

const productSchema = new mongoose.Schema({
    name: { type: String, required: true, trim: true },
    sku: { type: String, index: true, unique: true, sparse: true },
    description: { type: String, default: '' },
    detailedDescription: { type: String, default: '' },
    price: { type: Number, required: true, min: 0 },
    originalPrice: { type: Number, min: 0 },
    category: { type: String, index: true },
    subcategory: { type: String },
    brand: { type: String },
    image: { type: String },
    images: [{ type: String }],
    videos: [{ type: String }],
    stock: { type: Number, default: 0, min: 0 },
    attributes: { type: mongoose.Schema.Types.Mixed },
    variants: [{
        name: { type: String },
        value: { type: String },
        priceModifier: { type: Number, default: 0 },
        stock: { type: Number, default: 0 }
    }],
    tags: [{ type: String }],
    isActive: { type: Boolean, default: true },
    isFeatured: { type: Boolean, default: false },
    isBestSeller: { type: Boolean, default: false },
    isNew: { type: Boolean, default: false },
    weight: { type: Number, min: 0 }, // for shipping calculation
    dimensions: {
        length: { type: Number },
        width: { type: Number },
        height: { type: Number }
    },
    seo: {
        metaTitle: { type: String },
        metaDescription: { type: String },
        slug: { type: String, unique: true, sparse: true }
    },
    ratings: {
        average: { type: Number, default: 0, min: 0, max: 5 },
        count: { type: Number, default: 0 }
    },
    discount: {
        type: { type: String, enum: ['percentage', 'fixed'], default: 'percentage' },
        value: { type: Number, default: 0, min: 0 },
        startDate: { type: Date },
        endDate: { type: Date },
        isActive: { type: Boolean, default: false }
    },
    createdBy: { type: mongoose.Schema.Types.ObjectId, ref: 'User', required: true },
    promotions: [{ type: mongoose.Schema.Types.ObjectId, ref: 'Promotion' }],
}, { timestamps: true });

// Indexes for search and filtering
productSchema.index({ name: 'text', description: 'text', tags: 'text' });
productSchema.index({ category: 1, subcategory: 1 });
productSchema.index({ price: 1 });
productSchema.index({ 'ratings.average': -1 });
productSchema.index({ salesCount: -1 });
productSchema.index({ createdAt: -1 });
productSchema.index({ isFeatured: -1, isActive: 1 });
productSchema.index({ isBestSeller: -1, isActive: 1 });
productSchema.index({ isNew: -1, isActive: 1 });

module.exports = mongoose.model('Product', productSchema);
