from typing import Union
from fastapi import FastAPI, Request, File, UploadFile, Form, Query
from openai import OpenAI
import json
from transformers import AutoTokenizer, AutoModelForSequenceClassification, Trainer, TrainingArguments, EarlyStoppingCallback
import torch
import torch.nn.functional as F
from haversine import haversine
import pandas as pd
from typing import Optional
import requests
import dotenv
import os
import time
#pip install openai
#pip install transformers
#pip install torch
#pip install haversine
#pip install pandas
#pip install fastapi
#pip install "uvicorn[standard]"

# step0 (input : 음성,위치)
# ——— 위는 사전에 주어진다 ———
# step1 (input : 음성 , output : 음성요약, 키워드)
# step2 (input : 음성요약[step1의 output], output : 응급code)
# step3 (input :  응급코드[step3의 output], 위치[step0에서 넘어온 것]  , output :응급실목록들) 


app = FastAPI()


@app.get("/")
def read_root():
    return {"Hello": "World"}


# 0. load key file------------------
def load_file(filepath):
    with open(filepath, 'r') as file:
        return file.readline().strip()
    
    
# def get_region(latitude, longitude):
#     if 37<=latitude<38 and 126.5<=longitude<127.5:
#         return 'Seoul_Gyungi'
#     elif 38<=latitude<38.6 and 127.5<=longitude<129.6:
#         return 'Gangwon'
#     elif 36.5<=latitude<37 and 126.5<=longitude<127.5:
#         return 'Chungchung'
#     elif 34.6<=latitude<36.5 and 127.5<=longitude<129.6:
#         return 'Youngnam'
#     elif 34<=latitude<36.5 and 126<=longitude<127.5:
#         return 'Honam'
#     elif 33<=latitude<34 and 126<=longitude<127:
#         return 'Jeju'    
  
    
# 1-1 audio2text--------------------
def audio_to_text(audio_path, filename):
    client = OpenAI()
    with open(audio_path + filename, "rb") as audio_file:
        transcript = client.audio.transcriptions.create(
                        file=audio_file,
                        model="whisper-1",
                        language="ko",
                        response_format="text",
                        temperature=0.0)
    return transcript


# 1-2 text2summary------------------
def text_summary(input_text):
    # OpenAI 클라이언트 생성
    client = OpenAI()
    system_role = '''입력으로 응급 상황에서의 고객 요청이 텍스트로 들어옵니다. 
    텍스트를 요약하고, KTAS 5단계 기반하여 키워드를 작성하세요.
    응답은 다음의 형식을 지켜주세요.
    {"summary": "텍스트 요약, "keyword": "키워드1, 키워드2 ..."}
    '''
    response = client.chat.completions.create(
        model="gpt-3.5-turbo",
        messages=[
            {
                "role": "system",
                "content": system_role
            },
            {
                "role": "user",
                "content": input_text
            }
        ]
    )
    answer = response.choices[0].message.content
    parsed_answer = json.loads(answer)

    summary = parsed_answer["summary"]
    keywords = parsed_answer["keyword"]

    return summary, keywords


# 2. model prediction------------------
def predict(text, model, tokenizer):
    # 입력 문장 토크나이징
    device = torch.device("cuda" if torch.cuda.is_available() else "cpu")
    inputs = tokenizer(text, return_tensors="pt", truncation=True, padding=True)
    inputs = {key: value.to(device) for key, value in inputs.items()}  # 각 텐서를 GPU로 이동

    # 모델 예측
    with torch.no_grad():
        outputs = model(**inputs)

    # 로짓을 소프트맥스로 변환하여 확률 계산
    logits = outputs.logits
    probabilities = logits.softmax(dim=1)

    # 가장 높은 확률을 가진 클래스 선택
    pred = torch.argmax(probabilities, dim=-1).item()
    # 학습 시 레이블 0부터 시작하게 인코딩 해준 부분 다시 디코딩 (확인 요망)
    pred += 1

    return pred, probabilities


