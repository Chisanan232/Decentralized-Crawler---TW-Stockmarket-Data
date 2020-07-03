import requests
import argparse
import random
import json
import time
import re


class TerminalCommand:

    def get_argument(self):
        """
        Define and get the command line parameters.
        :return:
        """

        parser = argparse.ArgumentParser(description="Crawl stock market data.")

        parser.add_argument("--stock-api", type=str, dest="stock_api", default=None)
        parser.add_argument("--date", type=str, dest="date", default=None)
        parser.add_argument("--listed-company", type=str, dest="listed_company", default=None)
        parser.add_argument("--data-type", type=str, dest="data_type", default=False)
        parser.add_argument("--sleep", type=str, dest="sleep_code", default=False)
        parser.add_argument("--api-params", type=str, dest="api_params", default={})
        return parser.parse_args()


class StockCrawler:

    http_header = {"User-Agent": "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_14_5) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/81.0.4044.113 Safari/537.36"}

    def chk_data(self):
        pass


    def save_data(self):
        pass


    def data_conditions(self, date, stock_symbol, data_type="json"):
        """
        Integrate the API parameters value.
        :param date: Date time.
        :param stock_symbol: Company stock symbol.
        :param data_type: The data type of API.
        :return: A dictionary type data which save all info of above all..
        """

        date_checksum = re.search(r"[0-9]{8}", str(date))
        stock_symbol_checksum = re.search(r"[0-9]{4}", str(stock_symbol))
        if date_checksum is not None and stock_symbol_checksum is not None:
            return {
                "response": str(data_type),
                "date": str(date_checksum.group(0)),
                "stockNo": str(stock_symbol_checksum.group(0))
            }
        else:
            return {
                "error": {
                    "date": date,
                    "stock_symbol": stock_symbol,
                    "date_checksum": date_checksum,
                    "stock_symbol_checksum": stock_symbol_checksum
                },
                "content": "Data format is incorrect."
            }


    def stock_api(self):
        """
        The Taiwan 證券交易所 API.
        :return: String type data.
        """

        return "http://www.tse.com.tw/exchangeReport/STOCK_DAY"


    def generate_api(self, date, stock_symbol):
        """
        Generate the API parameters data and it's a dictionary type.
        :param date: Date time. It accepts formats YYYY-MM-DD, YYYY/MM/DD, YYYY MM DD.
        :param stock_symbol: Company stock symbol.
        :return: A dictionary type value which save all info which API needs.
        """

        def date_value(date):
            """
            Check date time.
            :param date: Date time value. It accepts formats YYYY-MM-DD, YYYY/MM/DD, YYYY MM DD.
            :return: String type value which combine date time YYYYMMDD.
            """

            if date:
                try:
                    int(date)
                    if len(date) == 8:
                        return str(date)
                    else:
                        raise ValueError("Function parameter 'date' should be a 8 unit length character.")
                except ValueError as e:
                    if re.search(r"[0-9]{1,5}", str(date)) is not None:
                        if "/" in str(date):
                            return "".join(map(lambda ele: ele.zfill(2) if len(ele) == 1 or len(ele) == 2 else ele.zfill(4), date.split(sep="/")))
                        elif " " in str(date):
                            return "".join(map(lambda ele: ele.zfill(2) if len(ele) == 1 or len(ele) == 2 else ele.zfill(4), date.split(sep=" ")))
                        elif "-" in str(date):
                            return "".join(map(lambda ele: ele.zfill(2) if len(ele) == 1 or len(ele) == 2 else ele.zfill(4), date.split(sep="-")))
                    else:
                        raise ValueError("Function parameter 'date' cannot be character which doesn't including any integer.")
            else:
                raise ValueError("Function parameter 'date' cannot be empty value.")

        stock_api = self.stock_api()
        __date = date_value(date)
        __conditions = self.data_conditions(date=__date, stock_symbol=stock_symbol)
        parameters = json.dumps({"api": stock_api, "condition": __conditions})
        print(parameters)
        return {
            "api": stock_api,
            "condition": __conditions
        }


    def send_http_request(self, api, conditions):
        """
        Send HTTP request.
        :param api: The Taiwan 證券交易所 API.
        :param conditions: The API parameters.
        :return: HTTP response object.
        """

        response = requests.get(api, params=conditions, headers=self.http_header)
        if response.status_code == requests.codes.ok:
            return response
        else:
            return None


    def send_request(self, stock_api, date, symbol, sleep_code):
        """
        Previous version. Developer should entry some parameter like stock symbol and api, etc.
        :param stock_api: The Taiwan 證券交易所 API.
        :param date: The datetime.
        :param symbol: The company stock symbol.
        :param sleep_code: (Boolean value) Let code sleep several seconds after send HTTP request.
        :return: The data which has be crawled and parsed.
        """

        conditions = self.data_conditions(date=date, stock_symbol=symbol)
        crawl_result = self.send_http_request(stock_api, conditions)
        if crawl_result is not None:
            json_data = json.loads(crawl_result.text)
            crawl_result = {"stat": "SUCCESS", "stockInfo": json_data}
            print(crawl_result)
        else:
            crawl_result = {"stat": "FAIL", "stockInfo": None}
            print(crawl_result)

        if sleep_code is True:
            time.sleep(random.randrange(random.randrange(1, 7), random.randrange(8, 24)))

        return crawl_result


    def akka_send_request(self, api_content, sleep_code):
        """
        The Akka with Kafka version method.
        :param api_content: The API parameters. (It's a dictionary type value, for command line, it's also be resolve json type value.)
        :param sleep_code: (Boolean value) Let code sleep several seconds after send HTTP request.
        :return: The data which has be crawled and parsed.
        """

        # if type(api_content) is not dict:
        #     raise TypeError("Function parameter 'api_content' should be a dictionary type value.")
        api_content = json.loads(api_content)

        stock_api = api_content.get("api", None)
        conditions = api_content.get("condition", {})
        if stock_api is None or conditions == {}:
            raise ValueError("Parameters value cannot be empty.")

        crawl_result = self.send_http_request(stock_api, conditions)
        if crawl_result is not None:
            json_data = json.loads(crawl_result.text)
            crawl_result = {"stat": "SUCCESS", "stockInfo": json_data}
            print(crawl_result)
        else:
            crawl_result = {"stat": "FAIL", "stockInfo": None}
            print(crawl_result)

        if sleep_code is True:
            time.sleep(random.randrange(random.randrange(1, 7), random.randrange(8, 24)))

        return crawl_result


if __name__ == '__main__':

    """
    https://blog.csdn.net/Sinsa110/article/details/51189456
    """

    cmd_opt = TerminalCommand()
    args = cmd_opt.get_argument()

    stock_crawler = StockCrawler()
    if args.stock_api is not None and args.date is not None and args.listed_company is not None:
        stock_crawler.send_request(args.stock_api, args.date, args.listed_company, args.sleep_code)
    if args.date is not None and args.listed_company is not None:
        stock_crawler.generate_api(args.date, args.listed_company)
    if args.api_params != {}:
        stock_crawler.akka_send_request(args.api_params, args.sleep_code)
