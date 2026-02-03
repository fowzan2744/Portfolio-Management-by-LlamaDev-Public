const API_BASE_URL = 'http://localhost:8080/api/portfolio';

export interface Holding {
  id: string;
  symbol: string;
  name: string;
  shares: number;
  avgCost: number;
  currentPrice: number;
  value: number;
  allocation: number;
  dailyChange: number;
  dailyChangePercent: number;
}

export interface PortfolioSummary {
  totalValue: number;
  totalInvested: number;
  totalGain: number;
  totalGainPercent: number;
  dailyChange: number;
  dailyChangePercent: number;
  dataAvailableSince?: string;
}

export interface PortfolioGrowth {
  time: string;
  value: number;
}

export interface Allocation {
  name: string;
  value: number;
  fullName: string;
}

export const api = {
  async getHoldings(): Promise<Holding[]> {
    try {
      const response = await fetch(`${API_BASE_URL}/holdings`);
      if (!response.ok) {
        throw new Error(`Failed to fetch holdings: ${response.status} ${response.statusText}`);
      }
      return response.json();
    } catch (err) {
      if (err instanceof TypeError && err.message.includes('fetch')) {
        throw new Error('Cannot connect to backend. Make sure it is running on http://localhost:8080');
      }
      throw err;
    }
  },

  async getPortfolioSummary(): Promise<PortfolioSummary> {
    try {
      const response = await fetch(`${API_BASE_URL}/summary`);
      if (!response.ok) {
        throw new Error(`Failed to fetch portfolio summary: ${response.status} ${response.statusText}`);
      }
      return response.json();
    } catch (err) {
      if (err instanceof TypeError && err.message.includes('fetch')) {
        throw new Error('Cannot connect to backend. Make sure it is running on http://localhost:8080');
      }
      throw err;
    }
  },

  async getPortfolioGrowth(range: '1D' | '1W'): Promise<PortfolioGrowth[]> {
    try {
      const response = await fetch(`${API_BASE_URL}/growth?range=${range}`);
      if (!response.ok) {
        throw new Error(`Failed to fetch portfolio growth: ${response.status} ${response.statusText}`);
      }
      return response.json();
    } catch (err) {
      if (err instanceof TypeError && err.message.includes('fetch')) {
        throw new Error('Cannot connect to backend. Make sure it is running on http://localhost:8080');
      }
      throw err;
    }
  },

  async getAllocation(): Promise<Allocation[]> {
    try {
      const response = await fetch(`${API_BASE_URL}/allocation`);
      if (!response.ok) {
        throw new Error(`Failed to fetch allocation: ${response.status} ${response.statusText}`);
      }
      return response.json();
    } catch (err) {
      if (err instanceof TypeError && err.message.includes('fetch')) {
        throw new Error('Cannot connect to backend. Make sure it is running on http://localhost:8080');
      }
      throw err;
    }
  },

  async addAsset(ticker: string, shares: number, avgCost: number): Promise<Holding> {
    const response = await fetch(`${API_BASE_URL}/assets`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({
        ticker: ticker.toUpperCase(),
        quantity: shares,
        avgBuyPrice: avgCost,
      }),
    });
    if (!response.ok) {
      const error = await response.text();
      throw new Error(error || 'Failed to add asset');
    }
    return response.json();
  },

  async removeAsset(ticker: string, quantity: number): Promise<void> {
    const response = await fetch(`${API_BASE_URL}/assets`, {
      method: 'DELETE',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({
        ticker: ticker.toUpperCase(),
        quantity: quantity,
      }),
    });
    if (!response.ok) {
      const error = await response.text();
      throw new Error(error || 'Failed to remove asset');
    }
  },
};
