사전학습된 KLUE/Bert 모델 필요. (깃허브 용량 제한으로 업로드 불가) \
.env 파일에 API KEY 저장 필요.

Docker Hub에 업로드 된 이미지를 통해 테스트 가능. \
kosonkh7/aivle_mini7_6_fastapi:v1.2.0 (or latest)

**Endpoint:** <br>
`POST /hospital_by_record`

**Request**: <br>
{ <br>
&emsp;"flie": .mp4 <br>
&emsp;"latitude": 위도(float) <br>
&emsp;"longitude": 경도(float) <br>
} <br>

**Response**: <br>
{   <br>
&emsp;"status": "200" <br>
&emsp;"scale": Fine Tuning 한 KLUE로 예측한 KTAS 등급  <br>
&emsp;"text": GPT 3.5 turbo로 요약한 응급 메세지  <br>
&emsp;"message": "응급실을 추천합니다." <br>
&emsp;"keyword": GPT 3.5 turbo로 추출한 주요 키워드 리스트 <br>
&emsp;"**recommend**": 3가지 추천 병원 데이터(JSON) <br>
} 

**recommend** (응답값에 포함된 JSON 데이터): <br>
"""<br>
요청 위치를 기반으로 네이버 지도 API를 이용하여, <br>
응급실 정보 DB 내 가까운 응급실 3가지를 소요 시간 순으로 정렬하여 아래와 같은 응급실 정보 응답<br>
"""<br>
row: { \
        &emsp;"hospitalName": row["병원이름"], \
        &emsp;"address": row["주소"], \
        &emsp;"emergencyMedicalInstitutionType": row["응급의료기관 종류"],\
        &emsp;"phoneNumber1": row["전화번호 1"],\
        &emsp;"phoneNumber3": row["전화번호 3"],\
        &emsp;"latitude": row["위도"],\
        &emsp;"longitude": row["경도"],\
        &emsp;"distance": row["거리"],\
        &emsp;"time": row["소요시간"],\
        &emsp;"points": row['이동좌표'],\
        &emsp;"mapRegion": row['맵영역'],\
        &emsp;"navigation": row["분기점네비"],\
        &emsp;'responseCode': row["응답코드"],\
        &emsp;'responseMessage': row["응답메시지"],\
        &emsp;"region": row["지역"]\
    }, axis=1)
