package Taiwan_stock_market_crawler_Cauchy.src.main.scala.config

sealed trait DataPart

case object ListedCompanyInfo extends DataPart
case object StockMarket extends DataPart
