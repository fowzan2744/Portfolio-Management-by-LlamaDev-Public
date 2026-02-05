import { useState, useEffect } from "react";
import { AppLayout } from "@/components/layout/AppLayout";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { api, PortfolioSummary, PortfolioGrowth, Allocation, Holding } from "@/lib/api";
import { TrendingUp, TrendingDown } from "lucide-react";
import { cn } from "@/lib/utils";
import {
  ComposedChart,
  Line,
  XAxis,
  YAxis,
  Tooltip,
  ResponsiveContainer,
  PieChart,
  Pie,
  Cell,
  Area,
} from "recharts";

const CHART_COLORS = [
  "hsl(220, 70%, 50%)",
  "hsl(160, 60%, 45%)",
  "hsl(45, 93%, 47%)",
  "hsl(280, 60%, 50%)",
  "hsl(15, 80%, 55%)",
];

type TimeRange = "1D" | "1W";

export default function Dashboard() {
  const [timeRange, setTimeRange] = useState<TimeRange>("1W");
  const [portfolioSummary, setPortfolioSummary] = useState<PortfolioSummary | null>(null);
  const [chartData, setChartData] = useState<PortfolioGrowth[]>([]);
  const [allocationData, setAllocationData] = useState<Allocation[]>([]);
  const [holdings, setHoldings] = useState<Holding[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const fetchData = async () => {
      try {
        setLoading(true);
        setError(null);
        const [summary, growth, allocation, holdingsData] = await Promise.all([
          api.getPortfolioSummary(),
          api.getPortfolioGrowth(timeRange),
          api.getAllocation(),
          api.getHoldings(),
        ]);
        setPortfolioSummary(summary);
        setChartData(growth || []);
        setAllocationData(allocation || []);
        setHoldings(holdingsData || []);
      } catch (err) {
        const errorMessage = err instanceof Error ? err.message : "Failed to load data";
        setError(errorMessage);
        console.error("Error fetching dashboard data:", err);
        // Set default empty data to prevent black screen
        setPortfolioSummary({
          totalValue: 0,
          totalInvested: 0,
          totalGain: 0,
          totalGainPercent: 0,
          dailyChange: 0,
          dailyChangePercent: 0,
          dataAvailableSince: "N/A",
        });
        setChartData([]);
        setAllocationData([]);
        setHoldings([]);
      } finally {
        setLoading(false);
      }
    };

    fetchData();
  }, [timeRange]);

  useEffect(() => {
    const fetchGrowth = async () => {
      try {
        const growth = await api.getPortfolioGrowth(timeRange);
        setChartData(growth);
      } catch (err) {
        console.error("Error fetching growth data:", err);
      }
    };

    if (portfolioSummary) {
      fetchGrowth();
    }
  }, [timeRange, portfolioSummary]);

  if (loading && !portfolioSummary) {
    return (
      <AppLayout>
        <div className="flex items-center justify-center h-64">
          <p className="text-muted-foreground">Loading...</p>
        </div>
      </AppLayout>
    );
  }

  if (!portfolioSummary) {
    return (
      <AppLayout>
        <div className="flex items-center justify-center h-64">
          <div className="text-center space-y-2">
            <p className="text-destructive">Failed to load portfolio data</p>
            {error && <p className="text-sm text-muted-foreground">Error: {error}</p>}
            <p className="text-sm text-muted-foreground">Make sure the backend is running on port 8080</p>
          </div>
        </div>
      </AppLayout>
    );
  }

  // Ensure all values have defaults to prevent undefined errors
  const totalValue = portfolioSummary?.totalValue ?? 0;
  const totalInvested = portfolioSummary?.totalInvested ?? 0;
  const totalGain = portfolioSummary?.totalGain ?? 0;
  const totalGainPercent = portfolioSummary?.totalGainPercent ?? 0;
  const dailyChange = portfolioSummary?.dailyChange ?? 0;
  const dailyChangePercent = portfolioSummary?.dailyChangePercent ?? 0;
  const isPositive = dailyChange >= 0;

  return (
    <AppLayout>
      <div className="space-y-6 animate-fade-in">
        {error && (
          <div className="bg-destructive/10 border border-destructive/20 rounded-lg p-4">
            <p className="text-destructive text-sm">
              ⚠️ {error} - Some data may not be available. Make sure the backend is running.
            </p>
          </div>
        )}
        
        {/* Header with Summary Stats */}
        <div className="space-y-1">
          <h1 className="text-3xl font-semibold">Welcome Fowzan!</h1>
         
          <h1 className="text-2xl font-semibold">Your Personalised Portfolio Dashboard</h1>
         
          <p className="text-muted-foreground">
            Track your investments and performance
          </p>
        </div>

        {/* Summary Cards */}
        <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-4">
          <Card className="card-hover">
            <CardContent className="pt-6">
              <div className="space-y-1">
                <p className="stat-label">Total Value</p>
                <p className="stat-value">
                  ${totalValue.toLocaleString()}
                </p>
              </div>
            </CardContent>
          </Card>

          <Card className="card-hover">
            <CardContent className="pt-6">
              <div className="space-y-1">
                <p className="stat-label">Total Invested</p>
                <p className="stat-value">
                  ${totalInvested.toLocaleString()}
                </p>
              </div>
            </CardContent>
          </Card>
          
          <Card className="card-hover">
            <CardContent className="pt-6">
              <div className="space-y-1">
                <p className="stat-label">Total Gain</p>
                <div className="flex items-center gap-2">
                  <p className={cn(
                    "stat-value",
                    totalGain >= 0 ? "text-success" : "text-destructive"
                  )}>
                    ${totalGain.toLocaleString()}
                  </p>
                  <span className={cn(
                    "text-sm",
                    totalGain >= 0 ? "text-success" : "text-destructive"
                  )}>
                    ({totalGainPercent.toFixed(2)}%)
                  </span>
                </div>
              </div>
            </CardContent>
          </Card>
     
          <Card className="card-hover">
            <CardContent className="pt-6">
              <div className="space-y-1">
                <p className="stat-label">Today's Change</p>
                <div className="flex items-center gap-2">
                  {isPositive ? (
                    <TrendingUp className="h-5 w-5 text-success" />
                  ) : (
                    <TrendingDown className="h-5 w-5 text-destructive" />
                  )}
                  <p className={cn(
                    "stat-value",
                    isPositive ? "text-success" : "text-destructive"
                  )}>
                    {isPositive ? "+" : ""}${dailyChange.toLocaleString()}
                  </p>
                  <span className={cn(
                    "text-sm",
                    isPositive ? "text-success" : "text-destructive"
                  )}>
                    ({isPositive ? "+" : ""}{dailyChangePercent.toFixed(2)}%)
                  </span>
                </div>
              </div>
            </CardContent>
          </Card> 
        </div>

        {/* Charts Grid - Two Column Layout */}
        <div className="grid gap-6 lg:grid-cols-2">
          {/* Portfolio Growth Chart */}
          <Card className="card-hover">
            <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-4">
              <CardTitle className="text-base font-medium">
                Portfolio Growth
              </CardTitle>
              <div className="flex gap-1">
                {(["1D", "1W"] as TimeRange[]).map((range) => (
                  <button
                    key={range}
                    onClick={() => setTimeRange(range)}
                    className={cn(
                      "filter-button",
                      timeRange === range
                        ? "filter-button-active"
                        : "filter-button-inactive"
                    )}
                  >
                    {range}
                  </button>
                ))}
              </div>
            </CardHeader>
            <CardContent>
              {chartData.length === 0 ? (
                <div className="flex items-center justify-center h-[300px] text-muted-foreground">
                  <p>No chart data available. Add assets to see portfolio growth.</p>
                </div>
              ) : (
                <div className="chart-container">
                  <ResponsiveContainer width="100%" height={300}>
                    <ComposedChart data={chartData}>
                      <defs>
                        <linearGradient id="portfolioGrowthFill" x1="0" y1="0" x2="0" y2="1">
                          <stop offset="0%" stopColor="hsl(var(--success))" stopOpacity={0.5} />
                          <stop offset="80%" stopColor="hsl(var(--success))" stopOpacity={0.08} />
                        </linearGradient>
                      </defs>
                      <XAxis 
                        dataKey="time" 
                        axisLine={false}
                        tickLine={false}
                        tick={{ fontSize: 12, fill: "hsl(var(--muted-foreground))" }}
                      />
                      <YAxis 
                        domain={['dataMin - 200', 'dataMax + 200']}
                        axisLine={false}
                        tickLine={false}
                        tick={{ fontSize: 12, fill: "hsl(var(--muted-foreground))" }}
                        tickFormatter={(value) => `$${(value / 1000).toFixed(1)}k`}
                      />
                      <Tooltip
                        contentStyle={{
                          backgroundColor: "hsl(var(--card))",
                          border: "1px solid hsl(var(--border))",
                          borderRadius: "8px",
                          boxShadow: "0 4px 12px rgba(0, 0, 0, 0.1)",
                        }}
                        formatter={(value: number) => [`$${value.toLocaleString()}`, "Value"]}
                      />
                      <Area
                        type="monotone"
                        dataKey="value"
                        stroke="none"
                        fill="url(#portfolioGrowthFill)"
                        isAnimationActive={true}
                      />
                      {/* <Line
                        type="monotone"
                        dataKey="value"
                        stroke="hsl(var(--chart-line))"
                        strokeWidth={2}
                        dot={false}
                        activeDot={{ r: 4, fill: "hsl(var(--chart-line))" }}
                      /> */}
                    </ComposedChart>
                  </ResponsiveContainer>
                </div>
              )}
            </CardContent>
          </Card>

          {/* Asset Allocation Chart */}
          <Card className="card-hover">
            <CardHeader className="pb-4">
              <CardTitle className="text-base font-medium">
                Asset Allocation
              </CardTitle>
            </CardHeader>
            <CardContent>
              {allocationData.length === 0 ? (
                <div className="flex items-center justify-center h-[300px] text-muted-foreground">
                  <p>No allocation data available. Add assets to see allocation.</p>
                </div>
              ) : (
                <>
                  <div className="chart-container">
                    <ResponsiveContainer width="100%" height={300}>
                      <PieChart>
                        <Pie
                          data={allocationData}
                          cx="50%"
                          cy="50%"
                          innerRadius={60}
                          outerRadius={100}
                          paddingAngle={2}
                          dataKey="value"
                        >
                          {allocationData.map((_, index) => (
                            <Cell
                              key={`cell-${index}`}
                              fill={CHART_COLORS[index % CHART_COLORS.length]}
                            />
                          ))}
                        </Pie>
                        <Tooltip
                          contentStyle={{
                            backgroundColor: "hsl(var(--card))",
                            border: "1px solid hsl(var(--border))",
                            borderRadius: "8px",
                            boxShadow: "0 4px 12px rgba(0, 0, 0, 0.1)",
                          }}
                          formatter={(value: number, _, props: any) => [
                            `${value.toFixed(1)}%`,
                            props.payload.fullName,
                          ]}
                        />
                      </PieChart>
                    </ResponsiveContainer>
                  </div>
                  {/* Legend */}
                  <div className="mt-4 flex flex-wrap justify-center gap-4">
                    {allocationData.map((item, index) => (
                      <div key={item.name} className="flex items-center gap-2">
                        <div
                          className="h-3 w-3 rounded-full"
                          style={{ backgroundColor: CHART_COLORS[index % CHART_COLORS.length] }}
                        />
                        <span className="text-sm text-muted-foreground">
                          {item.name} ({item.value.toFixed(1)}%)
                        </span>
                      </div>
                    ))}
                  </div>
                </>
              )}
            </CardContent>
          </Card>
        </div>

        {/* Holdings Table */}
        <Card className="card-hover">
          <CardHeader className="pb-4">
            <CardTitle className="text-base font-medium">Holdings</CardTitle>
          </CardHeader>
          <CardContent>
            <div className="overflow-x-auto">
              <table className="w-full">
                <thead>
                  <tr className="border-b border-border">
                    <th className="pb-3 text-left text-sm font-medium text-muted-foreground">
                      Asset
                    </th>
                    <th className="pb-3 text-right text-sm font-medium text-muted-foreground">
                      Price
                    </th>
                    <th className="pb-3 text-right text-sm font-medium text-muted-foreground">
                      Shares
                    </th>
                    <th className="pb-3 text-right text-sm font-medium text-muted-foreground">
                      Value
                    </th>
                    <th className="pb-3 text-right text-sm font-medium text-muted-foreground">
                      Today
                    </th>
                  </tr>
                </thead>
                <tbody>
                  {holdings.length === 0 ? (
                    <tr>
                      <td colSpan={5} className="py-8 text-center text-muted-foreground">
                        No holdings found
                      </td>
                    </tr>
                  ) : (
                    holdings.map((holding) => (
                      <tr
                        key={holding.id}
                        className="border-b border-border last:border-b-0"
                      >
                        <td className="py-4">
                          <div>
                            <p className="font-medium">{holding.symbol}</p>
                            <p className="text-sm text-muted-foreground">
                              {holding.name}
                            </p>
                          </div>
                        </td>
                        <td className="py-4 text-right">
                          ${holding.currentPrice.toFixed(2)}
                        </td>
                        <td className="py-4 text-right">{holding.shares}</td>
                        <td className="py-4 text-right font-medium">
                          ${holding.value.toLocaleString()}
                        </td>
                        <td className="py-4 text-right">
                          <span
                            className={cn(
                              "font-medium",
                              holding.dailyChange >= 0
                                ? "text-success"
                                : "text-destructive"
                            )}
                          >
                            {holding.dailyChange >= 0 ? "+" : ""}
                            {holding.dailyChangePercent.toFixed(2)}%
                          </span>
                        </td>
                      </tr>
                    ))
                  )}
                </tbody>
              </table>
            </div>
          </CardContent>
        </Card>
      </div>
    </AppLayout>
  );
}
