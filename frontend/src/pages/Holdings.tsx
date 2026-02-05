import { useState, useEffect } from "react";
import { AppLayout } from "@/components/layout/AppLayout";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
  DialogTrigger,
} from "@/components/ui/dialog";
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs";
import { api, Holding } from "@/lib/api";
import { cn } from "@/lib/utils";
import { TrendingUp, TrendingDown, Plus, Minus } from "lucide-react";
import { toast } from "sonner";

// Available tickers supported by the API
const AVAILABLE_TICKERS = [
  { symbol: "AAPL", name: "Apple Inc." },
  { symbol: "AMZN", name: "Amazon.com Inc." },
  { symbol: "TSLA", name: "Tesla, Inc." },
  { symbol: "FB", name: "Facebook Inc." },
  { symbol: "C", name: "Citigroup Inc." },
];

export default function Holdings() {
  const [holdingsList, setHoldingsList] = useState<Holding[]>([]);
  const [addDialogOpen, setAddDialogOpen] = useState(false);
  const [sellDialogOpen, setSellDialogOpen] = useState(false);
  const [selectedHolding, setSelectedHolding] = useState<Holding | null>(null);
  const [sellQuantity, setSellQuantity] = useState("");
  const [loading, setLoading] = useState(true);
  const [filter, setFilter] = useState<"all" | "profit" | "loss">("all");
  
  // Add asset form state
  const [newAsset, setNewAsset] = useState({
    symbol: "",
    shares: "",
    avgCost: "",
  });

  useEffect(() => {
    const fetchHoldings = async () => {
      try {
        setLoading(true);
        const holdings = await api.getHoldings();
        setHoldingsList(holdings);
      } catch (err) {
        toast.error(err instanceof Error ? err.message : "Failed to load holdings");
        console.error("Error fetching holdings:", err);
      } finally {
        setLoading(false);
      }
    };

    fetchHoldings();
  }, []);

  const totalValue = holdingsList.reduce((sum, h) => sum + h.value, 0);
  const totalInvested = holdingsList.reduce((sum, h) => sum + h.shares * h.avgCost, 0);

  // Filter holdings based on selected filter
  const filteredHoldings = holdingsList.filter((holding) => {
    const gainLoss = (holding.currentPrice - holding.avgCost) * holding.shares;
    switch (filter) {
      case "profit":
        return gainLoss > 0;
      case "loss":
        return gainLoss < 0;
      default:
        return true;
    }
  });

  const handleAddAsset = async () => {
    if (!newAsset.symbol || !newAsset.shares || !newAsset.avgCost) {
      toast.error("Please fill in all fields");
      return;
    }

    try {
      const shares = parseFloat(newAsset.shares);
      const avgCost = parseFloat(newAsset.avgCost);

      await api.addAsset(newAsset.symbol, shares, avgCost);
      
      // Refresh holdings
      const holdings = await api.getHoldings();
      setHoldingsList(holdings);
      
      setNewAsset({ symbol: "", shares: "", avgCost: "" });
      setAddDialogOpen(false);
      toast.success(`${newAsset.symbol.toUpperCase()} added to portfolio`);
    } catch (err) {
      toast.error(err instanceof Error ? err.message : "Failed to add asset");
    }
  };

  const handleSellAsset = async () => {
    if (!selectedHolding || !sellQuantity) {
      toast.error("Please enter a quantity");
      return;
    }

    const quantity = parseFloat(sellQuantity);
    if (quantity <= 0) {
      toast.error("Quantity must be greater than 0");
      return;
    }
    if (quantity > selectedHolding.shares) {
      toast.error(`You only have ${selectedHolding.shares} shares`);
      return;
    }

    try {
      await api.removeAsset(selectedHolding.symbol, quantity);
      
      // Refresh holdings
      const holdings = await api.getHoldings();
      setHoldingsList(holdings);
      
      if (quantity === selectedHolding.shares) {
        toast.success(`Sold all ${selectedHolding.symbol} shares`);
      } else {
        toast.success(`Sold ${quantity} shares of ${selectedHolding.symbol}`);
      }
      
      setSellQuantity("");
      setSelectedHolding(null);
      setSellDialogOpen(false);
    } catch (err) {
      toast.error(err instanceof Error ? err.message : "Failed to sell asset");
    }
  };

  const openSellDialog = (holding: Holding) => {
    setSelectedHolding(holding);
    setSellQuantity("");
    setSellDialogOpen(true);
  };

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
        <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
          <div className="space-y-1">
            <h1 className="text-2xl font-semibold">Holdings</h1>
            <p className="text-muted-foreground">
              View and manage your portfolio holdings
            </p>
          </div>

          {/* Add Asset Button */}
          <Dialog open={addDialogOpen} onOpenChange={setAddDialogOpen}>
            <DialogTrigger asChild>
              <Button className="gap-2">
                <Plus className="h-4 w-4" />
                Add Asset
              </Button>
            </DialogTrigger>
            <DialogContent>
              <DialogHeader>
                <DialogTitle>Add New Asset</DialogTitle>
                <DialogDescription>
                  Enter the details of the asset you want to add to your portfolio.
                </DialogDescription>
              </DialogHeader>
              <div className="grid gap-4 py-4">
                <div className="grid grid-cols-3 gap-4">
                <div className="space-y-2">
                  <Label htmlFor="symbol">Symbol</Label>
                  <Select
                    value={newAsset.symbol}
                    onValueChange={(value) => setNewAsset({ ...newAsset, symbol: value })}
                  >
                    <SelectTrigger>
                      <SelectValue placeholder="Select a ticker" />
                    </SelectTrigger>
                    <SelectContent>
                      {AVAILABLE_TICKERS.map((ticker) => (
                        <SelectItem key={ticker.symbol} value={ticker.symbol}>
                          {ticker.symbol} - {ticker.name}
                        </SelectItem>
                      ))}
                    </SelectContent>
                  </Select>
                </div>
                  <div className="space-y-2">
                    <Label htmlFor="shares">Shares</Label>
                    <Input
                      id="shares"
                      type="number"
                      placeholder="0"
                      value={newAsset.shares}
                      onChange={(e) => setNewAsset({ ...newAsset, shares: e.target.value })}
                    />
                  </div>
                  <div className="space-y-2">
                    <Label htmlFor="avgCost">Avg Cost ($)</Label>
                    <Input
                      id="avgCost"
                      type="number"
                      placeholder="0.00"
                      value={newAsset.avgCost}
                      onChange={(e) => setNewAsset({ ...newAsset, avgCost: e.target.value })}
                    />
                  </div>
                </div>
              </div>
              <DialogFooter>
                <Button variant="outline" onClick={() => setAddDialogOpen(false)}>
                  Cancel
                </Button>
                <Button onClick={handleAddAsset}>Add Asset</Button>
              </DialogFooter>
            </DialogContent>
          </Dialog>
        </div>

        {/* Filter Tabs */}
        <Tabs value={filter} onValueChange={(value) => setFilter(value as "all" | "profit" | "loss")}>
          <TabsList className="grid w-full grid-cols-3">
            <TabsTrigger value="all">All Holdings</TabsTrigger>
            <TabsTrigger value="profit">Profit</TabsTrigger>
            <TabsTrigger value="loss">Loss</TabsTrigger>
          </TabsList>
        </Tabs>

        {/* Holdings Cards */}
        <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-3">
          {filteredHoldings.length === 0 ? (
            <div className="col-span-full text-center py-8 text-muted-foreground">
              {holdingsList.length === 0
                ? "No holdings found. Add your first asset to get started."
                : `No ${filter === "all" ? "" : filter} holdings found.`}
            </div>
          ) : (
            filteredHoldings.map((holding) => {
            const isPositive = holding.dailyChange >= 0;
            const gainLoss = (holding.currentPrice - holding.avgCost) * holding.shares;
            const gainLossPercent = ((holding.currentPrice - holding.avgCost) / holding.avgCost) * 100;

            return (
              <Card key={holding.id} className="card-hover">
                <CardHeader className="pb-3">
                  <div className="flex items-start justify-between">
                    <div>
                      <CardTitle className="text-lg">{holding.symbol}</CardTitle>
                      <p className="text-sm text-muted-foreground">{holding.name}</p>
                    </div>
                    <div className={cn(
                      "flex items-center gap-1 text-sm font-medium",
                      isPositive ? "text-success" : "text-destructive"
                    )}>
                      {isPositive ? (
                        <TrendingUp className="h-4 w-4" />
                      ) : (
                        <TrendingDown className="h-4 w-4" />
                      )}
                      {isPositive ? "+" : ""}{holding.dailyChangePercent.toFixed(2)}%
                    </div> 
                  </div>
                </CardHeader>
                <CardContent className="space-y-4">
                  <div className="flex justify-between items-baseline">
                    <span className="text-2xl font-semibold">
                      ${holding.value.toLocaleString()}
                    </span>
                    <span className="text-sm text-muted-foreground">
                      {holding.allocation.toFixed(1)}% of portfolio
                    </span>
                  </div>

                  <div className="grid grid-cols-2 gap-4 pt-3 border-t border-border">
                    <div>
                      <p className="text-sm text-muted-foreground">Shares</p>
                      <p className="font-medium">{holding.shares}</p>
                    </div>
                    <div>
                      <p className="text-sm text-muted-foreground">Avg Cost</p>
                      <p className="font-medium">${holding.avgCost.toFixed(2)}</p>
                    </div>
                    <div>
                      <p className="text-sm text-muted-foreground">Current Price</p>
                      <p className="font-medium">${holding.currentPrice.toFixed(2)}</p>
                    </div>
                    <div>
                      <p className="text-sm text-muted-foreground">Total Gain/Loss</p>
                      <p className={cn(
                        "font-medium",
                        gainLoss >= 0 ? "text-success" : "text-destructive"
                      )}>
                        {gainLoss >= 0 ? "+" : ""}${gainLoss.toFixed(2)} ({gainLossPercent.toFixed(1)}%)
                      </p>
                    </div>
                  </div>

                  {/* Sell/Remove Button */}
                  <Button
                    variant="outline"
                    size="sm"
                    className="w-full gap-2 border-destructive/60 text-black hover:text-destructive hover:bg-destructive/10 dark:bg-destructive/90 dark:text-white dark:hover:bg-destructive/20"
                    onClick={() => openSellDialog(holding)}
                  >
                    <Minus className="h-4 w-4" />
                    Sell / Remove Shares
                  </Button>
                </CardContent>
              </Card>
            );
          }))}
        </div>

        {/* Sell Dialog */}
        <Dialog open={sellDialogOpen} onOpenChange={setSellDialogOpen}>
          <DialogContent>
            <DialogHeader>
              <DialogTitle>Sell {selectedHolding?.symbol} Shares</DialogTitle>
              <DialogDescription>
                Enter the number of shares you want to sell. You currently own {selectedHolding?.shares} shares.
              </DialogDescription>
            </DialogHeader>
            <div className="py-4">
              <div className="space-y-2">
                <Label htmlFor="sellQuantity">Quantity to Sell</Label>
                <Input
                  id="sellQuantity"
                  type="number"
                  placeholder="0"
                  min="1"
                  max={selectedHolding?.shares}
                  value={sellQuantity}
                  onChange={(e) => setSellQuantity(e.target.value)}
                />
                {sellQuantity && selectedHolding && (
                  <p className="text-sm text-muted-foreground">
                    Estimated proceeds: ${(parseFloat(sellQuantity) * selectedHolding.currentPrice).toLocaleString()}
                  </p>
                )}
              </div>
            </div>
            <DialogFooter>
              <Button variant="outline" onClick={() => setSellDialogOpen(false)}>
                Cancel
              </Button>
              <Button variant="destructive" onClick={handleSellAsset}>
                Confirm Sale
              </Button>
            </DialogFooter>
          </DialogContent>
        </Dialog>

        {/* Summary */}
        <Card className="card-hover">
          <CardContent className="pt-6">
            <div className="flex flex-wrap items-center justify-between gap-4">
              <div>
                <p className="text-sm text-muted-foreground">Total Holdings</p>
                <p className="text-xl font-semibold">{holdingsList.length} assets</p>
              </div>
              <div>
                <p className="text-sm text-muted-foreground">Total Invested</p>
                <p className="text-xl font-semibold">${totalInvested.toLocaleString()}</p>
              </div>
              <div>
                <p className="text-sm text-muted-foreground">Total Portfolio Value</p>
                <p className="text-xl font-semibold">${totalValue.toLocaleString()}</p>
              </div>
            </div>
          </CardContent>
        </Card>
      </div>
    </AppLayout>
  );
}
