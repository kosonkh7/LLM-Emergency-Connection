# LLM-Emergency-Recommendation
본 서비스를 이용하기 위하여 별도의 API키를 요구합니다.
- OPENAI API KEY
- NAVER MAPs API KEY

# Service Architecture
![image](https://github.com/user-attachments/assets/b156c5fc-d682-46e4-86be-f4c33fec22c5)

### 0. 입력
- 응급 상황에서의 음성(.mp4)
- 해당 위치 좌표(위도, 경도)

### 1. 음성 인식 및 요약
- Whisper(OpenAI API): 트랜스포머 기반의 자동 음성 인식(ASR) 시스템. 음성을 텍스트로 변환하는 역할.
- GPT 3.5(OpenAI API): Whisper로 변환한 텍스트를 요약 및 키워드 추출.

### 2. 응급 상황 등급 분류
- KLUE BERT: 한국어(KLUE Dataset)로 사전학습 된 Bert 모델.\
위 모델을 "자체 제작한 KTAS 5단계 응급 분류 기준 텍스트 데이터"를 기반으로 Fine-Tuning 하여, KTAS 등급 분류 역할.

### 3. 출력(응급실 연계, 추천)
- KTAS 1~3단계(중증 이상): 위치 좌표 기준 SQLite DB에 저장된 가까운 응급실 보유 병원과, 네이버 지도 API를 이용한 빠른 경로 반환
- KTAS 4~5단계, 그외 (경증 및 미응급 상황): 안내 메세지 출력

# Easy usage with Docker Image

docker run -p 8080:8080 kosonkh7/aivle_mini7_6_spring:v1.2.0 (or latest)

docker run -p 8000:8000 -e OPENAI_API_KEY=YOUR_OPENAI_API_KEY \
-e naver_client_id=YOUR_NAVER_CLIENT_ID \
-e naver_client_secret=YOUR_NAVER_CLIENT_SECRET \
kosonkh7/aivle_mini7_6_fastapi:v1.2.0 (or latest)

# Data Understanding



### [Why is proper classification of KTAS important?](https://www.law.go.kr/LSW//admRulLsInfoP.do?admRulId=85470&efYd=0#AJAX)

### [Text Dataset for Fine-Tuning](https://www.joongang.co.kr/article/25174325)

### [Dataset for Emergency Room](https://www.data.go.kr/data/15000563/openapi.do)
