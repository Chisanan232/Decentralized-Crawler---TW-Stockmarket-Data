#! /etc/anaconda/python3

from bs4 import BeautifulSoup
import requests
import json
import re


class Crawler:

    api = "https://isin.twse.com.tw/isin/C_public.jsp?strMode=2"
    http_header = {"User-Agent": "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_14_5) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/81.0.4044.113 Safari/537.36"}

    file_path = "/Users/bryantliu/Downloads/all_listed_company.json"

    @classmethod
    def send_http_request(cls):
        response = requests.get(cls.api, headers=cls.http_header)
        if response.status_code == requests.codes.ok:
            return response
        else:
            return None


    @classmethod
    def web_code_parser(cls, response):
        soup = BeautifulSoup(response.text, "html.parser")
        table_data = soup.find_all("tr")
        all_company_stock_info_list = []
        for data in table_data[1:]:
            data_contents = data.find_all("td")
            data_contents = [ele.text for ele in data_contents]
            if len(data_contents) == 7:
                if re.search(r"[0-9]{1,6}", str(data_contents[0])) is not None:
                    print(data_contents)
                    company_stock_info = {
                        "stock_symbol": str(data_contents[0]).rsplit()[0],
                        "company": str(data_contents[0]).rsplit()[1],
                        "ISIN": str(data_contents[1]),
                        "listed_data": str(data_contents[2]),
                        "listed_type": str(data_contents[3]),
                        "industry_type": str(data_contents[4]),
                        "CFICode": str(data_contents[5])
                    }
                    all_company_stock_info_list.append(company_stock_info)
                    print(", ".join(data_contents))
                    print("-------------------")
        return all_company_stock_info_list


    @classmethod
    def save_file(cls, data):
        with open(cls.file_path, "w", encoding="utf-8") as file:
            json_data = json.dumps(data, ensure_ascii=False)
            file.write(json_data)


    @classmethod
    def crawler_code(cls):
        response = cls.send_http_request()
        if response is not None:
            all_listed_company = cls.web_code_parser(response)
            cls.save_file(all_listed_company)
            print("[SUCCESS] Program Finish.")
        else:
            print("[FAIL] Program Error ...")


if __name__ == '__main__':

    Crawler.crawler_code()
