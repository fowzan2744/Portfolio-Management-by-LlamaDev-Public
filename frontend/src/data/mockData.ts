// Mock portfolio data

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

export const holdings: Holding[] = [
  {
    id: "1",
    symbol: "AAPL",
    name: "Apple Inc.",
    shares: 50,
    avgCost: 150.0,
    currentPrice: 178.50,
    value: 8925.0,
    allocation: 28.5,
    dailyChange: 125.0,
    dailyChangePercent: 1.42,
  },
  {
    id: "2",
    symbol: "MSFT",
    name: "Microsoft Corporation",
    shares: 30,
    avgCost: 280.0,
    currentPrice: 378.25,
    value: 11347.5,
    allocation: 36.2,
    dailyChange: -45.0,
    dailyChangePercent: -0.39,
  },
  {
    id: "3",
    symbol: "GOOGL",
    name: "Alphabet Inc.",
    shares: 25,
    avgCost: 120.0,
    currentPrice: 141.80,
    value: 3545.0,
    allocation: 11.3,
    dailyChange: 87.5,
    dailyChangePercent: 2.53,
  },
  {
    id: "4",
    symbol: "AMZN",
    name: "Amazon.com Inc.",
    shares: 20,
    avgCost: 140.0,
    currentPrice: 178.35,
    value: 3567.0,
    allocation: 11.4,
    dailyChange: -22.0,
    dailyChangePercent: -0.61,
  },
  {
    id: "5",
    symbol: "NVDA",
    name: "NVIDIA Corporation",
    shares: 10,
    avgCost: 450.0,
    currentPrice: 495.22,
    value: 4952.2,
    allocation: 12.6,
    dailyChange: 198.2,
    dailyChangePercent: 4.17,
  },
];

export const portfolioSummary: PortfolioSummary = {
  totalValue: 31336.7,
  totalInvested: 25850.0,
  totalGain: 5486.7,
  totalGainPercent: 21.23,
  dailyChange: 343.7,
  dailyChangePercent: 1.11,
};


export const portfolioGrowth1D = [
  { time: "9:30", value: 31000 },
  { time: "10:00", value: 31150 },
  { time: "10:30", value: 31050 },
  { time: "11:00", value: 31200 },
  { time: "11:30", value: 31180 },
  { time: "12:00", value: 31250 },
  { time: "12:30", value: 31220 },
  { time: "13:00", value: 31300 },
  { time: "13:30", value: 31280 },
  { time: "14:00", value: 31350 },
  { time: "14:30", value: 31320 },
  { time: "15:00", value: 31400 },
  { time: "15:30", value: 31380 },
  { time: "16:00", value: 31337 },
];

export const portfolioGrowth1W = [
  { time: "Mon", value: 30500 },
  { time: "Tue", value: 30800 },
  { time: "Wed", value: 30600 },
  { time: "Thu", value: 31100 },
  { time: "Fri", value: 31337 },
];

export const allocationData = holdings.map((h) => ({
  name: h.symbol,
  value: h.allocation,
  fullName: h.name,
}));

export const newsItems: NewsItem[] = [
  {
    id: "1",
    headline: "Apple Reports Record Q4 Earnings, Beats Analyst Expectations",
    source: "Reuters",
    publishedAt: "2 hours ago",
    summary: "Apple Inc. reported better-than-expected quarterly earnings, driven by strong iPhone sales and growing services revenue. The tech giant posted record revenue of $89.5 billion.",
    url: "https://example.com/news/1",
    relatedSymbols: ["AAPL"],
  },
  {
    id: "2",
    headline: "Microsoft Azure Growth Continues to Outpace Competitors",
    source: "Bloomberg",
    publishedAt: "4 hours ago",
    summary: "Microsoft's cloud computing platform Azure reported 29% revenue growth, maintaining its position as a leader in the enterprise cloud market.",
    url: "https://example.com/news/2",
    relatedSymbols: ["MSFT"],
  },
  {
    id: "3",
    headline: "NVIDIA Unveils Next-Generation AI Chips at Annual Conference",
    source: "TechCrunch",
    publishedAt: "6 hours ago",
    summary: "NVIDIA announced its latest GPU architecture designed specifically for AI workloads, promising significant performance improvements for data centers.",
    url: "https://example.com/news/3",
    relatedSymbols: ["NVDA"],
  },
  {
    id: "4",
    headline: "Fed Signals Potential Rate Cuts in Coming Months",
    source: "Wall Street Journal",
    publishedAt: "8 hours ago",
    summary: "Federal Reserve officials indicated openness to reducing interest rates as inflation shows signs of cooling, boosting market sentiment.",
    url: "https://example.com/news/4",
    relatedSymbols: [],
  },
  {
    id: "5",
    headline: "Amazon Web Services Launches New AI-Powered Analytics Tools",
    source: "CNBC",
    publishedAt: "10 hours ago",
    summary: "AWS introduced a suite of machine learning tools aimed at helping businesses extract insights from their data more efficiently.",
    url: "https://example.com/news/5",
    relatedSymbols: ["AMZN"],
  },
  {
    id: "6",
    headline: "Google's Alphabet Reports Strong Ad Revenue Growth",
    source: "Financial Times",
    publishedAt: "12 hours ago",
    summary: "Alphabet's advertising business showed resilience with 11% year-over-year growth, driven by YouTube and Search performance.",
    url: "https://example.com/news/6",
    relatedSymbols: ["GOOGL"],
  },
  {
    id: "7",
    headline: "Tech Sector Leads Market Rally Amid Economic Optimism",
    source: "MarketWatch",
    publishedAt: "1 day ago",
    summary: "Technology stocks led a broad market rally as investors grew confident about the economic outlook and corporate earnings.",
    url: "https://example.com/news/7",
    relatedSymbols: ["AAPL", "MSFT", "GOOGL", "AMZN", "NVDA"],
  },
];

export const aiInsights = [
  {
    id: "1",
    type: "concentration",
    message: "Your portfolio is heavily concentrated in the top 3 assets (MSFT, AAPL, NVDA), which account for 77% of total value.",
    severity: "warning",
  },
  {
    id: "2",
    type: "diversification",
    message: "Diversification across additional sectors could reduce drawdown risk during tech sector corrections.",
    severity: "info",
  },
  {
    id: "3",
    type: "volatility",
    message: "NVDA contributes approximately 45% of total portfolio volatility due to its high beta and significant allocation.",
    severity: "warning",
  },
  {
    id: "4",
    type: "performance",
    message: "Your portfolio has outperformed the S&P 500 by 8.5% year-to-date, primarily driven by NVDA gains.",
    severity: "success",
  },
  {
    id: "5",
    type: "correlation",
    message: "All holdings are in the technology sector with high correlation (>0.75), limiting diversification benefits.",
    severity: "info",
  },
];
