const mongoose = require('mongoose');

const cartItemSchema = new mongoose.Schema({
    product: { type: mongoose.Schema.Types.ObjectId, ref: 'Product', required: true },
    name: { type: String, required: true },
    price: { type: Number, required: true },
    qty: { type: Number, default: 1, min: 1 },
    variant: { type: String },
    image: { type: String },
    attributes: { type: mongoose.Schema.Types.Mixed }
});

const cartSchema = new mongoose.Schema({
    user: { type: mongoose.Schema.Types.ObjectId, ref: 'User', required: true, unique: true },
    items: [cartItemSchema],
    voucher: {
        code: { type: String },
        discount: { type: Number, default: 0 },
        type: { type: String, enum: ['percentage', 'fixed'] }
    }
}, { timestamps: true });

cartSchema.index({ user: 1 });

module.exports = mongoose.model('Cart', cartSchema);