// Simple EventEmitter for order status changes
class OrderStatusEmitter {
  constructor() {
    this.listeners = [];
  }

  on(callback) {
    this.listeners.push(callback);
    return () => {
      this.listeners = this.listeners.filter(l => l !== callback);
    };
  }

  emit(data) {
    this.listeners.forEach(callback => {
      try {
        callback(data);
      } catch (error) {
        console.error("Error in event listener:", error);
      }
    });
  }
}

export default new OrderStatusEmitter();
