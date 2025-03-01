# LLM-Emergency-Recommendation
본 서비스를 이용하기 위하여 별도의 API키를 요구합니다.
- OPENAI API KEY
- NAVER MAPs API KEY

# Service Architecture
* 전체 웹서비스가 아닌, LLM을 이용한 응급실 연계 서비스 구조에 대한 요약 이미지.
![image](https://github.com/user-attachments/assets/b156c5fc-d682-46e4-86be-f4c33fec22c5)

### 0. 입력
- 응급 상황에서의 음성(.mp4)
- 해당 위치 좌표(위도, 경도)

### 1. 음성 인식 및 요약
- Whisper(OpenAI API): 트랜스포머 기반의 자동 음성 인식(ASR) 시스템. 음성을 텍스트로 변환하는 역할.
- GPT 3.5(OpenAI API): Whisper로 변환한 텍스트를 요약 및 키워드 추출. \
  효과적으로 요약 및 키워드 추출 할 수 있도록 적절한 system_role 설정.

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
![image](https://github.com/user-attachments/assets/59d6eb24-08b2-4364-b916-7d59262be4f3)

"응급실 1년 살던 환자, 쫓겨난뒤 두달만에 또 병상 꿰찬 꼼수" [출처:중앙일보](https://www.joongang.co.kr/article/25174325)

응급실을 희망하는 비응급 환자로 인해, 정작 응급 상황에서 가용 공간, 담당 전문의 부족 현상 해소 필요성.

### [KTAS: 한국 응급환자 중증도 분류기준](https://www.law.go.kr/LSW//admRulLsInfoP.do?admRulId=85470&efYd=0#AJAX)
제5조(중증도 등급기준) 응급실 내원환자의 중증도 등급은 제4조에 의한 분류결과에 따라 다음 각 호와 같이 구분한다.  
1. 중증응급환자 : 중증도 분류결과 1등급 및 2등급 
2. 중증응급의심환자 : 중증도 분류결과 3등급 
3. 경증응급환자 및 비응급환자 : 중증도 분류결과 4등급 및 5등급 

### [Text Dataset for Fine-Tuning](https://www.joongang.co.kr/article/25174325)
KTAS 구분 데이터(csv) 기준으로, 직접 제작 및 ChatGPT를 이용하여\
총 1,675개의 가상의 요약 텍스트 데이터를 자체 제작

### [Dataset for Emergency Room](https://www.data.go.kr/data/15000563/openapi.do)
국가 공공데이터포털의 "국립중앙의료원_전국 응급의료기관 정보 조회 서비스" API 이용

응급실 보유한 병원만 API 요청 후, 추천에 필요한 정보만 **SQLite**에 저장하여 응급실 정보 DB 구축

### Reference
[Open AI / Whisper](https://openai.com/index/whisper/)
[Open AI / GPT 3.5 turbo](https://platform.openai.com/docs/models/gpt-3-5-turbo)
[KLUE/Bert-base](https://huggingface.co/klue/bert-base)
[Naver Maps API](https://www.ncloud.com/product/applicationService/maps)
[KTAS: 한국 응급환자 중증도 분류기준](https://www.law.go.kr/LSW//admRulLsInfoP.do?admRulId=85470&efYd=0#AJAX)
[국립중앙의료원_전국 응급의료기관 정보 조회 서비스](https://www.data.go.kr/data/15000563/openapi.do)
