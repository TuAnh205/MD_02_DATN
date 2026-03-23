const mongoose = require('mongoose');

const promotionSchema = new mongoose.Schema({
    name: { type: String, required: true },
    description: { type: String },
    type: {
        type: String,
        enum: ['percentage', 'fixed', 'buy_x_get_y', 'free_shipping'],
        required: true
    },
    value: { type: Number, min: 0 }, // percentage or fixed amount
    code: { type: String, unique: true, sparse: true }, // discount code
    conditions: {
        minOrderValue: { type: Number, default: 0 },
        maxDiscount: { type: Number }, // max discount amount for percentage
        applicableProducts: [{ type: mongoose.Schema.Types.ObjectId, ref: 'Product' }],
        applicableCategories: [{ type: String }],
        applicableBrands: [{ type: String }],
        userSpecific: [{ type: mongoose.Schema.Types.ObjectId, ref: 'User' }], // specific users
        firstTimeOnly: { type: Boolean, default: false },
        usageLimit: { type: Number }, // total usage limit
        userUsageLimit: { type: Number, default: 1 } // per user
    },
    isActive: { type: Boolean, default: true },
    startDate: { type: Date, default: Date.now },
    endDate: { type: Date },
    createdBy: { type: mongoose.Schema.Types.ObjectId, ref: 'User', required: true }
}, { timestamps: true });

// Index for efficient queries
promotionSchema.index({ code: 1 });
promotionSchema.index({ type: 1 });
promotionSchema.index({ isActive: 1, startDate: 1, endDate: 1 });

module.exports = mongoose.model('Promotion', promotionSchema);