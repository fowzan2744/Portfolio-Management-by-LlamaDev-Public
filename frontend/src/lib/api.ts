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

export interface NewsItem {
  id: string;
  headline: string;
  source: string;
  publishedAt: string;
  summary: string;
  url: string;
  relatedSymbols: string[];
  thumbnail?: string;
}

export interface AIInsightsRequest {
  riskProfile: string;
  investmentHorizon: string;
  notes?: string;
}

export interface RebalancingAction {
  action: string;
  symbol: string;
  reason: string;
  suggestedAllocation?: number;
}

export interface AIInsightsResponse {
  summary: string;
  risks: string[];
  opportunities: string[];
  rebalancingActions: RebalancingAction[];
  sentimentAnalysis: string;
  nextSteps: string[];
  isAiGenerated: boolean;
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

  async getPortfolioNews(): Promise<NewsItem[]> {
    try {
      const response = await fetch('http://localhost:8080/api/news/portfolio');
      if (!response.ok) {
        throw new Error(`Failed to fetch portfolio news: ${response.status}`);
      }
      return response.json();
    } catch (err) {
      console.error('Error fetching portfolio news:', err);
      return [];
    }
  },

  async getMarketNews(): Promise<NewsItem[]> {
    try {
      const response = await fetch('http://localhost:8080/api/news/market');
      if (!response.ok) {
        throw new Error(`Failed to fetch market news: ${response.status}`);
      }
      return response.json();
    } catch (err) {
      console.error('Error fetching market news:', err);
      return [];
    }
  },

  async getSummary(): Promise<PortfolioSummary> {
    try {
      const response = await fetch(`${API_BASE_URL}/summary`);
      if (!response.ok) {
        throw new Error(`Failed to fetch summary: ${response.status}`);
      }
      return response.json();
    } catch (err) {
      console.error('Error fetching portfolio summary:', err);
      throw err;
    }
  },

  async getGrowth(range: '1D' | '1W'): Promise<PortfolioGrowth[]> {
    try {
      const response = await fetch(`${API_BASE_URL}/growth?range=${range}`);
      if (!response.ok) {
        throw new Error(`Failed to fetch growth data: ${response.status}`);
      }
      return response.json();
    } catch (err) {
      console.error('Error fetching growth data:', err);
      throw err;
    }
  },

  async analyzePortfolio(request: AIInsightsRequest): Promise<AIInsightsResponse> {
    try {
      const response = await fetch('http://localhost:8080/api/ai/analyze', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify(request),
      });
      if (!response.ok) {
        throw new Error(`Failed to analyze portfolio: ${response.status}`);
      }
      return response.json();
    } catch (err) {
      console.error('Error analyzing portfolio:', err);
      throw err;
    }
  },
};
