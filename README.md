> ## 🗄️ Archived — 2026-09-24
>
> This repository is **archived and read-only**. It was a personal side project
> from 2020 and is no longer maintained or updated.
>
> The crawler targets website structures and third-party APIs as they existed in
> 2020; those have very likely changed since, so the scraping logic should not be
> expected to work as-is. It is kept public for reference only.
>
> No successor project is being maintained in its place.

---

# Decentralized-Crawler---TW-Stockmarket-Data

### Description
Crawl Taiwan Stockmarket data and do something like data mining or visualization, etc. <br>
<br>

### Motivation
Crawl stockmarket data and visualize it to help me observe the market state to help me make money. <br>
Also, the API has request limitation right now. So we need a decentralized and stable crawler program to help us get the data. <br>
<br>

### Skills
For this project, I classify it to 2 parts. One is crawler, another one is other prcesses logic-implements (like Multiple Actors relationship, Send message mechanism and build Kafka producer, consumer, etc). <br>

#### Crawler
Language: Python <br>
Version: 3.7 up <br>
Framework: Requests <br>

#### All other logic-Implements
Language: Scala <br>
Version: 2.12 <br>
Framework: Spark (version: 2.4.5), AKKA (version: 2.4.20) <br>
Message Queue Server: Kafka (version: 2.5.0) <br>
<br>


