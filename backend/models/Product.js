const mongoose = require('mongoose');

const productSchema = new mongoose.Schema({
    name: { type: String, required: true, trim: true },
    sku: { type: String, index: true, unique: true, sparse: true },
    description: { type: String, default: '' },
    price: { type: Number, required: true, min: 0 },
    category: { type: String, index: true },
    images: [{ type: String }],
    stock: { type: Number, default: 0, min: 0 },
    attributes: { type: mongoose.Schema.Types.Mixed },
    isActive: { type: Boolean, default: true },
}, { timestamps: true });

module.exports = mongoose.model('Product', productSchema);