# 3-1. get_distance------------------
def get_dist(start_lat, start_lng, dest_lat, dest_lng, c_id, c_key):
    url = "https://naveropenapi.apigw.ntruss.com/map-direction/v1/driving"
    headers = {
        "X-NCP-APIGW-API-KEY-ID": c_id,
        "X-NCP-APIGW-API-KEY": c_key,
    }
    params = {
        "start": f"{start_lng},{start_lat}",  # 출발지 (경도, 위도)
        "goal": f"{dest_lng},{dest_lat}",    # 목적지 (경도, 위도)
        "option": "trafast"  # 실시간 빠른 길 옵션
    }

    response = requests.get(url, headers=headers, params=params)
    data = response.json()
    status = data['code']
    message = data['message']
    
    dist = None
    duration = None
    path = None
    bbox = None
    guide = None
    # 'route'와 'trafast' 키가 존재하는지 확인하고 예외 처리
    try:
        if status == 0:
            dist = data['route']['trafast'][0]['summary']['distance']  # m(미터)
            dist = dist / 1000  # km로 변환
            duration = data['route']['trafast'][0]['summary']['duration']
            duration = round((duration+500) / (1000*60))
            path = data['route']['trafast'][0]['path']
            bbox = data['route']['trafast'][0]['summary']['bbox']
            guide = data['route']['trafast'][0]['guide']
        else:
            print(data['message'])
    except KeyError as e:
        print(f"응답 데이터에서 예상되는 키를 찾을 수 없음: {e}")
        message += f"응답 데이터에서 예상되는 키를 찾을 수 없음: {e}"
        
    return dist, duration, path, bbox, guide, status, message


# 3-2. recommendation------------------
def recommend_hospital3(path, emergency, start_lat, start_lng, optionNum, c_id, c_key):

    # 초기 lat 세팅 
    init_lat = 0.05
    init_long = 0.05

    # 초기에 대입. 
    temp = emergency.loc[emergency['위도'].between(start_lat-0.05, start_lat+0.05) & emergency['경도'].between(start_lng-0.05, start_lng+0.05)].copy()

    print (optionNum)
    # 3개 찾을 때까지 찾기 . 
    while (len(temp) < optionNum):
        temp = emergency.loc[emergency['위도'].between(start_lat-init_lat, start_lat+init_lat) & emergency['경도'].between(start_lng-init_long, start_lng+init_long)].copy()
        # 못 찾으면 0.01씩 늘림. 
        init_lat += 0.01
        init_long += 0.01


    print(temp)
    # 거리 계산
    # temp['거리'] = temp.apply(lambda x: get_dist(start_lat, start_lng, x['위도'], x['경도'], c_id, c_key), axis=1)
    temp[['거리', '소요시간', '이동좌표', '맵영역', '분기점네비', '응답코드', '응답메시지']]  = temp.apply(lambda x: get_dist(start_lat, start_lng, x['위도'], x['경도'], c_id, c_key), axis=1, result_type="expand")
    sorted_temp = temp.sort_values(by='거리').head(optionNum)
    # 결과를 JSON 형태로 변환
    result_json = sorted_temp.apply(lambda row: {
        "hospitalId": row["id"],
        "hospitalName": row["병원이름"],
        "address": row["주소"],
        "emergencyMedicalInstitutionType": row["응급의료기관 종류"],
        "phoneNumber1": row["전화번호 1"],
        "phoneNumber3": row["전화번호 3"] if row["전화번호 3"] else 'Nan',
        "latitude": row["위도"],
        "longitude": row["경도"],
        "region": row["지역"],
        "distance": row["거리"],
        "time": row["소요시간"],
        "points": row['이동좌표'],
        "mapRegion": row['맵영역'],
        "navigation": row["분기점네비"],
        'responseCode': row["응답코드"],
        'responseMessage': row["응답메시지"]
    }, axis=1).tolist()

    return result_json


