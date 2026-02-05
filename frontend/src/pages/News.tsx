import { useState, useEffect } from "react";
import { AppLayout } from "@/components/layout/AppLayout";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { holdings } from "@/data/mockData";
import { cn } from "@/lib/utils";
import { ExternalLink, Newspaper, Loader2, AlertCircle } from "lucide-react";
import { api, type NewsItem } from "@/lib/api";

type FilterType = "all" | "portfolio";

export default function News() {
  const [filter, setFilter] = useState<FilterType>("all");
  const [news, setNews] = useState<NewsItem[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const portfolioSymbols = holdings.map((h) => h.symbol);

  // Fetch news when filter changes
  useEffect(() => {
    const fetchNews = async () => {
      setLoading(true);
      setError(null);
      try {
        const data = filter === "portfolio" 
          ? await api.getPortfolioNews()
          : await api.getMarketNews();
        setNews(data);
      } catch (err) {
        setError(err instanceof Error ? err.message : "Failed to load news");
        setNews([]);
      } finally {
        setLoading(false);
      }
    };

    fetchNews();
  }, [filter]);

  const filteredNews = news;

  return (
    <AppLayout>
      <div className="space-y-6 animate-fade-in">
        <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
          <div className="space-y-1">
            <h1 className="text-2xl font-semibold">Market News</h1>
            <p className="text-muted-foreground">
              Stay updated with the latest market and portfolio news
            </p>
          </div>

          {/* Filter Buttons */}
          <div className="flex gap-2">
            <button
              onClick={() => setFilter("all")}
              className={cn(
                "filter-button",
                filter === "all" ? "filter-button-active" : "filter-button-inactive"
              )}
            >
              All News
            </button>
            <button
              onClick={() => setFilter("portfolio")}
              className={cn(
                "filter-button",
                filter === "portfolio" ? "filter-button-active" : "filter-button-inactive"
              )}
            >
              Portfolio Only
            </button>
          </div>
        </div>

        {/* News List */}
        <Card className="card-hover overflow-hidden">
          <CardHeader className="pb-0">
            <div className="flex items-center gap-2">
              <Newspaper className="h-5 w-5 text-muted-foreground" />
              <CardTitle className="text-base font-medium">
                {filter === "all" ? "Latest News" : "Portfolio News"}
              </CardTitle>
            </div>
          </CardHeader>
          <CardContent className="p-0 mt-4">
            {loading ? (
              <div className="p-12 flex flex-col items-center justify-center gap-3">
                <Loader2 className="h-8 w-8 animate-spin text-primary" />
                <p className="text-sm text-muted-foreground">Loading news...</p>
              </div>
            ) : error ? (
              <div className="p-12 flex flex-col items-center justify-center gap-3">
                <AlertCircle className="h-8 w-8 text-destructive" />
                <p className="text-sm text-destructive font-medium">{error}</p>
                <button 
                  onClick={() => window.location.reload()}
                  className="text-xs text-primary hover:underline"
                >
                  Try again
                </button>
              </div>
            ) : filteredNews.length === 0 ? (
              <div className="p-8 text-center">
                <p className="text-muted-foreground">
                  {filter === "portfolio" 
                    ? "No news found for your portfolio holdings." 
                    : "No news available at the moment."}
                </p>
              </div>
            ) : (
              <div className="divide-y divide-border">
                {filteredNews.map((news) => (
                  <a
                    key={news.id}
                    href={news.url}
                    target="_blank"
                    rel="noopener noreferrer"
                    className="news-card block"
                  >
                    <div className="flex gap-4">
                      <div className="flex-1 min-w-0 space-y-2">
                        <div className="flex items-start justify-between gap-2">
                          <h3 className="font-medium leading-snug line-clamp-2">
                            {news.headline}
                          </h3>
                          <ExternalLink className="h-4 w-4 flex-shrink-0 text-muted-foreground" />
                        </div>
                        
                        <p className="text-sm text-muted-foreground line-clamp-2">
                          {news.summary}
                        </p>

                        <div className="flex flex-wrap items-center gap-2 pt-1">
                          <span className="text-xs font-medium text-primary">
                            {news.source}
                          </span>
                          <span className="text-xs text-muted-foreground">
                            • {news.publishedAt}
                          </span>
                          {news.relatedSymbols.length > 0 && (
                            <>
                              <span className="text-xs text-muted-foreground">•</span>
                              <div className="flex gap-1">
                                {news.relatedSymbols.map((symbol) => (
                                  <span
                                    key={symbol}
                                    className={cn(
                                      "text-xs px-1.5 py-0.5 rounded",
                                      portfolioSymbols.includes(symbol)
                                        ? "bg-primary/10 text-primary font-medium"
                                        : "bg-muted text-muted-foreground"
                                    )}
                                  >
                                    {symbol}
                                  </span>
                                ))}
                              </div>
                            </>
                          )}
                        </div>
                      </div>
                    </div>
                  </a>
                ))}
              </div>
            )}
          </CardContent>
        </Card>
      </div>
    </AppLayout>
  );
}
