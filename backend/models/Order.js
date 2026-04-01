const mongoose = require('mongoose');

const orderItemSchema = new mongoose.Schema({
    product: { type: mongoose.Schema.Types.ObjectId, ref: 'Product', required: true },
    shopId: { type: mongoose.Schema.Types.ObjectId, ref: 'User', required: true },
    name: { type: String, required: true },
    sku: { type: String },
    price: { type: Number, required: true },
    originalPrice: { type: Number },
    qty: { type: Number, default: 1, min: 1 },
    variant: { type: String },
    image: { type: String },
    attributes: { type: mongoose.Schema.Types.Mixed }
});

const orderSchema = new mongoose.Schema({
    orderNumber: { type: String, unique: true, required: true },
    user: { type: mongoose.Schema.Types.ObjectId, ref: 'User', required: true },
    items: [orderItemSchema],
    subtotal: { type: Number, required: true },
    discount: {
        code: { type: String },
        amount: { type: Number, default: 0 },
        type: { type: String, enum: ['percentage', 'fixed'] }
    },
    shipping: {
        fee: { type: Number, default: 0 },
        method: { type: String, default: 'standard' },
        address: {
            name: { type: String, required: true },
            phone: { type: String, required: true },
            address: { type: String, required: true },
            city: { type: String, required: true },
            district: { type: String, required: true },
            ward: { type: String, required: true }
        },
        trackingNumber: { type: String },
        estimatedDelivery: { type: Date }
    },
    tax: { type: Number, default: 0 },
    total: { type: Number, required: true },
    status: {
        type: String,
        enum: ['chờ xác nhận', 'đã xác nhận', 'đang giao', 'đã nhận', 'đã hủy', 'trả hàng', 'hoàn tiền'],
        default: 'chờ xác nhận'
    },
    statusHistory: [{
        status: { type: String },
        timestamp: { type: Date, default: Date.now },
        note: { type: String }
    }],
    payment: {
        method: { type: String, enum: ['cod', 'momo', 'zalopay', 'vnpay', 'paypal', 'stripe', 'card', 'bank'], required: true },
        status: { type: String, enum: ['pending', 'paid', 'failed', 'refunded'], default: 'pending' },
        transactionId: { type: String },
        paidAt: { type: Date },
        refundAmount: { type: Number },
        refundReason: { type: String },
        // Card payment info (only safe data, no full card number or CVV)
        cardholderName: { type: String },
        cardLastFour: { type: String },
        // Bank transfer info
        bankName: { type: String },
        accountNumber: { type: String },
        accountHolder: { type: String }
    },
    notes: { type: String },
    cancellationReason: { type: String },
    returnReason: { type: String },
    invoice: {
        number: { type: String },
        url: { type: String }
    }
}, { timestamps: true });

// Indexes
orderSchema.index({ user: 1, createdAt: -1 });
orderSchema.index({ orderNumber: 1 });
orderSchema.index({ status: 1 });
orderSchema.index({ 'payment.status': 1 });

// Pre-save middleware to generate order number
orderSchema.pre('save', function(next) {
    if (this.isNew && !this.orderNumber) {
        this.orderNumber = 'ORD-' + Date.now() + '-' + Math.random().toString(36).substr(2, 5).toUpperCase();
    }
    next();
});

module.exports = mongoose.model('Order', orderSchema);
