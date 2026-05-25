const mockVoucherFindOne = jest.fn();
const mockVoucherDelete = jest.fn();
const mockVoucherSave = jest.fn();
const mockUserFindById = jest.fn();
const mockUserSave = jest.fn();

jest.mock("../models/Order", () => ({
  findById: jest.fn(),
}));

jest.mock("../models/Product", () => ({
  findByIdAndUpdate: jest.fn(),
}));

jest.mock("../models/Notification", () => ({}));

jest.mock("../models/User", () => ({
  findById: (...args) => mockUserFindById(...args),
}));

jest.mock("../models/Voucher", () => ({
  findOne: (...args) => mockVoucherFindOne(...args),
  findByIdAndDelete: (...args) => mockVoucherDelete(...args),
}));

jest.mock("../config/shopBillingPolicy", () => ({
  SHOP_BILLING_POLICY: {
    commissionRate: 0.05,
  },
  roundCurrency: (value) => Number(value),
  isOrderInFeeablePeriod: () => true,
}));

const {
  updateVoucherUsageAfterOrder,
} = require("../controllers/orderController");

describe("updateVoucherUsageAfterOrder", () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  test("xoá voucher khỏi cơ sở dữ liệu khi đạt giới hạn sử dụng", async () => {
    const userEntry = {
      voucher: { code: "SAVE10" },
      usedCount: 0,
      isConsumed: false,
    };

    const userRecord = {
      userVouchers: [userEntry],
      save: mockUserSave,
    };

    mockVoucherFindOne.mockResolvedValue({
      _id: "voucher-1",
      code: "SAVE10",
      usageLimit: 1,
      userLimit: 2,
      usedCount: 0,
      save: mockVoucherSave,
    });
    mockVoucherDelete.mockResolvedValue({});
    mockUserFindById.mockReturnValue({
      populate: jest.fn().mockResolvedValue(userRecord),
    });
    mockUserSave.mockResolvedValue(userRecord);

    await updateVoucherUsageAfterOrder({
      user: "user-1",
      discount: { code: "SAVE10" },
    });

    expect(mockVoucherFindOne).toHaveBeenCalledWith({ code: "SAVE10" });
    expect(mockVoucherDelete).toHaveBeenCalledWith("voucher-1");
    expect(mockVoucherSave).not.toHaveBeenCalled();
    expect(mockUserFindById).toHaveBeenCalledWith("user-1");
    expect(userEntry.usedCount).toBe(1);
    expect(userEntry.isConsumed).toBe(true);
    expect(mockUserSave).toHaveBeenCalled();
  });
});
