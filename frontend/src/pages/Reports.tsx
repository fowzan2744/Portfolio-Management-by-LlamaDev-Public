import { useState, useEffect } from "react";
import { AppLayout } from "@/components/layout/AppLayout";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Separator } from "@/components/ui/separator";
import { api, Holding, PortfolioSummary, Allocation } from "@/lib/api";
import { FileText, Download, TrendingUp, TrendingDown, PieChart, Wallet, DollarSign } from "lucide-react";
import { cn } from "@/lib/utils";
import { toast } from "sonner";

export default function Reports() {
  const [holdings, setHoldings] = useState<Holding[]>([]);
  const [portfolioSummary, setPortfolioSummary] = useState<PortfolioSummary | null>(null);
  const [allocationData, setAllocationData] = useState<Allocation[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const fetchData = async () => {
      try {
        setLoading(true);
        const [holdingsData, summary, allocation] = await Promise.all([
          api.getHoldings(),
          api.getPortfolioSummary(),
          api.getAllocation(),
        ]);
        setHoldings(holdingsData);
        setPortfolioSummary(summary);
        setAllocationData(allocation);
      } catch (err) {
        toast.error(err instanceof Error ? err.message : "Failed to load report data");
        console.error("Error fetching report data:", err);
      } finally {
        setLoading(false);
      }
    };

    fetchData();
  }, []);

  if (loading || !portfolioSummary) {
    return (
      <AppLayout>
        <div className="flex items-center justify-center h-64">
          <p className="text-muted-foreground">Loading...</p>
        </div>
      </AppLayout>
    );
  }

  const totalInvested = holdings.reduce((sum, h) => sum + h.shares * h.avgCost, 0);
  const totalGainLoss = portfolioSummary.totalValue - totalInvested;
  const totalGainLossPercent = totalInvested > 0 ? ((totalGainLoss / totalInvested) * 100) : 0;
  const isPositive = totalGainLoss >= 0;

  const handleDownloadPDF = () => {
    // Create a simple text-based report content
    const reportDate = new Date().toLocaleDateString('en-US', { 
      year: 'numeric', 
      month: 'long', 
      day: 'numeric' 
    });

    const reportContent = `
PORTFOLIO REPORT
Generated: ${reportDate}

════════════════════════════════════════════════════════

PORTFOLIO SUMMARY
─────────────────────────────────────────────────────────
Total Portfolio Value:     $${portfolioSummary.totalValue.toLocaleString()}
Total Invested:            $${totalInvested.toLocaleString()}
Total Gain/Loss:           ${isPositive ? '+' : ''}$${totalGainLoss.toLocaleString()} (${totalGainLossPercent.toFixed(2)}%)
Today's Change:            ${portfolioSummary.dailyChange >= 0 ? '+' : ''}$${portfolioSummary.dailyChange.toLocaleString()} (${portfolioSummary.dailyChangePercent.toFixed(2)}%)
Number of Holdings:        ${holdings.length} assets

════════════════════════════════════════════════════════

HOLDINGS BREAKDOWN
─────────────────────────────────────────────────────────
${holdings.map(h => {
  const gainLoss = (h.currentPrice - h.avgCost) * h.shares;
  const gainLossPercent = ((h.currentPrice - h.avgCost) / h.avgCost) * 100;
  return `
${h.symbol} - ${h.name}
  Shares: ${h.shares}
  Avg Cost: $${h.avgCost.toFixed(2)}
  Current Price: $${h.currentPrice.toFixed(2)}
  Current Value: $${h.value.toLocaleString()}
  Allocation: ${h.allocation.toFixed(1)}%
  Gain/Loss: ${gainLoss >= 0 ? '+' : ''}$${gainLoss.toFixed(2)} (${gainLossPercent.toFixed(1)}%)
`;
}).join('')}
════════════════════════════════════════════════════════

ASSET ALLOCATION
─────────────────────────────────────────────────────────
${allocationData.map(a => `${a.name}: ${a.value.toFixed(1)}%`).join('\n')}

════════════════════════════════════════════════════════
This report is for informational purposes only.
    `.trim();

    // Create and download the file
    const blob = new Blob([reportContent], { type: 'text/plain' });
    const url = URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = url;
    link.download = `portfolio-report-${new Date().toISOString().split('T')[0]}.txt`;
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
    URL.revokeObjectURL(url);
    
    toast.success("Report downloaded successfully");
  };

  return (
    <AppLayout>
      <div className="space-y-6 animate-fade-in">
        <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
          <div className="space-y-1">
            <h1 className="text-2xl font-semibold">Portfolio Report</h1>
            <p className="text-muted-foreground">
              View your portfolio summary and download reports
            </p>
          </div>
          
          <Button onClick={handleDownloadPDF} className="gap-2">
            <Download className="h-4 w-4" />
            Download PDF Report
          </Button>
        </div>

        {/* Portfolio Summary Card */}
        <Card className="card-hover">
          <CardHeader>
            <div className="flex items-center gap-2">
              <FileText className="h-5 w-5 text-primary" />
              <CardTitle className="text-lg">Portfolio Summary</CardTitle>
            </div>
          </CardHeader>
          <CardContent className="space-y-6">
            {/* Key Metrics */}
            <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-4">
              <div className="space-y-1 p-4 rounded-lg bg-muted/50">
                <div className="flex items-center gap-2 text-muted-foreground">
                  <Wallet className="h-4 w-4" />
                  <span className="text-sm">Total Value</span>
                </div>
                <p className="text-2xl font-semibold">
                  ${portfolioSummary.totalValue.toLocaleString()}
                </p>
              </div>
              
              <div className="space-y-1 p-4 rounded-lg bg-muted/50">
                <div className="flex items-center gap-2 text-muted-foreground">
                  <DollarSign className="h-4 w-4" />
                  <span className="text-sm">Total Invested</span>
                </div>
                <p className="text-2xl font-semibold">
                  ${totalInvested.toLocaleString()}
                </p>
              </div>
              
              <div className="space-y-1 p-4 rounded-lg bg-muted/50">
                <div className="flex items-center gap-2 text-muted-foreground">
                  {isPositive ? (
                    <TrendingUp className="h-4 w-4 text-success" />
                  ) : (
                    <TrendingDown className="h-4 w-4 text-destructive" />
                  )}
                  <span className="text-sm">Total Gain/Loss</span>
                </div>
                <p className={cn(
                  "text-2xl font-semibold",
                  isPositive ? "text-success" : "text-destructive"
                )}>
                  {isPositive ? "+" : ""}${totalGainLoss.toLocaleString()}
                </p>
                <p className={cn(
                  "text-sm",
                  isPositive ? "text-success" : "text-destructive"
                )}>
                  ({totalGainLossPercent.toFixed(2)}%)
                </p>
              </div>
              
              <div className="space-y-1 p-4 rounded-lg bg-muted/50">
                <div className="flex items-center gap-2 text-muted-foreground">
                  <PieChart className="h-4 w-4" />
                  <span className="text-sm">Holdings</span>
                </div>
                <p className="text-2xl font-semibold">{holdings.length}</p>
                <p className="text-sm text-muted-foreground">assets</p>
              </div>
            </div>

            <Separator />

            {/* Holdings Breakdown */}
            <div>
              <h3 className="font-medium mb-4">Holdings Breakdown</h3>
              <div className="overflow-x-auto">
                <table className="w-full">
                  <thead>
                    <tr className="border-b border-border">
                      <th className="pb-3 text-left text-sm font-medium text-muted-foreground">Asset</th>
                      <th className="pb-3 text-right text-sm font-medium text-muted-foreground">Shares</th>
                      <th className="pb-3 text-right text-sm font-medium text-muted-foreground">Avg Cost</th>
                      <th className="pb-3 text-right text-sm font-medium text-muted-foreground">Current</th>
                      <th className="pb-3 text-right text-sm font-medium text-muted-foreground">Value</th>
                      <th className="pb-3 text-right text-sm font-medium text-muted-foreground">Allocation</th>
                      <th className="pb-3 text-right text-sm font-medium text-muted-foreground">Gain/Loss</th>
                    </tr>
                  </thead>
                  <tbody>
                    {holdings.map((holding) => {
                      const gainLoss = (holding.currentPrice - holding.avgCost) * holding.shares;
                      const gainLossPercent = ((holding.currentPrice - holding.avgCost) / holding.avgCost) * 100;
                      
                      return (
                        <tr key={holding.id} className="border-b border-border last:border-b-0">
                          <td className="py-3">
                            <div>
                              <p className="font-medium">{holding.symbol}</p>
                              <p className="text-xs text-muted-foreground">{holding.name}</p>
                            </div>
                          </td>
                          <td className="py-3 text-right">{holding.shares}</td>
                          <td className="py-3 text-right">${holding.avgCost.toFixed(2)}</td>
                          <td className="py-3 text-right">${holding.currentPrice.toFixed(2)}</td>
                          <td className="py-3 text-right font-medium">${holding.value.toLocaleString()}</td>
                          <td className="py-3 text-right">{holding.allocation.toFixed(1)}%</td>
                          <td className="py-3 text-right">
                            <span className={cn(
                              "font-medium",
                              gainLoss >= 0 ? "text-success" : "text-destructive"
                            )}>
                              {gainLoss >= 0 ? "+" : ""}${gainLoss.toFixed(0)}
                              <span className="text-xs ml-1">({gainLossPercent.toFixed(1)}%)</span>
                            </span>
                          </td>
                        </tr>
                      );
                    })}
                  </tbody>
                </table>
              </div>
            </div>

            <Separator />

            {/* Asset Allocation */}
            <div>
              <h3 className="font-medium mb-4">Asset Allocation</h3>
              <div className="grid gap-3 sm:grid-cols-2 lg:grid-cols-5">
                {allocationData.map((item) => (
                  <div key={item.name} className="flex items-center justify-between p-3 rounded-lg bg-muted/50">
                    <div>
                      <p className="font-medium">{item.name}</p>
                      <p className="text-xs text-muted-foreground">{item.fullName}</p>
                    </div>
                    <span className="text-sm font-semibold">{item.value.toFixed(1)}%</span>
                  </div>
                ))}
              </div>
            </div>
          </CardContent>
        </Card>

        {/* Report Generated Note */}
        <Card className="card-hover">
          <CardContent className="pt-6">
            <div className="text-center space-y-2">
              <p className="text-sm text-muted-foreground">
                Report generated on {new Date().toLocaleDateString('en-US', { 
                  year: 'numeric', 
                  month: 'long', 
                  day: 'numeric',
                  hour: '2-digit',
                  minute: '2-digit'
                })}
              </p>
              <p className="text-xs text-muted-foreground">
                This report is for informational purposes only and does not constitute financial advice.
              </p>
            </div>
          </CardContent>
        </Card>
      </div>
    </AppLayout>
  );
}
