# Revenue API Integration Documentation

## Overview

The revenue tracking system is fully integrated with the backend API to display real-time sales data, graphs, and recent transactions.

---

## API Integration Flow

### 1. **Backend API Endpoint**

**Path:** `/api/shop/revenue?period={week|month|year}`  
**Method:** GET  
**Authentication:** Bearer Token

**Response Structure:**

```json
{
  "period": "month",
  "startDate": "2026-05-01T00:00:00.000Z",
  "summary": {
    "totalGrossRevenue": 1000000,
    "totalPlatformFees": 50000,
    "totalNetRevenue": 950000,
    "totalOrders": 25,
    "totalProducts": 45,
    "newCustomers": 12,
    "platformFeeRate": 0.05
  },
  "chart": {
    "values": [200000, 250000, 180000, 220000, 150000]
  },
  "chartNote": "Tăng trưởng so với kỳ trước +10%",
  "productDetails": [
    {
      "orderId": "...",
      "orderNumber": "#ORD-001",
      "productName": "iPhone 15 Pro Max",
      "productImage": "https://...",
      "sku": "SKU-12345",
      "price": 32500000,
      "quantity": 1,
      "grossAmount": 32500000,
      "platformFeeRate": 0.05,
      "platformFee": 1625000,
      "netAmount": 30875000,
      "paidAt": "2026-05-23T10:30:00.000Z",
      "feeStatus": "charged"
    }
    // ... more transactions
  ],
  "policy": { ... }
}
```

### 2. **Android Implementation**

#### RevenueActivity.java

**Location:** `app/src/main/java/.../screens/RevenueActivity.java`

**Key Functions:**

##### loadRevenue(String period)

- Fetches revenue data from API with selected period (week/month/year)
- Shows loading progress indicator
- Calls `/api/shop/revenue?period={period}`
- Includes Bearer token in Authorization header

```java
private void loadRevenue(String period) {
    currentPeriod = period;
    updatePeriodButtons(period);
    progressRevenue.setVisibility(View.VISIBLE);

    String token = SessionManager.getToken(this);
    String url = NetworkConstants.getApiBaseUrl() + "/api/shop/revenue?period=" + period;

    JsonObjectRequest request = new JsonObjectRequest(
        Request.Method.GET, url, null,
        response -> {
            progressRevenue.setVisibility(View.GONE);
            updateRevenueUi(response);
        },
        error -> {
            progressRevenue.setVisibility(View.GONE);
            showError("Không thể tải dữ liệu doanh thu");
        }
    ) {
        @Override
        public Map<String, String> getHeaders() {
            Map<String, String> headers = new HashMap<>();
            headers.put("Authorization", "Bearer " + token);
            headers.put("Content-Type", "application/json");
            return headers;
        }
    };

    Volley.newRequestQueue(this).add(request);
}
```

##### updateRevenueUi(JSONObject response)

- Parses API response
- Updates all 4 stat cards (Total Revenue, Orders, New Customers, Products)
- Updates chart with 5 revenue buckets
- Populates RecyclerView with transaction list

```java
private void updateRevenueUi(JSONObject response) {
    JSONObject summary = response.optJSONObject("summary");

    // Update stat cards
    tvCardRevenueTotal.setText(formatPrice(summary.optDouble("totalGrossRevenue", 0)));
    tvCardOrders.setText(String.valueOf(summary.optInt("totalOrders", 0)));
    tvCardNewCustomers.setText(String.valueOf(summary.optInt("newCustomers", 0)));
    tvCardProducts.setText(String.valueOf(summary.optInt("totalProducts", 0)));

    // Update chart
    JSONArray chartValues = response.optJSONObject("chart").optJSONArray("values");
    updateChart(chartValues);

    // Load transactions
    JSONArray productDetails = response.optJSONArray("productDetails");
    revenueTransactions.clear();
    for (int i = 0; i < productDetails.length(); i++) {
        JSONObject item = productDetails.optJSONObject(i);
        RevenueTransaction transaction = new RevenueTransaction(
            item.optString("orderNumber"),
            item.optString("productName"),
            item.optString("productImage"),
            item.optString("sku"),
            item.optInt("quantity"),
            item.optDouble("grossAmount"),
            item.optDouble("platformFee"),
            item.optDouble("netAmount"),
            item.optString("paidAt"),
            item.optString("feeStatus")
        );
        revenueTransactions.add(transaction);
    }
    revenueAdapter.notifyDataSetChanged();
}
```

##### updateChart(JSONArray values)

- Creates animated bar chart with 5 bars
- Heights scale dynamically based on revenue values
- Bars stored in `chartBars` array (barRevenue1-5)
- Max height = 32dp (minimum) + 80dp (scaled by revenue)

```java
private void updateChart(JSONArray values) {
    double max = 0;
    for (int i = 0; i < values.length() && i < chartBars.length; i++) {
        max = Math.max(max, values.optDouble(i, 0));
    }
    if (max <= 0) max = 1;

    for (int i = 0; i < chartBars.length; i++) {
        double value = values.optDouble(i, 0);
        int height = dpToPx(32) + (int) ((dpToPx(80) * value) / max);

        View bar = chartBars[i];
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) bar.getLayoutParams();
        params.height = height;
        bar.setLayoutParams(params);
    }
}
```

#### Layout Components

**activity_revenue.xml Structure:**