def getCode(request):
    path=''
    # 모델, 토크나이저 로드
    save_directory = path + "fine_tuned_bert"
    model = AutoModelForSequenceClassification.from_pretrained(save_directory)
    tokenizer = AutoTokenizer.from_pretrained(save_directory)

    summary, keywords = text_summary(request)
    predicted_class, _ = predict(summary, model, tokenizer)
    return predicted_class


#교통사고가 났어요 머리에 피가 많이나고 있습니다.
#(37.538435245262626, 126.98982802695484) 
# request : 응급상황 latitude: 위도 longitude: 경도
@app.get("/hospital_by_module")
def get_hospital(text: str,latitude: float,longitude: float, optionNum:Optional[int]):
    path=''

    print('optionNum : ', optionNum)

    # map_key = load_file(path+'map_key.txt')
    # map_key = json.loads(map_key)
    NAVER_CLIENT_ID = os.getenv('NAVER_CLIENT_ID')
    NAVER_CLIENT_SECRET = os.getenv('NAVER_CLIENT_SECRET')
    if not NAVER_CLIENT_ID or not NAVER_CLIENT_SECRET:
        raise ValueError("NAVER_API_KEY 환경 변수가 설정되지 않았습니다.")
    map_key = {'c_id':NAVER_CLIENT_ID, 'c_key':NAVER_CLIENT_SECRET}
    
    c_id, c_key = map_key['c_id'], map_key['c_key']

    emergency = pd.read_csv(path+'output_emergency_room.csv')

    # 모델, 토크나이저 로드
    save_directory = path + "fine_tuned_bert"
    model = AutoModelForSequenceClassification.from_pretrained(save_directory)
    tokenizer = AutoTokenizer.from_pretrained(save_directory)

    # 처리
    # result = audio_to_text(audio_path, filename)
    summary, keywords = text_summary(text)
    predicted_class, _ = predict(summary, model, tokenizer)

    result = None
    if predicted_class <= 3 :
        result = {"status":200, 
                  "scale": predicted_class, 
                  "text": text, 
                  "message": "응급실을 추천합니다.", 
                  "keyword": keywords}
        
        recommend = recommend_hospital3(path, emergency, latitude, longitude, optionNum, c_id, c_key)
        result.update({"recommend": recommend})
        print(result)
    else :
        print('개인 건강관리')

    return result


@app.post("/hospital_by_record")
async def recommend_emergency_with_record(file: UploadFile = File(...), latitude: float = Query(...), longitude: float = Query(...)):
    path = ""
    
    c_id = os.getenv('NAVER_CLIENT_ID')
    c_key = os.getenv('NAVER_CLIENT_SECRET')
    
    if not c_id or not c_key:
        raise ValueError("NAVER_API_KEY 환경 변수가 설정되지 않았습니다.")
    
    emergency = pd.read_csv(path+'output_emergency_room.csv')
    
    # 모델, 토크나이저 로드
    save_directory = path + "fine_tuned_bert"
    model = AutoModelForSequenceClassification.from_pretrained(save_directory)
    tokenizer = AutoTokenizer.from_pretrained(save_directory)

    # 처리
    name = f"record_{int(time.time())}.mp4"
    content = await file.read()
    with open(name, "wb") as f:
        f.write(content)     
    
    text = audio_to_text("", name)
    
    os.remove(name)
    
    summary, keywords = text_summary(text)
    predicted_class, _ = predict(summary, model, tokenizer)
    
    result = None
    if predicted_class <= 3 :
        result = {"status":200, 
                  "scale": predicted_class, 
                  "text": text, 
                  "message": "응급실을 추천합니다.", 
                  "keyword": keywords
                  }
        
        recommend = recommend_hospital3(path, emergency, latitude, longitude, c_id, c_key)
        result.update({"recommend": recommend})
        print(result)
    else :
        print('개인 건강관리')

    return result