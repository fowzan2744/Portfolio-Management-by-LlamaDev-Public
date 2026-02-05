import { useState, useEffect } from "react";
import { AppLayout } from "@/components/layout/AppLayout";
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Label } from "@/components/ui/label";
import { Textarea } from "@/components/ui/textarea";
import { 
  api, 
  type Allocation, 
  type PortfolioSummary, 
  type PortfolioGrowth,
  type AIInsightsRequest,
  type AIInsightsResponse 
} from "@/lib/api";
import { cn } from "@/lib/utils";
import { 
  TrendingUp, 
  TrendingDown, 
  DollarSign, 
  Percent, 
  BarChart3,
  Lightbulb,
  AlertCircle,
  CheckCircle2,
  RefreshCw,
  Loader2,
  Sparkles,
  Target,
  ArrowUpRight,
  ArrowDownRight
} from "lucide-react";
import { 
  PieChart, 
  Pie, 
  Cell, 
  ResponsiveContainer, 
  Tooltip,
  AreaChart,
  Area,
  XAxis,
  YAxis,
  CartesianGrid
} from "recharts";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";

const CHART_COLORS = [
  "hsl(220, 70%, 50%)",
  "hsl(160, 60%, 45%)",
  "hsl(45, 93%, 47%)",
  "hsl(280, 60%, 50%)",
  "hsl(15, 80%, 55%)",
];

type TabType = "overview" | "ai-insights";