```
ScrollView
├─ LinearLayout (Main Content)
│  ├─ TopBar (Back button, Title, Notifications)
│  ├─ Period Selector (Week/Month/Year buttons)
│  ├─ Stat Cards (4 cards in 2x2 grid)
│  │  ├─ Total Revenue
│  │  ├─ Orders Count
│  │  ├─ New Customers
│  │  └─ Products
│  ├─ Chart Section
│  │  ├─ LineChart (MPAndroidChart)
│  │  ├─ Chart Note (Growth percentage)
│  │  └─ Bar Chart (5 bars: barRevenue1-5)
│  ├─ Transactions Section
│  │  ├─ RecyclerView (rvRevenueTransactions)
│  │  └─ Empty State (tvRevenueEmpty)
│  └─ Old Transactions Card (Hardcoded examples)
```

#### RevenueTransactionAdapter.java

**Location:** `app/src/main/java/.../Adapters/RevenueTransactionAdapter.java`

**Features:**

- Displays each transaction with product image, name, SKU, quantity
- Shows order number and payment date
- Displays gross amount (收费), platform fee, and net revenue
- Color-coded status badges (Success/Processing/Cancelled)
- Image loading with Glide library
- Vietnamese locale price formatting

**ViewHolder Layout (item_revenue_transaction.xml):**

```
LinearLayout
├─ ImageView (Product image)
├─ LinearLayout (Text info)
│  ├─ ProductName
│  ├─ OrderInfo (Order# + Date)
│  ├─ ProductMeta (SKU + Quantity)
│  ├─ ProductAmount (Net revenue)
│  └─ ProductSubtitle (Revenue breakdown)
└─ Status Badge
```

---

## Data Flow Diagram

```
User opens RevenueActivity
        ↓
RevenueActivity.onCreate()
        ↓
setupPeriodButtons() + loadRevenue("month")
        ↓
API Call: GET /api/shop/revenue?period=month
        ↓ (with Bearer token)
        ↓
Backend shopController.getShopRevenue()
        ↓
Returns: {summary, chart, productDetails, ...}
        ↓
updateRevenueUi(response)
        ↓
├─ Update Stat Cards (tvCardRevenueTotal, tvCardOrders, etc.)
├─ Update Chart (updateChart with chartBuckets[0-4])
├─ Update ProgressBar (hidden after loading)
└─ Update RecyclerView with transactions (revenueTransactions list)
        ↓
RevenueTransactionAdapter.onBindViewHolder()
        ↓
Display each transaction in RecyclerView
```

---

## Revenue Calculation (Backend)

### For Each Period:

1. **Query all PAID orders** from `startDate` to now
2. **Filter by shop ID** to get only shop's items
3. **Calculate for each item:**
   - Gross Amount = product price × quantity
   - Platform Fee = Gross Amount × 5%
   - Net Revenue = Gross Amount - Platform Fee

4. **Aggregate statistics:**
   - Total Gross Revenue = sum of all gross amounts
   - Total Platform Fees = sum of all platform fees (5%)
   - Total Net Revenue = sum of all net amounts
   - Total Orders = distinct order count
   - Total Products = sum of all quantities
   - New Customers = distinct customer count

5. **Create 5 chart buckets:**
   - Divide time period into 5 equal buckets
   - Accumulate gross revenue in each bucket
   - Calculate percentage growth vs first bucket

---

## Period-Based Filtering

| Period    | Start Date                  |
| --------- | --------------------------- |
| **day**   | Today at 00:00              |
| **week**  | First day of current week   |
| **month** | First day of current month  |
| **year**  | January 1st of current year |

---

## Features Implemented

✅ **Real-time API Integration**

- Fetches live revenue data from backend
- Bearer token authentication
- Error handling with user notifications

✅ **Dynamic Stat Cards**

- Total Gross Revenue
- Total Orders Count
- New Customers Count
- Total Products Sold

✅ **Interactive Chart**

- 5-bucket revenue distribution
- Dynamic bar heights (32dp-112dp range)
- Growth percentage indicator
- Responsive to data changes

✅ **Transaction List**

- RecyclerView with adapter pattern
- Product image loading via Glide
- Revenue breakdown per transaction
- Status color coding
- Vietnamese currency formatting
- Sorted by payment date (newest first)

✅ **Period Switching**

- Week/Month/Year tabs
- Dynamic data refresh on period change
- Button state management

---

## Testing Checklist

- [x] API endpoint returns valid revenue data
- [x] RevenueActivity receives and parses response
- [x] Stat cards display correct summary values
- [x] Chart bars render with correct heights
- [x] RecyclerView populates with transactions
- [x] Period switching loads new data
- [x] Error handling shows appropriate messages
- [x] Progress indicator shows during loading
- [x] Empty state displayed when no transactions
- [x] Price formatting uses Vietnamese locale

---

## Troubleshooting

### If chart bars don't appear:

1. Check if `chartValues` array has 5 or more elements
2. Verify `updateChart()` is being called with valid JSONArray
3. Check bar heights in LinearLayout params

### If transactions don't load:

1. Verify `productDetails` array exists in API response
2. Check if shop has any paid orders in the period
3. Enable verbose logging in RevenueActivity

### If API call fails:

1. Check token validity (SessionManager.getToken())
2. Verify API URL in NetworkConstants
3. Check server logs for authentication errors

---

## Future Enhancements

- [ ] Add comparison with previous period
- [ ] Export revenue data to PDF/CSV
- [ ] Monthly recurring revenue tracking
- [ ] Customer segment analysis
- [ ] Top products by revenue
- [ ] Refund tracking integration
