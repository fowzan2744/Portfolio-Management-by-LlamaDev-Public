import { useState } from "react";
import { AppLayout } from "@/components/layout/AppLayout";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { newsItems, holdings } from "@/data/mockData";
import { cn } from "@/lib/utils";
import { ExternalLink, Newspaper } from "lucide-react";

type FilterType = "all" | "portfolio";

export default function News() {
  const [filter, setFilter] = useState<FilterType>("all");

  const portfolioSymbols = holdings.map((h) => h.symbol);

  const filteredNews = filter === "all"
    ? newsItems
    : newsItems.filter((news) =>
        news.relatedSymbols.some((symbol) => portfolioSymbols.includes(symbol))
      );

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
            {filteredNews.length === 0 ? (
              <div className="p-8 text-center">
                <p className="text-muted-foreground">No news found for your portfolio holdings.</p>
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