export default function Analytics() {
  const [activeTab, setActiveTab] = useState<TabType>("overview");
  const [allocationData, setAllocationData] = useState<Allocation[]>([]);
  const [summary, setSummary] = useState<PortfolioSummary | null>(null);
  const [growthData, setGrowthData] = useState<PortfolioGrowth[]>([]);
  const [loading, setLoading] = useState(true);
  
  // AI Insights state
  const [riskProfile, setRiskProfile] = useState("Moderate");
  const [investmentHorizon, setInvestmentHorizon] = useState("Medium");
  const [notes, setNotes] = useState("");
  const [aiInsights, setAiInsights] = useState<AIInsightsResponse | null>(null);
  const [analyzingAI, setAnalyzingAI] = useState(false);
  const [aiError, setAiError] = useState<string | null>(null);

  useEffect(() => {
    const fetchData = async () => {
      try {
        setLoading(true);
        const [allocation, portfolioSummary, growth] = await Promise.all([
          api.getAllocation(),
          api.getSummary(),
          api.getGrowth('1W')
        ]);
        setAllocationData(allocation);
        setSummary(portfolioSummary);
        setGrowthData(growth);
      } catch (err) {
        console.error("Error fetching analytics data:", err);
      } finally {
        setLoading(false);
      }
    };

    fetchData();
  }, []);

  const handleAnalyze = async () => {
    setAnalyzingAI(true);
    setAiError(null);
    try {
      const request: AIInsightsRequest = {
        riskProfile,
        investmentHorizon,
        notes: notes.trim() || undefined,
      };
      const insights = await api.analyzePortfolio(request);
      setAiInsights(insights);
      setActiveTab("ai-insights");
    } catch (err) {
      setAiError(err instanceof Error ? err.message : "Failed to analyze portfolio");
    } finally {
      setAnalyzingAI(false);
    }
  };

  const formatCurrency = (value: number) => {
    return new Intl.NumberFormat('en-US', {
      style: 'currency',
      currency: 'USD',
      minimumFractionDigits: 2,
    }).format(value);
  };

  const formatPercent = (value: number) => {
    return `${value >= 0 ? '+' : ''}${value.toFixed(2)}%`;
  };

  if (loading) {
    return (
      <AppLayout>
        <div className="flex items-center justify-center h-64">
          <Loader2 className="h-8 w-8 animate-spin text-primary" />
        </div>
      </AppLayout>
    );
  }

  return (
    <AppLayout>
      <div className="space-y-6 animate-fade-in">
        {/* Header */}
        <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
          <div className="space-y-1">
            <h1 className="text-2xl font-semibold">Analytics + AI</h1>
            <p className="text-muted-foreground">
              Portfolio insights powered by data analysis and artificial intelligence
            </p>
          </div>

      
        </div>

       

        {/* AI Insights Tab */}
      
          <div className="grid gap-6 lg:grid-cols-3">
            {/* AI Configuration Form */}
            <Card className="lg:col-span-1 card-hover">
              <CardHeader>
                <CardTitle className="flex items-center gap-2">
                  <Target className="h-5 w-5 text-primary" />
                  Personalize Analysis
                </CardTitle>
                <CardDescription>
                  Customize AI insights based on your investment profile
                </CardDescription>
              </CardHeader>
              <CardContent className="space-y-4">
                <div className="space-y-2">
                  <Label htmlFor="risk-profile">Risk Profile</Label>
                  <Select value={riskProfile} onValueChange={setRiskProfile}>
                    <SelectTrigger id="risk-profile">
                      <SelectValue />
                    </SelectTrigger>
                    <SelectContent>
                      <SelectItem value="Conservative">Conservative</SelectItem>
                      <SelectItem value="Moderate">Moderate</SelectItem>
                      <SelectItem value="Aggressive">Aggressive</SelectItem>
                    </SelectContent>
                  </Select>
                </div>

                <div className="space-y-2">
                  <Label htmlFor="investment-horizon">Investment Horizon</Label>
                  <Select value={investmentHorizon} onValueChange={setInvestmentHorizon}>
                    <SelectTrigger id="investment-horizon">
                      <SelectValue />
                    </SelectTrigger>
                    <SelectContent>
                      <SelectItem value="Short">Short Term (&lt; 1 year)</SelectItem>
                      <SelectItem value="Medium">Medium Term (1-5 years)</SelectItem>
                      <SelectItem value="Long">Long Term (&gt; 5 years)</SelectItem>
                    </SelectContent>
                  </Select>
                </div>

                <div className="space-y-2">
                  <Label htmlFor="notes">Additional Notes (Optional)</Label>
                  <Textarea
                    id="notes"
                    placeholder="Any specific questions or concerns..."
                    value={notes}
                    onChange={(e) => setNotes(e.target.value)}
                    rows={4}
                  />
                </div>

                <Button 
                  onClick={handleAnalyze} 
                  disabled={analyzingAI}
                  className="w-full"
                >
                  {analyzingAI ? (
                    <>
                      <Loader2 className="mr-2 h-4 w-4 animate-spin" />
                      Analyzing...
                    </>
                  ) : (
                    <>
                      <Sparkles className="mr-2 h-4 w-4" />
                      Analyze Portfolio
                    </>
                  )}
                </Button>

                {aiError && (
                  <div className="p-3 bg-destructive/10 border border-destructive/20 rounded-lg">
                    <p className="text-sm text-destructive">{aiError}</p>
                  </div>
                )}
              </CardContent>
            </Card>

            {/* AI Insights Display */}
            <div className="lg:col-span-2 space-y-4">
              {aiInsights ? (
                <>
                  {/* AI Generated Badge */}
                  <div className="flex items-center gap-2 text-sm">
                    {aiInsights.isAiGenerated ? (
                      <span className="inline-flex items-center px-3 py-1 rounded-full bg-primary/10 text-primary font-medium">
                        <Sparkles className="h-3 w-3 mr-1.5" />
                        AI-Powered Insights
                      </span>
                    ) : (
                      <span className="inline-flex items-center px-3 py-1 rounded-full bg-muted text-muted-foreground font-medium">
                        <AlertCircle className="h-3 w-3 mr-1.5" />
                        Basic Insights
                      </span>
                    )}
                  </div>

                  {/* Summary */}
                  <Card className="card-hover">
                    <CardHeader>
                      <CardTitle className="flex items-center gap-2">
                        <Lightbulb className="h-5 w-5 text-primary" />
                        Portfolio Summary
                      </CardTitle>
                    </CardHeader>
                    <CardContent>
                      <p className="text-sm leading-relaxed">{aiInsights.summary}</p>
                    </CardContent>
                  </Card>

                  {/* Risks */}
                  <Card className="card-hover border-red-200 dark:border-red-900/30">
                    <CardHeader>
                      <CardTitle className="flex items-center gap-2 text-red-600 dark:text-red-400">
                        <AlertCircle className="h-5 w-5" />
                        Identified Risks
                      </CardTitle>
                    </CardHeader>
                    <CardContent>
                      <ul className="space-y-2">
                        {aiInsights.risks.map((risk, index) => (
                          <li key={index} className="flex items-start gap-2 text-sm">
                            <div className="h-5 w-5 rounded-full bg-red-100 dark:bg-red-900/30 flex items-center justify-center flex-shrink-0 mt-0.5">
                              <span className="text-xs font-semibold text-red-600 dark:text-red-400">
                                {index + 1}
                              </span>
                            </div>
                            <span className="leading-relaxed">{risk}</span>
                          </li>
                        ))}
                      </ul>
                    </CardContent>
                  </Card>

                  {/* Opportunities */}
                  <Card className="card-hover border-green-200 dark:border-green-900/30">
                    <CardHeader>
                      <CardTitle className="flex items-center gap-2 text-green-600 dark:text-green-400">
                        <TrendingUp className="h-5 w-5" />
                        Opportunities
                      </CardTitle>
                    </CardHeader>
                    <CardContent>
                      <ul className="space-y-2">
                        {aiInsights.opportunities.map((opportunity, index) => (
                          <li key={index} className="flex items-start gap-2 text-sm">
                            <CheckCircle2 className="h-5 w-5 text-green-600 dark:text-green-400 flex-shrink-0 mt-0.5" />
                            <span className="leading-relaxed">{opportunity}</span>
                          </li>
                        ))}
                      </ul>
                    </CardContent>
                  </Card>

                  {/* Rebalancing Actions */}
                  {aiInsights.rebalancingActions.length > 0 && (
                    <Card className="card-hover">
                      <CardHeader>
                        <CardTitle className="flex items-center gap-2">
                          <RefreshCw className="h-5 w-5 text-primary" />
                          Rebalancing Recommendations
                        </CardTitle>
                      </CardHeader>
                      <CardContent>
                        <div className="space-y-3">
                          {aiInsights.rebalancingActions.map((action, index) => (
                            <div
                              key={index}
                              className="flex items-start justify-between p-3 rounded-lg bg-muted/50"
                            >
                              <div className="flex-1">
                                <div className="flex items-center gap-2 mb-1">
                                  <span className={cn(
                                    "px-2 py-0.5 rounded text-xs font-semibold",
                                    action.action === "BUY" && "bg-green-100 text-green-700 dark:bg-green-900/30 dark:text-green-400",
                                    action.action === "SELL" && "bg-red-100 text-red-700 dark:bg-red-900/30 dark:text-red-400",
                                    action.action === "REDUCE" && "bg-orange-100 text-orange-700 dark:bg-orange-900/30 dark:text-orange-400",
                                    action.action === "INCREASE" && "bg-blue-100 text-blue-700 dark:bg-blue-900/30 dark:text-blue-400",
                                    action.action === "HOLD" && "bg-gray-100 text-gray-700 dark:bg-gray-800 dark:text-gray-400"
                                  )}>
                                    {action.action}
                                  </span>
                                  <span className="font-semibold">{action.symbol}</span>
                                  {action.suggestedAllocation && (
                                    <span className="text-xs text-muted-foreground">
                                      Target: {action.suggestedAllocation.toFixed(1)}%
                                    </span>
                                  )}
                                </div>
                                <p className="text-sm text-muted-foreground">{action.reason}</p>
                              </div>
                            </div>
                          ))}
                        </div>
                      </CardContent>
                    </Card>
                  )}

                  {/* Sentiment Analysis */}
                  <Card className="card-hover">
                    <CardHeader>
                      <CardTitle className="flex items-center gap-2">
                        <BarChart3 className="h-5 w-5 text-primary" />
                        Market Sentiment
                      </CardTitle>
                    </CardHeader>
                    <CardContent>
                      <p className="text-sm leading-relaxed">{aiInsights.sentimentAnalysis}</p>
                    </CardContent>
                  </Card>

                  {/* Next Steps */}
                  <Card className="card-hover border-blue-200 dark:border-blue-900/30">
                    <CardHeader>
                      <CardTitle className="flex items-center gap-2 text-blue-600 dark:text-blue-400">
                        <Target className="h-5 w-5" />
                        Next Steps
                      </CardTitle>
                    </CardHeader>
                    <CardContent>
                      <ol className="space-y-2">
                        {aiInsights.nextSteps.map((step, index) => (
                          <li key={index} className="flex items-start gap-3 text-sm">
                            <div className="h-6 w-6 rounded-full bg-blue-100 dark:bg-blue-900/30 flex items-center justify-center flex-shrink-0">
                              <span className="text-xs font-semibold text-blue-600 dark:text-blue-400">
                                {index + 1}
                              </span>
                            </div>
                            <span className="leading-relaxed pt-0.5">{step}</span>
                          </li>
                        ))}
                      </ol>
                    </CardContent>
                  </Card>
                </>
              ) : (
                <Card className="card-hover">
                  <CardContent className="py-12">
                    <div className="text-center space-y-4">
                      <div className="flex justify-center">
                        <Sparkles className="h-12 w-12 text-muted-foreground/50" />
                      </div>
                      <div>
                        <h3 className="font-semibold text-lg mb-2">No insights yet</h3>
                        <p className="text-sm text-muted-foreground">
                          Configure your preferences and click "Analyze Portfolio" to get AI-powered insights
                        </p>
                      </div>
                    </div>
                  </CardContent>
                </Card>
              )}
            </div>
          </div>
        
      </div>
    </AppLayout>
  );
}