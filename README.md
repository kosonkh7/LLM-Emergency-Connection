# LLM-Emergency-Recommendation
LLM을 이용한 응급 상황 인식 및 응급실 연계 서비스

본 서비스를 이용하기 위하여 별도의 API키를 요구합니다.
- OPENAI API KEY
- NAVER MAPs API KEY

## Easy usage with Docker Image

docker run -p 8080:8080 kosonkh7/aivle_mini7_6_spring:v1.2.0 (or latest)

docker run -p 8000:8000 \
-e OPENAI_API_KEY=YOUR_OPENAI_API_KEY \
-e naver_client_id=YOUR_NAVER_CLIENT_ID \
-e naver_client_secret=YOUR_NAVER_CLIENT_SECRET \
kosonkh7/aivle_mini7_6_fastapi:v1.2.0 (or latest)

## Introduction

## Data Understanding

## Modeling
