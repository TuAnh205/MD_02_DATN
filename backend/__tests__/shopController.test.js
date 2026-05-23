const mockFind = jest.fn();
const mockAggregate = jest.fn();
const mockUserFindById = jest.fn();

jest.mock("../models/Order", () => ({
  find: (...args) => mockFind(...args),
  aggregate: (...args) => mockAggregate(...args),
}));

jest.mock("../models/User", () => ({
  findById: (...args) => mockUserFindById(...args),
}));

jest.mock("../models/Product", () => ({}));
jest.mock("../models/Review", () => ({}));
jest.mock("../models/Notification", () => ({}));

jest.mock("../config/shopBillingPolicy", () => ({
  SHOP_BILLING_POLICY: {
    feeStartDate: new Date("2020-01-01T00:00:00.000Z"),
    commissionRate: 0.05,
    version: "1.0.0",
  },
  roundCurrency: (value) => Number(Number(value).toFixed(2)),
}));

jest.mock("../services/shopBillingService", () => ({
  syncShopBilling: jest.fn(),
  topUpShopWallet: jest.fn(),
}));

const shopController = require("../controllers/shopController");

describe("shopController.getShopRevenue", () => {
  beforeEach(() => {
    jest.clearAllMocks();
    jest.useFakeTimers().setSystemTime(new Date("2026-05-23T12:00:00.000Z"));
  });

  afterEach(() => {
    jest.useRealTimers();
  });

  test("returns revenue data for delivered orders even when payment is not paid", async () => {
    const shopId = "507f1f77bcf86cd799439011";
    const deliveredOrder = {
      _id: "111111111111111111111111",
      orderNumber: "ORD-1",
      user: shopId,
      status: "đã nhận",
      payment: {
        status: "pending",
      },
      createdAt: new Date("2026-05-10T09:00:00.000Z"),
      updatedAt: new Date("2026-05-11T10:00:00.000Z"),
      items: [
        {
          _id: "222222222222222222222222",
          shopId,
          product: "333333333333333333333333",
          name: "Sản phẩm A",
          image: "img-a.png",
          sku: "SKU-1",
          price: 100,
          qty: 2,
        },
      ],
    };

    mockFind.mockReturnValue({
      select: jest.fn().mockImplementation((projection) => {
        if (projection.includes("user")) {
          return Promise.resolve([deliveredOrder]);
        }

        return Promise.resolve([
          {
            ...deliveredOrder,
            user: undefined,
          },
        ]);
      }),
    });

    mockAggregate.mockResolvedValue([
      {
        totalOrders: ["111111111111111111111111"],
        totalProducts: 2,
      },
    ]);

    mockUserFindById.mockReturnValue({
      select: jest.fn().mockResolvedValue({
        shopStatus: "active",
        shopWallet: { balance: 0 },
        shopBillingSummary: { outstandingAmount: 0 },
      }),
    });

    const req = {
      user: { id: shopId },
      query: { period: "month" },
    };

    const res = {
      status: jest.fn().mockReturnThis(),
      json: jest.fn(),
    };

    await shopController.getShopRevenue(req, res);

    expect(mockFind).toHaveBeenCalled();
    expect(res.status).not.toHaveBeenCalled();
    expect(res.json).toHaveBeenCalledWith(
      expect.objectContaining({
        period: "month",
        summary: expect.objectContaining({
          totalGrossRevenue: 200,
          totalPlatformFees: 10,
          totalNetRevenue: 190,
          totalOrders: 1,
          totalProducts: 2,
        }),
      }),
    );
  });
});
