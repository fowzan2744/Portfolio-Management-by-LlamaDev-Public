import { useState, useEffect } from "react";
import { AppLayout } from "@/components/layout/AppLayout";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { api, Allocation } from "@/lib/api";
import { cn } from "@/lib/utils";
import { Info, Lightbulb } from "lucide-react";
import { PieChart, Pie, Cell, ResponsiveContainer, Tooltip } from "recharts";

const CHART_COLORS = [
  "hsl(220, 70%, 50%)",
  "hsl(160, 60%, 45%)",
  "hsl(45, 93%, 47%)",
  "hsl(280, 60%, 50%)",
  "hsl(15, 80%, 55%)",
];

export default function Analytics() {
  const [allocationData, setAllocationData] = useState<Allocation[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const fetchData = async () => {
      try {
        setLoading(true);
        const allocation = await api.getAllocation();
        setAllocationData(allocation);
      } catch (err) {
        console.error("Error fetching allocation data:", err);
      } finally {
        setLoading(false);
      }
    };

    fetchData();
  }, []);

  if (loading) {
    return (
      <AppLayout>
        <div className="flex items-center justify-center h-64">
          <p className="text-muted-foreground">Loading...</p>
        </div>
      </AppLayout>
    );
  }

  return (
    <AppLayout>
      <div className="space-y-6 animate-fade-in">
        <div className="space-y-1">
          <h1 className="text-2xl font-semibold">Analytics + AI</h1>
          <p className="text-muted-foreground">
            Portfolio insights powered by data analysis
          </p>
        </div>

        <div className="grid gap-6 lg:grid-cols-2">
          {/* Asset Allocation Chart */}
          <Card className="card-hover">
            <CardHeader className="pb-4">
              <CardTitle className="text-base font-medium">
                Asset Allocation
              </CardTitle>
            </CardHeader>
            <CardContent>
              <div className="chart-container">
                <ResponsiveContainer width="100%" height={300}>
                  <PieChart>
                    <Pie
                      data={allocationData}
                      cx="50%"
                      cy="50%"
                      innerRadius={70}
                      outerRadius={110}
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
              <div className="mt-4 grid grid-cols-2 gap-3">
                {allocationData.map((item, index) => (
                  <div key={item.name} className="flex items-center gap-2">
                    <div
                      className="h-3 w-3 rounded-full flex-shrink-0"
                      style={{ backgroundColor: CHART_COLORS[index % CHART_COLORS.length] }}
                    />
                    <div className="min-w-0">
                      <span className="text-sm font-medium">{item.name}</span>
                      <span className="text-sm text-muted-foreground ml-1">
                        ({item.value.toFixed(1)}%)
                      </span>
                    </div>
                  </div>
                ))}
              </div>
            </CardContent>
          </Card>

          {/* AI Insights Panel */}
          <Card className="card-hover">
            <CardHeader className="pb-4">
              <div className="flex items-center gap-2">
                <Lightbulb className="h-5 w-5 text-primary" />
                <CardTitle className="text-base font-medium">
                  AI Insights
                </CardTitle>
              </div>
            </CardHeader>
            <CardContent>
              <div className="space-y-3">
                {allocationData.length === 0 ? (
                  <p className="text-sm text-muted-foreground">No portfolio data available for insights.</p>
                ) : (
                  <div className="space-y-2">
                    <p className="text-sm leading-relaxed">
                      Your portfolio consists of {allocationData.length} holdings with varying allocations.
                    </p>
                    {allocationData.length > 0 && (
                      <p className="text-sm leading-relaxed">
                        Top holding: {allocationData[0]?.name} ({allocationData[0]?.value.toFixed(1)}%)
                      </p>
                    )}
                  </div>
                )}
              </div>
              <div className="mt-6 pt-4 border-t border-border">
                <p className="text-xs text-muted-foreground">
                  Portfolio insights are based on your current holdings and allocations. 
                  They are for informational purposes only and do not constitute financial advice.
                </p>
              </div>
            </CardContent>
          </Card>
        </div>
      </div>
    </AppLayout>
  );
}
